package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(item: WishlistEntity)

    @Query("SELECT * FROM wishlist")
    fun getWishlist(): Flow<List<WishlistEntity>>

    @Query("DELETE FROM wishlist WHERE productId = :productId")
    suspend fun removeFromWishlist(productId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE productId = :productId)")
    suspend fun isWishlisted(productId: String): Boolean
}
