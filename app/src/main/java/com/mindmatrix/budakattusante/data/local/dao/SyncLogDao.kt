package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.SyncLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncLogDao {
    @Insert
    suspend fun insertLog(log: SyncLogEntity)

    @Query("SELECT * FROM sync_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<SyncLogEntity>>

    @Query("DELETE FROM sync_logs WHERE timestamp < :threshold")
    suspend fun deleteOldLogs(threshold: Long)
}
