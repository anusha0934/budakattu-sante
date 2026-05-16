package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindmatrix.budakattusante.data.local.entity.InventoryBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryBatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: InventoryBatchEntity)

    @Query("SELECT * FROM inventory_batch ORDER BY timestamp DESC")
    fun getAllBatches(): Flow<List<InventoryBatchEntity>>

    @Query("SELECT * FROM inventory_batch WHERE batchId = :id")
    suspend fun getBatchById(id: String): InventoryBatchEntity?

    @Query("SELECT * FROM inventory_batch WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedBatches(): List<InventoryBatchEntity>

    @Query("SELECT COUNT(*) FROM inventory_batch WHERE isSynced = 0")
    fun getUnsyncedCountFlow(): Flow<Int>

    @Query("UPDATE inventory_batch SET isSynced = 1, cloudImageUrl = :url WHERE batchId = :id")
    suspend fun markAsSynced(id: String, url: String)

    @Query("UPDATE inventory_batch SET processingStatus = :status WHERE batchId = :id")
    suspend fun updateBatchStatus(id: String, status: String)

    @Query("SELECT * FROM inventory_batch WHERE familyId = :familyId ORDER BY timestamp DESC")
    fun getBatchesByFamily(familyId: String): Flow<List<InventoryBatchEntity>>

    @Query("SELECT SUM(quantityKg) FROM inventory_batch WHERE productName = :name")
    suspend fun getTotalStockForProduct(name: String): Double?

    @Delete
    suspend fun deleteBatch(batch: InventoryBatchEntity)
}
