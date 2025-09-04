package net.hearnsoft.tcm.compose.data.repository

import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.Flow
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity
import net.hearnsoft.tcm.compose.data.database.entities.ArtistEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity

/**
 * 音乐仓库抽象基类
 * 定义所有音乐数据源的通用接口
 */
abstract class MusicRepository {

    // === 歌曲相关操作 ===
    abstract fun getAllSongs(): Flow<List<SongEntity>>
    abstract suspend fun getSongById(songId: Long): SongEntity?
    abstract suspend fun getSongByMediaStoreId(mediaStoreId: Long): SongEntity?
    abstract fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>>
    abstract fun getSongsByArtist(artistId: Long): Flow<List<SongEntity>>
    abstract fun getFavoriteSongs(): Flow<List<SongEntity>>
    abstract fun getMostPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>
    abstract fun getRecentlyPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>

    abstract suspend fun insertSong(song: SongEntity): Long
    abstract suspend fun insertSongs(songs: List<SongEntity>): List<Long>
    abstract suspend fun updateSong(song: SongEntity)
    abstract suspend fun deleteSong(song: SongEntity)
    abstract suspend fun deleteAllSongs()
    abstract suspend fun getSongCount(): Int

    abstract suspend fun incrementPlayCount(songId: Long, timestamp: Long = System.currentTimeMillis())
    abstract suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)

    // === 专辑相关操作 ===
    abstract fun getAllAlbums(): Flow<List<AlbumEntity>>
    abstract suspend fun getAlbumById(albumId: Long): AlbumEntity?
    abstract suspend fun getAlbumByMediaStoreId(mediaStoreAlbumId: Long): AlbumEntity?
    abstract suspend fun getAlbumsByArtist(artistName: String): Flow<List<AlbumEntity>>

    abstract suspend fun insertAlbum(album: AlbumEntity): Long
    abstract suspend fun insertAlbums(albums: List<AlbumEntity>): List<Long>
    abstract suspend fun updateAlbum(album: AlbumEntity)
    abstract suspend fun deleteAlbum(album: AlbumEntity)
    abstract suspend fun deleteAllAlbums()

    // === 艺术家相关操作 ===
    abstract fun getAllArtists(): Flow<List<ArtistEntity>>
    abstract suspend fun getArtistById(artistId: Long): ArtistEntity?
    abstract suspend fun getArtistByName(artistName: String): ArtistEntity?

    abstract suspend fun insertArtist(artist: ArtistEntity): Long
    abstract suspend fun insertArtists(artists: List<ArtistEntity>): List<Long>
    abstract suspend fun updateArtist(artist: ArtistEntity)
    abstract suspend fun deleteArtist(artist: ArtistEntity)
    abstract suspend fun deleteAllArtists()

    // === 数据同步操作 ===
    /**
     * 扫描并更新音乐库
     * @param onProgress 进度回调函数
     */
    abstract suspend fun scanAndUpdateLibrary(onProgress: ((String) -> Unit)? = null)

    /**
     * 将 MediaItem 转换为数据库实体
     * 子类可以重写此方法来处理不同来源的数据转换
     */
    abstract suspend fun convertMediaItemToEntities(mediaItem: MediaItem): Triple<SongEntity, AlbumEntity, ArtistEntity>
}