package com.mindmatrix.budakattusante.di

import android.content.Context
import com.mindmatrix.budakattusante.data.SyncManager
import com.mindmatrix.budakattusante.data.repository.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        syncRepository: SyncRepository
    ): SyncManager {
        return SyncManager(context, syncRepository)
    }
}
