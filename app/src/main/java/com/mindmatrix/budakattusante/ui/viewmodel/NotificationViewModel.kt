package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.budakattusante.data.local.entity.NotificationEntity
import com.mindmatrix.budakattusante.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }
}
