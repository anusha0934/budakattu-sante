package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "source_locations")
data class SourceLocationEntity(
    @PrimaryKey val locationId: String,
    val batchId: String,
    val forestRegion: String,
    val village: String,
    val latitude: Double,
    val longitude: Double,
    val collectionZone: String,
    val tribeName: String,
    val timestamp: Long = System.currentTimeMillis()
)
