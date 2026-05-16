package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.budakattusante.data.model.Address
import com.mindmatrix.budakattusante.data.repository.AddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val repository: AddressRepository
) : ViewModel() {

    val addresses: StateFlow<List<Address>> = repository.allAddresses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadAddresses() {
        // Handled by the flow
    }

    fun addAddress(address: Address) {
        viewModelScope.launch {
            repository.addAddress(address)
        }
    }

    fun deleteAddress(address: Address) {
        viewModelScope.launch {
            repository.deleteAddress(address)
        }
    }

    fun setDefaultAddress(id: String) {
        viewModelScope.launch {
            repository.setDefaultAddress(id)
        }
    }
}
