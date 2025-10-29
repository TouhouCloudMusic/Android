package net.hearnsoft.tcm.compose.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.hearnsoft.tcm.compose.data.database.entities.ArtistEntity

@Dao
interface ArtistDao {

    @Query("SELECT * FROM artists ORDER BY artist_name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE artist_id = :artistId")
    suspend fun getArtistById(artistId: Long): ArtistEntity?

    @Query("SELECT * FROM artists WHERE artist_name = :artistName")
    suspend fun getArtistByName(artistName: String): ArtistEntity?

    @Query("SELECT * FROM artists WHERE artist_name LIKE '%' || :query || '%' ORDER BY artist_name ASC")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists ORDER BY song_count DESC LIMIT :limit")
    fun getPopularArtists(limit: Int = 20): Flow<List<ArtistEntity>>

    @Query("UPDATE artists SET song_count = song_count + 1 WHERE artist_id = :artistId")
    suspend fun incrementSongCount(artistId: Long)

    @Query("UPDATE artists SET song_count = song_count - 1 WHERE artist_id = :artistId")
    suspend fun decrementSongCount(artistId: Long)

    @Query("UPDATE artists SET album_count = album_count + 1 WHERE artist_id = :artistId")
    suspend fun incrementAlbumCount(artistId: Long)

    @Query("UPDATE artists SET album_count = album_count - 1 WHERE artist_id = :artistId")
    suspend fun decrementAlbumCount(artistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>): List<Long>

    @Update
    suspend fun updateArtist(artist: ArtistEntity)

    @Delete
    suspend fun deleteArtist(artist: ArtistEntity)

    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()

    @Query("SELECT COUNT(*) FROM artists")
    suspend fun getArtistCount(): Int
}