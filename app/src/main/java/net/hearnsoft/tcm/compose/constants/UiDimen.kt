package net.hearnsoft.tcm.compose.constants

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


val NavigationBarHeight = 60.dp
val MiniPlayerHeight = 60.dp
val AppBarHeight = 64.dp

val PlayerHorizontalPadding = 16.dp

val NavigationBarAnimationSpec = spring<Dp>(stiffness = Spring.StiffnessMediumLow)