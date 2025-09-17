package net.hearnsoft.tcm.compose.ui.uicomponent

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.hearnsoft.tcm.compose.ui.utils.blurBitmapUnbounded

@Composable
fun CompatBlurImage(
    bitmap: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    blurRadius: Dp = 0.dp,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
        // 使用系统原生模糊 (Android 12+)
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier.blur(blurRadius, BlurredEdgeTreatment.Unbounded),
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality
        )
    } else {
        // 使用RenderScript模糊 (Android 11及以下)
        val context = LocalContext.current
        val blurRadiusPx = blurRadius.value
        val blurredBitmap = remember(bitmap, blurRadiusPx) {
            if (blurRadiusPx > 0) {
                blurBitmapUnbounded(context, bitmap.asAndroidBitmap(), blurRadiusPx).asImageBitmap()
            } else {
                bitmap
            }
        }
        val bitmapPainter = remember(blurredBitmap) {
            BitmapPainter(blurredBitmap, filterQuality = filterQuality)
        }
        Image(
            painter = bitmapPainter,
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
        )
    }
}