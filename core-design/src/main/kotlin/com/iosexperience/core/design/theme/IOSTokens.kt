package com.iosexperience.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Cores originais (inspiradas, não copiadas). Preto puro no escuro p/ economizar AMOLED. */
@Immutable
data class IOSColors(
    val background: Color,
    val glassTint: Color,
    val glassStroke: Color,
    val label: Color,
    val secondaryLabel: Color,
    val accent: Color,
    val success: Color,
    val destructive: Color,
    val isDark: Boolean,
    val card: Color = Color.Unspecified,
    val separator: Color = Color.Unspecified,
    val searchPill: Color = Color.Unspecified,
)

val DarkIOSColors = IOSColors(
    background = Color(0xFF000000),
    glassTint = Color(0xFFFFFFFF),
    glassStroke = Color(0x33FFFFFF),
    label = Color(0xFFFFFFFF),
    secondaryLabel = Color(0xFF8E8E93),
    accent = Color(0xFF0A84FF),
    success = Color(0xFF30D158),
    destructive = Color(0xFFFF453A),
    isDark = true,
    card = Color(0xFF1C1C1E),
    separator = Color(0xFF38383A),
    searchPill = Color(0x3D767680),
)

val LightIOSColors = IOSColors(
    background = Color(0xFFF2F2F7),
    glassTint = Color(0xFFFFFFFF),
    glassStroke = Color(0x66FFFFFF),
    label = Color(0xFF000000),
    secondaryLabel = Color(0xFF8E8E93),
    accent = Color(0xFF007AFF),
    success = Color(0xFF34C759),
    destructive = Color(0xFFFF3B30),
    isDark = false,
    card = Color(0xFFFFFFFF),
    separator = Color(0xFFC6C6C8),
    searchPill = Color(0x1F767680),
)

object IOSShapes {
    val Card: Shape = SquircleDpShape(22.dp)
    val Panel: Shape = SquircleDpShape(30.dp)
    val Icon: Shape = SquircleShape()
    val Pill: Shape = RoundedCornerShape(percent = 50)
}

val LocalIOSColors = staticCompositionLocalOf { DarkIOSColors }
val LocalPerformanceProfile = staticCompositionLocalOf { PerformanceProfile.BALANCED }
/** true quando o usuário desligou as animações do sistema (ANIMATOR_DURATION_SCALE = 0). */
val LocalReduceMotion = staticCompositionLocalOf { false }
