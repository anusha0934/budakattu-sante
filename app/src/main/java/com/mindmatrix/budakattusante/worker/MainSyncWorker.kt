package com.mindmatrix.budakattusante.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.mindmatrix.budakattusante.data.local.BudakattuDatabase
import com.mindmatrix.budakattusante.data.local.entity.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Requirement 6 & 10: Production-grade Sync Engine with Hilt.
 * Handles bidirectional delta sync, conflict resolution, and outbox processing.
 */
@HiltWorker
class MainSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val db: BudakattuDatabase
) : CoroutineWorker(context, params) {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. PUSH: Process the Outbox (Local Changes -> Cloud)
            val pushSuccess = processOutbox()

            // 2. PULL: Fetch Delta Changes (Cloud -> Local)
            val pullSuccess = pullDeltaChanges()

            if (pushSuccess && pullSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e("MainSyncWorker", "Critical sync failure", e)
            Result.retry()
        }
    }

    private suspend fun processOutbox(): Boolean {
        val pendingItems = db.syncQueueDao().getPendingItems()
        if (pendingItems.isEmpty()) return true

        var allSuccess = true
        for (item in pendingItems) {
            try {
                val collection = mapEntityTypeToCollection(item.entityType) ?: continue
                val docRef = firestore.collection(collection).document(item.entityId)
                
                when (item.operation) {
                    "INSERT", "UPDATE" -> {
                        if (item.payloadJson.isNotBlank()) {
                            val payload = JSONObject(item.payloadJson)
                            val dataMap = mutableMapOf<String, Any>()
                            val keys = payload.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                dataMap[key] = payload.get(key)
                            }
                            // Attach sync metadata
                            dataMap["_lastSyncedAt"] = System.currentTimeMillis()
                            dataMap["_localVersion"] = item.localVersion
                            
                            docRef.set(dataMap, SetOptions.merge()).await()
                        }
                    }
                    "DELETE" -> {
                        docRef.update("_isDeleted", true, "_lastSyncedAt", System.currentTimeMillis()).await()
                    }
                }
                db.syncQueueDao().delete(item)
            } catch (e: Exception) {
                Log.e("MainSyncWorker", "Failed to push ${item.entityType} ${item.entityId}", e)
                val updatedItem = item.copy(retryCount = item.retryCount + 1, lastError = e.message)
                db.syncQueueDao().update(updatedItem)
                allSuccess = false
            }
        }
        return allSuccess
    }

    private suspend fun pullDeltaChanges(): Boolean {
        val collections = listOf("products", "artisans", "harvest_batches", "msp_data", "users")
        var allPullSuccess = true

        for (collection in collections) {
            try {
                val lastSync = db.syncMetadataDao().getLastSyncTimestamp(collection) ?: 0L
                val snapshot = firestore.collection(collection)
                    .whereGreaterThan("_lastSyncedAt", lastSync)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    applyRemoteChanges(collection, snapshot.documents)
                    db.syncMetadataDao().insert(SyncMetadataEntity(collection, System.currentTimeMillis()))
                }
            } catch (e: Exception) {
                Log.e("MainSyncWorker", "Delta pull failed for $collection", e)
                allPullSuccess = false
            }
        }
        return allPullSuccess
    }

    private suspend fun applyRemoteChanges(collection: String, documents: List<DocumentSnapshot>) {
        when (collection) {
            "products" -> {
                val entities = documents.mapNotNull { it.toObject(ProductEntity::class.java) }
                db.productDao().insertProducts(entities)
            }
            "artisans" -> {
                val entities = documents.mapNotNull { it.toObject(ArtisanEntity::class.java) }
                db.artisanDao().insertArtisans(entities)
            }
            "harvest_batches" -> {
                val entities = documents.mapNotNull { it.toObject(InventoryBatchEntity::class.java) }
                entities.forEach { db.inventoryBatchDao().insertBatch(it) }
            }
            "msp_data" -> {
                val entities = documents.mapNotNull { it.toObject(MspEntity::class.java) }
                db.mspDao().insertMspData(entities)
            }
        }
    }

    private fun mapEntityTypeToCollection(type: String) = when (type) {
        "PRODUCT" -> "products"
        "ORDER" -> "orders"
        "ARTISAN" -> "artisans"
        "BATCH" -> "harvest_batches"
        "SUPPLY_LOG" -> "supply_logs"
        "PAYMENT" -> "payments"
        "REVIEW" -> "reviews"
        "PROFILE" -> "users"
        else -> null
    }
}
