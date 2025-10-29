package net.hearnsoft.tcm.compose.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY album_name ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE album_id = :albumId")
    suspend fun getAlbumById(albumId: Long): AlbumEntity?

    @Query("SELECT * FROM albums WHERE media_store_album_id = :mediaStoreAlbumId")
    suspend fun getAlbumByMediaStoreId(mediaStoreAlbumId: Long): AlbumEntity?

    @Query("SELECT * FROM albums WHERE album_artist = :albumArtist ORDER BY album_name ASC")
    fun getAlbumsByAlbumArtist(albumArtist: String): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE album_name LIKE '%' || :query || '%' ORDER BY album_name ASC")
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY song_count DESC LIMIT :limit")
    fun getPopularAlbums(limit: Int = 20): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>): List<Long>

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()

    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getAlbumCount(): Int
}