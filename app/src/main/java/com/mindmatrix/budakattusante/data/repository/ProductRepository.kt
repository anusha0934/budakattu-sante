package com.mindmatrix.budakattusante.data.repository

import com.google.gson.Gson
import com.mindmatrix.budakattusante.data.local.dao.*
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
import javax.inject.Inject
import javax.inject.Singleton
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData

@Singleton
class ProductRepository @Inject constructor(
    private val batchDao: InventoryBatchDao,
    private val productDao: ProductDao,
    private val artisanDao: ArtisanDao,
    private val syncQueueDao: SyncQueueDao,
    private val productHistoryDao: ProductHistoryDao,
    private val sourceLocationDao: SourceLocationDao,
    private val pendingUploadDao: PendingUploadDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val wishlistDao: WishlistDao,
    private val notificationDao: NotificationDao,
    private val firebaseGateway: FirebaseGateway,
    private val gson: Gson
) {
    // Primary streams from local Room DB
    val products: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.filter { !it.isDeleted }.map { it.toProduct() }
    }

    val localArtisans: Flow<List<Artisan>> = artisanDao.getAllArtisans().map { entities ->
        entities.map { it.toArtisan() }
    }

    val localSupplyLogs: Flow<List<SupplyLog>> = batchDao.getAllBatches().map { batches ->
        batches.map { it.toSupplyLog() }
    }
    
    val localBatches: Flow<List<InventoryBatchEntity>> = batchDao.getAllBatches()

    val pendingSyncCount: Flow<Int> = syncQueueDao.getSyncQueueCount()

    val wishlistItems: Flow<List<WishlistEntity>> = wishlistDao.getWishlist()

    /**
     * Requirement 16: Performance Optimization - Pagination support.
     */
    fun getProductsPaged(query: String = "", category: String? = null): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { ProductPagingSource(productDao, query, category) }
        ).flow
    }

    /**
     * Requirement 10 & 11: Delta Sync Implementation.
     */
    suspend fun refreshCatalog() = withContext(Dispatchers.IO) {
        val lastSync = syncMetadataDao.getLastSyncTimestamp("products") ?: 0L
        runCatching {
            val remoteChanges = firebaseGateway.loadProducts(since = lastSync)
            if (remoteChanges.isNotEmpty()) {
                productDao.insertProducts(remoteChanges.map { it.toEntity() })
                syncMetadataDao.insert(SyncMetadataEntity("products", System.currentTimeMillis()))
            }
        }
    }

    suspend fun addInventoryBatch(batch: InventoryBatchEntity) = withContext(Dispatchers.IO) {
        if (batchDao.getBatchById(batch.batchId) != null) {
            throw Exception("Duplicate Batch ID detected: ${batch.batchId}")
        }
        
        batchDao.insertBatch(batch)
        
        val historyId = UUID.randomUUID().toString()
        val history = ProductHistoryEntity(
            historyId = historyId,
            productId = batch.productName.lowercase().replace(" ", "-"),
            productName = batch.productName,
            artisanId = batch.artisanId,
            artisanName = batch.familyName,
            quantity = batch.quantityKg,
            harvestDate = batch.harvestDate,
            tribeName = "Soliga",
            forestRegion = batch.forestRegion,
            sourceLocation = "${batch.village}, ${batch.forestRegion}"
        )
        productHistoryDao.insertHistoryItem(history)

        if (batch.latitude != 0.0) {
            val location = SourceLocationEntity(
                locationId = UUID.randomUUID().toString(),
                batchId = batch.batchId,
                forestRegion = batch.forestRegion,
                village = batch.village,
                latitude = batch.latitude,
                longitude = batch.longitude,
                collectionZone = batch.collectionZone,
                tribeName = "Soliga"
            )
            sourceLocationDao.insertLocation(location)
            queueSyncItem("SOURCE_LOCATION", location.locationId, "INSERT", location)
        }

        if (batch.localImagePath.isNotBlank() && !batch.localImagePath.startsWith("http")) {
            pendingUploadDao.insert(PendingUploadEntity(
                uploadId = UUID.randomUUID().toString(),
                localUri = batch.localImagePath,
                remotePath = "products/${batch.batchId}.jpg",
                entityId = batch.batchId,
                entityType = "BATCH"
            ))
        }

        queueSyncItem("BATCH", batch.batchId, "INSERT", batch)
        queueSyncItem("PRODUCT_HISTORY", history.historyId, "INSERT", history)
    }

    suspend fun approveBatch(batchId: String) = withContext(Dispatchers.IO) {
        val batch = batchDao.getBatchById(batchId) ?: return@withContext
        val updatedBatch = batch.copy(processingStatus = "APPROVED")
        batchDao.insertBatch(updatedBatch)
        val productEntity = updatedBatch.toProductEntity()
        productDao.insertProducts(listOf(productEntity))
        queueSyncItem("BATCH", batchId, "UPDATE", updatedBatch)
        queueSyncItem("PRODUCT", productEntity.productId, "INSERT", productEntity)
    }

    suspend fun rejectBatch(batchId: String) = withContext(Dispatchers.IO) {
        val batch = batchDao.getBatchById(batchId) ?: return@withContext
        val updatedBatch = batch.copy(processingStatus = "REJECTED")
        batchDao.insertBatch(updatedBatch)
        queueSyncItem("BATCH", batchId, "UPDATE", updatedBatch)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        val entity = product.toEntity()
        productDao.insertProducts(listOf(entity))
        queueSyncItem("PRODUCT", product.productId, "UPDATE", entity)
    }

    suspend fun deleteProduct(productId: String) = withContext(Dispatchers.IO) {
        productDao.getProductById(productId)?.let {
            val deletedEntity = it.copy(isDeleted = true, lastUpdated = System.currentTimeMillis())
            productDao.insertProducts(listOf(deletedEntity))
            queueSyncItem("PRODUCT", productId, "DELETE", "")
        }
    }

    suspend fun saveArtisan(artisan: Artisan) = withContext(Dispatchers.IO) {
        val entity = artisan.toEntity(isSynced = false)
        artisanDao.insertArtisan(entity)
        
        if (artisan.profileImageUrl.isNotBlank() && !artisan.profileImageUrl.startsWith("http")) {
            pendingUploadDao.insert(PendingUploadEntity(
                uploadId = UUID.randomUUID().toString(),
                localUri = artisan.profileImageUrl,
                remotePath = "artisans/${artisan.artisanId}.jpg",
                entityId = artisan.artisanId,
                entityType = "ARTISAN"
            ))
        }
        queueSyncItem("ARTISAN", artisan.artisanId, "INSERT", artisan)
    }

    suspend fun getArtisan(artisanId: String): Artisan? = withContext(Dispatchers.IO) {
        artisanDao.getArtisanById(artisanId)?.toArtisan()
    }

    suspend fun addToWishlist(product: Product) = withContext(Dispatchers.IO) {
        wishlistDao.addToWishlist(WishlistEntity(
            productId = product.productId,
            name = product.name,
            price = product.pricePerKg,
            imageUrl = product.imageUrl
        ))
    }

    suspend fun removeFromWishlist(productId: String) = withContext(Dispatchers.IO) {
        wishlistDao.removeFromWishlist(productId)
    }

    fun getProductHistory(productId: String): Flow<List<ProductHistoryEntity>> =
        productHistoryDao.getHistoryForProduct(productId)

    /**
     * Requirement 5: Sync pending inventory batches to Firebase.
     */
    suspend fun syncPendingBatches() = withContext(Dispatchers.IO) {
        val pendingItems = syncQueueDao.getPendingItemsByType("BATCH")
        for (item in pendingItems) {
            try {
                val batch = gson.fromJson(item.payloadJson, InventoryBatchEntity::class.java)
                val cloudUrl = firebaseGateway.publishBatch(batch)
                syncQueueDao.delete(item)
                batchDao.markAsSynced(batch.batchId, cloudUrl)
            } catch (e: Exception) {
                // Keep in queue for next sync
            }
        }
    }

    /**
     * Requirement 5: Sync pending artisans to Firebase.
     */
    suspend fun syncPendingArtisans() = withContext(Dispatchers.IO) {
        val pendingItems = syncQueueDao.getPendingItemsByType("ARTISAN")
        for (item in pendingItems) {
            try {
                val artisan = gson.fromJson(item.payloadJson, Artisan::class.java)
                firebaseGateway.saveArtisan(artisan)
                syncQueueDao.delete(item)
                artisanDao.markAsSynced(artisan.artisanId)
            } catch (e: Exception) {
                // Keep in queue
            }
        }
    }

    private suspend fun queueSyncItem(type: String, id: String, op: String, payload: Any) {
        val syncItem = SyncQueueEntity(
            entityType = type,
            entityId = id,
            operation = op,
            payloadJson = if (payload is String) payload else gson.toJson(payload),
            priority = if (type == "ORDER") 10 else 1
        )
        syncQueueDao.insert(syncItem)
    }

    // Mapper methods
    private fun InventoryBatchEntity.toSupplyLog() = SupplyLog(
        batchId = batchId, 
        productName = productName, 
        category = category,
        totalQuantityKg = quantityKg, 
        pendingStockKg = quantityKg,
        earnings = quantityKg * pricePerKg,
        familyName = familyName,
        harvestDate = harvestDate, 
        synced = isSynced
    )

    private fun InventoryBatchEntity.toProduct() = Product(
        productId = batchId, vendorId = vendorId, artisanId = artisanId, batchId = batchId,
        name = productName, category = category, availableKg = quantityKg, pricePerKg = pricePerKg,
        mspPrice = mspPrice, marketPrice = marketPrice, fairTradeMargin = fairTradeMargin,
        governmentApproved = governmentApproved, isFairTradeCertified = true, certificationId = certificationId,
        pricingAuthority = pricingAuthority, mspLastUpdated = mspLastUpdated, tribalShare = tribalShare,
        familyName = familyName, tribeName = "Soliga", forestRegion = forestRegion, village = village,
        collectionZone = collectionZone, location = village.ifBlank { forestRegion }, sellerPhone = sellerPhone,
        description = description, harvestDate = harvestDate, expiryDate = expiryDate,
        imageUrl = cloudImageUrl.ifBlank { localImagePath }, audioDescUrl = audioDescPath,
        isLocalPendingSync = !isSynced, isPreOrder = isPreOrder, preorderStock = preorderStock,
        expectedHarvestDate = expectedHarvestDate
    )

    private fun InventoryBatchEntity.toProductEntity() = ProductEntity(
        productId = batchId, vendorId = vendorId, artisanId = artisanId, batchId = batchId,
        name = productName, category = category, availableKg = quantityKg, pricePerKg = pricePerKg,
        mspPrice = mspPrice, marketPrice = marketPrice, fairTradeMargin = fairTradeMargin,
        governmentApproved = governmentApproved, isFairTradeCertified = true, certificationId = certificationId,
        pricingAuthority = pricingAuthority, mspLastUpdated = mspLastUpdated, tribalShare = tribalShare,
        familyName = familyName, tribeName = "Soliga", forestRegion = forestRegion, village = village,
        collectionZone = collectionZone, location = village.ifBlank { forestRegion }, locationLat = latitude,
        locationLng = longitude, rating = 5.0, sellerPhone = sellerPhone, description = description,
        harvestDate = harvestDate, expiryDate = expiryDate, imageUrl = cloudImageUrl.ifBlank { localImagePath },
        vendorProfileImageUrl = "", artisanImageUrl = cloudImageUrl.ifBlank { localImagePath },
        audioDescUrl = audioDescPath, isHandmade = true, isPreOrder = isPreOrder, preorderStock = preorderStock,
        preorderCount = 0, expectedHarvestDate = expectedHarvestDate, lastUpdated = System.currentTimeMillis()
    )
}
