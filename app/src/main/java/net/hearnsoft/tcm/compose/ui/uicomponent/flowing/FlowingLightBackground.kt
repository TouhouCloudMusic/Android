package net.hearnsoft.tcm.compose.ui.uicomponent.flowing

import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.ui.uicomponent.CompatBlurImage

@Composable
fun FlowingLightBackground(
    isPlaying: Boolean,
    imageUrl: Uri?,
    modifier: Modifier = Modifier,
    onImageLoadResult: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val processor = remember { FlowingLightProcessor(context) }
    var processedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isValidImage by remember { mutableStateOf(false) }

    val infiniteTransition1 = remember { Animatable(0f) }
    val infiniteTransition2 = remember { Animatable(0f) }

    val shouldAnimate = imageUrl != null || isPlaying

    LaunchedEffect(Unit) {
        if (shouldAnimate) {
            launch {
                infiniteTransition1.animateTo(
                    targetValue = infiniteTransition1.value + 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 50000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
            launch {
                infiniteTransition2.animateTo(
                    targetValue = infiniteTransition2.value - 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 50000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        } else {
            // 停止动画,重置到初始状态
            infiniteTransition1.stop()
            infiniteTransition2.stop()
            infiniteTransition1.snapTo(0f)
            infiniteTransition2.snapTo(0f)
        }
    }
    val rotation1Value = infiniteTransition1.value
    val rotation2Value = infiniteTransition2.value


    // 加载和处理图片
    LaunchedEffect(imageUrl) {
        if (imageUrl != null) {
            try {
                val bitmap = processor.loadAndProcessImage(imageUrl)?.asImageBitmap()
                if (bitmap != null) {
                    processedBitmap = bitmap
                    isValidImage = true
                    onImageLoadResult?.invoke(true)
                } else {
                    // 加载失败或图片无效
                    processedBitmap = null
                    isValidImage = false
                    onImageLoadResult?.invoke(false)
                }
            } catch (e: Exception) {
                // 加载异常
                processedBitmap = null
                isValidImage = false
                onImageLoadResult?.invoke(false)
            }
        } else {
            // URI为null
            processedBitmap = null
            isValidImage = false
            onImageLoadResult?.invoke(false)
        }
    }

    // 根据是否有有效图片决定显示内容
    if (processedBitmap != null && isValidImage) {
        val colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.1f), BlendMode.Darken)

        val boxModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            modifier
                .graphicsLayer { clip = true }
                .blur(radius = 20.dp, edgeTreatment = BlurredEdgeTreatment.Rectangle)
        } else {
            // 在 Android 12 以下，为避免性能问题，不应用容器模糊
            // 内部的 CompatBlurImage 已经提供了模糊效果
            modifier.graphicsLayer { clip = true }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer(clip = true)
        ) {
            val baseModifier = Modifier.scale(3f)

            CompatBlurImage(
                bitmap = processedBitmap!!,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = baseModifier
                    .scale(0.9f)
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        rotationZ = rotation1Value
                    },
                blurRadius = 60.dp
            )

            CompatBlurImage(
                bitmap = processedBitmap!!,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = baseModifier
                    .scale(0.9f)
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        rotationZ = rotation1Value
                    },
                blurRadius = 60.dp
            )

            CompatBlurImage(
                bitmap = processedBitmap!!,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = baseModifier
                    .scale(0.9f)
                    .align(Alignment.BottomEnd)
                    .graphicsLayer {
                        rotationZ = rotation2Value
                    },
                blurRadius = 60.dp
            )

            CompatBlurImage(
                bitmap = processedBitmap!!,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = baseModifier
                    .scale(0.9f)
                    .align(Alignment.BottomStart)
                    .graphicsLayer {
                        rotationZ = rotation1Value
                    },
                blurRadius = 60.dp
            )

            CompatBlurImage(
                bitmap = processedBitmap!!,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = baseModifier
                    .scale(0.7f)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        rotationZ = rotation2Value
                    },
                blurRadius = 60.dp
            )

            // 覆盖一层深色的半透明前景，提升对比度
            Box(
                modifier = boxModifier
                    .fillMaxSize()
                    .background(Color.DarkGray.copy(alpha = 0.2f))
            )
        }
    } else {
        // 显示深灰色背景
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Color.DarkGray.copy(alpha = 0.2f)
                )
        )
    }
}