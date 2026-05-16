package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // MSP_UPDATE, ORDER_UPDATE, SYNC_ALERT
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
