package com.iosexperience.feature.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * "Ilha dinâmica" DENTRO do app (não é overlay do sistema). Fontes reais de dados:
 * timer interno do app, sessão de mídia ativa (via Notification Listener) e a notificação mais recente.
 * Prioridade: timer > mídia tocando > aviso de notificação. Sem dados => oculta.
 */
@Composable
fun DynamicIsland(
    timerRemainingSec: Int?,
    media: MediaInfo?,
    notice: NotifItem?,
    onCancelTimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val kind = when { timerRemainingSec != null -> 0; media?.playing == true -> 1; notice != null -> 2; else -> -1 }
    AnimatedVisibility(
        kind >= 0, modifier,
        enter = fadeIn() + scaleIn(spring(0.7f, Spring.StiffnessMediumLow), initialScale = 0.6f),
        exit = fadeOut() + scaleOut(targetScale = 0.6f),
    ) {
        val shape = RoundedCornerShape(if (expanded) 34.dp else 22.dp)
        Box(
            Modifier.padding(horizontal = 12.dp)
                .clip(shape).background(Color.Black)
                .animateContentSize(spring(0.78f, Spring.StiffnessMediumLow))
                .pointerInput(Unit) { detectTapGestures { expanded = !expanded } },
        ) {
            when (kind) {
                0 -> TimerContent(timerRemainingSec ?: 0, expanded, onCancelTimer)
                1 -> MediaContent(media!!, expanded)
                2 -> NoticeContent(notice!!, expanded)
            }
        }
    }
}

private fun fmt(sec: Int) = "%d:%02d".format(sec / 60, sec % 60)

@Composable
private fun TimerContent(rem: Int, expanded: Boolean, onCancel: () -> Unit) {
    if (!expanded) Row(Modifier.height(36.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Rounded.Timer, "Temporizador", Modifier.size(18.dp), tint = Color(0xFFFF9F0A))
        Text(fmt(rem), color = Color(0xFFFF9F0A), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    } else Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Temporizador", color = Color(0xFFFF9F0A), fontSize = 13.sp)
            Text(fmt(rem), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Light)
        }
        Icon(Icons.Rounded.Close, "Cancelar temporizador", Modifier.size(44.dp).clip(RoundedCornerShape(50)).background(Color(0xFF3A3A3C))
            .pointerInput(Unit) { detectTapGestures { onCancel() } }.padding(10.dp), tint = Color.White)
    }
}

@Composable
private fun MediaContent(m: MediaInfo, expanded: Boolean) {
    if (!expanded) Row(Modifier.height(36.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Rounded.GraphicEq, "Tocando", Modifier.size(18.dp), tint = Color(0xFF30D158))
        Text(m.title, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 150.dp))
    } else Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text(m.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(m.artist, color = Color(0x99EBEBF5), fontSize = 14.sp, maxLines = 1)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            val t = m.controller?.transportControls
            IslandBtn(Icons.Rounded.SkipPrevious, "Anterior") { t?.skipToPrevious() }
            IslandBtn(if (m.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (m.playing) "Pausar" else "Tocar") { if (m.playing) t?.pause() else t?.play() }
            IslandBtn(Icons.Rounded.SkipNext, "Próxima") { t?.skipToNext() }
        }
    }
}

@Composable
private fun NoticeContent(n: NotifItem, expanded: Boolean) {
    if (!expanded) Row(Modifier.height(36.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NotificationAppIcon(n.pkg, Modifier.size(20.dp))
        Text(n.appLabel, color = Color.White, fontSize = 13.sp, maxLines = 1, modifier = Modifier.widthIn(max = 140.dp))
    } else Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NotificationAppIcon(n.pkg, Modifier.size(40.dp))
        Column {
            Text(n.title.ifBlank { n.appLabel }, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(n.text, color = Color(0xCCFFFFFF), fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun IslandBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Icon(icon, desc, Modifier.size(52.dp).clip(RoundedCornerShape(50)).pointerInput(Unit) { detectTapGestures { onClick() } }.padding(10.dp), tint = Color.White)
}
