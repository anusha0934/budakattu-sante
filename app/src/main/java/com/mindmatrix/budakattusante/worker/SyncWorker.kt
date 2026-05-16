package com.mindmatrix.budakattusante.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mindmatrix.budakattusante.BudakattuSanteApp
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val container = (applicationContext as BudakattuSanteApp).container
            
            // Sync batches
            container.productRepository.syncPendingBatches()
            
            // Sync orders
            container.orderRepository.syncPendingOrders()
            
            // Sync artisans
            container.productRepository.syncPendingArtisans()
            
            // Refresh catalog to get latest remote data
            container.productRepository.refreshCatalog()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK = "budakattu-offline-sync"

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(onlineConstraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun scheduleImmediate(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(onlineConstraints())
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        private fun onlineConstraints(): Constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
    }
}
