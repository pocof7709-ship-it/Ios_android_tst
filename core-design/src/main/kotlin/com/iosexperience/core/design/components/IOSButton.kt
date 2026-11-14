package com.iosexperience.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.iosexperience.core.design.theme.IOS
import com.iosexperience.core.design.theme.IOSShapes
import com.iosexperience.core.design.theme.IOSType

@Composable
fun IOSButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = IOS.colors
    Box(
        modifier.heightIn(min = 48.dp).clip(IOSShapes.Pill)
            .background(if (enabled) c.accent else c.accent.copy(alpha = 0.4f))
            .semantics { role = Role.Button }
            .iosPress(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = Color.White, style = IOSType.Title) }
}
