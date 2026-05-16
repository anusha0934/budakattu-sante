package com.mindmatrix.budakattusante.data.model

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val role: String = "CUSTOMER",
    val businessName: String = "",
    val tribeName: String = "",
    val upiId: String = "",
    val address: String = "",
    val village: String = "",
    val district: String = "",
    val categories: List<String> = emptyList(),
    val description: String = ""
)
