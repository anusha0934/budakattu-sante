package com.mindmatrix.budakattusante.data.remote

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.mindmatrix.budakattusante.data.local.entity.InventoryBatchEntity
import com.mindmatrix.budakattusante.data.local.entity.OrderEntity
import com.mindmatrix.budakattusante.data.model.*
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseGateway {
    
    private fun firestore(): FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private fun storage(): FirebaseStorage? = try {
        FirebaseStorage.getInstance()
    } catch (e: Exception) {
        null
    }

    suspend fun loadProducts(since: Long = 0): List<Product> {
        val db = firestore() ?: return emptyList()
        return try {
            val query = if (since > 0) {
                db.collection("products").whereGreaterThan("_lastSynced", since)
            } else {
                db.collection("products")
            }
            
            query.get().await().documents.mapNotNull { document ->
                document.toObject(Product::class.java)?.copy(productId = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun uploadImage(uriString: String, path: String): String {
        val storage = storage() ?: return uriString
        if (!uriString.startsWith("content://") && !uriString.startsWith("file://")) return uriString
        
        return try {
            val uri = Uri.parse(uriString)
            val ref = storage.reference.child(path)
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            uriString
        }
    }

    suspend fun publishBatch(batch: InventoryBatchEntity): String {
        val imageUrl = if (batch.localImagePath.isNotBlank()) {
            uploadImage(batch.localImagePath, "products/${batch.batchId}.jpg")
        } else {
            batch.cloudImageUrl
        }
        
        val vendorDetails = getBusinessDetails(batch.vendorId)
        val artisan = if (batch.artisanId.isNotBlank()) getArtisan(batch.artisanId) else null
        
        val product = Product(
            productId = batch.batchId,
            vendorId = batch.vendorId,
            artisanId = batch.artisanId,
            batchId = batch.batchId,
            name = batch.productName,
            category = batch.category,
            availableKg = batch.quantityKg,
            pricePerKg = batch.pricePerKg,
            mspPrice = batch.mspPrice,
            familyName = batch.familyName,
            tribeName = artisan?.tribeName ?: batch.familyName, 
            forestRegion = batch.forestRegion,
            village = batch.village,
            collectionZone = batch.collectionZone,
            location = batch.village.ifBlank { batch.forestRegion },
            sellerPhone = batch.sellerPhone,
            description = batch.description,
            harvestDate = batch.harvestDate,
            expiryDate = batch.expiryDate,
            imageUrl = imageUrl,
            vendorProfileImageUrl = vendorDetails?.profileImageUrl ?: "",
            audioDescUrl = batch.audioDescPath,
            isPreOrder = batch.isPreOrder,
            preorderStock = batch.preorderStock,
            expectedHarvestDate = batch.expectedHarvestDate
        )

        val db = firestore() ?: return imageUrl
        
        try {
            db.runTransaction { transaction ->
                val prodRef = db.collection("products").document(batch.batchId)
                transaction.set(prodRef, product)
                
                val batchRef = db.collection("harvest_batches").document(batch.batchId)
                transaction.set(batchRef, batch.copy(cloudImageUrl = imageUrl))
                
                // Add to product history
                val historyEntry = mapOf(
                    "productId" to batch.batchId,
                    "productName" to batch.productName,
                    "artisanId" to batch.artisanId,
                    "artisanName" to (artisan?.name ?: "Unknown Artisan"),
                    "batchId" to batch.batchId,
                    "harvestDate" to batch.harvestDate,
                    "quantityCollected" to batch.quantityKg,
                    "tribeName" to (artisan?.tribeName ?: "Soliga"),
                    "forestRegion" to batch.forestRegion,
                    "sourceLocation" to "${batch.village}, ${batch.forestRegion}",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "_lastSynced" to System.currentTimeMillis()
                )
                transaction.set(db.collection("product_history").document(), historyEntry)

                // Add to source locations
                val sourceLocation = mapOf(
                    "batchId" to batch.batchId,
                    "forestRegion" to batch.forestRegion,
                    "village" to batch.village,
                    "latitude" to batch.latitude,
                    "longitude" to batch.longitude,
                    "collectionZone" to batch.collectionZone,
                    "tribeName" to (artisan?.tribeName ?: "Soliga"),
                    "timestamp" to FieldValue.serverTimestamp(),
                    "_lastSynced" to System.currentTimeMillis()
                )
                transaction.set(db.collection("source_locations").document(batch.batchId), sourceLocation)
            }.await()
        } catch (e: Exception) {
            // Handle transaction failure
        }

        return imageUrl
    }

    suspend fun publishOrder(order: OrderEntity) {
        val db = firestore() ?: return
        try {
            db.runTransaction { transaction ->
                val orderRef = db.collection("orders").document(order.orderId)
                transaction.set(orderRef, order)
                
                if (order.isPreOrder) {
                    val productRef = db.collection("products").document(order.productId)
                    transaction.update(productRef, "preorderCount", FieldValue.increment(1))
                    transaction.update(productRef, "_lastSynced", System.currentTimeMillis())
                }
            }.await()
        } catch (e: Exception) {
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        val db = firestore() ?: return
        try {
            db.collection("orders")
                .document(orderId)
                .update(mapOf(
                    "orderStatus" to status,
                    "_lastSynced" to System.currentTimeMillis()
                ))
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun cancelOrder(orderId: String, productId: String, isPreOrder: Boolean) {
        val db = firestore() ?: return
        try {
            db.runTransaction { transaction ->
                val orderRef = db.collection("orders").document(orderId)
                transaction.delete(orderRef)
                
                if (isPreOrder) {
                    val productRef = db.collection("products").document(productId)
                    transaction.update(productRef, "preorderCount", FieldValue.increment(-1))
                    transaction.update(productRef, "_lastSynced", System.currentTimeMillis())
                }
            }.await()
        } catch (e: Exception) {
        }
    }

    suspend fun saveBusinessDetails(details: BusinessDetails) {
        val db = firestore() ?: return
        try {
            db.collection("vendors")
                .document(details.vendorId)
                .set(details, SetOptions.merge())
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun getBusinessDetails(vendorId: String): BusinessDetails? {
        val db = firestore() ?: return null
        return try {
            db.collection("vendors")
                .document(vendorId)
                .get()
                .await()
                .toObject(BusinessDetails::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val db = firestore() ?: return
        try {
            db.collection("users")
                .document(profile.userId)
                .set(profile, SetOptions.merge())
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        val db = firestore() ?: return null
        return try {
            db.collection("users")
                .document(userId)
                .get()
                .await()
                .toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addPaymentRecord(record: PaymentRecord) {
        val db = firestore() ?: return
        try {
            db.collection("payments")
                .document(record.transactionId)
                .set(record)
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun getPaymentRecords(vendorId: String): List<PaymentRecord> {
        val db = firestore() ?: return emptyList()
        return try {
            db.collection("payments")
                .whereEqualTo("vendorId", vendorId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(PaymentRecord::class.java) }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addToCart(userId: String, product: Product) {
        val db = firestore() ?: return
        if (userId.isBlank()) return
        try {
            db.collection("users")
                .document(userId)
                .collection("cart")
                .document(product.productId)
                .set(product)
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun removeFromCart(userId: String, productId: String) {
        val db = firestore() ?: return
        if (userId.isBlank()) return
        try {
            db.collection("users")
                .document(userId)
                .collection("cart")
                .document(productId)
                .delete()
                .await()
        } catch (e: Exception) {
        }
    }

    // --- Artisan Support ---
    suspend fun saveArtisan(artisan: Artisan) {
        val db = firestore() ?: return
        try {
            val data = mapOf(
                "artisanId" to artisan.artisanId,
                "vendorId" to artisan.vendorId,
                "name" to artisan.name,
                "tribeName" to artisan.tribeName,
                "familyName" to artisan.familyName,
                "villageName" to artisan.villageName,
                "contactNumber" to artisan.contactNumber,
                "specialization" to artisan.specialization,
                "yearsOfExperience" to artisan.yearsOfExperience,
                "profileImageUrl" to artisan.profileImageUrl,
                "_lastSynced" to System.currentTimeMillis()
            )
            db.collection("artisans")
                .document(artisan.artisanId)
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun getArtisan(artisanId: String): Artisan? {
        val db = firestore() ?: return null
        return try {
            db.collection("artisans")
                .document(artisanId)
                .get()
                .await()
                .toObject(Artisan::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getArtisansByVendor(vendorId: String, since: Long = 0): List<Artisan> {
        val db = firestore() ?: return emptyList()
        return try {
            val query = db.collection("artisans").whereEqualTo("vendorId", vendorId)
            val filteredQuery = if (since > 0) query.whereGreaterThan("_lastSynced", since) else query
            
            filteredQuery.get().await().documents.mapNotNull { it.toObject(Artisan::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Harvest Batch Support ---
    suspend fun getHarvestBatches(vendorId: String, since: Long = 0): List<InventoryBatchEntity> {
        val db = firestore() ?: return emptyList()
        return try {
            val query = db.collection("harvest_batches").whereEqualTo("vendorId", vendorId)
            val filteredQuery = if (since > 0) query.whereGreaterThan("_lastSynced", since) else query
            
            filteredQuery.get().await().documents.mapNotNull { it.toObject(InventoryBatchEntity::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateBatchStatus(batchId: String, status: String) {
        val db = firestore() ?: return
        try {
            db.collection("harvest_batches")
                .document(batchId)
                .update(mapOf(
                    "processingStatus" to status,
                    "_lastSynced" to System.currentTimeMillis()
                ))
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun getProductHistory(productId: String): List<ProductHistory> {
        val db = firestore() ?: return emptyList()
        return try {
            db.collection("product_history")
                .whereEqualTo("productId", productId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(ProductHistory::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSourceLocations(): List<SourceLocation> {
        val db = firestore() ?: return emptyList()
        return try {
            db.collection("source_locations")
                .get()
                .await()
                .toObjects(SourceLocation::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
