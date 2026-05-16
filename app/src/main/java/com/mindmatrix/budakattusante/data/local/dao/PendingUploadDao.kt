package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.PendingUploadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingUploadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(upload: PendingUploadEntity)

    @Query("SELECT * FROM pending_uploads WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY timestamp ASC")
    fun getPendingUploadsFlow(): Flow<List<PendingUploadEntity>>

    @Query("SELECT * FROM pending_uploads WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY timestamp ASC")
    suspend fun getPendingUploads(): List<PendingUploadEntity>

    @Update
    suspend fun update(upload: PendingUploadEntity)

    @Query("UPDATE pending_uploads SET status = :status WHERE uploadId = :id")
    suspend fun updateStatus(id: String, status: String)

    @Delete
    suspend fun delete(upload: PendingUploadEntity)

    @Query("DELETE FROM pending_uploads WHERE uploadId = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM pending_uploads WHERE status = 'PENDING' OR status = 'FAILED'")
    fun getPendingCount(): Flow<Int>
}
