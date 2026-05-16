package com.mindmatrix.budakattusante.data.repository

import com.google.gson.Gson
import com.mindmatrix.budakattusante.data.local.dao.ArtisanDao
import com.mindmatrix.budakattusante.data.local.dao.InventoryBatchDao
import com.mindmatrix.budakattusante.data.local.dao.ProductDao
import com.mindmatrix.budakattusante.data.local.dao.SyncQueueDao
import com.mindmatrix.budakattusante.data.local.entity.*
import com.mindmatrix.budakattusante.data.model.Artisan
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.data.model.SupplyLog
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ProductRepository(
    private val batchDao: InventoryBatchDao,
    private val productDao: ProductDao,
    private val artisanDao: ArtisanDao,
    private val syncQueueDao: SyncQueueDao,
    private val firebaseGateway: FirebaseGateway
) {
    private val gson = Gson()

    val localSupplyLogs: Flow<List<SupplyLog>> =
        batchDao.getAllBatches().map { batches ->
            batches.map { it.toSupplyLog() }
        }

    val products: Flow<List<Product>> =
        productDao.getAllProducts().map { entities ->
            entities.map { it.toProduct() }
        }

    val localArtisans: Flow<List<Artisan>> =
        artisanDao.getAllArtisans().map { entities ->
            entities.map { it.toArtisan() }
        }

    val pendingSyncCount: Flow<Int> = syncQueueDao.getSyncQueueCount()

    suspend fun refreshCatalog() = withContext(Dispatchers.IO) {
        val remote = runCatching { firebaseGateway.loadProducts() }.getOrDefault(emptyList())
        if (remote.isNotEmpty()) {
            productDao.clearAll()
            productDao.insertProducts(remote.map { it.toEntity() })
        }
    }

    suspend fun addInventoryBatch(batch: InventoryBatchEntity) = withContext(Dispatchers.IO) {
        batchDao.insertBatch(batch)
        
        // Pushing to sync queue for offline support
        val syncItem = SyncQueueEntity(
            entityType = "BATCH",
            entityId = batch.batchId,
            operation = "INSERT",
            payloadJson = gson.toJson(batch),
            priority = 1
        )
        syncQueueDao.insert(syncItem)
    }

    suspend fun saveArtisan(artisan: Artisan) = withContext(Dispatchers.IO) {
        val entity = artisan.toEntity(isSynced = false)
        artisanDao.insertArtisan(entity)
        
        val syncItem = SyncQueueEntity(
            entityType = "ARTISAN",
            entityId = artisan.artisanId,
            operation = "INSERT",
            payloadJson = gson.toJson(artisan),
            priority = 2
        )
        syncQueueDao.insert(syncItem)
    }

    private fun InventoryBatchEntity.toSupplyLog(): SupplyLog =
        SupplyLog(
            batchId = batchId,
            productName = productName,
            category = category,
            quantityKg = quantityKg,
            pricePerKg = pricePerKg,
            familyName = familyName,
            harvestDate = harvestDate,
            synced = isSynced
        )

    private fun InventoryBatchEntity.toProduct(): Product =
        Product(
            productId = batchId,
            vendorId = vendorId,
            artisanId = artisanId,
            batchId = batchId,
            name = productName,
            category = category,
            availableKg = quantityKg,
            pricePerKg = pricePerKg,
            mspPrice = mspPrice,
            familyName = familyName,
            forestRegion = forestRegion,
            village = village,
            collectionZone = collectionZone,
            sellerPhone = sellerPhone,
            description = description,
            harvestDate = harvestDate,
            expiryDate = expiryDate,
            imageUrl = cloudImageUrl.ifBlank { localImagePath },
            audioDescUrl = audioDescPath,
            isLocalPendingSync = !isSynced,
            isPreOrder = isPreOrder,
            preorderStock = preorderStock,
            expectedHarvestDate = expectedHarvestDate
        )
}
