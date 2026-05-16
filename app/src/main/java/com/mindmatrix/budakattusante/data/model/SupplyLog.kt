package com.mindmatrix.budakattusante.data.model

data class SupplyLog(
    val batchId: String = "",
    val productName: String = "",
    val category: String = "",
    val quantityKg: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val familyName: String = "",
    val harvestDate: String = "",
    val synced: Boolean = false
)
