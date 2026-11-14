package com.iosexperience.core.design.theme

/** Perfis de desempenho (seção 14). Controlam efeitos custosos em todo o design system. */
enum class PerformanceProfile(val blurRadiusDp: Int, val animationsEnabled: Boolean, val glassOverlayAlpha: Float) {
    ECONOMIC(blurRadiusDp = 0, animationsEnabled = false, glassOverlayAlpha = 0.85f),
    BALANCED(blurRadiusDp = 12, animationsEnabled = true, glassOverlayAlpha = 0.55f),
    ADVANCED(blurRadiusDp = 28, animationsEnabled = true, glassOverlayAlpha = 0.35f),
}
