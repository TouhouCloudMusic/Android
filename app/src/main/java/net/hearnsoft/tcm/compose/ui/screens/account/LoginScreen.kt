package net.hearnsoft.tcm.compose.ui.screens.account

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.moriafly.salt.ui.ItemButton
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

@Composable
@UnstableSaltUiApi
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val authState = userViewModel.authState.collectAsState().value

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // 登录成功，返回上一个界面
                navController.popBackStack()
                Toast.makeText(context, context.getString(R.string.welcome_back, authState.user.name), Toast.LENGTH_LONG).show()
            }
            is AuthState.Error -> {
                // 登录失败，显示错误信息
                Logger.err("LoginScreen", "登录失败: ${authState.message}")
                Toast.makeText(context, context.getString(R.string.login_failed, authState.message), Toast.LENGTH_LONG).show()
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
            text = stringResource(R.string.login_button),
            onClick = {
                userViewModel.signIn(
                    LoginCredential(
                        username = username,
                        password = password
                    )
                )
            },
        )

        Spacer(Modifier.fillMaxHeight().weight(1f))

        RoundedColumn {
            ItemButton(
                text = stringResource(R.string.register_account),
                onClick = {
                    navController.navigate(ScreenRoute.RegisterPage.route)
                }
            )
            ItemButton(
                text = stringResource(R.string.forgot_password),
                onClick = {
                    Logger.debug("LoginScreen", "忘记密码")
                }
            )
        }
    }
}