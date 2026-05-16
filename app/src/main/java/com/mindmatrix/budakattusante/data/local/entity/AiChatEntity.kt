package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ai_chats")
data class AiChatEntity(
    @PrimaryKey val chatId: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)
