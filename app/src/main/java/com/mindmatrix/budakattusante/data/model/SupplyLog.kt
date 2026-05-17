package com.mindmatrix.budakattusante.data.model

data class SupplyLog(
    val batchId: String = "",
    val productName: String = "",
    val category: String = "",
    val totalQuantityKg: Double = 0.0,
    val soldQuantityKg: Double = 0.0,
    val pendingStockKg: Double = 0.0,
    val earnings: Double = 0.0,
    val paymentStatus: String = "PENDING",
    val familyName: String = "",
    val harvestDate: String = "",
    val synced: Boolean = false
)
