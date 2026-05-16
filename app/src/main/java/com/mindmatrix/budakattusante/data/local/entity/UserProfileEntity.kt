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
    val lastUpdated: Long = System.currentTimeMillis()
)

fun UserProfileEntity.toModel() = UserProfile(
    userId = userId,
    name = name,
    email = email,
    phoneNumber = phoneNumber,
    profileImageUrl = profileImageUrl,
    role = role
)

fun UserProfile.toEntity() = UserProfileEntity(
    userId = userId,
    name = name,
    email = email,
    phoneNumber = phoneNumber,
    profileImageUrl = profileImageUrl,
    role = role
)
