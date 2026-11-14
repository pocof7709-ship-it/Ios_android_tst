package com.iosexperience.app.ui

import android.app.NotificationManager
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Cor do preenchimento da bateria: verde carregando, vermelho <= 20%, senão branco. */
fun batteryFillColor(level: Int, charging: Boolean): Color = when {
    charging -> Color(0xFF30D158)
    level <= 20 -> Color(0xFFFF453A)
    else -> Color.White
}

/** Barra de status DO APP (a do sistema não é controlável). Dados reais: hora, bateria, rede, Não Perturbe. */
@Composable
fun FakeStatusBar(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(60_000 - now % 60_000) } }
    val time = remember(now) { LocalTime.now().format(DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(ctx)) "HH:mm" else "h:mm")) }
    val dnd = remember(now) { ctx.getSystemService(NotificationManager::class.java).currentInterruptionFilter.let { it != NotificationManager.INTERRUPTION_FILTER_ALL && it != NotificationManager.INTERRUPTION_FILTER_UNKNOWN } }
    val battery by produceState(0 to false) {
        while (true) {
            val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val lvl = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = (i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100).coerceAtLeast(1)
            val st = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            value = (if (lvl < 0) 0 else lvl * 100 / scale) to (st == BatteryManager.BATTERY_STATUS_CHARGING)
            delay(60_000)
        }
    }
    // 0 = sem rede, 1 = Wi-Fi, 2 = dados móveis (nível de sinal não é lido: exigiria READ_PHONE_STATE)
    val net by produceState(0) {
        val cm = ctx.getSystemService(ConnectivityManager::class.java)
        fun kind(c: NetworkCapabilities?) = when {
            c?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> 1
            c?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> 2
            else -> 0
        }
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) { value = kind(caps) }
            override fun onLost(network: Network) { value = 0 }
        }
        try { cm.registerDefaultNetworkCallback(cb) } catch (e: Exception) { }
        awaitDispose { try { cm.unregisterNetworkCallback(cb) } catch (e: Exception) { } }
    }

    Row(modifier.fillMaxWidth().height(44.dp).padding(horizontal = 28.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(time, Modifier.weight(1f), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (dnd) Icon(Icons.Rounded.NightsStay, "Não perturbe", Modifier.size(14.dp), tint = Color.White)
            if (net == 2) Canvas(Modifier.size(17.dp, 11.dp)) {
                for (i in 0 until 4) {
                    val h = size.height * (0.4f + 0.2f * i)
                    drawRoundRect(Color.White, Offset(i * 4.5.dp.toPx(), size.height - h), Size(3.dp.toPx(), h), CornerRadius(1.dp.toPx()))
                }
            }
            if (net == 1) Icon(Icons.Rounded.Wifi, "Wi-Fi", Modifier.size(18.dp), tint = Color.White)
            Canvas(Modifier.size(27.dp, 13.dp)) {
                val body = size.width - 3.dp.toPx()
                drawRoundRect(Color.White.copy(alpha = 0.4f), size = Size(body, size.height), cornerRadius = CornerRadius(4.dp.toPx()), style = Stroke(1.dp.toPx()))
                val fillW = (body - 4.dp.toPx()) * battery.first / 100f
                drawRoundRect(batteryFillColor(battery.first, battery.second), Offset(2.dp.toPx(), 2.dp.toPx()), Size(fillW, size.height - 4.dp.toPx()), CornerRadius(2.5.dp.toPx()))
                drawRoundRect(Color.White.copy(alpha = 0.4f), Offset(body + 1.dp.toPx(), size.height * 0.3f), Size(1.5.dp.toPx(), size.height * 0.4f), CornerRadius(1.dp.toPx()))
            }
        }
    }
}
