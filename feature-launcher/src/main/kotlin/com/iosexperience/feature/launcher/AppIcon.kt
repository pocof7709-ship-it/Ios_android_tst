package com.iosexperience.feature.launcher

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import com.iosexperience.core.data.Graph
import com.iosexperience.core.design.theme.SquircleShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Ícone real do app, recortado com o arredondamento configurado (cache LRU; carregamento fora da main thread). */
@Composable
fun AppIcon(key: String, size: Dp, cornerPercent: Int, modifier: Modifier = Modifier) {
    val bmp by produceState<Bitmap?>(Graph.icons.peek(key), key) {
        if (value == null) value = withContext(Dispatchers.Default) { Graph.icons.get(key) }
    }
    Box(modifier.size(size).clip(SquircleShape(cornerPercent / 100f)).background(Color(0x33808080))) {
        bmp?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize()) }
    }
}
