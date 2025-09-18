package net.hearnsoft.tcm.compose.ui.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val PlayerForegroundColorLight = Color.White.copy(alpha = 0.9f)
val PlayerForegroundColorDark = Color.Black.copy(alpha = 0.9f)

fun getPlayerUIColor(isDark: Boolean): Color {
    return if (isDark) PlayerForegroundColorLight else PlayerForegroundColorDark
}

val LocalPlayerUIColor = compositionLocalOf {
    PlayerForegroundColorLight
}