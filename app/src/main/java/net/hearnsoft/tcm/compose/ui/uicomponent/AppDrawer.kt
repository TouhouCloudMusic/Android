package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.ui.screens.ScreenRoute

// 抽取菜单项数据类
data class DrawerMenuItem(
    val title: String,
    val route: String,
    val isMainScreen: Boolean = false
)

@ExperimentalMaterial3Api
@UnstableSaltUiApi
@Composable
fun AppDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavController,
    currentMainScreenRoute: MutableState<String>
) {
    val drawerHome = stringResource(R.string.drawer_home)
    val drawerScanMedia = stringResource(R.string.drawer_scan_media)
    val drawerSettings = stringResource(R.string.drawer_settings)
    
    // 可配置的菜单项列表
    val drawerMenuItems = remember(drawerHome, drawerScanMedia, drawerSettings) {
        listOf(
            DrawerMenuItem(drawerHome, "", isMainScreen = true),
            DrawerMenuItem(drawerScanMedia, ScreenRoute.Scan.route),
            DrawerMenuItem(drawerSettings, ScreenRoute.Settings.route)
        )
    }

    val selectedItem = remember { mutableStateOf(drawerMenuItems[0].title) }

    DismissibleDrawerSheet(
        drawerState = drawerState,
        drawerContainerColor = SaltTheme.colors.background
    ) {
        RoundedColumn(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            // 使用循环生成菜单项
            drawerMenuItems.forEach { menuItem ->
                DrawerItemComponent(
                    menuItem = menuItem,
                    onClick = {
                        selectedItem.value = menuItem.title
                        scope.launch {
                            handleNavigation(
                                navController = navController,
                                menuItem = menuItem,
                                currentMainScreenRoute = currentMainScreenRoute.value
                            )
                            drawerState.close()
                        }
                    }
                )
            }
        }
    }
}

@UnstableSaltUiApi
@Composable
private fun DrawerItemComponent(
    menuItem: DrawerMenuItem,
    onClick: () -> Unit
) {
    Item(
        onClick = onClick,
        text = menuItem.title,
        textColor = SaltTheme.colors.text
    )
}

private fun handleNavigation(
    navController: NavController,
    menuItem: DrawerMenuItem,
    currentMainScreenRoute: String
) {
    when {
        menuItem.isMainScreen -> {
            // 导航到当前的主屏幕路由
            navController.navigate(currentMainScreenRoute) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }

        menuItem.route.isNotEmpty() -> {
            navController.navigate(menuItem.route) {
                launchSingleTop = true
            }
        }
    }
}