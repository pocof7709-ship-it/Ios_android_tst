package com.iosexperience.core.design.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/** Constantes da curva de canto contínuo (superelipse aproximada por 3 Béziers por canto). Geometria própria, sem ativos de terceiros. */
private const val K = 1.52866483f

/** Constrói o contorno "squircle" de raio visual [r] (px). O raio é limitado para que as retas não se cruzem. */
fun squirclePath(w: Float, h: Float, rRequested: Float): Path {
    val r = rRequested.coerceIn(0f, minOf(w, h) / 2f / K)
    val k = K * r
    val path = Path()
    // (a, b) em unidades de r: a = distância ao longo da aresta horizontal, b = ao longo da vertical.
    fun corner(cx: Float, cy: Float, dx: Float, dy: Float, swap: Boolean) {
        fun px(a: Float, b: Float) = cx + dx * (if (swap) b else a) * r
        fun py(a: Float, b: Float) = cy + dy * (if (swap) a else b) * r
        path.cubicTo(px(1.08849296f, 0f), py(1.08849296f, 0f), px(0.86840694f, 0f), py(0.86840694f, 0f), px(0.63149379f, 0.07491139f), py(0.63149379f, 0.07491139f))
        path.cubicTo(px(0.37282383f, 0.12949149f), py(0.37282383f, 0.12949149f), px(0.12949149f, 0.37282383f), py(0.12949149f, 0.37282383f), px(0.07491139f, 0.63149379f), py(0.07491139f, 0.63149379f))
        path.cubicTo(px(0f, 0.86840694f), py(0f, 0.86840694f), px(0f, 1.08849296f), py(0f, 1.08849296f), px(0f, K), py(0f, K))
    }
    path.moveTo(k, 0f)
    path.lineTo(w - k, 0f); corner(w, 0f, -1f, 1f, false)          // canto superior direito
    path.lineTo(w, h - k); corner(w, h, -1f, -1f, true)            // inferior direito
    path.lineTo(k, h); corner(0f, h, 1f, -1f, false)               // inferior esquerdo
    path.lineTo(0f, k); corner(0f, 0f, 1f, 1f, true)               // superior esquerdo
    path.close()
    return path
}

/** Raio como fração do menor lado (0,2237 ≈ proporção de ícone de app do iOS). */
class SquircleShape(private val cornerFraction: Float = 0.2237f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(squirclePath(size.width, size.height, minOf(size.width, size.height) * cornerFraction))
    override fun equals(other: Any?) = other is SquircleShape && other.cornerFraction == cornerFraction
    override fun hashCode() = cornerFraction.hashCode()
}

/** Raio fixo em dp (cartões, dock, sheets). */
class SquircleDpShape(private val radius: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(squirclePath(size.width, size.height, with(density) { radius.toPx() }))
    override fun equals(other: Any?) = other is SquircleDpShape && other.radius == radius
    override fun hashCode() = radius.hashCode()
}
