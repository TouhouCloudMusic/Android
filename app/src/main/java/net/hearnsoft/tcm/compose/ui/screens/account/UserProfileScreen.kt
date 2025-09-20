package net.hearnsoft.tcm.compose.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonType
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.domain.model.auth.AuthState
import net.hearnsoft.tcm.compose.domain.model.user.User
import net.hearnsoft.tcm.compose.ui.screens.ScreenRoute
import net.hearnsoft.tcm.compose.ui.utils.getFullImageUrl
import net.hearnsoft.tcm.compose.ui.viewmodel.UserViewModel
import java.time.format.DateTimeFormatter

@Composable
@UnstableSaltUiApi
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel,
    username: String? = null // null表示查看自己的资料
) {
    val profileUser = userViewModel.profileUser.collectAsState().value
    val profileLoading = userViewModel.profileLoading.collectAsState().value
    val profileError = userViewModel.profileError.collectAsState().value

    // 确定要显示的用户信息
    val displayUser = if (username == null) {
        // 显示自己的资料
        userViewModel.user.collectAsState().value
    } else {
        // 显示指定用户的资料
        profileUser
    }

    // 如果是查看其他用户资料，需要加载数据
    LaunchedEffect(username) {
        if (username != null) {
            userViewModel.getUserProfile(username)
        }
    }

    // 清理数据，避免内存泄漏
    DisposableEffect(Unit) {
        onDispose {
            if (username != null) {
                userViewModel.clearProfileUser()
            }
        }
    }

    // 显示加载中状态
    if (username != null && profileLoading && profileUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "加载中...",
                style = SaltTheme.textStyles.main
            )
        }
        return
    }

    // 显示错误状态
    if (username != null && profileError != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "加载失败",
                    style = SaltTheme.textStyles.main
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profileError,
                    style = SaltTheme.textStyles.sub
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            UserProfileHeader(
                user = displayUser,
                navController = navController,
                isOwnProfile = username == null
            )
        }
        // 占位内容
        items(30) { index ->
            Text(
                text = "Placeholder content item #${index + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = SaltTheme.textStyles.main
            )
        }
    }
}

@Composable
@UnstableSaltUiApi
private fun UserProfileHeader(
    user: User?,
    navController: NavController,
    isOwnProfile: Boolean = false
) {
    Column {
        // 背景横幅图片
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getFullImageUrl(user?.bannerUrl))
                    .placeholder(R.drawable.test_res1)
                    .error(R.drawable.test_res1)
                    .crossfade(true)
                    .build(),
                contentDescription = "User Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 半透明遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.DarkGray.copy(alpha = 0.1f),
                                Color.DarkGray.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // 头像和用户名
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 用户头像
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getFullImageUrl(user?.avatarUrl))
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = "User Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )

                Column {
                    // 用户名
                    Text(
                        text = user?.name ?: if (isOwnProfile) "未登录" else "未知用户",
                        style = SaltTheme.textStyles.main.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    // 用户角色标签
                    if (!user?.roles.isNullOrEmpty()) {
                        Row(Modifier.padding(start = 16.dp)) {
                            user.roles.take(3).forEach { role ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            SaltTheme.colors.highlight.copy(alpha = 0.8f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = role.name,
                                        style = SaltTheme.textStyles.sub.copy(
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // 用户信息区域
        Column(Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 用户简介
            if (!user?.bio.isNullOrBlank()) {
                Text(
                    text = user.bio,
                    style = SaltTheme.textStyles.main.copy(fontSize = 15.sp),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // 最后登录时间
            user?.lastLogin?.let { lastLogin ->
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = "Last Login",
                        modifier = Modifier.size(16.dp),
                        tint = SaltTheme.colors.subText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "最后登录: ${lastLogin.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))}",
                        style = SaltTheme.textStyles.sub.copy(fontSize = 14.sp)
                    )
                }
            }

            // 关注状态（仅在查看他人资料时显示）
            if (isOwnProfile) {
                Button(
                    onClick = {
                        navController.navigate(ScreenRoute.EditProfile.route)
                    },
                    text = "编辑资料",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
            } else {
                user?.isFollowing?.let { isFollowing ->
                    Button(
                        onClick = {

                        },
                        text = if (isFollowing) "已关注" else "关注",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        type = if (isFollowing) ButtonType.Sub else ButtonType.Highlight
                    )
                }
            }

            // 分隔线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SaltTheme.colors.stroke)
                    .padding(top = 16.dp)
            )
        }
    }
}