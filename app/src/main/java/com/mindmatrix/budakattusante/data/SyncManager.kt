package com.mindmatrix.budakattusante.data

import android.content.Context
import androidx.work.*
import com.mindmatrix.budakattusante.data.local.BudakattuDatabase
import com.mindmatrix.budakattusante.data.repository.SyncRepository
import com.mindmatrix.budakattusante.worker.MainSyncWorker
import com.mindmatrix.budakattusante.worker.ImageUploadWorker
import com.mindmatrix.budakattusante.worker.MSPRefreshWorker
import com.mindmatrix.budakattusante.worker.CleanupWorker
import java.util.concurrent.TimeUnit

class SyncManager(
    private val context: Context,
    private val syncRepository: SyncRepository
) {
    private val workManager = WorkManager.getInstance(context)

    fun startSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 1. Image Uploads (High Priority for Vendors)
        val imageUploadRequest = OneTimeWorkRequestBuilder<ImageUploadWorker>()
            .setConstraints(constraints)
            .addTag("sync_images")
            .build()

        // 2. Data Sync (Main Outbox Processor)
        val dataSyncRequest = OneTimeWorkRequestBuilder<MainSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag("sync_data")
            .build()

        // 3. Periodic Background Sync
        val periodicDataSync = PeriodicWorkRequestBuilder<MainSyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag("periodic_sync")
            .build()

        // Chain work: Upload images before syncing entities that might depend on them
        workManager.beginUniqueWork("BUDAKATTU_SYNC", ExistingWorkPolicy.REPLACE, imageUploadRequest)
            .then(dataSyncRequest)
            .enqueue()

        workManager.enqueueUniquePeriodicWork(
            "BUDAKATTU_PERIODIC",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicDataSync
        )

        // 4. MSP Refresh (Daily)
        val mspRefresh = PeriodicWorkRequestBuilder<MSPRefreshWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork("MSP_REFRESH", ExistingPeriodicWorkPolicy.KEEP, mspRefresh)
        
        // 5. Cleanup (Weekly)
        val cleanup = PeriodicWorkRequestBuilder<CleanupWorker>(7, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiresDeviceIdle(true).build())
            .build()
        workManager.enqueueUniquePeriodicWork("CLEANUP", ExistingPeriodicWorkPolicy.KEEP, cleanup)
    }

    fun syncNow() {
        syncRepository.scheduleImmediateSync()
    }
}
