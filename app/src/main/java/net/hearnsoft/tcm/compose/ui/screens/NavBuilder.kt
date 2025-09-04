package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel

@UnstableSaltUiApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@UnstableApi
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    playerViewModel: PlayerViewModel
) {
    composable(ScreenRoute.Explore.route) {
        ExploreScreen(navController = navController)
    }
    composable(ScreenRoute.Library.route) {

    }
    composable(ScreenRoute.Statistics.route) {

    }
    composable(ScreenRoute.Music.route) {
        MusicScreen(
            navController = navController,
            playerViewModel = playerViewModel,
        )
    }
    composable(ScreenRoute.Account.route) {

    }
    composable(
        route = ScreenRoute.Album.route,
        arguments = listOf(
            navArgument("albumId") { type = NavType.LongType }
        )
    ) { backStackEntry ->
        val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
        AlbumScreen(
            albumId = albumId,
            navController = navController,
            playerViewModel = playerViewModel
        )
    }
    composable(ScreenRoute.Settings.route) {
        SettingsScreen()
    }
    composable(ScreenRoute.Scan.route) {
        MusicScanScreen(
            playerViewModel = playerViewModel,
        )
    }
}