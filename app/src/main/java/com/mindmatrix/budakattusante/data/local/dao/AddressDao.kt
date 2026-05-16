package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses")
    fun getAllAddresses(): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0")
    suspend fun clearDefault()

    @Transaction
    suspend fun setDefaultAddress(id: String) {
        clearDefault()
        setAsDefault(id)
    }

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :id")
    suspend fun setAsDefault(id: String)
}
