package com.mindmatrix.budakattusante.data.repository

import android.content.Context
import androidx.work.*
import com.mindmatrix.budakattusante.data.local.dao.SyncQueueDao
import com.mindmatrix.budakattusante.data.local.entity.SyncQueueEntity
import com.mindmatrix.budakattusante.worker.MainSyncWorker
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class SyncRepository(
    private val syncQueueDao: SyncQueueDao,
    private val context: Context
) {
    val pendingSyncCount: Flow<Int> = syncQueueDao.getSyncQueueCount()

    suspend fun pushToQueue(entity: SyncQueueEntity) {
        syncQueueDao.insert(entity)
        scheduleImmediateSync()
    }

    fun scheduleImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<MainSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag("immediate_sync")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "MainSyncWorker",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<MainSyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag("periodic_sync")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PeriodicSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }

    suspend fun getPendingItems() = syncQueueDao.getPendingItems()
    
    suspend fun markAsSynced(id: Long) = syncQueueDao.deleteById(id)
}
