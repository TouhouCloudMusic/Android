package net.hearnsoft.tcm.compose.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.compositionLocalOf

val LocalPlayerAwareWindowInsets = compositionLocalOf<WindowInsets> {
    error("No PlayerAwareWindowInsets provided")
}