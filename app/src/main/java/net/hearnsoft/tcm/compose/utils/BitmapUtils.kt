package net.hearnsoft.tcm.compose.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object BitmapUtils {

    /**
     * 将Bitmap保存为临时文件并返回Uri
     * @param context 上下文
     * @param bitmap 要保存的位图
     * @param prefix 文件名前缀，可选
     * @param quality 压缩质量，默认90
     * @param format 压缩格式，默认JPEG
     */
    fun saveBitmapToTempFile(
        context: Context,
        bitmap: Bitmap,
        prefix: String = "temp",
        quality: Int = 90,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Uri? {
        return try {
            // 创建临时文件目录
            val tempDir = File(context.cacheDir, "temp_images")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            // 生成文件名
            val extension = when (format) {
                Bitmap.CompressFormat.JPEG -> "jpg"
                Bitmap.CompressFormat.PNG -> "png"
                Bitmap.CompressFormat.WEBP -> "webp"
                else -> "jpg"
            }
            val fileName = "${prefix}_${System.currentTimeMillis()}.$extension"

            val tempFile = File(tempDir, fileName)

            // 保存Bitmap到文件
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(format, quality, out)
            }

            // 使用FileProvider生成Uri
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
        } catch (e: IOException) {
            Logger.err("BitmapUtils", "Failed to save bitmap to temp file", e)
            null
        }
    }

    /**
     * 清理临时文件
     * @param context 上下文
     * @param maxAge 文件最大保留时间（毫秒），默认1小时
     */
    fun cleanupTempFiles(context: Context, maxAge: Long = 3600000) {
        try {
            val tempDir = File(context.cacheDir, "temp_images")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (System.currentTimeMillis() - file.lastModified() > maxAge) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Logger.err("BitmapUtils", "Failed to cleanup temp files", e)
        }
    }
}