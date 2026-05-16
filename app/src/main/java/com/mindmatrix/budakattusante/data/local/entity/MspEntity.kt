package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "msp_data")
data class MspEntity(
    @PrimaryKey val category: String,
    val approvedMsp: Double,
    val marketPrice: Double,
    val lastUpdated: String,
    val authority: String = "TRIFED"
)
