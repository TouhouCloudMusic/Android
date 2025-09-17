package net.hearnsoft.tcm.compose.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer

// 应用加法混合模式的扩展函数,先留着,以后可能会用到
fun Modifier.plusBlend() = this.then(
    Modifier.graphicsLayer {
        blendMode = BlendMode.Plus
        alpha = 0.9f
    }
)