package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.budakattusante.data.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text, isUser = true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val response = aiRepository.askAi(text)
                val aiMessage = ChatMessage(response, isUser = false)
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get response: ${e.message}"
                )
            }
        }
    }

    fun clearChat() {
        _uiState.value = ChatUiState()
    }
}
