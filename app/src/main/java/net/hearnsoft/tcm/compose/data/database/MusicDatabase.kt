package net.hearnsoft.tcm.compose.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.hearnsoft.tcm.compose.data.database.converters.UriConverter
import net.hearnsoft.tcm.compose.data.database.dao.AlbumDao
import net.hearnsoft.tcm.compose.data.database.dao.ArtistDao
import net.hearnsoft.tcm.compose.data.database.dao.SongDao
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity
import net.hearnsoft.tcm.compose.data.database.entities.ArtistEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(UriConverter::class)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_database"
                )
                    .fallbackToDestructiveMigration(false) // 开发阶段使用，生产环境需要提供迁移策略
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}