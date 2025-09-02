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