package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindmatrix.budakattusante.data.model.Address
import com.mindmatrix.budakattusante.data.model.AddressType
import java.util.UUID

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val houseNumber: String,
    val street: String,
    val city: String = "",
    val village: String,
    val district: String,
    val state: String,
    val zipCode: String,
    val landmark: String,
    val isDefault: Boolean = false,
    val type: AddressType = AddressType.HOME
)

fun AddressEntity.toModel() = Address(
    id = id,
    name = name,
    phone = phone,
    houseNumber = houseNumber,
    street = street,
    city = city,
    village = village,
    district = district,
    state = state,
    zipCode = zipCode,
    landmark = landmark,
    isDefault = isDefault,
    type = type
)

fun Address.toEntity() = AddressEntity(
    id = id,
    name = name,
    phone = phone,
    houseNumber = houseNumber,
    street = street,
    city = city,
    village = village,
    district = district,
    state = state,
    zipCode = zipCode,
    landmark = landmark,
    isDefault = isDefault,
    type = type
)
