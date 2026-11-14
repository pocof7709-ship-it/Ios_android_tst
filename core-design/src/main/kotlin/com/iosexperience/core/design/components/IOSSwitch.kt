package com.iosexperience.core.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.iosexperience.core.design.theme.IOS
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.getValue
import androidx.compose.material3.minimumInteractiveComponentSize
@Composable

fun IOSSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = IOS.colors
    val haptic = LocalHapticFeedback.current
    val track by animateColorAsState(if (checked) c.success else Color(0x55787880), label = "track")
    val x by animateDpAsState(if (checked) 20.dp else 0.dp, spring(dampingRatio = 0.7f, stiffness = 600f), label = "thumb")
    Box(
        modifier
            .minimumInteractiveComponentSize() // alvo de toque >= 48dp (acessibilidade)
            .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onCheckedChange(it) })
            .size(51.dp, 31.dp).clip(CircleShape).background(track).padding(2.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.offset(x = x).size(27.dp).shadow(2.dp, CircleShape).background(Color.White, CircleShape))
    }
}
