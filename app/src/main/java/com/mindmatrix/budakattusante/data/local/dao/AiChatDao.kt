package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mindmatrix.budakattusante.data.local.entity.AiChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatDao {
    @Insert
    suspend fun insertChat(chat: AiChatEntity)

    @Query("SELECT * FROM ai_chats ORDER BY timestamp DESC")
    fun getAllChats(): Flow<List<AiChatEntity>>

    @Query("DELETE FROM ai_chats")
    suspend fun clearHistory()
}
