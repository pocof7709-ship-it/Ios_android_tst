package com.iosexperience.core.design.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

/** Três níveis de material (tabela da especificação). */
enum class MaterialLevel { Thin, Regular, Thick }

/** Parâmetros resolvidos de um material. Função pura => testável em JVM. */
data class MaterialSpec(val tintAlpha: Float, val saturation: Float, val backdrop: Boolean, val shadow: Boolean)

fun materialSpec(level: MaterialLevel, dark: Boolean, profile: PerformanceProfile): MaterialSpec {
    val base = when (level) {
        MaterialLevel.Thin -> if (dark) 0.30f else 0.35f
        MaterialLevel.Regular -> if (dark) 0.45f else 0.55f
        MaterialLevel.Thick -> if (dark) 0.65f else 0.75f
    }
    return if (profile == PerformanceProfile.ECONOMIC)
        // Sem cópia do fundo, sem saturação, sem sombra: tinta quase sólida.
        MaterialSpec(tintAlpha = minOf(0.95f, base + 0.35f), saturation = 1f, backdrop = false, shadow = false)
    else MaterialSpec(tintAlpha = base, saturation = 1.8f, backdrop = true, shadow = true)
}

fun saturationMatrix(s: Float): ColorMatrix {
    val inv = 1f - s
    val r = 0.213f * inv; val g = 0.715f * inv; val b = 0.072f * inv
    return ColorMatrix(floatArrayOf(r + s, g, b, 0f, 0f, r, g + s, b, 0f, 0f, r, g, b + s, 0f, 0f, 0f, 0f, 0f, 1f, 0f))
}

/**
 * Superfície de "vidro". Como o wallpaper é do próprio app, o "fundo desfocado" é uma cópia do wallpaper
 * alinhada à janela, com saturação ~180% (gradientes suaves não mudam ao desfocar, então não há blur por quadro).
 * Sem wallpaper próprio (usando o do sistema), cai para tinta translúcida + borda.
 */
@Composable
fun IOSSurface(
    modifier: Modifier = Modifier,
    shape: Shape = IOSShapes.Card,
    level: MaterialLevel = MaterialLevel.Regular,
    shadow: Boolean = level != MaterialLevel.Thin,
    content: @Composable () -> Unit,
) {
    val c = IOS.colors
    val spec = materialSpec(level, c.isDark, IOS.profile)
    val wp = LocalWallpaper.current
    val view = LocalView.current
    var pos by remember { mutableStateOf(Offset.Zero) }
    val filter = remember(spec.saturation) { ColorFilter.colorMatrix(saturationMatrix(spec.saturation)) }
    val border = remember { Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.12f))) }
    val tint = if (c.isDark) Color.Black else Color.White
    val shadowColor = Color.Black.copy(alpha = if (c.isDark) 0.4f else 0.08f)
    Box(
        modifier
            .then(if (shadow && spec.shadow) Modifier.shadow(8.dp, shape, clip = false, ambientColor = shadowColor, spotColor = shadowColor) else Modifier)
            .clip(shape)
            .onGloballyPositioned { pos = it.positionInWindow() }
            .drawBehind {
                if (spec.backdrop && wp != null) {
                    translate(-pos.x, -pos.y) {
                        drawWallpaper(wp.palette, wp.dark, view.rootView.width.toFloat(), view.rootView.height.toFloat(), filter, noise = false)
                    }
                }
                drawRect(tint.copy(alpha = spec.tintAlpha))
            }
            .border(0.5.dp, border, shape),
    ) { content() }
}
