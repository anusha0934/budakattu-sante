package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String, // "ARTISAN", "BATCH", "PRODUCT", "ORDER", "REVIEW", "PROFILE"
    val entityId: String,
    val operation: String, // "INSERT", "UPDATE", "DELETE"
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = 0, // Higher numbers processed first
    val retryCount: Int = 0,
    val lastError: String? = null,
    val localVersion: Long = System.currentTimeMillis()
)
