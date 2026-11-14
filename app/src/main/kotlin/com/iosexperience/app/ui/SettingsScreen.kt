package com.iosexperience.app.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.iosexperience.core.data.CalendarRepository
import com.iosexperience.core.data.Graph
import com.iosexperience.core.data.Perf
import com.iosexperience.core.data.ThemeMode
import com.iosexperience.core.data.UiSettings
import com.iosexperience.core.design.components.GlassCard
import com.iosexperience.core.design.components.IOSButton
import com.iosexperience.core.design.components.IOSSwitch
import com.iosexperience.core.design.theme.AccentPalette
import com.iosexperience.core.design.theme.IOS
import com.iosexperience.feature.ai.ProviderRegistry
import com.iosexperience.feature.lockscreen.ClockStyleNames
import com.iosexperience.feature.notifications.NotificationStore

@Composable
fun SettingsScreen(s: UiSettings, notificationAccess: Boolean, onResetHome: () -> Unit, onClearAiHistory: () -> Unit, onClose: () -> Unit) {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }
    val nm = ctx.getSystemService(NotificationManager::class.java)
    val canWrite = remember(tick) { Settings.System.canWrite(ctx) }
    val dndAccess = remember(tick) { nm.isNotificationPolicyAccessGranted }
    val calendar = remember(tick) { CalendarRepository.hasPermission(ctx) }
    val listenerOn = remember(tick) { NotificationStore.hasAccess(ctx) }
    fun update(b: (UiSettings) -> UiSettings) = Graph.settings.update(b)
    fun open(i: Intent) { try { ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (e: Exception) { } }

    val calPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) update { it.copy(showCalendar = true) }; tick++ }
    var confirmResetHome by remember { mutableStateOf(false) }
    var confirmResetAll by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf("") }
    var keySaved by remember(tick) { mutableStateOf(Graph.keys.has("remote")) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Configurações", Modifier.weight(1f), color = IOS.colors.label, style = com.iosexperience.core.design.theme.IOSType.LargeTitle)
                Text("Concluído", color = IOS.colors.accent, fontSize = 16.sp, modifier = Modifier.pointerInput(Unit) { detectTapGestures { onClose() } }.padding(8.dp))
            }
        }
        item { Group("Aparência") {
            Choice("Tema", listOf("Sistema", "Escuro", "Claro"), s.themeMode.ordinal) { i -> update { it.copy(themeMode = ThemeMode.values()[i]) } }
            Label("Cor de destaque")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                AccentPalette.forEachIndexed { i, (_, c) ->
                    Box(Modifier.size(36.dp).clip(CircleShape).background(c).pointerInput(i) { detectTapGestures { update { it.copy(accentIndex = i) } } }, contentAlignment = Alignment.Center) {
                        if (i == s.accentIndex) Text("✓", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
            Slide("Arredondamento dos ícones", "${s.iconCorner}%", s.iconCorner.toFloat(), 10f..50f) { v -> update { it.copy(iconCorner = v.toInt()) } }
            Slide("Tamanho dos ícones", "${s.iconSizeDp} dp", s.iconSizeDp.toFloat(), 48f..72f) { v -> update { it.copy(iconSizeDp = v.toInt()) } }
            Choice(androidx.compose.ui.res.stringResource(com.iosexperience.app.R.string.settings_wallpaper), com.iosexperience.core.design.theme.Wallpapers.all.map { it.name }, s.wallpaper) { i -> update { it.copy(wallpaper = i) } }
            Row(verticalAlignment = Alignment.CenterVertically) { Label(androidx.compose.ui.res.stringResource(com.iosexperience.app.R.string.settings_use_system_wallpaper), Modifier.weight(1f)); IOSSwitch(s.useSystemWallpaper, { v -> update { it.copy(useSystemWallpaper = v) } }) }
            Note(androidx.compose.ui.res.stringResource(com.iosexperience.app.R.string.settings_use_system_wallpaper_note))
            Row(verticalAlignment = Alignment.CenterVertically) { Label(androidx.compose.ui.res.stringResource(com.iosexperience.app.R.string.settings_fake_status_bar), Modifier.weight(1f)); IOSSwitch(s.fakeStatusBar, { v -> update { it.copy(fakeStatusBar = v) } }) }
            Note(androidx.compose.ui.res.stringResource(com.iosexperience.app.R.string.settings_fake_status_bar_note))
            Choice("Estilo do relógio", ClockStyleNames, s.clockStyle) { i -> update { it.copy(clockStyle = i) } }
            IOSButton("Escolher papel de parede do sistema", { open(Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER), "Papel de parede")) })
        } }
        item { Group("Tela inicial") {
            Slide("Linhas", "${s.gridRows}", s.gridRows.toFloat(), 3f..8f) { v -> update { it.copy(gridRows = v.toInt()) } }
            Slide("Colunas", "${s.gridColumns}", s.gridColumns.toFloat(), 3f..6f) { v -> update { it.copy(gridColumns = v.toInt()) } }
            Note("Toque e segure um ícone para arrastar. Solte no centro de outro para criar uma pasta.")
            IOSButton("Restaurar organização", { confirmResetHome = true })
        } }
        item { Group("Desempenho") {
            Choice("Perfil", listOf("Econômico", "Equilibrado", "Visual avançado"), s.perf.ordinal) { i -> update { it.copy(perf = Perf.values()[i]) } }
            Note("Econômico: sem desfoque e taxa de 60 Hz. Visual avançado: desfoque maior e 120 Hz quando o HyperOS permitir.")
            Row(verticalAlignment = Alignment.CenterVertically) { Label("Animações", Modifier.weight(1f)); IOSSwitch(s.animations, { v -> update { it.copy(animations = v) } }) }
        } }
        item { Group("Notificações e tela de bloqueio") {
            Perm("Acesso a notificações", listenerOn, "Mostrar e dispensar notificações e controlar a mídia. Lê só título/texto, na memória.") { open(NotificationStore.accessSettingsIntent()) }
            Row(verticalAlignment = Alignment.CenterVertically) { Label("Mostrar prévia do conteúdo", Modifier.weight(1f)); IOSSwitch(s.showLockPreviews, { v -> update { it.copy(showLockPreviews = v) } }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Label("Próximo evento do calendário", Modifier.weight(1f))
                IOSSwitch(s.showCalendar && calendar, { v -> if (!v) update { it.copy(showCalendar = false) } else if (calendar) update { it.copy(showCalendar = true) } else calPerm.launch(Manifest.permission.READ_CALENDAR) }) }
            Note("A tela de bloqueio daqui é uma tela alternativa dentro do app: não substitui a tela de bloqueio segura do sistema.")
        } }
        item { Group("Central de controle") {
            Perm("Modificar configurações do sistema", canWrite, "Necessária só para brilho e rotação automática.") { open(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, android.net.Uri.parse("package:${ctx.packageName}"))) }
            Perm("Acesso ao Não perturbe", dndAccess, "Necessária só para ligar/desligar o Não perturbe.") { open(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)) }
            Note("Wi-Fi, Bluetooth e modo avião não podem ser alternados por apps; os botões abrem o painel do sistema.")
        } }
        item { Group("Inteligência artificial") {
            Row(verticalAlignment = Alignment.CenterVertically) { Label("Ativar assistente", Modifier.weight(1f)); IOSSwitch(s.aiEnabled, { v -> update { it.copy(aiEnabled = v) } }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Label("Guardar histórico no aparelho", Modifier.weight(1f)); IOSSwitch(s.aiHistory, { v -> update { it.copy(aiHistory = v) } }) }
            Label("Provedor")
            ProviderRegistry.all.forEach { p ->
                Text((if (s.aiProvider == p.id || (p.id == "local" && !ProviderRegistry.all.any { it.id == s.aiProvider && it.implemented })) "● " else "○ ") + p.displayName,
                    color = if (p.implemented) IOS.colors.label else IOS.colors.secondaryLabel, fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth().pointerInput(p.id) { if (p.implemented) detectTapGestures { update { it.copy(aiProvider = p.id) } } }.padding(vertical = 10.dp))
            }
            Label("Chave de API (reservada para provedores remotos futuros)")
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(IOS.colors.glassTint.copy(alpha = 0.15f)).padding(12.dp)) {
                if (apiKey.isEmpty()) Text(if (keySaved) "Chave salva (cifrada no Keystore)" else "Cole a chave aqui", color = IOS.colors.secondaryLabel, fontSize = 14.sp)
                BasicTextField(apiKey, { apiKey = it }, singleLine = true, visualTransformation = PasswordVisualTransformation(), cursorBrush = SolidColor(IOS.colors.accent),
                    textStyle = TextStyle(color = IOS.colors.label, fontSize = 14.sp), modifier = Modifier.fillMaxWidth())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IOSButton("Salvar chave", { Graph.keys.save("remote", apiKey.trim()); apiKey = ""; keySaved = Graph.keys.has("remote") }, enabled = apiKey.isNotBlank())
                IOSButton("Remover", { Graph.keys.save("remote", ""); keySaved = false }, enabled = keySaved)
            }
            Note("Nesta versão só o provedor local existe: nenhum dado, comando ou notificação sai do aparelho. A chave é guardada cifrada, mas ainda não é usada.")
            IOSButton("Apagar histórico do assistente", onClearAiHistory)
        } }
        item { Group("Sobre") {
            Text("iOS Experience AI v0.2.0 — projeto pessoal, inspirado (não afiliado) ao iOS.", color = IOS.colors.label, fontSize = 14.sp)
            Note("Defina como tela inicial em Configurações → Apps → Apps padrão → Tela inicial. Funções que dependem de permissão aparecem acima com o estado real.")
            IOSButton("Restaurar todas as configurações", { confirmResetAll = true })
        } }
    }
    if (confirmResetHome) Confirm("Restaurar organização?", "Pastas e posições dos ícones voltam ao padrão (ordem alfabética).", { confirmResetHome = false; onResetHome() }, { confirmResetHome = false })
    if (confirmResetAll) Confirm("Restaurar configurações?", "Aparência, grade, desempenho e IA voltam ao padrão. A organização da tela inicial é mantida.", { confirmResetAll = false; Graph.settings.reset() }, { confirmResetAll = false })
}

@Composable private fun Group(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), color = IOS.colors.secondaryLabel, style = com.iosexperience.core.design.theme.IOSType.SectionHeader, modifier = Modifier.padding(start = 16.dp, bottom = 6.dp))
        // Cartão opaco de 10 dp (como em Ajustes do iOS), sem vidro.
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(IOS.colors.card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}
@Composable private fun Label(t: String, modifier: Modifier = Modifier) = Text(t, modifier, color = IOS.colors.label, fontSize = 15.sp)
@Composable private fun Note(t: String) = Text(t, color = IOS.colors.secondaryLabel, fontSize = 12.sp)

@Composable private fun Slide(title: String, value: String, v: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Row { Label(title, Modifier.weight(1f)); Text(value, color = IOS.colors.secondaryLabel, fontSize = 14.sp) }
        Slider(v, onChange, valueRange = range, steps = (range.endInclusive - range.start).toInt() - 1)
    }
}

@Composable private fun Choice(title: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Column {
        Label(title)
        Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEachIndexed { i, o ->
                Text(o, color = if (i == selected) Color.White else IOS.colors.label, fontSize = 13.sp, maxLines = 1,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(if (i == selected) IOS.colors.accent else IOS.colors.glassTint.copy(alpha = 0.15f))
                        .pointerInput(i) { detectTapGestures { onSelect(i) } }.padding(horizontal = 12.dp, vertical = 10.dp))
            }
        }
    }
}

@Composable private fun Perm(name: String, granted: Boolean, purpose: String, onOpen: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Label(name, Modifier.weight(1f))
            Text(if (granted) "Concedida" else "Abrir", color = if (granted) IOS.colors.success else IOS.colors.accent, fontSize = 14.sp,
                modifier = Modifier.pointerInput(Unit) { detectTapGestures { onOpen() } }.padding(8.dp))
        }
        Note(purpose)
    }
}

@Composable private fun Confirm(title: String, text: String, onYes: () -> Unit, onNo: () -> Unit) = AlertDialog(
    onDismissRequest = onNo, title = { Text(title) }, text = { Text(text) },
    confirmButton = { TextButton(onYes) { Text("Restaurar") } }, dismissButton = { TextButton(onNo) { Text("Cancelar") } },
)
