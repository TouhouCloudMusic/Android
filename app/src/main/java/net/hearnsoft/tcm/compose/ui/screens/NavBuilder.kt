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
import net.hearnsoft.tcm.compose.ui.screens.account.AccountScreen
import net.hearnsoft.tcm.compose.ui.screens.account.LoginScreen
import net.hearnsoft.tcm.compose.ui.screens.account.RegisterScreen
import net.hearnsoft.tcm.compose.ui.screens.account.UserEditProfileScreen
import net.hearnsoft.tcm.compose.ui.screens.account.UserProfileScreen
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel
import net.hearnsoft.tcm.compose.ui.viewmodel.SearchViewModel
import net.hearnsoft.tcm.compose.ui.viewmodel.UserViewModel

@UnstableSaltUiApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@UnstableApi
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    playerViewModel: PlayerViewModel,
    searchViewModel: SearchViewModel,
    userViewModel: UserViewModel,
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
        AccountScreen(
            navController = navController,
            userViewModel = userViewModel
        )
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
    composable(ScreenRoute.SearchPage.route) {
        SearchScreen(
            searchViewModel = searchViewModel,
            playerViewModel = playerViewModel
        )
    }
    // 登录页面
    composable(ScreenRoute.LoginPage.route) {
        LoginScreen(
            navController = navController,
            userViewModel = userViewModel
        )
    }
    // 注册页面
    composable(ScreenRoute.RegisterPage.route) {
        RegisterScreen(
            navController = navController,
            userViewModel = userViewModel
        )
    }
    // 自己的用户资料页面
    composable(ScreenRoute.MyProfile.route) {
        UserProfileScreen(
            navController = navController,
            userViewModel = userViewModel,
            username = null // null表示查看自己的资料
        )
    }
    // 编辑资料页面
    composable(ScreenRoute.EditProfile.route) {
        UserEditProfileScreen(
            userViewModel = userViewModel,
            navController = navController
        )
    }
    // 其他用户的资料页面
    composable(
        route = ScreenRoute.UserProfile.route,
        arguments = listOf(
            navArgument("username") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val username = backStackEntry.arguments?.getString("username") ?: ""
        UserProfileScreen(
            navController = navController,
            userViewModel = userViewModel,
            username = username
        )
    }
}