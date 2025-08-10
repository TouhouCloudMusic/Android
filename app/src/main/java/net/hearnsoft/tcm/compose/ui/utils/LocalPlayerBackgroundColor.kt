package net.hearnsoft.tcm.compose.ui.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalPlayerBackgroundColor = compositionLocalOf<Color> {
    // 提供默认值，避免在未提供时出错
    Color.Unspecified
}