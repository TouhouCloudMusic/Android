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
                .height(140.dp)
        ) {
            // 背景图片
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getFullImageUrl(user?.bannerUrl))
                    .placeholder(R.drawable.test_res1)
                    .crossfade(true)
                    .build(),
                contentDescription = "User Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            // 遮罩层
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .background(Color.DarkGray.copy(alpha = 0.3f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
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
                Spacer(modifier = Modifier.size(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = user?.name ?: "未登录",
                        style = SaltTheme.textStyles.main,
                        maxLines = 1
                    )
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
                        style = SaltTheme.textStyles.sub
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
            roles = emptyList(),
            isFollowing = null,
            bio = "This is a sample bio for preview purposes."
        )
    )
}