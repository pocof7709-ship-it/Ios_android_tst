package com.iosexperience.feature.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.iosexperience.core.design.theme.LocalWallpaper
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.iosexperience.core.data.HomeEntry
import com.iosexperience.core.design.components.GlassCard
import com.iosexperience.core.design.theme.IOS
import com.iosexperience.core.design.theme.IOSShapes

@Composable
fun FolderDialog(
    folder: HomeEntry.Folder, labelOf: (String) -> String, iconSizeDp: Int, corner: Int,
    onRename: (String) -> Unit, onOpen: (String, Rect) -> Unit, onRemove: (String) -> Unit, onDismiss: () -> Unit,
) {
    var name by remember(folder.id) { mutableStateOf(folder.name) }
    Dialog(onDismissRequest = { onRename(name); onDismiss() }) {
      CompositionLocalProvider(LocalWallpaper provides null) {
        GlassCard(Modifier.fillMaxWidth().background(Color0, IOSShapes.Panel), IOSShapes.Panel) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                BasicTextField(name, { name = it.take(24) }, singleLine = true, cursorBrush = SolidColor(IOS.colors.accent),
                    textStyle = TextStyle(color = IOS.colors.label, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                LazyVerticalGrid(GridCells.Fixed(3), Modifier.heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(folder.keys, key = { it }) { k ->
                        var b by remember { mutableStateOf(Rect.Zero) }
                        Column(Modifier.pointerInput(k) { detectTapGestures(onTap = { onOpen(k, b) }, onLongPress = { onRemove(k) }) },
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            AppIcon(k, iconSizeDp.dp, corner, Modifier.androidxOnPos { b = it })
                            Text(labelOf(k), color = IOS.colors.label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Text("Toque e segure um app para tirá-lo da pasta", color = IOS.colors.secondaryLabel, fontSize = 11.sp, modifier = Modifier.padding(top = 12.dp))
            }
        }
      }
    }
}

private val Color0 = androidx.compose.ui.graphics.Color(0x66000000)
private fun Modifier.androidxOnPos(set: (Rect) -> Unit): Modifier = this.onGloballyPositioned { set(it.boundsInWindow()) }

/** Pesquisa de apps + atalhos para as telas do launcher. */
@Composable
fun SearchOverlay(
    apps: Collection<LauncherApp>, iconSizeDp: Int, corner: Int,
    onOpen: (String, Rect) -> Unit, quickActions: List<Pair<String, () -> Unit>>, modifier: Modifier = Modifier,
) {
    var q by remember { mutableStateOf("") }
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { fr.requestFocus() }
    val results = remember(q, apps) { if (q.isBlank()) emptyList() else apps.filter { it.label.contains(q.trim(), ignoreCase = true) }.take(30) }
    Column(modifier.fillMaxSize().statusBarsPadding().imePadding().padding(16.dp)) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(IOS.colors.glassTint.copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Search, null, tint = IOS.colors.secondaryLabel)
            Box(Modifier.padding(start = 10.dp)) {
                if (q.isEmpty()) Text("Buscar apps", color = IOS.colors.secondaryLabel, fontSize = 16.sp)
                BasicTextField(q, { q = it }, singleLine = true, cursorBrush = SolidColor(IOS.colors.accent),
                    textStyle = TextStyle(color = IOS.colors.label, fontSize = 16.sp), modifier = Modifier.fillMaxWidth().focusRequester(fr))
            }
        }
        LazyRow(Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(quickActions) { (label, act) ->
                Text(label, color = IOS.colors.label, fontSize = 14.sp, modifier = Modifier.clip(RoundedCornerShape(50)).background(IOS.colors.glassTint.copy(alpha = 0.2f))
                    .pointerInput(label) { detectTapGestures { act() } }.padding(horizontal = 16.dp, vertical = 12.dp))
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(results, key = { it.key }) { a ->
                var b by remember { mutableStateOf(Rect.Zero) }
                Row(Modifier.fillMaxWidth().pointerInput(a.key) { detectTapGestures { onOpen(a.key, b) } }.padding(8.dp)
                    .androidxOnPos { b = it }, verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(a.key, 44.dp, corner)
                    Text(a.label, Modifier.padding(start = 14.dp), color = IOS.colors.label, fontSize = 16.sp)
                }
            }
        }
    }
}

/** Seletor de app (dock e atalhos da central de controle). */
@Composable
fun AppPickerDialog(apps: Collection<LauncherApp>, corner: Int, title: String, onPick: (LauncherApp) -> Unit, onDismiss: () -> Unit, extra: List<Pair<String, () -> Unit>> = emptyList()) {
    Dialog(onDismissRequest = onDismiss) {
      CompositionLocalProvider(LocalWallpaper provides null) {
        GlassCard(Modifier.fillMaxWidth().heightIn(max = 520.dp).background(Color0, IOSShapes.Panel), IOSShapes.Panel) {
            Column(Modifier.padding(16.dp)) {
                Text(title, color = IOS.colors.label, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn {
                    items(extra) { (l, a) -> Text(l, color = IOS.colors.accent, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().pointerInput(l) { detectTapGestures { a() } }.padding(12.dp)) }
                    items(apps.toList(), key = { it.key }) { a ->
                        Row(Modifier.fillMaxWidth().pointerInput(a.key) { detectTapGestures { onPick(a) } }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(a.key, 40.dp, corner); Text(a.label, Modifier.padding(start = 12.dp), color = IOS.colors.label)
                        }
                    }
                }
            }
        }
      }
    }
}

/** Multitarefa: histórico dos apps abertos POR este launcher (o Android não expõe os recentes do sistema a apps comuns). */
@Composable
fun RecentsScreen(keys: List<String>, labelOf: (String) -> String, corner: Int, onOpen: (String, Rect) -> Unit, onRemove: (String) -> Unit, onClear: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().statusBarsPadding().padding(vertical = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Multitarefa", Modifier.weight(1f), color = IOS.colors.label, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            if (keys.isNotEmpty()) Text("Limpar", color = IOS.colors.accent, fontSize = 16.sp, modifier = Modifier.pointerInput(Unit) { detectTapGestures { onClear() } }.padding(8.dp))
        }
        if (keys.isEmpty()) Text("Os apps que você abrir por aqui aparecerão nesta lista.\nO Android não permite mostrar prévias nem os apps recentes do sistema.",
            color = IOS.colors.secondaryLabel, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
        else LazyRow(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            items(keys, key = { it }) { k ->
                var b by remember { mutableStateOf(Rect.Zero) }
                GlassCard(Modifier.width(220.dp).height(400.dp).androidxOnPos { b = it }
                    .pointerInput(k) { detectTapGestures(onTap = { onOpen(k, b) }, onLongPress = { onRemove(k) }) }, IOSShapes.Panel) {
                    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        AppIcon(k, 96.dp, corner)
                        Text(labelOf(k), Modifier.padding(top = 16.dp), color = IOS.colors.label, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, textAlign = TextAlign.Center)
                        Text("Toque para voltar · segure para remover", Modifier.padding(top = 8.dp), color = IOS.colors.secondaryLabel, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
