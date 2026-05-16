package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.budakattusante.data.local.entity.InventoryBatchEntity
import com.mindmatrix.budakattusante.data.repository.ProductRepository
import com.mindmatrix.budakattusante.data.repository.VendorRepository
import com.mindmatrix.budakattusante.data.repository.MspRepository
import com.mindmatrix.budakattusante.data.local.entity.MspEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val pendingBatches: List<InventoryBatchEntity> = emptyList(),
    val mspData: List<MspEntity> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val vendorRepository: VendorRepository,
    private val mspRepository: MspRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AdminUiState> = combine(
        productRepository.localBatches,
        mspRepository.mspData,
        _isLoading,
        _message
    ) { batches, msp, loading, msg ->
        AdminUiState(
            pendingBatches = batches.filter { it.processingStatus == "PENDING" },
            mspData = msp,
            isLoading = loading,
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminUiState())

    fun approveBatch(batchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.approveBatch(batchId)
                _message.value = "Batch approved and listed for buyers."
            } catch (e: Exception) {
                _message.value = "Approval failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectBatch(batchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.rejectBatch(batchId)
                _message.value = "Batch rejected."
            } catch (e: Exception) {
                _message.value = "Rejection failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMsp(msp: MspEntity) {
        viewModelScope.launch {
            mspRepository.updateMspData(listOf(msp))
            _message.value = "MSP updated for ${msp.category}"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
