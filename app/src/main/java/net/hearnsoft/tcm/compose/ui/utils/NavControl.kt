package net.hearnsoft.tcm.compose.ui.utils

import androidx.navigation.NavController

// 判断 NavController 是否可以返回上一个界面
val NavController.canGoBack: Boolean
    get() = currentBackStackEntry?.destination?.route != graph.startDestinationRoute