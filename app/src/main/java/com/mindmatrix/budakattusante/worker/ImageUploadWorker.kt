package com.mindmatrix.budakattusante.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.storage.FirebaseStorage
import com.mindmatrix.budakattusante.data.local.BudakattuDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Requirement 8 & 13: Local Image Handling & ImageUploadWorker.
 * Guarantees that harvest and artisan photos are uploaded even if captured offline.
 */
@HiltWorker
class ImageUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val db: BudakattuDatabase
) : CoroutineWorker(context, params) {

    private val storage = FirebaseStorage.getInstance()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val pendingUploads = db.pendingUploadDao().getPendingUploads()
            if (pendingUploads.isEmpty()) return@withContext Result.success()

            var allSuccess = true
            for (upload in pendingUploads) {
                try {
                    db.pendingUploadDao().updateStatus(upload.uploadId, "UPLOADING")
                    
                    val uri = Uri.parse(upload.localUri)
                    val ref = storage.reference.child(upload.remotePath)
                    
                    // Requirement 8: Retry failed uploads automatically
                    ref.putFile(uri).await()
                    val downloadUrl = ref.downloadUrl.await().toString()
                    
                    // Update the corresponding entity with the new cloud URL
                    updateEntityUrl(upload.entityId, upload.entityType, downloadUrl)
                    
                    // Cleanup pending upload record
                    db.pendingUploadDao().delete(upload)
                } catch (e: Exception) {
                    Log.e("ImageUploadWorker", "Failed to upload ${upload.uploadId}", e)
                    db.pendingUploadDao().updateStatus(upload.uploadId, "FAILED")
                    allSuccess = false
                }
            }

            if (allSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e("ImageUploadWorker", "Global worker error", e)
            Result.retry()
        }
    }

    private suspend fun updateEntityUrl(entityId: String, type: String, url: String) {
        when (type) {
            "PRODUCT" -> {
                db.productDao().getProductById(entityId)?.let { entity ->
                    db.productDao().insertProducts(listOf(entity.copy(imageUrl = url)))
                }
            }
            "ARTISAN" -> {
                db.artisanDao().getArtisanById(entityId)?.let { entity ->
                    db.artisanDao().insertArtisan(entity.copy(profileImageUrl = url))
                }
            }
            "BATCH" -> {
                db.inventoryBatchDao().getBatchById(entityId)?.let { entity ->
                    db.inventoryBatchDao().insertBatch(entity.copy(cloudImageUrl = url))
                }
            }
        }
    }
}
