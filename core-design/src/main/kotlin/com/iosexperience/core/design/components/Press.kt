package com.iosexperience.core.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.iosexperience.core.design.theme.LocalReduceMotion

/**
 * Feedback de toque estilo iOS no lugar do ripple: escurece (opacidade ~0,6 em 0,1 s) e volta em 0,2 s;
 * [pressedScale] opcional com spring. Respeita ANIMATOR_DURATION_SCALE = 0.
 */
fun Modifier.iosPress(
    enabled: Boolean = true,
    pressedAlpha: Float = 0.6f,
    pressedScale: Float = 1f,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val reduce = LocalReduceMotion.current
    val alpha by animateFloatAsState(if (pressed) pressedAlpha else 1f, if (reduce) snap() else tween(if (pressed) 100 else 200), label = "pressAlpha")
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, if (reduce) snap() else spring(dampingRatio = 0.85f, stiffness = 450f), label = "pressScale")
    this
        .graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale }
        .pointerInput(enabled, onLongClick != null) {
            if (enabled) detectTapGestures(
                onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                onTap = { onClick() },
                onLongPress = if (onLongClick != null) { _: Offset -> onLongClick() } else null,
            )
        }
}
