package com.mindmatrix.budakattusante.data.model

data class Artisan(
    val artisanId: String = "",
    val vendorId: String = "",
    val name: String = "",
    val tribeName: String = "Soliga",
    val familyName: String = "",
    val villageName: String = "",
    val contactNumber: String = "",
    val specialization: String = "",
    val yearsOfExperience: Int = 0,
    val profileImageUrl: String = ""
)
