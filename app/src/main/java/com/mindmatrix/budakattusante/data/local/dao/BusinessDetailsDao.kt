package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.BusinessDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessDetails(details: BusinessDetailsEntity)

    @Query("SELECT * FROM business_details WHERE vendorId = :vendorId")
    suspend fun getBusinessDetails(vendorId: String): BusinessDetailsEntity?

    @Query("SELECT * FROM business_details WHERE vendorId = :vendorId")
    fun getBusinessDetailsFlow(vendorId: String): Flow<BusinessDetailsEntity?>
}
