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
    imageUrl: Uri?,
    modifier: Modifier = Modifier,
    onImageLoadResult: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val processor = remember { FlowingLightProcessor(context) }
    var processedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isValidImage by remember { mutableStateOf(false) }

    // 使用 Animatable 实现无缝、永不重置的连续动画
    val rotation1 = remember { Animatable(0f) }
    val rotation2 = remember { Animatable(0f) }
    val rotation3 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 启动三个独立的、持续进行的动画
        launch {
            rotation1.animateTo(
                targetValue = 360f * 1000, // 一个非常大的目标值
                animationSpec = tween(durationMillis = 25000 * 1000, easing = LinearEasing)
            )
        }
        launch {
            rotation2.animateTo(
                targetValue = -360f * 1000, // 反向旋转
                animationSpec = tween(durationMillis = 30000 * 1000, easing = LinearEasing)
            )
        }
        launch {
            rotation3.animateTo(
                targetValue = 360f * 1000,
                animationSpec = tween(durationMillis = 35000 * 1000, easing = LinearEasing)
            )
        }
    }

    // 在 graphicsLayer 中直接使用 Animatable 的值
    val rotation1Value = rotation1.value
    val rotation2Value = rotation2.value
    val rotation3Value = rotation3.value

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
            modifier.graphicsLayer { clip = true }
                .blur(radius = 20.dp, edgeTreatment = BlurredEdgeTreatment.Rectangle)
        } else {
            // 在 Android 12 以下，为避免性能问题，不应用容器模糊
            // 内部的 CompatBlurImage 已经提供了模糊效果
            modifier.graphicsLayer { clip = true }
        }

        Box(
            modifier = modifier.fillMaxSize()
                .graphicsLayer(clip = true)
        ) {
            val baseModifier = Modifier.scale(3f)

            // 第一层
            CompatBlurImage(
                bitmap = processedBitmap!!,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = baseModifier
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        rotationZ = rotation1Value * 0.3f
                        translationX = -100f
                        translationY = -100f
                    },
                blurRadius = 50.dp
            )

            // 第二层
            CompatBlurImage(
                bitmap = processedBitmap!!,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = baseModifier
                    .scale(1.2f) // 额外放大
                    .align(Alignment.Center)
                    .graphicsLayer {
                        rotationZ = rotation2Value * 0.25f
                        translationX = 30f
                        translationY = -50f
                    },
                blurRadius = 50.dp
            )

            // 第三层
            CompatBlurImage(
                bitmap = processedBitmap!!,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = baseModifier
                    .align(Alignment.BottomEnd)
                    .scale(1.5f) // 额外放大
                    .graphicsLayer {
                        rotationZ = rotation3Value * 0.3f
                        translationX = 80f
                        translationY = 60f
                    },
                blurRadius = 50.dp
            )

            // 第四层 - 左下角，轻微旋转
            CompatBlurImage(
                bitmap = processedBitmap!!,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = baseModifier
                    .align(Alignment.BottomStart)
                    .scale(0.9f)
                    .graphicsLayer {
                        rotationZ = rotation1Value * -0.2f
                        translationX = -50f
                        translationY = 40f
                    },
                blurRadius = 50.dp
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