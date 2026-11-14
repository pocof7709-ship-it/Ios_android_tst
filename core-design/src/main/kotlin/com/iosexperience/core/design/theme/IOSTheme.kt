package com.iosexperience.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    profile: PerformanceProfile = PerformanceProfile.BALANCED,
    accent: androidx.compose.ui.graphics.Color? = null,
    content: @Composable () -> Unit,
) {
    val base = if (darkTheme) DarkIOSColors else LightIOSColors
    val c = if (accent != null) base.copy(accent = accent) else base
    val scheme = if (darkTheme) darkColorScheme(primary = c.accent, background = c.background, onBackground = c.label)
    else lightColorScheme(primary = c.accent, background = c.background, onBackground = c.label)
    val ctx = LocalContext.current
    val reduce = remember { Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
    // LocalOverscrollConfiguration = null remove o brilho/stretch do Android; o "bounce" elástico ainda é pendente.
    CompositionLocalProvider(LocalIOSColors provides c, LocalPerformanceProfile provides profile, LocalReduceMotion provides reduce, LocalOverscrollConfiguration provides null) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

object IOS {
    val colors: IOSColors @Composable get() = LocalIOSColors.current
    val profile: PerformanceProfile @Composable get() = LocalPerformanceProfile.current
}
