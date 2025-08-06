package net.hearnsoft.tcm.compose.data.repository

import androidx.annotation.OptIn
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.UnstableApi
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    @UnstableApi
    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        localMusicRepository: LocalMusicRepository
    ): MusicRepository
}