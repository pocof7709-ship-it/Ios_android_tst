package com.iosexperience.feature.notifications

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iosexperience.core.data.Graph
import com.iosexperience.core.design.components.GlassCard
import com.iosexperience.core.design.components.IOSButton
import com.iosexperience.core.design.theme.IOS
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun NotificationAppIcon(pkg: String, modifier: Modifier = Modifier) {
    val bmp by produceState(Graph.icons.peek("pkg:$pkg"), pkg) {
        if (value == null) value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { Graph.icons.get("pkg:$pkg") }
    }
    Box(modifier.clip(RoundedCornerShape(22)).then(Modifier)) { bmp?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize()) } }
}

@Composable
fun NotificationCard(item: NotifItem, showContent: Boolean, onOpen: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    var off by remember { mutableFloatStateOf(0f) }
    val time = remember(item.whenMs) { DateUtils.getRelativeTimeSpanString(item.whenMs, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString() }
    GlassCard(
        modifier.fillMaxWidth()
            .offset { androidx.compose.ui.unit.IntOffset(off.roundToInt(), 0) }
            .semantics(mergeDescendants = true) { contentDescription = "${item.appLabel}. ${if (showContent) item.title + ". " + item.text else "Conteúdo oculto"}. $time" }
            .pointerInput(item.key) { detectTapGestures(onTap = { onOpen() }) }
            .pointerInput(item.key, item.clearable) {
                if (item.clearable) detectHorizontalDragGestures(
                    onDragEnd = { if (abs(off) > 140.dp.toPx()) onDismiss() else off = 0f },
                    onDragCancel = { off = 0f },
                ) { c, d -> c.consume(); off += d }
            },
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NotificationAppIcon(item.pkg, Modifier.size(38.dp))
            Column(Modifier.weight(1f)) {
                Row {
                    Text(if (showContent) item.title.ifBlank { item.appLabel } else item.appLabel, Modifier.weight(1f),
                        color = IOS.colors.label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(time, color = IOS.colors.secondaryLabel, fontSize = 12.sp)
                }
                Text(if (showContent) item.text else "Notificação", color = IOS.colors.label.copy(alpha = 0.85f),
                    fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Central de notificações: agrupada por app, mais recente primeiro, deslize p/ dispensar. */
@Composable
fun NotificationCenter(
    items: List<NotifItem>,
    hasAccess: Boolean,
    showContent: Boolean,
    onOpenAccessSettings: () -> Unit,
    onOpen: (NotifItem) -> Unit,
    onDismiss: (NotifItem) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Notificações",
    onClose: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(setOf<String>()) }
    var confirmClear by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), color = IOS.colors.label, style = com.iosexperience.core.design.theme.IOSType.LargeTitle.copy(fontSize = 28.sp))
            if (onClose != null) Text("✕", color = IOS.colors.label, fontSize = 18.sp, modifier = Modifier.semantics { contentDescription = "Fechar" }.pointerInput(Unit) { detectTapGestures { onClose() } }.padding(10.dp))
            if (items.any { it.clearable }) Text("Limpar", color = IOS.colors.accent, fontSize = 16.sp,
                modifier = Modifier.pointerInput(Unit) { detectTapGestures { confirmClear = true } }.padding(8.dp))
        }
        when {
            !hasAccess -> GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Acesso necessário", color = IOS.colors.label, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Text("Para mostrar suas notificações aqui, o Android exige que você conceda “Acesso a notificações”. " +
                        "O app lê título e texto só para exibi-los neste aparelho; nada é salvo nem enviado.",
                        color = IOS.colors.secondaryLabel, fontSize = 14.sp)
                    Text("No HyperOS, se a opção aparecer bloqueada: Info do app → ⋮ → Permitir configurações restritas.",
                        color = IOS.colors.secondaryLabel, fontSize = 13.sp)
                    IOSButton("Abrir configurações", onOpenAccessSettings)
                }
            }
            items.isEmpty() -> Text("Nenhuma notificação", color = IOS.colors.secondaryLabel, modifier = Modifier.padding(top = 40.dp).align(Alignment.CenterHorizontally))
            else -> {
                val groups = items.groupBy { it.pkg }.values.sortedByDescending { g -> g.maxOf { it.whenMs } }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                    groups.forEach { g ->
                        val pkg = g.first().pkg
                        val open = pkg in expanded
                        val shown = if (open) g else g.take(1)
                        item(key = "h$pkg") {
                            Row(Modifier.fillMaxWidth().animateItem().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(g.first().appLabel.uppercase(), Modifier.weight(1f), color = IOS.colors.secondaryLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                if (g.size > 1) Text(if (open) "Mostrar menos" else "+${g.size - 1} mais", color = IOS.colors.accent, fontSize = 13.sp,
                                    modifier = Modifier.pointerInput(pkg) { detectTapGestures { expanded = if (open) expanded - pkg else expanded + pkg } }.padding(6.dp))
                            }
                        }
                        items(shown, key = { it.key }) { n ->
                            NotificationCard(n, showContent, { onOpen(n) }, { onDismiss(n) }, Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }
    if (confirmClear) androidx.compose.material3.AlertDialog(
        onDismissRequest = { confirmClear = false },
        title = { Text("Limpar notificações?") },
        text = { Text("Isso dispensa todas as notificações que o Android permite remover.") },
        confirmButton = { androidx.compose.material3.TextButton({ confirmClear = false; onClearAll() }) { Text("Limpar") } },
        dismissButton = { androidx.compose.material3.TextButton({ confirmClear = false }) { Text("Cancelar") } },
    )
}
