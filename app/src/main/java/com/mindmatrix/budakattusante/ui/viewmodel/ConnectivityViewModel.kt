package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.budakattusante.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    val connectivityStatus: StateFlow<ConnectivityObserver.Status> = 
        connectivityObserver.observe()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ConnectivityObserver.Status.Unavailable
            )
}
