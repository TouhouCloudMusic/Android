package net.hearnsoft.tcm.compose.constants

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


val NavigationBarHeight = 60.dp
val MiniPlayerHeight = 62.dp
val AppBarHeight = 54.dp

val PlayerHorizontalPadding = 26.dp
val PlayerVerticalPadding = 26.dp

val PlayerCoverVerticalPadding = 16.dp

val NavigationBarAnimationSpec = spring<Dp>(stiffness = Spring.StiffnessMediumLow)