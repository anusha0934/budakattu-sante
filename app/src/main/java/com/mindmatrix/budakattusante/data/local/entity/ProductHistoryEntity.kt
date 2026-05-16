package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "product_history")
data class ProductHistoryEntity(
    @PrimaryKey val historyId: String = UUID.randomUUID().toString(),
    val productId: String,
    val productName: String,
    val artisanId: String,
    val artisanName: String,
    val quantity: Double,
    val harvestDate: String,
    val tribeName: String,
    val forestRegion: String,
    val sourceLocation: String,
    val timestamp: Long = System.currentTimeMillis()
)
