package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

@ExperimentalMaterial3Api
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior
) {
    composable(ScreenRoute.Explore.route) {
        ExploreScreen(navController = navController)
    }
    composable(ScreenRoute.Library.route) {

    }
    composable(ScreenRoute.Statistics.route) {

    }
    composable(ScreenRoute.Music.route) {

    }
    composable(ScreenRoute.Account.route) {

    }
}