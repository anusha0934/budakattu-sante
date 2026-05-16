@file:OptIn(ExperimentalMaterial3Api::class)

package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.ui.components.*
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.ProductViewModel
import com.mindmatrix.budakattusante.ui.viewmodel.VoiceViewModel

/**
 * Requirement 1, 6, 15: Production-ready Customer Dashboard.
 * Implements Pagination (Requirement 16), Featured Discovery (Requirement 15), 
 * and AI Voice Navigation (Requirement 11).
 */
@Composable
fun CustomerDashboard(
    productViewModel: ProductViewModel,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onViewOrders: () -> Unit,
    onViewProfile: () -> Unit,
    onViewCategories: () -> Unit,
    onViewCart: () -> Unit,
    onViewMap: () -> Unit,
    onViewNotifications: () -> Unit,
    voiceViewModel: VoiceViewModel,
    userProfileImage: String? = null,
    userName: String = "Mallamma"
) {
    val uiState by productViewModel.uiState.collectAsStateWithLifecycle()
    // Requirement 16: Pagination for performance
    val pagedProducts = productViewModel.repository.getProductsPaged(
        query = uiState.searchQuery,
        category = uiState.selectedCategory
    ).collectAsLazyPagingItems()

    var showVoiceAssistant by remember { mutableStateOf(false) }
    val voiceState by voiceViewModel.voiceState.collectAsStateWithLifecycle()

    LaunchedEffect(voiceState.spokenText) {
        if (voiceState.spokenText.isNotBlank()) {
            productViewModel.updateSearchQuery(voiceState.spokenText)
            showVoiceAssistant = false
        }
    }

    Scaffold(
        bottomBar = {
            BudakattuBottomNav(
                currentScreen = "Home",
                onHome = { /* Already here */ },
                onCategories = onViewCategories,
                onOrders = onViewOrders,
                onCart = onViewCart,
                onProfile = onViewProfile
            )
        },
        containerColor = SandBeige 
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                CustomerHeader(userName, userProfileImage, onViewNotifications, onViewProfile)
            }

            // AI Search Bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                SearchAndVoiceSection(
                    query = uiState.searchQuery,
                    onQueryChange = { productViewModel.updateSearchQuery(it) },
                    onVoiceClick = { showVoiceAssistant = true }
                )
            }

            // AI Recommendations (Requirement 10)
            if (uiState.smartAiRecommendations.isNotBlank() && uiState.searchQuery.isBlank()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    AiRecommendationBanner(uiState.smartAiRecommendations)
                }
            }

            // Requirement 15: Featured Discovery - Upcoming Harvests
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = "Upcoming Harvests", onSeeAll = {})
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.upcomingHarvests) { product ->
                        PreOrderCard(product, onClick = { onProductClick(product) })
                    }
                }
            }

            // Categories Selection
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = "Seasonal Categories", onSeeAll = onViewCategories)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val harvests = listOf("Honey", "Grains", "Crafts", "Tubers", "Fruits")
                    items(harvests) { name ->
                        HarvestChip(name, isSelected = uiState.selectedCategory == name) {
                            productViewModel.selectCategory(name)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Requirement 16: Paged Product List
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = "Handmade for You", onSeeAll = {})
            }

            items(pagedProducts.itemCount) { index ->
                pagedProducts[index]?.let { product ->
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        CatalogItem(
                            product = product, 
                            onClick = { onProductClick(product) },
                            onAddToCart = { if (!product.isPreOrder) onAddToCart(product) else onProductClick(product) }
                        )
                    }
                }
            }

            // Handle Loading and Error states for Paging
            val loadState = pagedProducts.loadState
            when (loadState.append) {
                is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ForestGreen)
                    }
                }
                is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorState(onRetry = { pagedProducts.retry() })
                }
                else -> {}
            }
            
            if (pagedProducts.itemCount == 0 && !uiState.loading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        message = "No forest products found matching your search.",
                        onAction = { productViewModel.updateSearchQuery("") }
                    )
                }
            }
        }
    }

    if (showVoiceAssistant) {
        VoiceAssistantSheet(
            onDismiss = { showVoiceAssistant = false },
            voiceViewModel = voiceViewModel
        )
    }
}

@Composable
fun CustomerHeader(name: String, image: String?, onNotif: () -> Unit, onProfile: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text("Namaskara,", style = MaterialTheme.typography.bodyLarge, color = EarthBrown)
            Text(name, style = MaterialTheme.typography.headlineLarge, color = ForestGreen, fontWeight = FontWeight.Black)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNotif, modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)) {
                Icon(Icons.Outlined.Notifications, "Notifications", tint = ForestGreen)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, ForestGreen, CircleShape)
                    .clickable { onProfile() }
            ) {
                AsyncImage(model = image, contentDescription = "Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
    }
}

@Composable
fun SearchAndVoiceSection(query: String, onQueryChange: (String) -> Unit, onVoiceClick: () -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search forest products...") },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = EarthBrown) },
        trailingIcon = {
            IconButton(onClick = onVoiceClick, modifier = Modifier.background(ForestGreen.copy(alpha = 0.1f), CircleShape)) {
                Icon(Icons.Default.Mic, "Voice Search", tint = ForestGreen)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ForestGreen,
            unfocusedBorderColor = Color.Transparent,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        )
    )
    Spacer(Modifier.height(16.dp))
}

@Composable
fun AiRecommendationBanner(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = ForestGreen.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.1f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = ForestGreen, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = EarthBrown, fontWeight = FontWeight.Medium)
        }
    }
}
