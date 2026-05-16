package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mindmatrix.budakattusante.data.local.entity.InventoryBatchEntity
import com.mindmatrix.budakattusante.data.local.entity.NotificationEntity
import com.mindmatrix.budakattusante.data.model.Artisan
import com.mindmatrix.budakattusante.data.model.BusinessDetails
import com.mindmatrix.budakattusante.data.model.PaymentRecord
import com.mindmatrix.budakattusante.data.repository.*
import com.mindmatrix.budakattusante.data.local.dao.NotificationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class VendorAnalytics(
    val mostProductiveFamily: String = "N/A",
    val seasonalTrends: Map<String, Double> = emptyMap(),
    val totalArtisans: Int = 0,
    val activeBatches: Int = 0,
    val totalTribalEarnings: Double = 0.0,
    val mspProtectedRevenue: Double = 0.0,
    val fairTradePremiumGenerated: Double = 0.0,
    val topProducts: List<Pair<String, Double>> = emptyList()
)

data class SyncStatus(
    val pendingUploads: Int = 0,
    val lastSyncedTime: String = "Never",
    val isSyncing: Boolean = false
)

@HiltViewModel
class VendorViewModel @Inject constructor(
    private val repository: VendorRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val draftRepository: DraftRepository,
    private val aiRepository: AiRepository,
    private val notificationDao: NotificationDao,
    private val gson: Gson
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _lastSynced = MutableStateFlow("Never")
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _aiDescription = MutableStateFlow("")
    val aiDescription: StateFlow<String> = _aiDescription.asStateFlow()

    private val _sellerGuidance = MutableStateFlow<String?>(null)
    val sellerGuidance: StateFlow<String?> = _sellerGuidance.asStateFlow()

    val businessDetails: StateFlow<BusinessDetails> = repository.getBusinessDetailsFlow("")
        .map { it ?: BusinessDetails() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessDetails())

    val artisans: StateFlow<List<Artisan>> = productRepository.localArtisans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val harvestBatches: StateFlow<List<InventoryBatchEntity>> = productRepository.localBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncStatus: StateFlow<SyncStatus> = combine(
        productRepository.pendingSyncCount,
        _isSyncing,
        _lastSynced
    ) { pending, syncing, lastTime ->
        SyncStatus(
            pendingUploads = pending,
            lastSyncedTime = lastTime,
            isSyncing = syncing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    val analytics: StateFlow<VendorAnalytics> = productRepository.products
        .map { products -> 
            checkLowStockAlerts(products)
            calculateAnalytics(products) 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VendorAnalytics())

    val walletBalance = MutableStateFlow(0.0)
    val monthlyEarnings = MutableStateFlow(0.0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadVendorData(vendorId: String) {
        if (vendorId.isBlank()) return
        viewModelScope.launch {
            repository.getBusinessDetails(vendorId)
            val records = repository.getPaymentRecords(vendorId)
            calculateStats(records)
        }
    }

    /**
     * Requirement 5: AI Seller Guidance & Seasonal Prediction.
     */
    fun getSellerGuidance(productName: String) {
        viewModelScope.launch {
            val month = SimpleDateFormat("MMMM", Locale.US).format(Date())
            val guidance = aiRepository.predictSeasonalDemand(productName, month)
            _sellerGuidance.value = guidance
        }
    }

    /**
     * Requirement 5: AI product description generation.
     */
    fun generateAiDescription(name: String, category: String, region: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val desc = aiRepository.generateProductDescription(name, category, region)
            _aiDescription.value = desc
            _isLoading.value = false
        }
    }

    /**
     * Requirement 14: Draft saving for offline-first usage.
     */
    fun saveProductDraft(userId: String, batch: InventoryBatchEntity) {
        viewModelScope.launch {
            draftRepository.saveDraft(userId, "PRODUCT_HARVEST", gson.toJson(batch))
        }
    }

    suspend fun getProductDraft(userId: String): InventoryBatchEntity? {
        return draftRepository.getDraft(userId, "PRODUCT_HARVEST")?.let {
            runCatching { gson.fromJson(it, InventoryBatchEntity::class.java) }.getOrNull()
        }
    }

    fun clearProductDraft(userId: String) {
        viewModelScope.launch {
            draftRepository.clearDraft(userId, "PRODUCT_HARVEST")
        }
    }

    private fun calculateAnalytics(products: List<com.mindmatrix.budakattusante.data.model.Product>): VendorAnalytics {
        if (products.isEmpty()) return VendorAnalytics()

        val familyProductivity = products.groupBy { it.familyName }
            .mapValues { it.value.sumOf { p -> p.availableKg } }
        val mostProductive = familyProductivity.maxByOrNull { it.value }?.key ?: "N/A"

        val tribalEarnings = products.sumOf { it.pricePerKg * it.availableKg * 0.8 }
        val mspRevenue = products.filter { it.mspPrice > 0 }.sumOf { it.pricePerKg * it.availableKg }
        val fairTradePremium = products.sumOf { (it.pricePerKg - it.marketPrice).coerceAtLeast(0.0) * it.availableKg }
        
        return VendorAnalytics(
            mostProductiveFamily = mostProductive,
            totalArtisans = familyProductivity.size,
            activeBatches = products.size,
            totalTribalEarnings = tribalEarnings,
            mspProtectedRevenue = mspRevenue,
            fairTradePremiumGenerated = fairTradePremium
        )
    }

    /**
     * Requirement 16 & 18: Low stock alerts & Notifications.
     */
    private fun checkLowStockAlerts(products: List<com.mindmatrix.budakattusante.data.model.Product>) {
        viewModelScope.launch {
            products.filter { it.availableKg > 0 && it.availableKg < 5.0 && !it.isPreOrder }.forEach { product ->
                val notification = NotificationEntity(
                    id = "LOW_STOCK_${product.productId}_${System.currentTimeMillis() / 3600000}",
                    title = "Low Stock Alert: ${product.name}",
                    message = "Only ${product.availableKg}kg left. Coordinate with family ${product.familyName} for fresh harvest.",
                    type = "LOW_STOCK",
                    timestamp = System.currentTimeMillis()
                )
                notificationDao.insertNotification(notification)
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                productRepository.refreshCatalog()
                _lastSynced.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                _message.value = "Sync completed successfully"
            } catch (e: Exception) {
                _message.value = "Sync failed: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private fun calculateStats(records: List<PaymentRecord>) {
        var balance = 0.0
        var monthly = 0.0
        val monthFormat = SimpleDateFormat("MM-yyyy", Locale.US)
        val currentMonth = monthFormat.format(Date())

        records.forEach { record ->
            if (record.status == "SUCCESS") {
                if (record.type == "CREDIT") {
                    balance += record.amount
                    val recordMonth = monthFormat.format(Date(record.timestamp))
                    if (recordMonth == currentMonth) {
                        monthly += record.amount
                    }
                } else if (record.type == "DEBIT") {
                    balance -= record.amount
                }
            }
        }
        walletBalance.value = balance
        monthlyEarnings.value = monthly
    }

    fun saveBusinessDetails(details: BusinessDetails, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.saveBusinessDetails(details)
                _message.value = "Business details updated"
                onSuccess()
            } catch (e: Exception) {
                _message.value = "Failed to update details"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
