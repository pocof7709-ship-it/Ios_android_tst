package com.iosexperience.feature.lockscreen

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iosexperience.core.data.CalEvent
import com.iosexperience.core.data.CalendarRepository
import com.iosexperience.core.design.components.RoundToggle
import com.iosexperience.feature.controlcenter.SystemControls
import com.iosexperience.feature.notifications.NotifItem
import com.iosexperience.feature.notifications.NotificationCard
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val ClockStyleNames = listOf("Fino", "Empilhado", "Mono", "Arredondado")

/**
 * Tela de bloqueio ALTERNATIVA, dentro do app. NÃO substitui nem contorna a tela de bloqueio segura do sistema
 * (apps comuns não podem). Não bloqueia o aparelho; é uma "tela de relance" com relógio, agenda e notificações.
 */
@Composable
fun LockScreen(
    clockStyle: Int,
    notifications: List<NotifItem>,
    showPreviews: Boolean,
    showCalendar: Boolean,
    onOpenNotification: (NotifItem) -> Unit,
    onCamera: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(60_000 - now % 60_000) } }
    val event by produceState<CalEvent?>(null, showCalendar, now / 300_000) {
        value = if (showCalendar) kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { CalendarRepository.nextEvent(ctx) } else null
    }
    val is24 = DateFormat.is24HourFormat(ctx)
    val t = remember(now) { LocalTime.now() }
    val date = remember(now) { DateUtils.formatDateTime(ctx, now, DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR) }
    val torch by SystemControls.get(ctx).torchOn.collectAsState()

    Box(
        modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0x66000000), Color(0x00000000), Color(0x99000000))))
            .pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(onDragStart = { total = 0f }, onDragEnd = { if (total < -160.dp.toPx()) onClose() }) { c, d -> c.consume(); total += d }
            }
            .statusBarsPadding().navigationBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(64.dp))
            Text(date, color = Color.White.copy(alpha = 0.9f), fontSize = 17.sp, fontWeight = FontWeight.Medium)
            if (event != null) Text("${event!!.title} · " + (if (event!!.allDay) "dia todo" else DateUtils.formatDateTime(ctx, event!!.beginMs, DateUtils.FORMAT_SHOW_TIME)),
                color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
            Clock(t, clockStyle, is24)
            LazyColumn(Modifier.weight(1f).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notifications.take(4), key = { it.key }) { n -> NotificationCard(n, showPreviews, { onOpenNotification(n) }, { com.iosexperience.feature.notifications.NotificationStore.dismiss(n.key) }) }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                RoundToggle(Icons.Rounded.FlashlightOn, "Lanterna", torch, { SystemControls.get(ctx).toggleTorch() }, size = 56.dp)
                Text("Deslize para cima para sair", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Center)
                RoundToggle(Icons.Rounded.CameraAlt, "Câmera", null, onCamera, size = 56.dp)
            }
            // Barra de "desbloqueio" (visual; sair é deslizar para cima).
            Box(Modifier.padding(bottom = 6.dp).size(134.dp, 5.dp).background(Color.White.copy(alpha = 0.9f), androidx.compose.foundation.shape.RoundedCornerShape(50)))
        }
    }
}

@Composable
private fun Clock(t: LocalTime, style: Int, is24: Boolean) {
    val hh = t.format(DateTimeFormatter.ofPattern(if (is24) "HH" else "h"))
    val mm = t.format(DateTimeFormatter.ofPattern("mm"))
    val c = Color.White.copy(alpha = 0.95f)
    when (style) {
        1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(hh, color = c, style = TextStyle(fontSize = 108.sp, fontWeight = FontWeight.Black, lineHeight = 100.sp))
            Text(mm, color = c.copy(alpha = 0.8f), style = TextStyle(fontSize = 108.sp, fontWeight = FontWeight.Black, lineHeight = 100.sp))
        }
        2 -> Text("$hh:$mm", color = c, style = TextStyle(fontSize = 84.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium))
        3 -> Text("$hh:$mm", color = c, style = TextStyle(fontSize = 92.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold))
        else -> Text("$hh:$mm", color = c, style = com.iosexperience.core.design.theme.IOSType.Clock((132 * (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp / 390f).coerceIn(0.9f, 1.15f)).sp))
    }
}
