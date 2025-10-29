package net.hearnsoft.tcm.compose.ui.screens.account

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.domain.model.auth.AuthState
import net.hearnsoft.tcm.compose.ui.screens.ScreenRoute
import net.hearnsoft.tcm.compose.ui.uicomponent.AccountHeaderCard
import net.hearnsoft.tcm.compose.ui.viewmodel.UserViewModel
import net.hearnsoft.tcm.compose.utils.Logger

@Composable
@ExperimentalMaterial3Api
@UnstableSaltUiApi
fun AccountScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel,
) {

    val user = userViewModel.user.collectAsState().value
    val authState = userViewModel.authState.collectAsState().value
    val context = LocalContext.current

    // 用于控制下拉刷新状态
    var isManualRefreshing by remember { mutableStateOf(false) }

    // 监听authState变化，处理刷新完成和错误情况
    LaunchedEffect(authState) {
        if (isManualRefreshing) {
            when (authState) {
                is AuthState.Authenticated, is AuthState.NotAuthenticated -> {
                    // 刷新完成，取消下拉状态
                    isManualRefreshing = false
                }
                is AuthState.NetworkError -> {
                    // 网络错误，显示Toast
                    Toast.makeText(context, context.getString(R.string.network_error, authState.message), Toast.LENGTH_SHORT).show()
                    isManualRefreshing = false
                    // 清除错误状态
                    userViewModel.clearAuthError()
                }
                is AuthState.Error -> {
                    // 出现错误，显示Toast并取消下拉状态
                    Toast.makeText(context, context.getString(R.string.fetch_user_info_failed, authState.message), Toast.LENGTH_SHORT).show()
                    isManualRefreshing = false
                    // 清除错误状态
                    userViewModel.clearAuthError()
                }
                is AuthState.Loading -> {
                    // 保持刷新状态
                }
            }
        }
    }

    // 账户界面内容
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isManualRefreshing,
        onRefresh = {
            isManualRefreshing = true
            userViewModel.checkAuthStatus()
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            AccountHeaderCard(
                modifier = modifier,
                user = user,
                onClick = {
                    when (authState) {
                        is AuthState.Authenticated -> {
                            // 已登录，可以导航到用户详情页面
                            Logger.info("AccountScreen", "User already authenticated")
                            navController.navigate(ScreenRoute.MyProfile.route)
                        }
                        is AuthState.NetworkError -> {
                            // 网络错误时，如果有用户信息就进入个人页面，否则进入登录页面
                            if (user != null) {
                                navController.navigate(ScreenRoute.MyProfile.route)
                            } else {
                                navController.navigate(ScreenRoute.LoginPage.route)
                            }
                        }
                        else -> {
                            // 未登录，导航到登录页面
                            Logger.info("AccountScreen", "Navigate to login screen")
                            navController.navigate(ScreenRoute.LoginPage.route)
                        }
                    }
                }
            )
            LazyColumn(Modifier.fillMaxWidth()) {
                repeat(20) {
                    item {
                        Text("Placeholder item #$it", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }

}