package com.iosexperience.feature.controlcenter

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.iosexperience.core.design.components.GlassCard
import com.iosexperience.core.design.components.PillSlider
import com.iosexperience.core.design.components.RoundToggle
import com.iosexperience.core.design.theme.IOS
import com.iosexperience.core.design.theme.IOSShapes
import com.iosexperience.feature.notifications.MediaInfo

/** Um atalho já resolvido pelo app host (ícone, nome e ação). */
data class ShortcutSlot(val icon: ImageVector, val label: String, val active: Boolean?, val onClick: () -> Unit, val onLongClick: () -> Unit)

@Composable
fun ControlCenter(
    media: MediaInfo?,
    hasNotificationAccess: Boolean,
    darkModeActive: Boolean,
    onToggleDarkMode: () -> Unit,
    shortcuts: List<ShortcutSlot>,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val controls = remember { SystemControls.get(ctx) }
    var st by remember { mutableStateOf(controls.snapshot()) }
    var message by remember { mutableStateOf<String?>(null) }
    val torch by controls.torchOn.collectAsState()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { st = controls.snapshot() }
    fun handle(r: ActionResult) {
        when (r) { is ActionResult.Redirected -> message = r.message; is ActionResult.Failed -> message = r.message; ActionResult.Done -> message = null }
        st = controls.snapshot()
    }

    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassCard(Modifier.weight(1f).aspectRatio(1f)) {
                Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceEvenly) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        RoundToggle(Icons.Rounded.Flight, "Modo avião", st.airplane, { handle(controls.openAirplane()) }, size = 60.dp, activeColor = androidx.compose.ui.graphics.Color(0xFFFF9F0A))
                        RoundToggle(Icons.Rounded.Wifi, "Wi-Fi", st.wifi, { handle(controls.openWifi()) }, size = 60.dp, activeColor = IOS.colors.accent)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        RoundToggle(Icons.Rounded.Bluetooth, "Bluetooth", st.bluetooth, { handle(controls.openBluetooth()) }, size = 60.dp, activeColor = IOS.colors.accent)
                        RoundToggle(Icons.Rounded.DarkMode, "Modo escuro do iOS Experience", darkModeActive, onToggleDarkMode, size = 60.dp)
                    }
                }
            }
            GlassCard(Modifier.weight(1f).aspectRatio(1f), IOSShapes.Card) { MediaCard(media, hasNotificationAccess) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoundToggle(if (st.autoRotate) Icons.Rounded.ScreenRotation else Icons.Rounded.ScreenLockRotation, "Rotação automática", st.autoRotate, { handle(controls.toggleAutoRotate(st.autoRotate)) }, Modifier.weight(1f), size = 72.dp)
                    RoundToggle(Icons.Rounded.DoNotDisturbOn, "Não perturbe", st.dnd, { handle(controls.toggleDnd(st.dnd)) }, Modifier.weight(1f), size = 72.dp)
                }
                RoundToggle(Icons.Rounded.FlashlightOn, "Lanterna", torch, { handle(controls.toggleTorch()) }, Modifier.fillMaxWidth(), size = 72.dp)
            }
            PillSlider(st.brightness, { v -> st = st.copy(brightness = v) }, Icons.Rounded.WbSunny, "Brilho", Modifier.weight(0.5f).height(156.dp),
                onValueChangeFinished = { handle(controls.setBrightness(st.brightness)) })
            PillSlider(st.volume, { v -> st = st.copy(volume = v); controls.setVolume(v) }, Icons.AutoMirrored.Rounded.VolumeUp, "Volume de mídia", Modifier.weight(0.5f).height(156.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            shortcuts.forEach { s ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RoundToggle(s.icon, s.label, s.active, s.onClick, size = 68.dp, onLongClick = s.onLongClick)
                    Text(s.label, color = IOS.colors.label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        Text("Toque e segure um atalho para trocá-lo por um app.", color = IOS.colors.secondaryLabel, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        message?.let { GlassCard(Modifier.fillMaxWidth()) { Text(it, Modifier.padding(14.dp), color = IOS.colors.label, fontSize = 13.sp) } }
    }
}

@Composable
private fun MediaCard(media: MediaInfo?, hasAccess: Boolean) {
    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
        if (media == null) {
            Icon(Icons.Rounded.MusicNote, null, tint = IOS.colors.secondaryLabel)
            Text(if (hasAccess) "Nenhuma mídia tocando" else "Conceda acesso a notificações para ver e controlar a mídia",
                color = IOS.colors.secondaryLabel, fontSize = 12.sp)
        } else {
            Column {
                Text(media.title, color = IOS.colors.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(media.artist, color = IOS.colors.secondaryLabel, fontSize = 12.sp, maxLines = 1)
            }
            val t = media.controller?.transportControls
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                RoundToggle(Icons.Rounded.SkipPrevious, "Anterior", null, { t?.skipToPrevious() }, size = 40.dp)
                RoundToggle(if (media.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (media.playing) "Pausar" else "Tocar", null, { if (media.playing) t?.pause() else t?.play() }, size = 44.dp)
                RoundToggle(Icons.Rounded.SkipNext, "Próxima", null, { t?.skipToNext() }, size = 40.dp)
            }
        }
    }
}
