package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String = UUID.randomUUID().toString(),
    val productId: String,
    val vendorId: String = "",
    val buyerId: String = "",
    val productName: String,
    val productImageUrl: String = "",
    val vendorProfileImageUrl: String = "",
    val buyerProfileImageUrl: String = "",
    val quantityOrdered: Double,
    val buyerName: String,
    val buyerPhone: String,
    val buyerAddress: String = "",
    val totalAmount: Double,
    val orderStatus: String = "RESERVED", // RESERVED, PENDING_HARVEST, READY_FOR_DELIVERY, SHIPPED, DELIVERED
    val paymentMethod: String = "PENDING",
    val paymentStatus: String = "PENDING", // PENDING, COMPLETED, FAILED
    val deliveryDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isPreOrder: Boolean = false,
    val harvestDate: String = "",
    val estimatedDeliveryDate: String = "",
    val batchId: String = ""
)
