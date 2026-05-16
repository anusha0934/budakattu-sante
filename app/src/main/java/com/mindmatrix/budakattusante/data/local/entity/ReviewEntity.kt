package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val reviewId: String = UUID.randomUUID().toString(),
    val productId: String,
    val userId: String,
    val userName: String,
    val rating: Double,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
