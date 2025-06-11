package net.hearnsoft.tcm.infrastructure.repository.music

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import net.hearnsoft.tcm.domain.model.song.SongSortingRule
import net.hearnsoft.tcm.infrastructure.logger.Logger.debug
import net.hearnsoft.tcm.infrastructure.logger.Logger.err
import androidx.core.net.toUri

/**
 * 本地歌曲扫描工具类
 */
@UnstableApi
object LocalMusicScanner {
    /**
     * 扫描设备中的音乐文件并转换为MediaItems
     *
     * @param context 应用上下文
     * @return 音乐MediaItems列表
     */
    fun scanDeviceMusic(context: Context): List<MediaItem> {
        val musicItems: MutableList<MediaItem> = ArrayList()

        // 定义媒体存储投影
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        // 过滤只包含音乐文件
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        // 按标题排序
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            ).use {
                if (it != null) {
                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val albumIdColumn =
                        it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val durationColumn =
                        it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                    while (it.moveToNext()) {
                        val id = it.getLong(idColumn)
                        val title = it.getString(titleColumn)
                        val artist = it.getString(artistColumn)
                        val album = it.getString(albumColumn)
                        val albumId = it.getLong(albumIdColumn)
                        val duration = it.getLong(durationColumn)
                        val path = it.getString(pathColumn)

                        // 创建专辑封面URI
                        val albumArtUri =
                            "content://media/external/audio/album-cover-art/$albumId".toUri()

                        // 创建媒体项URI
                        val contentUri = Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id.toString()
                        )

                        // 创建MediaItem
                        val mediaItem = MediaItem.Builder()
                            .setMediaId(id.toString()) // 使用数据库ID作为唯一的mediaId
                            .setUri(contentUri)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(title)
                                    .setArtist(artist)
                                    .setAlbumTitle(album)
                                    .setArtworkUri(albumArtUri)
                                    .setDurationMs(duration) // 添加duration到metadata
                                    .build()
                            )
                            .build()

                        musicItems.add(mediaItem)
                        debug(
                            "MusicScanner",
                            "歌曲已添加: $title"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            err("MusicScanner", "扫描音乐时出错: " + e.message)
        }

        return musicItems
    }

    /**
     * 对音乐列表进行排序（使用已有的LocalMusicSorter）
     *
     * @param items 要排序的音乐列表
     * @param rule  排序规则
     * @return 排序后的列表
     */
    @JvmStatic
    fun sortMusicList(items: List<MediaItem>?, rule: SongSortingRule): List<MediaItem>? {
        return LocalMusicSorter.sortMusicList(items, rule)
    }
}
