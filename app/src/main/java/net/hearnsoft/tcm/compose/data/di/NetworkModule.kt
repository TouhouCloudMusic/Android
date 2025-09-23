package net.hearnsoft.tcm.compose.data.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.hearnsoft.tcm.compose.BuildConfig
import net.hearnsoft.tcm.compose.data.remote.ApiService
import net.hearnsoft.tcm.compose.data.repository.UserRepositoryImpl
import net.hearnsoft.tcm.compose.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(@ApplicationContext context: Context): ApiService {
        return ApiService.getInstance(context, BuildConfig.DEBUG)
    }

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        contentResolver: ContentResolver
    ): UserRepository {
        return UserRepositoryImpl(apiService, contentResolver)
    }
}