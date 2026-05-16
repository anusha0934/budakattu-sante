package com.mindmatrix.budakattusante.data.model

import com.google.firebase.firestore.FieldValue

data class ProductHistory(
    val historyId: String = "",
    val productId: String = "",
    val productName: String = "",
    val artisanId: String = "",
    val artisanName: String = "",
    val quantity: Double = 0.0,
    val harvestDate: String = "",
    val tribeName: String = "",
    val forestRegion: String = "",
    val timestamp: Any? = null
)
