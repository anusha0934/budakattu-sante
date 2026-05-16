package com.mindmatrix.budakattusante.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindmatrix.budakattusante.BudakattuSanteApp

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val appContainer = (applicationContext as? BudakattuSanteApp)?.container
            ?: return Result.failure()

        val productRepo = appContainer.productRepository
        val orderRepo = appContainer.orderRepository

        return try {
            // Attempt to sync pending inventory batches
            productRepo.syncPendingBatches()
            
            // Attempt to sync pending artisans
            productRepo.syncPendingArtisans()
            
            // Attempt to sync pending orders
            orderRepo.syncPendingOrders()
            
            // Refresh catalog from remote to keep local cache updated
            productRepo.refreshCatalog()

            Result.success()
        } catch (e: Exception) {
            // WorkManager will retry based on backoff policy if network is available
            Result.retry()
        }
    }
}
