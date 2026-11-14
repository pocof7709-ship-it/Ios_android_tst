package com.iosexperience.feature.launcher

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.iosexperience.core.design.theme.IOSSurface
import com.iosexperience.core.design.theme.MaterialLevel
import com.iosexperience.core.design.theme.SquircleDpShape

/** Dock translúcido (vidro). Toque longo em um ícone troca o app daquela posição. */
@Composable
fun Dock(
    keys: List<String>, labelOf: (String) -> String, iconSizeDp: Int, corner: Int,
    onOpen: (String, Rect) -> Unit, onReplace: (slot: Int) -> Unit, modifier: Modifier = Modifier,
) {
    IOSSurface(modifier.fillMaxWidth().height(iconSizeDp.dp + 28.dp), SquircleDpShape(30.dp), MaterialLevel.Thin, shadow = true) {
        Row(Modifier.fillMaxSize().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            keys.forEachIndexed { i, k ->
                var b by remember { mutableStateOf(Rect.Zero) }
                AppIcon(k, iconSizeDp.dp, corner, Modifier
                    .onGloballyPositioned { b = it.boundsInWindow() }
                    .semantics { contentDescription = labelOf(k) }
                    .pointerInput(k) { detectTapGestures(onTap = { onOpen(k, b) }, onLongPress = { onReplace(i) }) })
            }
        }
    }
}
