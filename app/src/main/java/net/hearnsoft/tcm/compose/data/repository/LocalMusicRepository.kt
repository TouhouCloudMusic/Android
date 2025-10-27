package net.hearnsoft.tcm.compose.data.repository

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import net.hearnsoft.tcm.compose.data.database.MusicDatabase
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity
import net.hearnsoft.tcm.compose.data.database.entities.ArtistEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.utils.FilePathUtils
import net.hearnsoft.tcm.compose.utils.LocalMusicScanner
import net.hearnsoft.tcm.compose.utils.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@UnstableApi
class LocalMusicRepository @Inject constructor(
    private val database: MusicDatabase,
    @ApplicationContext private val context: Context
) : MusicRepository() {

    private val songDao = database.songDao()
    private val albumDao = database.albumDao()
    private val artistDao = database.artistDao()

    // === 歌曲相关操作 ===
    override fun getAllSongs(): Flow<List<SongEntity>> = songDao.getAllSongs()
    override suspend fun getSongById(songId: Long): SongEntity? = songDao.getSongById(songId)
    override suspend fun getSongByMediaStoreId(mediaStoreId: Long): SongEntity? = songDao.getSongByMediaStoreId(mediaStoreId)
    override fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>> = songDao.getSongsByAlbum(albumId)
    override fun getSongsByArtist(artistId: Long): Flow<List<SongEntity>> = songDao.getSongsByArtist(artistId)
    override fun getFavoriteSongs(): Flow<List<SongEntity>> = songDao.getFavoriteSongs()
    override fun getMostPlayedSongs(limit: Int): Flow<List<SongEntity>> = songDao.getMostPlayedSongs(limit)
    override fun getRecentlyPlayedSongs(limit: Int): Flow<List<SongEntity>> = songDao.getRecentlyPlayedSongs(limit)

    override suspend fun insertSong(song: SongEntity): Long = songDao.insertSong(song)
    override suspend fun insertSongs(songs: List<SongEntity>): List<Long> = songDao.insertSongs(songs)
    override suspend fun updateSong(song: SongEntity) = songDao.updateSong(song)
    override suspend fun deleteSong(song: SongEntity) = songDao.deleteSong(song)
    override suspend fun deleteAllSongs() = songDao.deleteAllSongs()
    override suspend fun getSongCount(): Int = songDao.getSongCount()

    override suspend fun incrementPlayCount(songId: Long, timestamp: Long) = songDao.incrementPlayCount(songId, timestamp)
    override suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean) = songDao.updateFavoriteStatus(songId, isFavorite)

    // === 专辑相关操作 ===
    override fun getAllAlbums(): Flow<List<AlbumEntity>> = albumDao.getAllAlbums()
    override suspend fun getAlbumById(albumId: Long): AlbumEntity? = albumDao.getAlbumById(albumId)
    override suspend fun getAlbumByMediaStoreId(mediaStoreAlbumId: Long): AlbumEntity? = albumDao.getAlbumByMediaStoreId(mediaStoreAlbumId)
    override suspend fun getAlbumsByAlbumArtist(albumArtist: String): Flow<List<AlbumEntity>> = albumDao.getAlbumsByAlbumArtist(albumArtist)

    override suspend fun insertAlbum(album: AlbumEntity): Long = albumDao.insertAlbum(album)
    override suspend fun insertAlbums(albums: List<AlbumEntity>): List<Long> = albumDao.insertAlbums(albums)
    override suspend fun updateAlbum(album: AlbumEntity) = albumDao.updateAlbum(album)
    override suspend fun deleteAlbum(album: AlbumEntity) = albumDao.deleteAlbum(album)
    override suspend fun deleteAllAlbums() = albumDao.deleteAllAlbums()

    // === 艺术家相关操作 ===
    override fun getAllArtists(): Flow<List<ArtistEntity>> = artistDao.getAllArtists()
    override suspend fun getArtistById(artistId: Long): ArtistEntity? = artistDao.getArtistById(artistId)
    override suspend fun getArtistByName(artistName: String): ArtistEntity? = artistDao.getArtistByName(artistName)

    override suspend fun insertArtist(artist: ArtistEntity): Long = artistDao.insertArtist(artist)
    override suspend fun insertArtists(artists: List<ArtistEntity>): List<Long> = artistDao.insertArtists(artists)
    override suspend fun updateArtist(artist: ArtistEntity) = artistDao.updateArtist(artist)
    override suspend fun deleteArtist(artist: ArtistEntity) = artistDao.deleteArtist(artist)
    override suspend fun deleteAllArtists() = artistDao.deleteAllArtists()

    // === 数据同步操作 ===
    override suspend fun scanAndUpdateLibrary(onProgress: ((String) -> Unit)?) {
        try {
            val scannedItems = LocalMusicScanner.scanDeviceMusic(context)
            Logger.debug("LocalMusicRepository", "扫描到 ${scannedItems.size} 首歌曲")

            if (scannedItems.isEmpty()) {
                onProgress?.invoke("No media files found.")
                return
            }

            // 先清空现有数据库内容
            deleteAllSongs()
            deleteAllAlbums()
            deleteAllArtists()
            Logger.debug("LocalMusicRepository", "已清空现有数据库内容")

            // 使用 Map 来追踪已插入的艺术家和专辑，避免重复查询数据库
            val insertedArtists = mutableMapOf<String, Long>() // artistName -> artistId
            val insertedAlbums = mutableMapOf<Long, Long>() // mediaStoreAlbumId -> albumId
            val artistSongCount = mutableMapOf<String, Int>()
            val albumSongCount = mutableMapOf<Long, Int>()

            var processedCount = 0

            for (mediaItem in scannedItems) {
                val (songEntity, albumEntity, artistEntity) = convertMediaItemToEntities(mediaItem)

                // 统计艺术家歌曲数量
                artistSongCount[artistEntity.artistName] =
                    artistSongCount.getOrDefault(artistEntity.artistName, 0) + 1

                // 统计专辑歌曲数量
                albumSongCount[albumEntity.mediaStoreAlbumId] =
                    albumSongCount.getOrDefault(albumEntity.mediaStoreAlbumId, 0) + 1

                // 处理艺术家
                val artistId = insertedArtists[artistEntity.artistName] ?: run {
                    val finalArtistEntity = artistEntity.copy(
                        songCount = artistSongCount[artistEntity.artistName] ?: 1
                    )
                    val newArtistId = insertArtist(finalArtistEntity)
                    insertedArtists[artistEntity.artistName] = newArtistId
                    Logger.debug("LocalMusicRepository", "插入艺术家: ${artistEntity.artistName}, 歌曲数: ${finalArtistEntity.songCount}")
                    newArtistId
                }

                // 如果艺术家已存在但歌曲数量发生了变化，需要更新
                if (insertedArtists.containsKey(artistEntity.artistName)) {
                    val currentCount = artistSongCount[artistEntity.artistName] ?: 1
                    val existingArtist = getArtistById(artistId)
                    if (existingArtist != null && existingArtist.songCount != currentCount) {
                        updateArtist(existingArtist.copy(songCount = currentCount))
                        Logger.debug("LocalMusicRepository", "更新艺术家歌曲数: ${artistEntity.artistName} -> $currentCount")
                    }
                }

                // 处理专辑
                val albumId = insertedAlbums[albumEntity.mediaStoreAlbumId] ?: run {
                    val finalAlbumEntity = albumEntity.copy(
                        songCount = albumSongCount[albumEntity.mediaStoreAlbumId] ?: 1,
                    )
                    val newAlbumId = insertAlbum(finalAlbumEntity)
                    insertedAlbums[albumEntity.mediaStoreAlbumId] = newAlbumId
                    Logger.debug("LocalMusicRepository", "插入专辑: ${albumEntity.albumName}, 歌曲数: ${finalAlbumEntity.songCount}")
                    newAlbumId
                }

                // 如果专辑已存在但歌曲数量或总时长发生了变化，需要更新
                if (insertedAlbums.containsKey(albumEntity.mediaStoreAlbumId)) {
                    val currentSongCount = albumSongCount[albumEntity.mediaStoreAlbumId] ?: 1
                    val existingAlbum = getAlbumById(albumId)
                    if (existingAlbum != null && existingAlbum.songCount != currentSongCount) {
                        updateAlbum(existingAlbum.copy(
                            songCount = currentSongCount
                        ))
                        Logger.debug("LocalMusicRepository", "更新专辑: ${albumEntity.albumName}, 歌曲数: $currentSongCount")
                    }
                }

                // 插入歌曲
                val finalSongEntity = songEntity.copy(
                    artistId = artistId,
                    albumId = albumId,
                    artistName = artistEntity.artistName,
                    albumName = albumEntity.albumName
                )

                insertSong(finalSongEntity)
                Logger.debug("LocalMusicRepository", "插入歌曲: ${finalSongEntity.title}")

                processedCount++
                onProgress?.invoke("${songEntity.title}\n($processedCount/${scannedItems.size})")
            }
            Logger.debug("LocalMusicRepository", "音乐库更新完成，共处理 ${scannedItems.size} 首歌曲")
        } catch (e: Exception) {
            Logger.err("LocalMusicRepository", "更新音乐库时出错: ${e.message}")
            onProgress?.invoke("更新失败: ${e.message}")
        }
    }

    override suspend fun convertMediaItemToEntities(mediaItem: MediaItem): Triple<SongEntity, AlbumEntity, ArtistEntity> {
        val metadata = mediaItem.mediaMetadata
        val mediaStoreId = mediaItem.mediaId.toLongOrNull() ?: 0L

        val artistName = metadata.artist?.toString() ?: "Unknown Artist"
        val albumName = metadata.albumTitle?.toString() ?: "Unknown Album"
        val albumArtist = metadata.albumArtist?.toString() ?: artistName
        val title = metadata.title?.toString() ?: "Unknown Title"
        val albumYear = metadata.recordingYear

        // 从 extras 中获取音轨信息
        val trackNumber = if (metadata.extras?.containsKey("track_number") == true) {
            metadata.extras?.getInt("track_number")
        } else null

        val discNumber = if (metadata.extras?.containsKey("disc_number") == true) {
            metadata.extras?.getInt("disc_number")
        } else null

        // 从 MediaItem 的 URI 中提取专辑ID
        val albumId = try {
            metadata.extras?.getLong("album_id") ?: 0L
        } catch (e: Exception) {
            0L
        }

        // 从 MediaItem 中获取真实路径
        val filePath = FilePathUtils.getRealPathFromUri(context, mediaItem.localConfiguration?.uri ?: Uri.EMPTY) ?: ""

        // 创建艺术家实体
        val artistEntity = ArtistEntity(
            artistName = artistName,
            songCount = 0, // 在插入时会正确设置
            albumCount = 0
        )

        // 创建专辑实体
        val albumEntity = AlbumEntity(
            mediaStoreAlbumId = albumId,
            albumName = albumName,
            albumArtist = albumArtist,
            artworkUri = metadata.artworkUri,
            songCount = 0,
            albumYear = albumYear
        )

        // 创建歌曲实体
        val songEntity = SongEntity(
            mediaStoreId = mediaStoreId,
            title = title,
            artistId = 0L, // 稍后会更新
            albumId = 0L, // 稍后会更新
            artistName = artistName, // 添加艺术家名称
            artworkUri = metadata.artworkUri,
            albumName = albumName,   // 添加专辑名称
            duration = metadata.durationMs ?: 0L,
            filePath = filePath,
            contentUri = mediaItem.localConfiguration?.uri ?: Uri.EMPTY,
            trackNumber = trackNumber,
            discNumber = discNumber
        )

        return Triple(songEntity, albumEntity, artistEntity)
    }
}