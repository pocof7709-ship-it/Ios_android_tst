package com.iosexperience.app.ui

import android.app.ActivityOptions
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.res.stringResource
import com.iosexperience.app.R
import com.iosexperience.core.design.theme.LocalReduceMotion
import com.iosexperience.core.design.theme.LocalWallpaper
import com.iosexperience.core.design.theme.WallpaperLayer
import com.iosexperience.core.design.theme.WallpaperSpec
import com.iosexperience.core.design.theme.Wallpapers
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.iosexperience.core.data.Graph
import com.iosexperience.core.data.HomeEntry
import com.iosexperience.core.design.components.GlassCard
import com.iosexperience.core.design.theme.AccentPalette
import com.iosexperience.core.design.theme.IOS
import com.iosexperience.core.design.theme.IOSShapes
import com.iosexperience.core.design.theme.IOSTheme
import com.iosexperience.app.toProfile
import com.iosexperience.core.data.ThemeMode
import com.iosexperience.feature.ai.AiScreen
import com.iosexperience.feature.ai.AiViewModel
import com.iosexperience.feature.controlcenter.ControlCenter
import com.iosexperience.feature.controlcenter.ShortcutSlot
import com.iosexperience.feature.controlcenter.SystemControls
import com.iosexperience.feature.launcher.*
import com.iosexperience.feature.lockscreen.LockScreen
import com.iosexperience.feature.notifications.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

enum class Overlay { None, Control, Notifications, Search, Lock, Ai, Settings, Recents }

@Composable
fun HomeScreen(vm: LauncherViewModel, aiVm: AiViewModel, homeSignal: Int) {
    val ctx = LocalContext.current
    val view = LocalView.current
    val s by Graph.settings.state.collectAsState()
    val installed by vm.installed.collectAsState()
    val entries by vm.entries.collectAsState()
    val dock by vm.dock.collectAsState()
    val recents by vm.recents.collectAsState()
    val shortcutKeys by vm.shortcuts.collectAsState()
    val notifs by NotificationStore.items.collectAsState()
    val media by NotificationStore.media.collectAsState()
    val torch by SystemControls.get(ctx).torchOn.collectAsState()
    var access by remember { mutableStateOf(NotificationStore.hasAccess(ctx)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { access = NotificationStore.hasAccess(ctx); vm.refresh() }

    // Escala por largura (referência 390 dp), limitada a 0,9–1,15.
    val k = (LocalConfiguration.current.screenWidthDp / 390f).coerceIn(0.9f, 1.15f)
    val iconDp = (s.iconSizeDp * k).roundToInt()
    val anim = s.animations && !LocalReduceMotion.current
    val sysDark = isSystemInDarkTheme()
    val wallDark = when (s.themeMode) { ThemeMode.DARK -> true; ThemeMode.LIGHT -> false; ThemeMode.SYSTEM -> sysDark }
    val wallpaper = remember(s.wallpaper, wallDark) { WallpaperSpec(Wallpapers.get(s.wallpaper), wallDark) }
    val ownWallpaper = !s.useSystemWallpaper

    var overlay by remember { mutableStateOf(Overlay.None) }
    var folder by remember { mutableStateOf<HomeEntry.Folder?>(null) }
    var dockSlot by remember { mutableStateOf<Int?>(null) }
    var shortcutSlot by remember { mutableStateOf<Int?>(null) }
    var timerDialog by remember { mutableStateOf(false) }

    // Timer interno (a "atividade ao vivo" real que o app consegue oferecer sem serviço em segundo plano).
    var timerEnd by remember { mutableLongStateOf(0L) }
    var timerRem by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(timerEnd) {
        if (timerEnd == 0L) { timerRem = null; return@LaunchedEffect }
        while (System.currentTimeMillis() < timerEnd) { timerRem = ((timerEnd - System.currentTimeMillis() + 999) / 1000).toInt(); delay(250) }
        timerRem = null; timerEnd = 0L
        Toast.makeText(ctx, "Temporizador concluído", Toast.LENGTH_LONG).show()
    }
    // Aviso de notificação recém-chegada na ilha (só notificações com menos de 8 s).
    var notice by remember { mutableStateOf<NotifItem?>(null) }
    val newest = notifs.firstOrNull()
    LaunchedEffect(newest?.key, newest?.whenMs) {
        val n = newest
        if (n != null && System.currentTimeMillis() - n.whenMs < 8_000) { notice = n; delay(5_000); notice = null }
    }

    val label: (String) -> String = { installed[it]?.label ?: it.substringBefore('/') }
    fun openApp(key: String, b: Rect) {
        val opts = if (anim) ActivityOptions.makeScaleUpAnimation(view, b.left.roundToInt(), b.top.roundToInt(), b.width.roundToInt(), b.height.roundToInt()).toBundle() else null
        if (!vm.open(key, opts)) Toast.makeText(ctx, "Não foi possível abrir este app", Toast.LENGTH_SHORT).show()
        else { overlay = Overlay.None; folder = null }
    }
    fun openNotification(n: NotifItem) {
        try { n.intent?.send(); overlay = Overlay.None } catch (e: Exception) { Toast.makeText(ctx, "Esta notificação não pode ser aberta", Toast.LENGTH_SHORT).show() }
    }
    fun start(i: Intent) { try { ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (e: Exception) { Toast.makeText(ctx, "App indisponível neste aparelho", Toast.LENGTH_SHORT).show() } }

    val pageSize = s.gridRows * s.gridColumns
    val appPages = entries.chunked(pageSize).ifEmpty { listOf(emptyList()) }
    val pager = rememberPagerState(initialPage = 1) { 1 + appPages.size }
    LaunchedEffect(homeSignal) { if (homeSignal > 0) { overlay = Overlay.None; folder = null; pager.animateScrollToPage(1) } }
    BackHandler(true) { if (folder != null) folder = null else overlay = Overlay.None }

    val overlayOpen = overlay != Overlay.None
    val blurDp by animateDpAsState(if (overlayOpen) IOS_BLUR(s.perf.toProfile().blurRadiusDp) else 0.dp, label = "blur")
    val enter = if (anim) fadeIn() + slideInVertically { -it / 8 } else EnterTransition.None
    val exit = if (anim) fadeOut() + slideOutVertically { -it / 8 } else ExitTransition.None
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(Modifier.fillMaxSize()) {
        if (ownWallpaper) WallpaperLayer(wallpaper)
        // ----- Camada sobre o papel de parede: sempre rótulos claros (como no iOS) -----
        IOSTheme(darkTheme = true, profile = s.perf.toProfile(), accent = AccentPalette[s.accentIndex].second) {
          CompositionLocalProvider(LocalWallpaper provides (if (ownWallpaper) wallpaper else null)) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (overlayOpen) 0.45f else 0.12f))) {
                Box(Modifier.fillMaxSize().then(if (blurDp > 0.dp) Modifier.blur(blurDp) else Modifier)) {
                    HorizontalPager(pager, Modifier.fillMaxSize()) { p ->
                        if (p == 0) TodayPage(media, notifs.size, s.showCalendar, { overlay = Overlay.Notifications })
                        else Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 56.dp, bottom = iconDp.dp + 150.dp)) {
                            val start = (p - 1) * pageSize
                            HomePage(appPages[p - 1], start, s.gridRows, s.gridColumns, iconDp, s.iconCorner, label,
                                modifier = Modifier.padding(horizontal = 20.dp), onOpenApp = ::openApp, onOpenFolder = { folder = it }, onDrop = vm::drop)
                        }
                    }
                    Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(pager.pageCount) { i -> Box(Modifier.size(7.dp).background(Color.White.copy(alpha = if (i == pager.currentPage) 0.95f else 0.35f), CircleShape)) }
                        }
                        GlassCard(Modifier.padding(bottom = 10.dp).pointerInput(Unit) { detectTapGestures { overlay = Overlay.Search } }, IOSShapes.Pill) {
                            Row(Modifier.padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Icon(Icons.Rounded.Search, "Buscar", Modifier.size(16.dp), tint = Color.White)
                                Text("Buscar", Modifier.padding(start = 6.dp), color = Color.White, fontSize = 13.sp)
                            }
                        }
                        Dock(dock, label, iconDp, s.iconCorner, ::openApp, { dockSlot = it })
                    }
                }

                if (s.fakeStatusBar) FakeStatusBar(Modifier.align(Alignment.TopCenter))

                // Gestos de borda superior: esquerda = notificações, direita = central de controle.
                Row(Modifier.fillMaxWidth().height(topInset + 22.dp)) {
                    listOf(Overlay.Notifications, Overlay.Control).forEach { target ->
                        Box(Modifier.weight(1f).fillMaxHeight().pointerInput(target) {
                            var total = 0f
                            detectVerticalDragGestures(onDragStart = { total = 0f }, onDragEnd = { if (total > 40.dp.toPx()) overlay = target }) { c, d -> c.consume(); total += d }
                        })
                    }
                }

                DynamicIsland(timerRem, media, notice, onCancelTimer = { timerEnd = 0L },
                    modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 4.dp))

                AnimatedVisibility(overlay == Overlay.Notifications, enter = enter, exit = exit) {
                    Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 44.dp).pointerInput(Unit) { detectTapGestures { } }) {
                        NotificationCenter(notifs, access, s.showLockPreviews, { ctx.startActivity(NotificationStore.accessSettingsIntent()) },
                            ::openNotification, { NotificationStore.dismiss(it.key) }, { NotificationStore.dismissAllClearable() },
                            title = stringResource(R.string.notification_center_title), onClose = { overlay = Overlay.None })
                    }
                }
                AnimatedVisibility(overlay == Overlay.Control, enter = enter, exit = exit) {
                    Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 52.dp).verticalScroll(rememberScrollState())) {
                        val slots = shortcutKeys.mapIndexed { idx, k -> shortcutSlot(k, idx, torch, label, ctx, { timerDialog = true }, { shortcutSlot = idx }, ::start, ::openApp) }
                        ControlCenter(media, access, s.themeMode != ThemeMode.LIGHT,
                            { Graph.settings.update { it.copy(themeMode = if (it.themeMode == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT) } }, slots)
                    }
                }
                AnimatedVisibility(overlay == Overlay.Search, enter = fadeIn(), exit = fadeOut()) {
                    SearchOverlay(installed.values, iconDp, s.iconCorner, ::openApp, listOf(
                        "Configurações" to { overlay = Overlay.Settings }, "Assistente" to { overlay = Overlay.Ai },
                        "Tela de bloqueio" to { overlay = Overlay.Lock }, "Multitarefa" to { overlay = Overlay.Recents },
                        "Papel de parede" to { start(Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER), "Papel de parede")) },
                    ))
                }
                AnimatedVisibility(overlay == Overlay.Recents, enter = fadeIn(), exit = fadeOut()) {
                    RecentsScreen(recents, label, s.iconCorner, ::openApp, vm::removeRecent, vm::clearRecents)
                }
                AnimatedVisibility(overlay == Overlay.Lock, enter = fadeIn(), exit = fadeOut()) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f))) {
                        LockScreen(s.clockStyle, notifs, s.showLockPreviews, s.showCalendar, ::openNotification,
                            { start(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)) }, { overlay = Overlay.None })
                    }
                }
            }
          }
        }

        // ----- Telas opacas (usam o tema escolhido pelo usuário) -----
        AnimatedVisibility(overlay == Overlay.Ai, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
            Box(Modifier.fillMaxSize().background(IOS.colors.background).statusBarsPadding()) {
                AiScreen(aiVm, s, onOpenSettings = { overlay = Overlay.Settings })
                Text("Fechar", color = IOS.colors.accent, fontSize = 16.sp, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).pointerInput(Unit) { detectTapGestures { overlay = Overlay.None } })
            }
        }
        AnimatedVisibility(overlay == Overlay.Settings, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
            Box(Modifier.fillMaxSize().background(IOS.colors.background).statusBarsPadding()) {
                SettingsScreen(s, access, onResetHome = vm::resetHome, onClearAiHistory = aiVm::clearHistory, onClose = { overlay = Overlay.None })
            }
        }
    }

    folder?.let { f ->
        // Sempre usa a versão mais recente da pasta (renomear/remover refletem imediatamente).
        val live = entries.filterIsInstance<HomeEntry.Folder>().firstOrNull { it.id == f.id }
        if (live == null) folder = null else FolderDialog(live, label, iconDp, s.iconCorner,
            onRename = { vm.renameFolder(live.id, it) }, onOpen = ::openApp, onRemove = { vm.removeFromFolder(live.id, it) }, onDismiss = { folder = null })
    }
    dockSlot?.let { slot ->
        AppPickerDialog(installed.values, s.iconCorner, "Trocar app do dock", { vm.setDockApp(slot, it.key); dockSlot = null }, { dockSlot = null })
    }
    shortcutSlot?.let { slot ->
        AppPickerDialog(installed.values, s.iconCorner, "Atalho da central de controle", { vm.setShortcut(slot, it.key); shortcutSlot = null }, { shortcutSlot = null },
            extra = listOf("Lanterna" to { vm.setShortcut(slot, "builtin:torch"); shortcutSlot = null }, "Temporizador" to { vm.setShortcut(slot, "builtin:timer"); shortcutSlot = null },
                "Calculadora" to { vm.setShortcut(slot, "builtin:calc"); shortcutSlot = null }, "Câmera" to { vm.setShortcut(slot, "builtin:camera"); shortcutSlot = null }))
    }
    if (timerDialog) AlertDialog(
        onDismissRequest = { timerDialog = false }, title = { Text("Temporizador") },
        text = { Text("O temporizador roda dentro do app e aparece na ilha dinâmica. Ele não toca alarme se o app for fechado pelo sistema.") },
        confirmButton = { Column { listOf(1, 5, 10, 15, 30).forEach { m -> TextButton({ timerEnd = System.currentTimeMillis() + m * 60_000L; timerDialog = false; overlay = Overlay.None }) { Text("$m min") } } } },
        dismissButton = { TextButton({ timerDialog = false }) { Text("Cancelar") } },
    )
}

private fun IOS_BLUR(dp: Int) = dp.dp

private fun shortcutSlot(
    key: String, idx: Int, torch: Boolean, label: (String) -> String, ctx: android.content.Context,
    onTimer: () -> Unit, onEdit: () -> Unit, start: (Intent) -> Unit, openApp: (String, Rect) -> Unit,
): ShortcutSlot = when (key) {
    "builtin:torch" -> ShortcutSlot(Icons.Rounded.FlashlightOn, "Lanterna", torch, { SystemControls.get(ctx).toggleTorch() }, onEdit)
    "builtin:timer" -> ShortcutSlot(Icons.Rounded.Timer, "Timer", null, onTimer, onEdit)
    "builtin:calc" -> ShortcutSlot(Icons.Rounded.Calculate, "Calculadora", null, { start(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)) }, onEdit)
    "builtin:camera" -> ShortcutSlot(Icons.Rounded.CameraAlt, "Câmera", null, { start(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)) }, onEdit)
    else -> ShortcutSlot(Icons.Rounded.Apps, label(key), null, { openApp(key, Rect.Zero) }, onEdit)
}
