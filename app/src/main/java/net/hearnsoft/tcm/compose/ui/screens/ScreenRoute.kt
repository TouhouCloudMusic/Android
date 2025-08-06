package net.hearnsoft.tcm.compose.ui.screens

sealed class ScreenRoute(val route: String) {
    object Explore : ScreenRoute("explore")
    object Library : ScreenRoute("library")
    object Statistics : ScreenRoute("statistics")
    object Music : ScreenRoute("music")
    object Account : ScreenRoute("account")

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