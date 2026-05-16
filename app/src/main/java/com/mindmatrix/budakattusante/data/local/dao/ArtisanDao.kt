package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.ArtisanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtisanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtisan(artisan: ArtisanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtisans(artisans: List<ArtisanEntity>)

    @Query("SELECT * FROM artisans")
    fun getAllArtisans(): Flow<List<ArtisanEntity>>

    @Query("SELECT * FROM artisans WHERE artisanId = :id")
    suspend fun getArtisanById(id: String): ArtisanEntity?

    @Query("SELECT * FROM artisans WHERE vendorId = :vendorId")
    fun getArtisansByVendor(vendorId: String): Flow<List<ArtisanEntity>>

    @Query("SELECT * FROM artisans WHERE isSynced = 0")
    suspend fun getUnsyncedArtisans(): List<ArtisanEntity>

    @Query("UPDATE artisans SET isSynced = 1 WHERE artisanId = :id")
    suspend fun markAsSynced(id: String)
}
