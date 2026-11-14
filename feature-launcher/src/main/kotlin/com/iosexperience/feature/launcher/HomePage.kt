package com.iosexperience.feature.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.iosexperience.core.design.theme.SquircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.iosexperience.core.data.HomeEntry
import com.iosexperience.core.design.theme.IOS
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Uma página da grade. Arrastar após toque longo: soltar no CENTRO de outro item cria/aumenta pasta;
 * soltar nas bordas reordena. (Mover entre páginas: v0.3 — ver docs/LIMITATIONS.md.)
 */
@Composable
fun HomePage(
    entries: List<HomeEntry>,
    pageStart: Int,
    rows: Int,
    cols: Int,
    iconSizeDp: Int,
    corner: Int,
    labelOf: (String) -> String,
    onOpenApp: (key: String, bounds: Rect) -> Unit,
    onOpenFolder: (HomeEntry.Folder) -> Unit,
    onDrop: (from: Int, to: Int, merge: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var dragIdx by remember { mutableIntStateOf(-1) }
    var dragOff by remember { mutableStateOf(Offset.Zero) }
    val onDropState by rememberUpdatedState(onDrop)

    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val cellW = maxWidth / cols
        val cellH = minOf(maxHeight / rows, iconSizeDp.dp + 38.dp)
        val cwPx = with(density) { cellW.toPx() }
        val chPx = with(density) { cellH.toPx() }

        entries.forEachIndexed { i, e ->
            key(e.id) {
                val col = i % cols; val row = i / cols
                val dragging = dragIdx == i
                var bounds by remember { mutableStateOf(Rect.Zero) }
                var pressed by remember { mutableStateOf(false) }
                val pressScale by animateFloatAsState(if (dragging) 1.05f else if (pressed) 0.92f else 1f, spring(dampingRatio = 0.85f, stiffness = 450f), label = "press")
                val labelText = if (e is HomeEntry.Folder) e.name else labelOf(e.id)
                Column(
                    Modifier
                        .offset(cellW * col, cellH * row)
                        .size(cellW, cellH)
                        .zIndex(if (dragging) 10f else 0f)
                        .offset { if (dragging) IntOffset(dragOff.x.roundToInt(), dragOff.y.roundToInt()) else IntOffset.Zero }
                        .scale(pressScale)
                        .alpha(if (dragging) 0.9f else 1f)
                        .onGloballyPositioned { bounds = it.boundsInWindow() }
                        .semantics(mergeDescendants = true) {
                            contentDescription = if (e is HomeEntry.Folder) "Pasta $labelText" else labelText
                            onClick(label = "Abrir") { if (e is HomeEntry.Folder) onOpenFolder(e) else onOpenApp(e.id, bounds); true }
                        }
                        .pointerInput(e.id) { detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false }, onTap = { if (e is HomeEntry.Folder) onOpenFolder(e) else onOpenApp(e.id, bounds) }) }
                        .pointerInput(e.id, i) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { dragIdx = i; dragOff = Offset.Zero; haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                onDrag = { c, d -> c.consume(); dragOff += d },
                                onDragEnd = {
                                    val cx = col * cwPx + cwPx / 2 + dragOff.x
                                    val cy = row * chPx + chPx / 2 + dragOff.y
                                    val tc = floor(cx / cwPx).toInt().coerceIn(0, cols - 1)
                                    val tr = floor(cy / chPx).toInt().coerceIn(0, rows - 1)
                                    val target = (tr * cols + tc).coerceAtMost(entries.lastIndex)
                                    val fx = (cx - tc * cwPx) / cwPx; val fy = (cy - tr * chPx) / chPx
                                    val center = fx in 0.28f..0.72f && fy in 0.2f..0.8f
                                    if (target != i) onDropState(pageStart + i, pageStart + target, center)
                                    dragIdx = -1; dragOff = Offset.Zero
                                },
                                onDragCancel = { dragIdx = -1; dragOff = Offset.Zero },
                            )
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    when (e) {
                        is HomeEntry.App -> AppIcon(e.key, iconSizeDp.dp, corner)
                        is HomeEntry.Folder -> FolderIcon(e, iconSizeDp.dp, corner)
                    }
                    Text(labelText, Modifier.padding(top = 4.dp), color = IOS.colors.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1,
                        style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(0f, 1f), 2f)),
                        overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun FolderIcon(folder: HomeEntry.Folder, size: androidx.compose.ui.unit.Dp, corner: Int) {
    Box(Modifier.size(size).clip(SquircleShape(corner / 100f)).background(IOS.colors.glassTint.copy(alpha = 0.25f)).padding(size * 0.1f)) {
        val mini = size * 0.26f
        folder.keys.take(9).forEachIndexed { i, k ->
            Box(Modifier.offset(mini * 1.07f * (i % 3), mini * 1.07f * (i / 3))) { AppIcon(k, mini, corner) }
        }
    }
}
