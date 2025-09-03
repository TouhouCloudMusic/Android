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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.ui.screens.ScreenRoute

@ExperimentalMaterial3Api
@UnstableSaltUiApi
@Composable
fun AppDrawer(
    modifier: Modifier = Modifier,
    drawerState : DrawerState,
    scope: CoroutineScope,
    navController: NavController,
    currentMainScreenRoute: MutableState<String>
) {
    val drawerList = listOf("首页", "设置")
    val drawerSelectedItem = remember { mutableStateOf(drawerList[0]) }

    DismissibleDrawerSheet(
        drawerState = drawerState,
        drawerContainerColor = SaltTheme.colors.background
    ) {
        RoundedColumn(Modifier.verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(4.dp))
            Item(
                onClick = {
                    drawerSelectedItem.value = drawerList[0]
                    scope.launch {
                        // 导航到当前的主屏幕路由
                        navController.navigate(currentMainScreenRoute.value) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                        drawerState.close()
                    }
                },
                text = drawerList[0],
                textColor = if (drawerSelectedItem.value == drawerList[0]) SaltTheme.colors.highlight else SaltTheme.colors.text,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
            )
            Spacer(Modifier.height(4.dp))
            Item(
                onClick = {
                    drawerSelectedItem.value = drawerList[1]
                    scope.launch {
                        navController.navigate(ScreenRoute.Settings.route) {
                            // 避免多次点击侧边栏设置按钮时，重复添加Settings到返回栈
                            launchSingleTop = true
                        }
                        drawerState.close()
                    }
                },
                text = drawerList[1],
                textColor = if (drawerSelectedItem.value == drawerList[1]) SaltTheme.colors.highlight else SaltTheme.colors.text,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
            )
            Spacer(Modifier.height(4.dp))
        }
    }

}