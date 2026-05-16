package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.SyncMetadataEntity

@Dao
interface SyncMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: SyncMetadataEntity)

    @Query("SELECT lastSyncTimestamp FROM sync_metadata WHERE collectionName = :collectionName")
    suspend fun getLastSyncTimestamp(collectionName: String): Long?

    @Query("UPDATE sync_metadata SET lastSyncTimestamp = :timestamp WHERE collectionName = :collectionName")
    suspend fun updateLastSyncTimestamp(collectionName: String, timestamp: Long)
}
