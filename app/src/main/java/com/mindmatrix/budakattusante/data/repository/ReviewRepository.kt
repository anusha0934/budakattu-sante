package com.mindmatrix.budakattusante.data.repository

import com.google.gson.Gson
import com.mindmatrix.budakattusante.data.local.dao.ReviewDao
import com.mindmatrix.budakattusante.data.local.dao.SyncQueueDao
import com.mindmatrix.budakattusante.data.local.entity.ReviewEntity
import com.mindmatrix.budakattusante.data.local.entity.SyncQueueEntity
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ReviewRepository(
    private val reviewDao: ReviewDao,
    private val syncQueueDao: SyncQueueDao,
    private val firebaseGateway: FirebaseGateway
) {
    private val gson = Gson()

    fun getReviewsForProduct(productId: String): Flow<List<ReviewEntity>> {
        return reviewDao.getReviewsForProduct(productId)
    }

    /**
     * Requirement 10: Offline Review Support.
     * Saves review locally and queues for sync.
     */
    suspend fun addReview(review: ReviewEntity) = withContext(Dispatchers.IO) {
        reviewDao.insertReview(review)
        
        val syncItem = SyncQueueEntity(
            entityType = "REVIEW",
            entityId = review.reviewId,
            operation = "INSERT",
            payloadJson = gson.toJson(review),
            priority = 2
        )
        syncQueueDao.insert(syncItem)
    }

    suspend fun syncPendingReviews(): Int = withContext(Dispatchers.IO) {
        // Handled by MainSyncWorker
        0
    }
}
