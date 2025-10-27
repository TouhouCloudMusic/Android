package net.hearnsoft.tcm.compose.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

object FilePathUtils {
    /**
     * 根据 Uri 获取真实文件路径
     * @param context Context
     * @param uri Uri
     * @return 真实文件路径，若无法获取则返回 null
     */
    fun getRealPathFromUri(context: Context, uri: Uri): String? {
        if (uri.scheme != "content") {
            return uri.path
        }

        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    cursor.getString(columnIndex)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Logger.err("FilePathUtils", "Error getting real path from URI: $uri", e)
            null
        }
    }

    /**
     * 从完整文件路径中获取其父目录的路径。
     *
     * @param filePath 文件的完整路径
     * @return 父目录的路径字符串，如果没有父目录则返回 null。
     */
    fun getParentFolderPath(filePath: String?): String? {
        return if (filePath.isNullOrBlank()) {
            null
        } else {
            File(filePath).parent
        }
    }
}