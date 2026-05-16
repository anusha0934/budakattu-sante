package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY lastUpdated DESC LIMIT :limit OFFSET :offset")
    suspend fun getProductsPaged(limit: Int, offset: Int): List<ProductEntity>

    @Query("SELECT * FROM products WHERE isDeleted = 0 AND category = :category ORDER BY lastUpdated DESC LIMIT :limit OFFSET :offset")
    suspend fun getProductsByCategoryPaged(category: String, limit: Int, offset: Int): List<ProductEntity>

    @Query("SELECT * FROM products WHERE isDeleted = 0 AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY lastUpdated DESC LIMIT :limit OFFSET :offset")
    suspend fun searchProductsPaged(query: String, limit: Int, offset: Int): List<ProductEntity>

    @Query("SELECT * FROM products WHERE productId = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("UPDATE products SET availableKg = availableKg - :quantity WHERE productId = :id")
    suspend fun reduceStock(id: String, quantity: Double)

    @Query("UPDATE products SET preorderCount = preorderCount + 1 WHERE productId = :id")
    suspend fun incrementPreorderCount(id: String)

    @Query("DELETE FROM products WHERE productId = :id")
    suspend fun deleteProductById(id: String)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Query("SELECT * FROM products WHERE isDeleted = 0 AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')")
    fun searchProducts(query: String): Flow<List<ProductEntity>>
}
