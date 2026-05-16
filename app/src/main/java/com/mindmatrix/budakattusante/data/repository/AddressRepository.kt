package com.mindmatrix.budakattusante.data.repository

import com.mindmatrix.budakattusante.data.local.dao.AddressDao
import com.mindmatrix.budakattusante.data.local.entity.AddressEntity
import com.mindmatrix.budakattusante.data.model.Address
import com.mindmatrix.budakattusante.data.model.AddressType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddressRepository(private val addressDao: AddressDao) {
    val allAddresses: Flow<List<Address>> = addressDao.getAllAddresses().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun addAddress(address: Address) {
        addressDao.insertAddress(address.toEntity())
    }

    suspend fun deleteAddress(address: Address) {
        addressDao.deleteAddress(address.toEntity())
    }

    suspend fun setDefaultAddress(id: String) {
        addressDao.setDefaultAddress(id)
    }

    private fun AddressEntity.toModel() = Address(
        id = id,
        name = name,
        phone = phone,
        street = street,
        city = city,
        state = state,
        zipCode = zipCode,
        isDefault = isDefault,
        type = type
    )

    private fun Address.toEntity() = AddressEntity(
        id = id,
        name = name,
        phone = phone,
        street = street,
        city = city,
        state = state,
        zipCode = zipCode,
        isDefault = isDefault,
        type = type
    )
}
