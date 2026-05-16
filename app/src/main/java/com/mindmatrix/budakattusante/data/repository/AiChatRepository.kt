package com.mindmatrix.budakattusante.data.repository

import com.mindmatrix.budakattusante.data.local.dao.AiChatDao
import com.mindmatrix.budakattusante.data.local.entity.AiChatEntity
import kotlinx.coroutines.flow.Flow

class AiChatRepository(
    private val aiChatDao: AiChatDao,
    private val aiRepository: AiRepository
) {
    val chatHistory: Flow<List<AiChatEntity>> = aiChatDao.getAllChats()

    suspend fun askAi(question: String): String {
        val answer = aiRepository.askAi(question)
        aiChatDao.insertChat(AiChatEntity(question = question, answer = answer))
        return answer
    }

    suspend fun clearHistory() {
        aiChatDao.clearHistory()
    }
}
