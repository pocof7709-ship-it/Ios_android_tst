package com.iosexperience.app.ui

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iosexperience.core.data.CalEvent
import com.iosexperience.core.data.CalendarRepository
import com.iosexperience.core.design.components.GlassCard
import com.iosexperience.core.design.theme.IOS
import com.iosexperience.feature.notifications.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Página "Hoje": widgets nativos (Compose). Widgets de terceiros (AppWidgetHost) ficam para a v0.3. */
@Composable
fun TodayPage(media: MediaInfo?, notificationCount: Int, showCalendar: Boolean, onOpenNotifications: () -> Unit, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(60_000 - now % 60_000) } }
    val event by produceState<CalEvent?>(null, showCalendar, now / 300_000) {
        value = if (showCalendar) withContext(Dispatchers.IO) { CalendarRepository.nextEvent(ctx) } else null
    }
    val battery by produceState(0 to false) {
        while (true) {
            val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val lvl = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = (i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100).coerceAtLeast(1)
            val st = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            value = (if (lvl < 0) 0 else lvl * 100 / scale) to (st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL)
            delay(30_000)
        }
    }
    val time = remember(now) { LocalTime.now().format(DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(ctx)) "HH:mm" else "h:mm")) }
    val date = remember(now) { DateUtils.formatDateTime(ctx, now, DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR) }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(androidx.compose.ui.res.stringResource(com.iosexperience.app.R.string.today_title), color = IOS.colors.label, style = com.iosexperience.core.design.theme.IOSType.LargeTitle)
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text(date, color = IOS.colors.accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(time, color = IOS.colors.label, fontSize = 56.sp, fontWeight = FontWeight.Light)
                if (showCalendar) Text(event?.let { "${it.title} · " + if (it.allDay) "dia todo" else DateUtils.formatDateTime(ctx, it.beginMs, DateUtils.FORMAT_SHOW_TIME) } ?: "Sem eventos nas próximas 24h",
                    color = IOS.colors.secondaryLabel, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassCard(Modifier.weight(1f).height(128.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(if (battery.second) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull, "Bateria", tint = if (battery.second) IOS.colors.success else IOS.colors.label)
                    Column {
                        Text("${battery.first}%", color = IOS.colors.label, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                        Text(if (battery.second) "Carregando" else "Bateria", color = IOS.colors.secondaryLabel, fontSize = 13.sp)
                    }
                }
            }
            GlassCard(Modifier.weight(1f).height(128.dp).pointerInput(Unit) { detectTapGestures { onOpenNotifications() } }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Rounded.Notifications, null, tint = IOS.colors.label)
                    Column {
                        Text("$notificationCount", color = IOS.colors.label, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                        Text("Notificações", color = IOS.colors.secondaryLabel, fontSize = 13.sp)
                    }
                }
            }
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("MÍDIA", color = IOS.colors.secondaryLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                if (media == null) Text("Nada tocando (requer acesso a notificações)", color = IOS.colors.secondaryLabel, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
                else {
                    Text(media.title, color = IOS.colors.label, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                    Text(media.artist, color = IOS.colors.secondaryLabel, fontSize = 14.sp, maxLines = 1)
                    Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        val t = media.controller?.transportControls
                        Text("⏮", fontSize = 26.sp, color = IOS.colors.label, modifier = Modifier.pointerInput(Unit) { detectTapGestures { t?.skipToPrevious() } }.padding(8.dp))
                        Text(if (media.playing) "⏸" else "▶", fontSize = 26.sp, color = IOS.colors.label, modifier = Modifier.pointerInput(media.playing) { detectTapGestures { if (media.playing) t?.pause() else t?.play() } }.padding(8.dp))
                        Text("⏭", fontSize = 26.sp, color = IOS.colors.label, modifier = Modifier.pointerInput(Unit) { detectTapGestures { t?.skipToNext() } }.padding(8.dp))
                    }
                }
            }
        }
    }
}
