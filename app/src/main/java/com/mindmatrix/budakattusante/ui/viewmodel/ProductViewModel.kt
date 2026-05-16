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
    private val _message = MutableStateFlow<String?>(null)
    private val _smartAiRecommendations = MutableStateFlow("Finding the best forest products for you...")

    /**
     * Requirement 10 & 16: Combine multiple data streams into a single UI state.
     * Fixed type inference issues for production stability.
     */
    val uiState: StateFlow<ProductUiState> = combine(
        listOf(
            repository.products,
            repository.localSupplyLogs,
            repository.pendingSyncCount,
            repository.wishlistItems,
            _searchQuery,
            _selectedCategory,
            _loading,
            _message,
            _smartAiRecommendations
        )
    ) { array ->
        val products = array[0] as List<Product>
        val logs = array[1] as List<SupplyLog>
        val syncCount = array[2] as Int
        val wishlist = array[3] as List<WishlistEntity>
        val query = array[4] as String
        val category = array[5] as String?
        val loading = array[6] as Boolean
        val message = array[7] as String?
        val aiRecs = array[8] as String

        val filtered = products.filter { product ->
            val matchesQuery = query.isBlank() || 
                               product.name.contains(query, ignoreCase = true) || 
                               product.category.contains(query, ignoreCase = true) ||
                               product.familyName.contains(query, ignoreCase = true) ||
                               product.village.contains(query, ignoreCase = true)
            val matchesCategory = category == null || product.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }

        ProductUiState(
            catalog = products,
            filteredProducts = filtered,
            localProducts = products.filter { it.isLocalPendingSync },
            supplyLogs = logs,
            recommendations = products.filter { it.rating >= 4.5 && !it.isPreOrder }.take(5),
            smartAiRecommendations = aiRecs,
            upcomingHarvests = products.filter { it.isPreOrder },
            wishlist = wishlist,
            pendingSyncCount = syncCount,
            loading = loading,
            message = message,
            searchQuery = query,
            selectedCategory = category
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

    /**
     * Requirement 5 & 8: GenAI smart recommendations based on current catalog.
     */
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

    fun addBatch(batch: InventoryBatchEntity) {
        viewModelScope.launch {
            try {
                repository.addInventoryBatch(batch)
                _message.value = "Harvest logged offline. Will sync automatically."
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
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
