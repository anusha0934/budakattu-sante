package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "inventory_batch")
data class InventoryBatchEntity(
    @PrimaryKey val batchId: String = "BATCH-${UUID.randomUUID().toString().take(8).uppercase()}",
    val vendorId: String = "",
    val artisanId: String = "",
    val productName: String,
    val category: String,
    val quantityKg: Double,
    val pricePerKg: Double,
    val mspPrice: Double = 0.0,
    val marketPrice: Double = 0.0,
    val fairTradeMargin: Double = 0.0,
    val governmentApproved: Boolean = false,
    val certificationId: String = "",
    val pricingAuthority: String = "TRIFED",
    val mspLastUpdated: String = "",
    val tribalShare: Double = 0.0,
    val familyId: String,
    val familyName: String,
    val sellerPhone: String,
    val description: String,
    val harvestDate: String,
    val expiryDate: String = "",
    val processingStatus: String = "PENDING", // PENDING, APPROVED, REJECTED
    val forestRegion: String = "",
    val village: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val collectionZone: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val localImagePath: String = "",
    val cloudImageUrl: String = "",
    val audioDescPath: String = "",
    
    // Pre-order fields
    val isPreOrder: Boolean = false,
    val preorderStock: Int = 0,
    val expectedHarvestDate: String = ""
)
