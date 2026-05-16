package com.mindmatrix.budakattusante.data.repository

import com.mindmatrix.budakattusante.data.local.dao.DraftDao
import com.mindmatrix.budakattusante.data.local.entity.DraftEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DraftRepository @Inject constructor(
    private val draftDao: DraftDao
) {
    /**
     * Requirement 14: Auto-save forms.
     * Saves a partial form state locally to prevent data loss in forest areas.
     */
    suspend fun saveDraft(userId: String, type: String, jsonData: String) {
        val draft = DraftEntity(
            draftId = "${type}_${userId}",
            userId = userId,
            formType = type,
            jsonData = jsonData
        )
        draftDao.saveDraft(draft)
    }

    suspend fun getDraft(userId: String, type: String): String? {
        return draftDao.getDraft(userId, type)?.jsonData
    }

    fun getAllDrafts(userId: String): Flow<List<DraftEntity>> {
        return draftDao.getAllDrafts(userId)
    }

    suspend fun clearDraft(userId: String, type: String) {
        draftDao.deleteDraftByType(userId, type)
    }
}
