package com.mindmatrix.budakattusante.data.model

data class BusinessDetails(
    val vendorId: String = "",
    val shopName: String = "",
    val ownerName: String = "",
    val tribeName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val village: String = "",
    val district: String = "",
    val state: String = "",
    val forestRegion: String = "",
    val upiId: String = "",
    val productCategories: List<String> = emptyList(),
    val businessDescription: String = "",
    val profileImageUrl: String = "",
    val isApproved: Boolean = false
)
