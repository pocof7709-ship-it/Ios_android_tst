package com.iosexperience.core.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import com.iosexperience.core.design.theme.IOS

/** Botão circular de vidro da central de controle. [active] = estado real; null = desconhecido. */
@Composable
fun RoundToggle(
    icon: ImageVector,
    description: String,
    active: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    onLongClick: (() -> Unit)? = null,
    activeColor: Color? = null,
) {
    val bg by animateColorAsState(
        if (active == true) (activeColor ?: Color.White) else Color.White.copy(alpha = 0.22f), label = "toggle")
    Box(
        modifier.size(size).clip(CircleShape).background(bg)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.Button
                if (active != null) stateDescription = if (active) "Ligado" else "Desligado"
            }
            .pointerInput(onClick, onLongClick) { detectTapGestures(onTap = { onClick() }, onLongPress = onLongClick?.let { l -> { _: androidx.compose.ui.geometry.Offset -> l() } }) },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, Modifier.size(size * 0.42f), tint = if (active == true && activeColor == null) Color(0xFF1C1C1E) else Color.White) }
}
