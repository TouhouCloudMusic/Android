package net.hearnsoft.tcm.compose.ui.screens.account

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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

@SuppressLint("LocalContextGetResourceValueCall")
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
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            ItemEditPassword(
                text = password,
                onChange = {
                    password = it
                },
                hint = stringResource(R.string.password_hint),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (username.isNotBlank() && password.isNotBlank()) {
                            userViewModel.signIn(
                                LoginCredential(
                                    username = username,
                                    password = password
                                )
                            )
                        } else {
                            Toast.makeText(context,
                                context.getString(R.string.user_pass_not_empty_toast),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = authState is AuthState.Loading,
                label = "loginButtonCrossfade"
            ) { isLoading ->
                if (isLoading) {
                    Box(Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(
                                    vertical = SaltTheme.dimens.padding * 0.5f
                                )
                                .align(Alignment.Center),
                            color = SaltTheme.colors.highlight,
                            strokeWidth = 3.dp
                        )
                    }
                } else {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = SaltTheme.dimens.padding,
                                vertical = SaltTheme.dimens.padding * 0.5f
                            ),
                        text = stringResource(R.string.login_button),
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                userViewModel.signIn(
                                    LoginCredential(
                                        username = username,
                                        password = password
                                    )
                                )
                            } else {
                                Toast.makeText(context,
                                    context.getString(R.string.user_pass_not_empty_toast),
                                    Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

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