package net.hearnsoft.tcm.compose.ui.screens

sealed class ScreenRoute(open val route: String) {
    object Explore : ScreenRoute("explore")
    object Library : ScreenRoute("library")
    object Statistics : ScreenRoute("statistics")
    object Music : ScreenRoute("music")
    object Account : ScreenRoute("account")

    object Album : ScreenRoute("album/{albumId}") {
        fun createRoute(albumId: Long): String = "album/$albumId"
    }

    object Settings : ScreenRoute("settings")

    object Scan : ScreenRoute("scan")

    object SearchPage : ScreenRoute("searchPage")

    object LoginPage : ScreenRoute("loginPage")
    object RegisterPage : ScreenRoute("registerPage")

    // 自己的用户资料页面
    object MyProfile : ScreenRoute("profile/me")
    // 编辑资料页面
    object EditProfile : ScreenRoute("profile/edit")

    // 其他用户的资料页面
    object UserProfile : ScreenRoute("profile/{username}") {
        fun createRoute(username: String): String = "profile/$username"
    }

    companion object {
        val MainScreens = listOf(
            Explore,
            Library,
            Statistics,
            Music,
            Account
        )
    }
}