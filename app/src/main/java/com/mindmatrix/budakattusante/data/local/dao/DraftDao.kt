package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import com.mindmatrix.budakattusante.data.local.entity.DraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: DraftEntity)

    @Query("SELECT * FROM form_drafts WHERE userId = :userId AND formType = :type")
    suspend fun getDraft(userId: String, type: String): DraftEntity?

    @Query("SELECT * FROM form_drafts WHERE userId = :userId")
    fun getAllDrafts(userId: String): Flow<List<DraftEntity>>

    @Query("DELETE FROM form_drafts WHERE draftId = :draftId")
    suspend fun deleteDraft(draftId: String)

    @Query("DELETE FROM form_drafts WHERE userId = :userId AND formType = :type")
    suspend fun deleteDraftByType(userId: String, type: String)
}
