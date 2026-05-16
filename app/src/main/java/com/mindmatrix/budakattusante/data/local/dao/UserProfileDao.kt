package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getProfileFlow(userId: String): Flow<UserProfileEntity?>

    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteProfile(userId: String)
}
