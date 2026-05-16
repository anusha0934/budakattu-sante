package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindmatrix.budakattusante.data.local.entity.MspEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MspDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMspData(data: List<MspEntity>)

    @Query("SELECT * FROM msp_data")
    fun getAllMspData(): Flow<List<MspEntity>>

    @Query("SELECT * FROM msp_data WHERE category = :category")
    suspend fun getMspForCategory(category: String): MspEntity?
}
