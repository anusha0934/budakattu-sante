package com.mindmatrix.budakattusante.data.model

import java.util.UUID

data class Address(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val phone: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val landmark: String = "",
    val isDefault: Boolean = false,
    val type: AddressType = AddressType.HOME
)

enum class AddressType {
    HOME, WORK, OTHER
}
