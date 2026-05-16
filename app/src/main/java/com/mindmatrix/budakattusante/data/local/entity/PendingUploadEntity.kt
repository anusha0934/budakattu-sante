package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_uploads")
data class PendingUploadEntity(
    @PrimaryKey val uploadId: String, // UUID
    val localUri: String,
    val remotePath: String, // Firebase Storage path
    val entityId: String,   // ID of the product/artisan this image belongs to
    val entityType: String, // "PRODUCT", "ARTISAN", "BATCH"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // "PENDING", "UPLOADING", "FAILED"
)
