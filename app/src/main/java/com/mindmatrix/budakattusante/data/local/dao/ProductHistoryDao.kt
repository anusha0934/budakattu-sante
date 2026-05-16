package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindmatrix.budakattusante.data.local.entity.ProductHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: List<ProductHistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(history: ProductHistoryEntity)

    @Query("SELECT * FROM product_history WHERE productId = :productId ORDER BY timestamp DESC")
    fun getHistoryForProduct(productId: String): Flow<List<ProductHistoryEntity>>

    @Query("SELECT * FROM product_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ProductHistoryEntity>>

    @Query("DELETE FROM product_history")
    suspend fun clearAll()
}
