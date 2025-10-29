package net.hearnsoft.tcm.compose.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.hearnsoft.tcm.compose.data.database.dao.AlbumDao
import net.hearnsoft.tcm.compose.data.database.dao.ArtistDao
import net.hearnsoft.tcm.compose.data.database.dao.SongDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MusicDatabase::class.java,
            "music_database"
        )
            .fallbackToDestructiveMigration(false) // 开发阶段使用，生产环境需要提供迁移策略
            .build()
    }

    @Provides
    fun provideSongDao(database: MusicDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    fun provideAlbumDao(database: MusicDatabase): AlbumDao {
        return database.albumDao()
    }

    @Provides
    fun provideArtistDao(database: MusicDatabase): ArtistDao {
        return database.artistDao()
    }
}