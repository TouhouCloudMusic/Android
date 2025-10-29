package net.hearnsoft.tcm.compose.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import com.google.android.renderscript.Toolkit
import kotlin.math.ceil

fun Bitmap.zoom(newWidth: Float, newHeight: Float): Bitmap {
    val matrix = Matrix()
    val scaleWidth = newWidth / width
    val scaleHeight = newHeight / height
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.blur(radius: Float = 25f): Bitmap {
    // 使用RenderScript Toolkit替代已弃用的RenderScript
    return Toolkit.blur(this, radius.coerceIn(1f, 25f).toInt())
}

fun Bitmap.brightness(): Float {
    val smallBmp = zoom(3f, 3f) // 转3*3大小的位图
    val pixel = smallBmp[1, 1] // 取中间位置的像素
    val r = (pixel shr 16 and 0xff) / 255.0f
    val g = (pixel shr 8 and 0xff) / 255.0f
    val b = (pixel and 0xff) / 255.0f
    return 0.299f * r + 0.587f * g + 0.114f * b // 计算灰阶
}

fun Bitmap.handleImageEffect(saturation: Float): Bitmap {
    val saturationMatrix = ColorMatrix()
    saturationMatrix.setSaturation(saturation)
    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(saturationMatrix)
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    canvas.drawBitmap(this, 0F, 0F, paint)
    return bitmap
}

fun Bitmap.mesh(floats: FloatArray): Bitmap {
    val newBit = Bitmap.createBitmap(this)
    val canvas = Canvas(newBit)
    canvas.drawBitmapMesh(this, 5, 5, floats, 0, null, 0, null)
    return newBit
}

@Suppress("DEPRECATION")
fun blurBitmapWithRenderScript(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
    val rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(rs, bitmap)
    val output = Allocation.createTyped(rs, input.type)
    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    script.setRadius(radius.coerceIn(0f, 25f))
    script.setInput(input)
    script.forEach(output)
    val result = createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
    output.copyTo(result)
    rs.destroy()
    return result
}

fun blurBitmapUnbounded(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
    val padding = ceil(radius.toDouble()).toInt()
    val newWidth = bitmap.width + padding * 2
    val newHeight = bitmap.height + padding * 2

    val paddedBitmap = createBitmap(newWidth, newHeight, bitmap.config ?: Bitmap.Config.ARGB_8888)

    val canvas = Canvas(paddedBitmap)
    canvas.drawBitmap(
        bitmap,
        padding.toFloat(), // left
        padding.toFloat(), // top
        null // paint
    )

    return blurBitmapWithRenderScript(context, paddedBitmap, radius)
}
