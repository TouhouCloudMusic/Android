package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.domain.model.user.User
import net.hearnsoft.tcm.compose.domain.model.user.UserRole
import net.hearnsoft.tcm.compose.ui.utils.getFullImageUrl

@Composable
@UnstableSaltUiApi
fun AccountHeaderCard(
    modifier: Modifier = Modifier,
    user: User?,
    onClick: () -> Unit = {}
) {
    // 账户信息卡片组件
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(),
        border = BorderStroke(1.dp, SaltTheme.colors.stroke),
        colors = CardColors(
            contentColor = SaltTheme.colors.background,
            containerColor = SaltTheme.colors.background,
            disabledContainerColor = SaltTheme.colors.background,
            disabledContentColor = SaltTheme.colors.background
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // 背景图片
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getFullImageUrl(user?.bannerUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = "User Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            // 遮罩层
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .background(Color.DarkGray.copy(alpha = 0.3f))
            )
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getFullImageUrl(user?.avatarUrl))
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(50.dp))
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Row(Modifier.padding(vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)) {
                        Text(
                            text = user?.name ?: "未登录",
                            style = SaltTheme.textStyles.main,
                            maxLines = 1,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        // 用户角色标签
                        if (!user?.roles.isNullOrEmpty()) {
                            Row(Modifier.padding(start = 8.dp)) {
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
                    Text(
                        text = if (user != null) {
                            if (!user.bio.isNullOrBlank()) {
                                user.bio
                            } else {
                                "这个人很懒，什么都没留下"
                            }
                        } else {
                            "点击登录"
                        },
                        maxLines = 1,
                        style = SaltTheme.textStyles.sub,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
@UnstableSaltUiApi
@Preview
fun AccountHeaderCardPreview() {
    AccountHeaderCard(
        modifier = Modifier,
        user = User(
            name = "username",
            avatarUrl = "https://avatars.githubusercontent.com/u/20487725?v=4",
            bannerUrl = null,
            lastLogin = null,
            roles = listOf(
                UserRole(
                    id = 1,
                    name = "Admin"
                )
            ),
            isFollowing = null,
            bio = "This is a sample bio for preview purposes."
        )
    )
}