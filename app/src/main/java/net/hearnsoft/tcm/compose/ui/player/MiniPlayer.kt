package net.hearnsoft.tcm.compose.ui.player

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import net.hearnsoft.tcm.compose.R

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
) {

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
            Image(
                modifier = Modifier.size(50.dp, 50.dp),
                painter = painterResource(id = R.drawable.ic_nav_music),
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
            /*val state by viewModel.state.collectAsState()
            val song = state.map { it.song }
                .getOrElse { error("Song not found but it's impossible") }*/

            Text(
                text = "Foo", style = SaltTheme.textStyles.main
            )
            CompositionLocalProvider(LocalContentColor provides SaltTheme.colors.subText) {
                Text(
                    text = "Foo", style = SaltTheme.textStyles.sub
                )
            }
        }
        // 播放按钮
        Box(
            modifier = Modifier
                .size(48.dp, 48.dp)
                .align(Alignment.CenterVertically)
        ) {

            /*val isPlaying by viewModel.isPlaying.collectAsState()*/

            PlayPauseButton(
                modifier = Modifier.align(Alignment.Center),
                isPlaying = true,
                onClick = {
                    //暂时什么都不做
                    /*viewModel.toggleIsPlaying()*/
                }
            )
            CircularProgressIndicator(
                progress = {
                    0f
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
    val icon = if (isPlaying) {
        AnimatedImageVector.animatedVectorResource(R.drawable.avd_play_to_pause)
    } else {
        AnimatedImageVector.animatedVectorResource(R.drawable.avd_pause_to_play)
    }

    IconButton(
        modifier = modifier
            .size(48.dp)
            .padding(8.dp),
        onClick = {
            onClick()
        },
    ) {
        Image(
            painter = rememberAnimatedVectorPainter(
                icon, isPlaying
            ),
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
@Preview
fun MiniPlayerPreview() {
    SaltTheme {
        MiniPlayer(
            modifier = Modifier
                .padding(16.dp)
                .background(SaltTheme.colors.background)
        )
    }
}