package net.hearnsoft.tcm.compose.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel

@UnstableSaltUiApi
@ExperimentalMaterial3Api
@UnstableApi
@ExperimentalFoundationApi
@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel
) {

    val currentMediaItem = playerViewModel.currentMediaItem.collectAsState().value
    val artworkUri = currentMediaItem?.mediaMetadata?.artworkUri
    val title = currentMediaItem?.mediaMetadata?.title ?: "未知歌曲"
    val artist = currentMediaItem?.mediaMetadata?.artist ?: "未知艺术家"

    // 进度
    val currentPosition = playerViewModel.currentPosition.collectAsState().value
    val duration = playerViewModel.duration.collectAsState().value

    // 播放状态
    val isPlaying = playerViewModel.isPlaying.collectAsState().value

    Row(
        modifier = modifier
            .background(color = SaltTheme.colors.background)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {

        Spacer(
            modifier = Modifier
                .size(6.dp)
                .align(Alignment.CenterVertically)
        )
        // 封面图片容器和背景
        Card(
            modifier = Modifier
                .size(50.dp, 50.dp)
                .padding(4.dp)
                .align(Alignment.CenterVertically),
            shape = RoundedCornerShape(8.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUri ?: R.drawable.ic_nav_music) // 默认图片,
                    .crossfade(true)
                    .crossfade(1000)
                    .build(),
                modifier = Modifier.size(50.dp, 50.dp),
                contentDescription = "Cover art",
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        }
        // 间隔8dp
        Spacer(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .align(Alignment.CenterVertically)
        )
        // 歌曲信息容器
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = title.toString(),
                style = SaltTheme.textStyles.main,
                maxLines = 1
            )
            CompositionLocalProvider(LocalContentColor provides SaltTheme.colors.subText) {
                Text(
                    text = artist.toString(), style = SaltTheme.textStyles.sub
                )
            }
        }
        // 播放按钮
        Box(
            modifier = Modifier
                .size(48.dp, 48.dp)
                .align(Alignment.CenterVertically)
        ) {
            PlayPauseButton(
                modifier = Modifier.align(Alignment.Center),
                isPlaying = isPlaying,
                onClick = {
                    playerViewModel.togglePlayPause()
                }
            )
            CircularProgressIndicator(
                progress = {
                    if (duration > 0) {
                        currentPosition.toFloat() / duration.toFloat()
                    } else {
                        0f
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center),
                color = SaltTheme.colors.highlight,
                strokeWidth = 3.dp,
                trackColor = SaltTheme.colors.subBackground,
            )
        }
        // 播放列表按钮
        Box(
            modifier = Modifier
                .size(48.dp, 48.dp)
                .align(Alignment.CenterVertically)
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.Center),
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_music_list),
                    contentDescription = "播放列表",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    // 点击时的缩放动画
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(100),
        label = "press_scale"
    )

    // 状态变化时的缩放动画
    val stateScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = tween(300),
        label = "state_scale"
    )

    IconButton(
        modifier = modifier
            .size(48.dp)
            .padding(8.dp),
        onClick = onClick,
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        when (interaction) {
                            is PressInteraction.Press -> isPressed = true
                            is PressInteraction.Release -> isPressed = false
                            is PressInteraction.Cancel -> isPressed = false
                        }
                    }
                }
            }
    ) {
        Icon(
            painter = painterResource(
                if (isPlaying) {
                    R.drawable.pause
                } else {
                    R.drawable.play
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .scale(pressScale * stateScale), // 组合两个缩放效果
        )
    }
}
