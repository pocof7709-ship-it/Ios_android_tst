package com.iosexperience.core.design.theme

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import kotlin.math.hypot

/** Wallpapers PRÓPRIOS (gradientes procedurais). Nada vem de ativos da Apple. */
@Immutable
data class WallpaperPalette(val name: String, val top: Color, val mid: Color, val bottom: Color, val glowA: Color, val glowB: Color)

object Wallpapers {
    val all = listOf(
        WallpaperPalette("Aurora", Color(0xFF1B2A6B), Color(0xFF6A3DB8), Color(0xFFE0527A), Color(0xFF4FD1C5), Color(0xFFFFB86B)),
        WallpaperPalette("Oceano", Color(0xFF031B34), Color(0xFF0B5FA5), Color(0xFF3ED6D0), Color(0xFF7AE7FF), Color(0xFF2C6BFF)),
        WallpaperPalette("Entardecer", Color(0xFF2B1055), Color(0xFFD5457B), Color(0xFFFF9A5A), Color(0xFFFFD27A), Color(0xFF8E44FF)),
        WallpaperPalette("Grafite", Color(0xFF0B0B0F), Color(0xFF2B2D42), Color(0xFF5C6B8A), Color(0xFF7C8BB0), Color(0xFF3A3F63)),
    )
    fun get(index: Int) = all[index.coerceIn(all.indices)]
}

@Immutable
class WallpaperSpec(val palette: WallpaperPalette, val dark: Boolean)

/** Wallpaper atual desenhado pelo app; null = usando o wallpaper do sistema (vidro sem cópia do fundo). */
val LocalWallpaper = staticCompositionLocalOf<WallpaperSpec?> { null }

// Ruído fixo de ~2% (64x64, repetido) contra faixas de gradiente. Gerado uma vez.
private val noiseShader: Shader by lazy {
    val s = 64
    val bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
    val rnd = java.util.Random(7)
    for (x in 0 until s) for (y in 0 until s) { val v = rnd.nextInt(256); bmp.setPixel(x, y, android.graphics.Color.argb(5, v, v, v)) }
    ImageShader(bmp.asImageBitmap(), TileMode.Repeated, TileMode.Repeated)
}

/**
 * Desenha o wallpaper em coordenadas de janela [w]x[h]. [filter] permite a saturação do "vidro";
 * [noise]=false nas cópias dentro de superfícies de vidro (barato).
 */
fun DrawScope.drawWallpaper(p: WallpaperPalette, dark: Boolean, w: Float, h: Float, filter: ColorFilter? = null, noise: Boolean = true) {
    fun c(col: Color) = if (dark) col else lerp(col, Color.White, 0.3f)
    val full = Size(w, h)
    drawRect(Brush.linearGradient(listOf(c(p.top), c(p.mid), c(p.bottom)), start = Offset.Zero, end = Offset(w * 0.6f, h)), size = full, colorFilter = filter)
    drawRect(Brush.radialGradient(listOf(c(p.glowA).copy(alpha = 0.50f), Color.Transparent), center = Offset(w * 0.2f, h * 0.25f), radius = w * 0.9f), size = full, colorFilter = filter)
    drawRect(Brush.radialGradient(listOf(c(p.glowB).copy(alpha = 0.40f), Color.Transparent), center = Offset(w * 0.85f, h * 0.72f), radius = w * 0.8f), size = full, colorFilter = filter)
    if (dark) drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)), center = Offset(w / 2, h / 2), radius = hypot(w, h) / 2f), size = full)
    if (noise) drawRect(ShaderBrush(noiseShader), size = full)
}

/** Camada de fundo da Home. Desenhada uma vez; o RenderNode é reaproveitado entre quadros. */
@Composable
fun WallpaperLayer(spec: WallpaperSpec, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) { drawWallpaper(spec.palette, spec.dark, size.width, size.height) }
}
