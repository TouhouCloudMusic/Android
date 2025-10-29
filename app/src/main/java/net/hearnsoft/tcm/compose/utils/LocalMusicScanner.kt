package net.hearnsoft.tcm.compose.utils

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.hearnsoft.tcm.compose.pref.SettingsDataStore

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

        val settingsDataStore = SettingsDataStore(context)
        val musicScanNotInclude60sMedia = runCatching {
            runBlocking {
                settingsDataStore.isMusicScanNotInclude60sMedia.first()
            }
        }.getOrDefault(true) // 默认启用过滤60秒以下的音乐文件

        // 定义媒体存储投影
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID, // 确保获取专辑ID
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.CD_TRACK_NUMBER,
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
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val albumArtistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val cdTrackColumn = cursor.getColumnIndex(MediaStore.Audio.Media.CD_TRACK_NUMBER)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Title"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumArtist = cursor.getString(albumArtistColumn) ?: artist
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val year = cursor.getInt(yearColumn)

                    // 如果启用了过滤60秒以下的音乐文件选项，则跳过这些文件
                    if (musicScanNotInclude60sMedia && duration < 60_000) {
                        Logger.debug(TAG, "跳过短音乐文件: $title - $artist, 时长: ${duration}ms")
                        continue
                    }

                    // 获取音轨号和碟号
                    val trackInfo = if (trackColumn >= 0) {
                        val value = cursor.getInt(trackColumn)
                        if (value > 0) value else null
                    } else null

                    val cdTrackInfo = if (cdTrackColumn >= 0) {
                        val value = cursor.getInt(cdTrackColumn)
                        if (value > 0) value else null
                    } else null

                    // 创建专辑封面URI
                    val albumArtUri = "content://media/external/audio/albumart/$albumId".toUri()
                    Logger.debug(TAG, "专辑封面URI: $albumArtUri for 专辑ID: $albumId")

                    // 解析音轨号和碟号（TRACK 字段格式通常是 DDTT，DD是碟号，TT是音轨号）
                    val discNumber = trackInfo?.let { track ->
                        if (track > 1000) track / 1000 else null
                    }

                    val trackNumber = when {
                        trackInfo != null && trackInfo > 1000 -> trackInfo % 1000
                        trackInfo != null && trackInfo > 0 -> trackInfo
                        cdTrackInfo != null && cdTrackInfo > 0 -> cdTrackInfo
                        else -> null
                    }

                    // 创建媒体项URI
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )

                    // 创建额外信息Bundle，包含专辑ID
                    val extras = Bundle().apply {
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
                                .setAlbumArtist(albumArtist)
                                .setDurationMs(duration)
                                .setRecordingYear(year)
                                .setExtras(extras.apply {
                                    trackNumber?.let { putInt("track_number", it) }
                                    discNumber?.let { putInt("disc_number", it) }
                                })
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