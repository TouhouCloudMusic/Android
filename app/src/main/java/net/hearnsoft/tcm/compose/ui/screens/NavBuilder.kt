package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
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
}