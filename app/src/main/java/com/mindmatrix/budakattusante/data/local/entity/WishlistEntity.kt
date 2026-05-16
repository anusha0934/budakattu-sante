package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class WishlistEntity(
    @PrimaryKey val productId: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)
