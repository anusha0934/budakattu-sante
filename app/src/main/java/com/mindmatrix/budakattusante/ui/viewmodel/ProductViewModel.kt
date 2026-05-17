package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.budakattusante.data.local.entity.InventoryBatchEntity
import com.mindmatrix.budakattusante.data.local.entity.ProductHistoryEntity
import com.mindmatrix.budakattusante.data.local.entity.ReviewEntity
import com.mindmatrix.budakattusante.data.local.entity.WishlistEntity
import com.mindmatrix.budakattusante.data.model.Artisan
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.data.model.SupplyLog
import com.mindmatrix.budakattusante.data.repository.AiRepository
import com.mindmatrix.budakattusante.data.repository.ProductRepository
import com.mindmatrix.budakattusante.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProductUiState(
    val catalog: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val localProducts: List<Product> = emptyList(),
    val supplyLogs: List<SupplyLog> = emptyList(),
    val recommendations: List<Product> = emptyList(),
    val smartAiRecommendations: String = "Finding the best forest products for you...",
    val upcomingHarvests: List<Product> = emptyList(),
    val wishlist: List<WishlistEntity> = emptyList(),
    val pendingSyncCount: Int = 0,
    val loading: Boolean = false,
    val isPublishing: Boolean = false,
    val message: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String? = null
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    val repository: ProductRepository,
    private val reviewRepository: ReviewRepository,
    private val aiRepository: AiRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _loading = MutableStateFlow(false)
    private val _publishing = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)
    private val _smartAiRecommendations = MutableStateFlow("Finding the best forest products for you...")

    private data class CombinedData1(
        val products: List<Product>,
        val logs: List<SupplyLog>,
        val syncCount: Int,
        val wishlist: List<WishlistEntity>
    )

    private data class CombinedData2(
        val query: String,
        val category: String?,
        val loading: Boolean,
        val publishing: Boolean,
        val message: String?,
        val aiRecs: String
    )

    val uiState: StateFlow<ProductUiState> = combine(
        combine(
            repository.products,
            repository.localSupplyLogs,
            repository.pendingSyncCount,
            repository.wishlistItems
        ) { products, logs, syncCount, wishlist ->
            CombinedData1(products, logs, syncCount, wishlist)
        },
        combine(
            combine(_searchQuery, _selectedCategory, _loading) { q, c, l -> Triple(q, c, l) },
            combine(_publishing, _message, _smartAiRecommendations) { p, m, a -> Triple(p, m, a) }
        ) { t1, t2 ->
            CombinedData2(
                query = t1.first,
                category = t1.second,
                loading = t1.third,
                publishing = t2.first,
                message = t2.second,
                aiRecs = t2.third
            )
        }
    ) { d1, d2 ->
        val filtered = d1.products.filter { product ->
            val matchesQuery = d2.query.isBlank() || 
                               product.name.contains(d2.query, ignoreCase = true) || 
                               product.category.contains(d2.query, ignoreCase = true) ||
                               product.familyName.contains(d2.query, ignoreCase = true) ||
                               product.village.contains(d2.query, ignoreCase = true)
            val matchesCategory = d2.category == null || product.category.equals(d2.category, ignoreCase = true)
            matchesQuery && matchesCategory
        }

        ProductUiState(
            catalog = d1.products,
            filteredProducts = filtered,
            localProducts = d1.products.filter { it.isLocalPendingSync },
            supplyLogs = d1.logs,
            recommendations = d1.products.filter { it.rating >= 4.5 && !it.isPreOrder }.take(5),
            smartAiRecommendations = d2.aiRecs,
            upcomingHarvests = d1.products.filter { it.isPreOrder },
            wishlist = d1.wishlist,
            pendingSyncCount = d1.syncCount,
            loading = d2.loading,
            isPublishing = d2.publishing,
            message = d2.message,
            searchQuery = d2.query,
            selectedCategory = d2.category
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductUiState()
    )

    init {
        refreshCatalog()
        fetchSmartRecommendations()
    }

    fun fetchSmartRecommendations() {
        viewModelScope.launch {
            val productNames = uiState.value.catalog.map { it.name }.take(10)
            if (productNames.isNotEmpty()) {
                val recs = aiRepository.getSmartRecommendations(listOf("Organic", "Pure", "Sustainable"), productNames)
                _smartAiRecommendations.value = recs
            }
        }
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            _loading.value = true
            repository.refreshCatalog()
            _loading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    suspend fun getArtisan(artisanId: String): Artisan? {
        return repository.getArtisan(artisanId)
    }

    fun getProductHistory(productId: String): Flow<List<ProductHistoryEntity>> {
        return repository.getProductHistory(productId)
    }

    fun getReviews(productId: String): Flow<List<ReviewEntity>> {
        return reviewRepository.getReviewsForProduct(productId)
    }

    fun addReview(productId: String, rating: Double, comment: String) {
        viewModelScope.launch {
            val review = ReviewEntity(
                reviewId = UUID.randomUUID().toString(),
                productId = productId,
                userId = "USER_ID", 
                userName = "Budakattu Buyer", 
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
            reviewRepository.addReview(review)
            _message.value = "Review submitted successfully"
        }
    }

    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val isWishlisted = uiState.value.wishlist.any { it.productId == product.productId }
            if (isWishlisted) {
                repository.removeFromWishlist(product.productId)
                _message.value = "Removed from Wishlist"
            } else {
                repository.addToWishlist(product)
                _message.value = "Saved to Wishlist"
            }
        }
    }

    /**
     * Requirement 7: Add harvest batch.
     * Uses _publishing state to avoid being blocked by _loading (catalog refresh).
     */
    suspend fun addBatch(batch: InventoryBatchEntity): Boolean {
        _publishing.value = true
        return try {
            repository.addInventoryBatch(batch)
            _message.value = "Harvest logged successfully! Syncing to cloud..."
            true
        } catch (e: Exception) {
            _message.value = "Error: ${e.message}"
            false
        } finally {
            _publishing.value = false
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            _message.value = "Product removed."
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
