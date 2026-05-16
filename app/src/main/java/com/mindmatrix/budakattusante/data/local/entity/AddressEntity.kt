package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindmatrix.budakattusante.data.model.AddressType
import java.util.UUID

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val isDefault: Boolean = false,
    val type: AddressType = AddressType.HOME
)
