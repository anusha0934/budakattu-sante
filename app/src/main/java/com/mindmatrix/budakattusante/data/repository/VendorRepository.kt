package com.mindmatrix.budakattusante.data.repository

import com.mindmatrix.budakattusante.data.local.dao.BusinessDetailsDao
import com.mindmatrix.budakattusante.data.model.BusinessDetails
import com.mindmatrix.budakattusante.data.model.PaymentRecord
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import com.mindmatrix.budakattusante.data.local.entity.toEntity
import com.mindmatrix.budakattusante.data.local.entity.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VendorRepository(
    private val businessDetailsDao: BusinessDetailsDao,
    private val firebaseGateway: FirebaseGateway
) {
    fun getBusinessDetailsFlow(vendorId: String): Flow<BusinessDetails?> =
        businessDetailsDao.getBusinessDetailsFlow(vendorId).map { it?.toModel() }

    suspend fun saveBusinessDetails(details: BusinessDetails) = withContext(Dispatchers.IO) {
        businessDetailsDao.insertBusinessDetails(details.toEntity())
        runCatching {
            firebaseGateway.saveBusinessDetails(details)
        }
    }

    suspend fun getBusinessDetails(vendorId: String): BusinessDetails? = withContext(Dispatchers.IO) {
        // Try local first
        val local = businessDetailsDao.getBusinessDetails(vendorId)
        if (local != null) return@withContext local.toModel()
        
        // Fetch remote and cache
        firebaseGateway.getBusinessDetails(vendorId)?.also {
            businessDetailsDao.insertBusinessDetails(it.toEntity())
        }
    }

    suspend fun getPaymentRecords(vendorId: String): List<PaymentRecord> = withContext(Dispatchers.IO) {
        firebaseGateway.getPaymentRecords(vendorId)
    }

    suspend fun addPaymentRecord(record: PaymentRecord) = withContext(Dispatchers.IO) {
        firebaseGateway.addPaymentRecord(record)
    }
}
