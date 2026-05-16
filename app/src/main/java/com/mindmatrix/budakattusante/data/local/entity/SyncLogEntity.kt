package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val event: String, // "PUSH_START", "PUSH_SUCCESS", "PUSH_FAILURE", "PULL_START", etc.
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null
)
