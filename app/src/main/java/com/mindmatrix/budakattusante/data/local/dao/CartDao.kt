package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartEntity)

    @Query("SELECT * FROM cart ORDER BY timestamp DESC")
    fun getCartItems(): Flow<List<CartEntity>>

    @Query("UPDATE cart SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateQuantity(productId: String, quantity: Double)

    @Query("DELETE FROM cart WHERE productId = :productId")
    suspend fun removeFromCart(productId: String)

    @Query("DELETE FROM cart")
    suspend fun clearCart()
}
