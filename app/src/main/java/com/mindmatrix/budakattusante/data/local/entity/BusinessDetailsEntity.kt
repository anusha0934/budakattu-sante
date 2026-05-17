package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindmatrix.budakattusante.data.model.BusinessDetails

@Entity(tableName = "business_details")
data class BusinessDetailsEntity(
    @PrimaryKey val vendorId: String,
    val shopName: String,
    val ownerName: String,
    val tribeName: String,
    val phoneNumber: String,
    val email: String,
    val address: String,
    val village: String,
    val district: String,
    val state: String,
    val forestRegion: String,
    val upiId: String,
    val productCategories: String, // Stored as comma-separated string for simplicity in Room
    val businessDescription: String,
    val profileImageUrl: String,
    val isApproved: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun BusinessDetailsEntity.toModel() = BusinessDetails(
    vendorId = vendorId,
    shopName = shopName,
    ownerName = ownerName,
    tribeName = tribeName,
    address = address,
    phoneNumber = phoneNumber,
    email = email,
    village = village,
    district = district,
    state = state,
    forestRegion = forestRegion,
    upiId = upiId,
    productCategories = if (productCategories.isBlank()) emptyList() else productCategories.split(","),
    businessDescription = businessDescription,
    profileImageUrl = profileImageUrl,
    isApproved = isApproved
)

fun BusinessDetails.toEntity() = BusinessDetailsEntity(
    vendorId = vendorId,
    shopName = shopName,
    ownerName = ownerName,
    tribeName = tribeName,
    address = address,
    phoneNumber = phoneNumber,
    email = email,
    village = village,
    district = district,
    state = state,
    forestRegion = forestRegion,
    upiId = upiId,
    productCategories = productCategories.joinToString(","),
    businessDescription = businessDescription,
    profileImageUrl = profileImageUrl,
    isApproved = isApproved
)
