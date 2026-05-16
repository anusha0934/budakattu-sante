package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindmatrix.budakattusante.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY timestamp DESC")
    fun getReviewsForProduct(productId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE isSynced = 0")
    suspend fun getUnsyncedReviews(): List<ReviewEntity>

    @Query("UPDATE reviews SET isSynced = 1 WHERE reviewId = :reviewId")
    suspend fun markAsSynced(reviewId: String)
}
