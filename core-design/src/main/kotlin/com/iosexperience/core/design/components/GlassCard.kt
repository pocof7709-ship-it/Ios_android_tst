package com.iosexperience.core.design.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.iosexperience.core.design.theme.IOSShapes
import com.iosexperience.core.design.theme.IOSSurface
import com.iosexperience.core.design.theme.MaterialLevel

/** Cartão de vidro. Delegado a [IOSSurface] (material Thin/Regular/Thick, perfil de desempenho, wallpaper). */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = IOSShapes.Card,
    level: MaterialLevel = MaterialLevel.Regular,
    content: @Composable () -> Unit,
) = IOSSurface(modifier, shape, level, content = content)
