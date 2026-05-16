package com.mindmatrix.budakattusante.di

import android.content.Context
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import com.mindmatrix.budakattusante.util.ConnectivityObserver
import com.mindmatrix.budakattusante.util.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseGateway(): FirebaseGateway {
        return FirebaseGateway()
    }
}
