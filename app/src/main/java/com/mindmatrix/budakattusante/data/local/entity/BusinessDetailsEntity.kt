package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindmatrix.budakattusante.data.model.BusinessDetails

@Entity(tableName = "business_details")
data class BusinessDetailsEntity(
    @PrimaryKey val vendorId: String,
    val shopName: String,
    val ownerName: String,
    val phoneNumber: String,
    val address: String,
    val tribalCategory: String,
    val profileImageUrl: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun BusinessDetailsEntity.toModel() = BusinessDetails(
    vendorId = vendorId,
    shopName = shopName,
    ownerName = ownerName,
    address = address,
    phoneNumber = phoneNumber,
    tribalCategory = tribalCategory,
    profileImageUrl = profileImageUrl
)

fun BusinessDetails.toEntity() = BusinessDetailsEntity(
    vendorId = vendorId,
    shopName = shopName,
    ownerName = ownerName,
    address = address,
    phoneNumber = phoneNumber,
    tribalCategory = tribalCategory,
    profileImageUrl = profileImageUrl
)
