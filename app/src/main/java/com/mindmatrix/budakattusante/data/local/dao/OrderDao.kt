package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindmatrix.budakattusante.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedOrders(): List<OrderEntity>

    @Query("SELECT COUNT(*) FROM orders WHERE isSynced = 0")
    fun getUnsyncedCountFlow(): Flow<Int>

    @Query("UPDATE orders SET isSynced = 1 WHERE orderId = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("UPDATE orders SET orderStatus = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("DELETE FROM orders WHERE orderId = :orderId")
    suspend fun deleteOrder(orderId: String)
}
