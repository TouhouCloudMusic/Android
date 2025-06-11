package net.hearnsoft.tcm.domain.repository

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import net.hearnsoft.tcm.domain.dao.LocalMusicDao
import net.hearnsoft.tcm.domain.model.music.MusicEntity
import net.hearnsoft.tcm.domain.model.song.SongSortingRule
import net.hearnsoft.tcm.domain.model.song.SongSortingStrategy
import net.hearnsoft.tcm.utils.LocalMusicScanner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@UnstableApi
class MusicRepository @Inject constructor(
    private val localMusicDao: LocalMusicDao
) {

    fun getAllMusic(): Flow<List<MediaItem>> {
        return localMusicDao.getAllMusic().map { entities ->
            entities.map { entity -> entity.toMediaItem() }
        }
    }

    suspend fun loadMusicFromDatabase(): List<MediaItem> {
        return localMusicDao.getAllMusicSync().map { it.toMediaItem() }
    }

    suspend fun scanAndSaveMusic(context: Context): List<MediaItem> {
        val scannedMusic = LocalMusicScanner.scanDeviceMusic(context)
        val musicEntities = scannedMusic.map { mediaItem ->
            mediaItem.toMusicEntity()
        }

        // 清空旧数据并插入新数据
        localMusicDao.clearAll()
        localMusicDao.insertMusic(musicEntities)

        return scannedMusic
    }

    suspend fun getMusicCount(): Int {
        return localMusicDao.getMusicCount()
    }

    suspend fun sortMusic(rule: SongSortingRule): List<MediaItem> {
        val entities = when (rule.strategy) {
            SongSortingStrategy.Title -> localMusicDao.getMusicOrderByTitle()
            SongSortingStrategy.ArtistName -> localMusicDao.getMusicOrderByArtist()
            SongSortingStrategy.CreatedAt -> localMusicDao.getMusicOrderByDateAdded()
            SongSortingStrategy.UpdatedAt -> localMusicDao.getMusicOrderByDateAdded()
            SongSortingStrategy.PlayCount -> {
                // PlayCount暂时使用duration作为替代排序，后续可以添加播放次数字段
                localMusicDao.getMusicOrderByDuration()
            }
            null -> localMusicDao.getMusicOrderByTitle() // 默认按标题排序
        }

        val sortedEntities = if (rule.reverse) entities.reversed() else entities
        return sortedEntities.map { it.toMediaItem() }
    }

    suspend fun searchMusic(query: String): List<MediaItem> {
        return localMusicDao.searchMusic(query).map { it.toMediaItem() }
    }

    suspend fun refreshMusicLibrary(context: Context): List<MediaItem> {
        return scanAndSaveMusic(context)
    }

    // Java兼容的CompletableFuture异步方法
    fun loadMusicFromDatabaseAsync(): CompletableFuture<List<MediaItem>> {
        return CoroutineScope(Dispatchers.IO).future {
            loadMusicFromDatabase()
        }
    }

    fun scanAndSaveMusicAsync(context: Context): CompletableFuture<List<MediaItem>> {
        return CoroutineScope(Dispatchers.IO).future {
            scanAndSaveMusic(context)
        }
    }

    fun getMusicCountAsync(): CompletableFuture<Int> {
        return CoroutineScope(Dispatchers.IO).future {
            getMusicCount()
        }
    }

    fun sortMusicAsync(rule: SongSortingRule): CompletableFuture<List<MediaItem>> {
        return CoroutineScope(Dispatchers.IO).future {
            sortMusic(rule)
        }
    }

    fun searchMusicAsync(query: String): CompletableFuture<List<MediaItem>> {
        return CoroutineScope(Dispatchers.IO).future {
            searchMusic(query)
        }
    }

    fun refreshMusicLibraryAsync(context: Context): CompletableFuture<List<MediaItem>> {
        return CoroutineScope(Dispatchers.IO).future {
            refreshMusicLibrary(context)
        }
    }

}

// 扩展函数：MediaItem转MusicEntity
@UnstableApi
private fun MediaItem.toMusicEntity(): MusicEntity {
    return MusicEntity(
        mediaId = this.mediaId,
        title = this.mediaMetadata.title?.toString() ?: "Unknown",
        artist = this.mediaMetadata.artist?.toString(),
        album = this.mediaMetadata.albumTitle?.toString(),
        albumArtUri = this.mediaMetadata.artworkUri?.toString(),
        duration = this.mediaMetadata.durationMs ?: 0L,
        uri = this.localConfiguration?.uri?.toString() ?: "",
        dateAdded = System.currentTimeMillis(),
    )
}

// 扩展函数：MusicEntity转MediaItem
@UnstableApi
private fun MusicEntity.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(this.mediaId)
        .setUri(this.uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(this.title)
                .setArtist(this.artist)
                .setAlbumTitle(this.album)
                .setArtworkUri(this.albumArtUri?.let { Uri.parse(it) })
                .setDurationMs(this.duration)
                .build()
        )
        .build()
}