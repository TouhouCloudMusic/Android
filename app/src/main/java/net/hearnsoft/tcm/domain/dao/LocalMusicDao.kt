package net.hearnsoft.tcm.domain.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.hearnsoft.tcm.domain.model.music.MusicEntity

@Dao
interface LocalMusicDao {
    @Query("SELECT * FROM local_music ORDER BY title ASC")
    fun getAllMusic(): Flow<List<MusicEntity>>

    @Query("SELECT * FROM local_music ORDER BY title ASC")
    suspend fun getAllMusicSync(): List<MusicEntity>

    @Query("SELECT * FROM local_music WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    suspend fun searchMusic(query: String): List<MusicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusic(music: List<MusicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleMusic(music: MusicEntity)

    @Delete
    suspend fun deleteMusic(music: MusicEntity)

    @Query("DELETE FROM local_music")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM local_music")
    suspend fun getMusicCount(): Int

    @Query("SELECT * FROM local_music ORDER BY dateAdded DESC")
    suspend fun getMusicOrderByDateAdded(): List<MusicEntity>

    @Query("SELECT * FROM local_music ORDER BY duration DESC")
    suspend fun getMusicOrderByDuration(): List<MusicEntity>
}