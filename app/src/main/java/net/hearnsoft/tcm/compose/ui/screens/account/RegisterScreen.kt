package net.hearnsoft.tcm.compose.ui.screens.account

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.ItemEditPassword
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.domain.model.auth.AuthState
import net.hearnsoft.tcm.compose.domain.model.auth.LoginCredential
import net.hearnsoft.tcm.compose.ui.screens.ScreenRoute
import net.hearnsoft.tcm.compose.ui.viewmodel.UserViewModel
import net.hearnsoft.tcm.compose.utils.Logger

@SuppressLint("LocalContextGetResourceValueCall")
@UnstableSaltUiApi
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel,
) {
    // 注册界面内容
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val authState = userViewModel.authState.collectAsState().value
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // 注册成功，返回到主界面
                // 导航到当前的主屏幕路由
                navController.navigate(ScreenRoute.Account.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                Toast.makeText(context, context.getString(R.string.welcome_join, authState.user.name), Toast.LENGTH_LONG).show()
            }
            is AuthState.Error -> {
                // 注册失败，显示错误信息
                Logger.err("RegisterScreen", "注册失败: ${authState.message}")
                Toast.makeText(context, context.getString(R.string.register_failed, authState.message), Toast.LENGTH_LONG).show()
                userViewModel.clearAuthError()
            }
            else -> {
                // 其他状态不处理
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        RoundedColumn {
            ItemEdit(
                text = username,
                onChange = {
                    username = it
                },
                hint = stringResource(R.string.username_hint),
            )
            ItemEditPassword(
                text = password,
                onChange = {
                    password = it
                },
                hint = stringResource(R.string.password_hint),
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SaltTheme.dimens.padding,
                    vertical = SaltTheme.dimens.padding * 0.5f
                ),
            text = stringResource(R.string.register_button),
            enabled = !isLoading,
            onClick = {
                userViewModel.signUp(
                    LoginCredential(
                        username = username,
                        password = password
                    )
                )
            },
        )
    }

}