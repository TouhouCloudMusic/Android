package net.hearnsoft.tcm.compose.ui.player

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.constants.PlayerCoverVerticalPadding
import net.hearnsoft.tcm.compose.constants.PlayerHorizontalPadding
import net.hearnsoft.tcm.compose.pref.PlayerCoverType

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CoverPager(
    modifier: Modifier = Modifier,
    artworkUri: Uri?,
    isPlaying: Boolean,
    coverType: Int = PlayerCoverType.DEFAULT.ordinal,
) {

    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            // 从当前角度开始，无限循环旋转
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 20000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            // 停止动画
            rotation.stop()
        }
    }

    // 封面容器
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PlayerHorizontalPadding, vertical = PlayerCoverVerticalPadding)
            .sizeIn(maxHeight = 600.dp, maxWidth = 600.dp)
    ) {

        when (coverType) {
            PlayerCoverType.DEFAULT.ordinal -> {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .aspectRatio(1f)
                        .scale(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    // 封面图片
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artworkUri ?: R.drawable.ic_nav_music)
                            .crossfade(true)
                            .crossfade(1000)
                            .error(R.drawable.ic_nav_music) // 错误时使用占位图
                            .fallback(R.drawable.ic_nav_music) // URI为null时使用占位图
                            .build(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .scale(1f),
                        contentDescription = "Cover Art",
                        contentScale = ContentScale.Crop
                    )
                }
            }
            PlayerCoverType.CIRCLE.ordinal -> {
                // 圆形封面，模仿黑胶唱片
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .aspectRatio(1f)
                        .scale(1f)
                        .padding(16.dp)
                        .graphicsLayer{
                            rotationZ = rotation.value
                            cameraDistance = 8 * density
                        },
                    shape = RoundedCornerShape(999.dp),
                ) {
                    // 封面图片
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artworkUri ?: R.drawable.ic_nav_music)
                            .crossfade(true)
                            .crossfade(1000)
                            .error(R.drawable.ic_nav_music) // 错误时使用占位图
                            .fallback(R.drawable.ic_nav_music) // URI为null时使用占位图
                            .build(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .scale(1f),
                        contentDescription = "Cover Art",
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }



    }
}