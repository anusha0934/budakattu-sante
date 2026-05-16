package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindmatrix.budakattusante.data.model.Artisan

@Entity(tableName = "artisans")
data class ArtisanEntity(
    @PrimaryKey val artisanId: String,
    val vendorId: String,
    val name: String,
    val tribeName: String,
    val familyName: String,
    val villageName: String,
    val contactNumber: String,
    val specialization: String,
    val profileImageUrl: String,
    val yearsOfExperience: Int,
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun ArtisanEntity.toArtisan() = Artisan(
    artisanId = artisanId,
    vendorId = vendorId,
    name = name,
    tribeName = tribeName,
    familyName = familyName,
    villageName = villageName,
    contactNumber = contactNumber,
    specialization = specialization,
    profileImageUrl = profileImageUrl,
    yearsOfExperience = yearsOfExperience
)

fun Artisan.toEntity(isSynced: Boolean = false) = ArtisanEntity(
    artisanId = artisanId,
    vendorId = vendorId,
    name = name,
    tribeName = tribeName,
    familyName = familyName,
    villageName = villageName,
    contactNumber = contactNumber,
    specialization = specialization,
    profileImageUrl = profileImageUrl,
    yearsOfExperience = yearsOfExperience,
    isSynced = isSynced,
    lastUpdated = System.currentTimeMillis()
)
