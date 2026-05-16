package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mindmatrix.budakattusante.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncItem: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue ORDER BY priority DESC, timestamp ASC")
    fun getAllSyncItems(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue ORDER BY priority DESC, timestamp ASC")
    suspend fun getPendingItems(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE entityType = :type ORDER BY priority DESC, timestamp ASC")
    suspend fun getPendingItemsByType(type: String): List<SyncQueueEntity>

    @Update
    suspend fun update(syncItem: SyncQueueEntity)

    @Delete
    suspend fun delete(syncItem: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getSyncQueueCount(): Flow<Int>

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}
