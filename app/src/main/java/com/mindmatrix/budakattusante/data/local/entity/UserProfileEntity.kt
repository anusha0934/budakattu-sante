package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindmatrix.budakattusante.data.model.UserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val profileImageUrl: String,
    val role: String,
    val businessName: String,
    val tribeName: String,
    val upiId: String,
    val address: String,
    val village: String,
    val district: String,
    val categories: String, // Room doesn't support List<String> easily without TypeConverter, using comma-separated string
    val description: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun UserProfileEntity.toModel() = UserProfile(
    userId = userId,
    name = name,
    email = email,
    phoneNumber = phoneNumber,
    profileImageUrl = profileImageUrl,
    role = role,
    businessName = businessName,
    tribeName = tribeName,
    upiId = upiId,
    address = address,
    village = village,
    district = district,
    categories = if (categories.isEmpty()) emptyList() else categories.split(","),
    description = description
)

fun UserProfile.toEntity() = UserProfileEntity(
    userId = userId,
    name = name,
    email = email,
    phoneNumber = phoneNumber,
    profileImageUrl = profileImageUrl,
    role = role,
    businessName = businessName,
    tribeName = tribeName,
    upiId = upiId,
    address = address,
    village = village,
    district = district,
    categories = categories.joinToString(","),
    description = description
)
