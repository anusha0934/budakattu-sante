package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.SourceLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SourceLocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<SourceLocationEntity>)

    @Query("SELECT * FROM source_locations WHERE batchId = :batchId")
    fun getLocationForBatch(batchId: String): Flow<SourceLocationEntity?>

    @Query("SELECT * FROM source_locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<SourceLocationEntity>>

    @Query("DELETE FROM source_locations")
    suspend fun clearAll()
}
