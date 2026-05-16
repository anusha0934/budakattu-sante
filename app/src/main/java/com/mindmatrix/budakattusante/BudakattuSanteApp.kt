package com.mindmatrix.budakattusante

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mindmatrix.budakattusante.data.AppContainer
import com.mindmatrix.budakattusante.data.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BudakattuSanteApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncManager: SyncManager

    // Requirement: Expose container for non-Hilt workers or legacy code
    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        // Requirement 13: Initialize and start background synchronization
        syncManager.startSync()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
