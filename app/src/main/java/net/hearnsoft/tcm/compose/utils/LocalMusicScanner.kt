package net.hearnsoft.tcm.compose.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingRule
import androidx.core.net.toUri

/**
 * 本地歌曲扫描工具类
 */
@UnstableApi
object LocalMusicScanner {
    private const val TAG = "MusicScanner"

    /**
     * 扫描设备中的音乐文件并转换为MediaItems
     *
     * @param context 应用上下文
     * @return 音乐MediaItems列表
     */
    fun scanDeviceMusic(context: Context): List<MediaItem> {
        val musicItems = mutableListOf<MediaItem>()

        // 定义媒体存储投影
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID, // 确保获取专辑ID
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        // 过滤只包含音乐文件
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        // 按标题排序
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Title"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)

                    // 创建专辑封面URI
                    val albumArtUri = "content://media/external/audio/albumart/$albumId".toUri()
                    Logger.debug(TAG, "专辑封面URI: $albumArtUri for 专辑ID: $albumId")

                    // 创建媒体项URI
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )

                    // 创建额外信息Bundle，包含专辑ID
                    val extras = android.os.Bundle().apply {
                        putLong("album_id", albumId)
                    }

                    // 创建MediaItem
                    val mediaItem = MediaItem.Builder()
                        .setMediaId(id.toString())
                        .setUri(contentUri)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(title)
                                .setArtist(artist)
                                .setAlbumTitle(album)
                                .setArtworkUri(albumArtUri)
                                .setDurationMs(duration)
                                .setExtras(extras)
                                .build()
                        )
                        .build()

                    musicItems.add(mediaItem)
                    Logger.debug(TAG, "歌曲已添加: $title - $artist - $album")
                }
            }
        } catch (e: Exception) {
            Logger.err(TAG, "扫描音乐时出错: ${e.message}")
        }

        return musicItems
    }
}