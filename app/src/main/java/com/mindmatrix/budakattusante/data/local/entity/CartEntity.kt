package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey val productId: String,
    val name: String,
    val pricePerKg: Double,
    val imageUrl: String,
    val quantity: Double,
    val vendorId: String,
    val isPreOrder: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
