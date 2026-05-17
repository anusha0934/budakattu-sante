package com.mindmatrix.budakattusante.data.repository

import com.mindmatrix.budakattusante.data.local.dao.AddressDao
import com.mindmatrix.budakattusante.data.local.entity.AddressEntity
import com.mindmatrix.budakattusante.data.local.entity.toModel
import com.mindmatrix.budakattusante.data.local.entity.toEntity
import com.mindmatrix.budakattusante.data.model.Address
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(private val addressDao: AddressDao) {
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
}
