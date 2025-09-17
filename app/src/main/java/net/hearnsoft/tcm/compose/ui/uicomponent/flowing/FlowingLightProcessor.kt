package net.hearnsoft.tcm.compose.ui.uicomponent.flowing

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hearnsoft.tcm.compose.ui.utils.blur
import net.hearnsoft.tcm.compose.ui.utils.brightness
import net.hearnsoft.tcm.compose.ui.utils.handleImageEffect
import net.hearnsoft.tcm.compose.ui.utils.mesh
import net.hearnsoft.tcm.compose.ui.utils.zoom
import net.hearnsoft.tcm.compose.utils.Logger

class FlowingLightProcessor(private val context: Context) {

    // Apple Music中的Mesh参数之一
    private val meshFloats = floatArrayOf(
        -0.2351f, -0.0967f, 0.2135f, -0.1414f, 0.9221f, -0.0908f, 0.9221f, -0.0685f, 1.3027f, 0.0253f, 1.2351f, 0.1786f,
        -0.3768f, 0.1851f, 0.2f, 0.2f, 0.6615f, 0.3146f, 0.9543f, 0.0f, 0.6969f, 0.1911f, 1.0f, 0.2f,
        0.0f, 0.4f, 0.2f, 0.4f, 0.0776f, 0.2318f, 0.6f, 0.4f, 0.6615f, 0.3851f, 1.0f, 0.4f,
        0.0f, 0.6f, 0.1291f, 0.6f, 0.4f, 0.6f, 0.4f, 0.4304f, 0.4264f, 0.5792f, 1.2029f, 0.8188f,
        -0.1192f, 1.0f, 0.6f, 0.8f, 0.4264f, 0.8104f, 0.6f, 0.8f, 0.8f, 0.8f, 1.0f, 0.8f,
        0.0f, 1.0f, 0.0776f, 1.0283f, 0.4f, 1.0f, 0.6f, 1.0f, 0.8f, 1.0f, 1.1868f, 1.0283f
    )

    suspend fun loadAndProcessImage(imageUri: Uri?): Bitmap? = withContext(Dispatchers.IO) {
        try {
            Logger.debug("FlowingLightProcessor", "Loading image from URI: $imageUri")
            return@withContext (
                    ImageLoader(context)
                        .execute(
                            ImageRequest.Builder(context)
                                .data(imageUri ?: Uri.EMPTY)
                                .allowHardware(false)
                                .crossfade(true)
                                .build(),
                        ).image as? BitmapImage
                    )?.bitmap?.let { processBitmap(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun processBitmap(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        // 1. 将图片缩小到合适尺寸，保持比例
        val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
        val targetHeight = (300 * aspectRatio).toInt()

        val resized = bitmap.zoom(300f, targetHeight.toFloat())

        // 2. 增加饱和度
        val saturated = resized.handleImageEffect(1.8f)

        // 3. 根据亮度调整处理参数，应用Apple Music的mesh效果
        val brightness = saturated.brightness()

        when {
            brightness < 0.3f -> {
                // 暗色调处理
                saturated
                    .blur(15f)
                    .zoom(600f, (targetHeight * 2).toFloat())
                    .blur(10f)
            }
            brightness > 0.7f -> {
                // 亮色调处理 - 应用mesh变形
                saturated
                    .blur(20f)
                    .mesh(meshFloats)
                    .zoom(580f, (targetHeight * 1.9f))
                    .blur(8f)
            }
            else -> {
                // 中等亮度处理
                saturated
                    .blur(18f)
                    .zoom(590f, (targetHeight * 1.95f))
                    .blur(12f)
            }
        }
    }
}