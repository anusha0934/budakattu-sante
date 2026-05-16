package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "form_drafts")
data class DraftEntity(
    @PrimaryKey val draftId: String, // e.g., "ADD_PRODUCT_DRAFT"
    val userId: String,
    val formType: String, // "PRODUCT", "ARTISAN", "ORDER"
    val jsonData: String,
    val lastModified: Long = System.currentTimeMillis()
)
