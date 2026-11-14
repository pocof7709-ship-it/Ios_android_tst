package com.iosexperience.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.iosexperience.core.design.theme.IOS

/** Slider vertical em "pílula" (brilho/volume). Acessível: expõe progresso e ação setProgress. */
@Composable
fun PillSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier,
    onValueChangeFinished: () -> Unit = {},
) {
    var h by remember { mutableFloatStateOf(1f) }
    val v = value.coerceIn(0f, 1f)
    Box(
        modifier
            .clip(RoundedCornerShape(34.dp))
            .background(IOS.colors.glassTint.copy(alpha = 0.18f))
            .onSizeChanged { h = it.height.toFloat().coerceAtLeast(1f) }
            .semantics {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(v, 0f..1f)
                setProgress { t -> onValueChange(t.coerceIn(0f, 1f)); onValueChangeFinished(); true }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = { onValueChangeFinished() },
                    onDragCancel = { onValueChangeFinished() },
                ) { change, _ -> change.consume(); onValueChange(1f - change.position.y / h) }
            }
            .pointerInput(Unit) { detectTapGestures { onValueChange(1f - it.y / h); onValueChangeFinished() } },
    ) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(v).background(Color.White.copy(alpha = 0.94f)))
        Icon(icon, null, Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp).size(26.dp),
            tint = if (v > 0.12f) Color(0xFF3A3A3C) else Color.White)
    }
}
