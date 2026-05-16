package com.mindmatrix.budakattusante.data.repository

import com.mindmatrix.budakattusante.data.local.dao.SyncQueueDao
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import com.mindmatrix.budakattusante.data.local.entity.SyncQueueEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val productRepository: ProductRepository,
    private val firebaseGateway: FirebaseGateway,
    private val syncQueueDao: SyncQueueDao,
    private val gson: Gson
) {
    /**
     * Requirement 7: Admin Panel - Approve harvest batches.
     * Triggers sync queue item for real-time cloud approval.
     */
    suspend fun approveProduct(batchId: String) = withContext(Dispatchers.IO) {
        productRepository.approveBatch(batchId)
    }

    suspend fun rejectProduct(batchId: String) = withContext(Dispatchers.IO) {
        productRepository.rejectBatch(batchId)
    }

    suspend fun updateGlobalMsp(category: String, msp: Double) = withContext(Dispatchers.IO) {
        val payload = mapOf("approvedMsp" to msp, "lastUpdated" to System.currentTimeMillis())
        val syncItem = SyncQueueEntity(
            entityType = "MSP",
            entityId = category,
            operation = "UPDATE",
            payloadJson = gson.toJson(payload),
            priority = 5
        )
        syncQueueDao.insert(syncItem)
    }
}
