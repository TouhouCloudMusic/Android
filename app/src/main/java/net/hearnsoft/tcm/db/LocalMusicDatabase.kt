package net.hearnsoft.tcm.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.hearnsoft.tcm.domain.dao.LocalMusicDao
import net.hearnsoft.tcm.domain.model.music.MusicEntity

@Database(
    entities = [MusicEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LocalMusicDatabase : RoomDatabase() {
    abstract fun musicDao(): LocalMusicDao

    companion object {
        @Volatile
        private var INSTANCE: LocalMusicDatabase? = null

        fun getDatabase(context: Context): LocalMusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalMusicDatabase::class.java,
                    "local_music_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}