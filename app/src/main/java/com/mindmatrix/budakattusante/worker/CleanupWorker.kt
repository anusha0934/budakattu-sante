package com.mindmatrix.budakattusante.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindmatrix.budakattusante.data.local.BudakattuDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = BudakattuDatabase.getInstance(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Requirement 13 & 16: Performance Optimization & Cache Cleanup
            // Remove old notifications (older than 30 days) - would need a Query in NotificationDao
            
            // Clean up old AI chat history
            db.aiChatDao().clearHistory()

            // Success
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
