package com.mindmatrix.budakattusante.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mindmatrix.budakattusante.data.local.entity.ReviewEntity
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.data.model.Artisan
import com.mindmatrix.budakattusante.ui.components.*
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.ProductViewModel
import com.mindmatrix.budakattusante.ui.viewmodel.VoiceViewModel
import kotlinx.coroutines.delay

/**
 * Requirement 1, 3, 5, 10: Production-ready Product Details with Traceability, Reviews, and Wishlist.
 * Enhanced with AI Audio Descriptions (Requirement 9).
 */
@Composable
fun ProductDetailScreen(
    product: Product,
    onBack: () -> Unit,
    onNavigateToConfirm: (Double, Boolean) -> Unit,
    productViewModel: ProductViewModel,
    voiceViewModel: VoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var artisan by remember { mutableStateOf<Artisan?>(null) }
    val productHistory by productViewModel.getProductHistory(product.productId).collectAsState(initial = emptyList())
    val reviews by productViewModel.getReviews(product.productId).collectAsStateWithLifecycle(initialValue = emptyList())
    val uiState by productViewModel.uiState.collectAsStateWithLifecycle()
    val isWishlisted = uiState.wishlist.any { it.productId == product.productId }

    LaunchedEffect(product.artisanId) {
        if (product.artisanId.isNotBlank()) {
            artisan = productViewModel.getArtisan(product.artisanId)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SandBeige)) {
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            // Hero Image with Tribal Overlay
            Box(modifier = Modifier.fillMaxWidth().height(380.dp)) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )
                
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ForestGreen)
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out this tribal product on Budakattu Sante: ${product.name} - ₹${product.pricePerKg.toInt()}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(Icons.Default.Share, "Share", tint = ForestGreen)
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = { productViewModel.toggleWishlist(product) },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(
                                if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                "Wishlist",
                                tint = if (isWishlisted) Color.Red else ForestGreen
                            )
                        }
                    }
                }

                if (product.isFairTradeCertified) {
                    FairTradeBadge(modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp))
                }

                // Batch Summary
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, null, tint = TribalGold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Batch: ${product.batchId.ifBlank { "TRIBAL-HVT-2026" }}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // Pre-Order Timer
                if (product.isPreOrder) {
                    HarvestCountdown(product.expectedHarvestDate)
                    Spacer(Modifier.height(24.dp))
                }

                // Header Info
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.headlineLarge, color = ForestGreen, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = TribalGold, modifier = Modifier.size(16.dp))
                            Text(" ${product.rating} (${reviews.size} reviews)", style = MaterialTheme.typography.bodyMedium, color = EarthBrown)
                        }
                    }
                    Text(
                        "₹${product.pricePerKg.toInt()}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = EarthBrown,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(24.dp))
                
                if (product.isPreOrder) {
                    StockProgress(product.preorderCount, product.preorderStock)
                    Spacer(Modifier.height(24.dp))
                }

                TraceabilitySection(product)
                Spacer(Modifier.height(24.dp))
                
                FairTradePricingSection(product)
                Spacer(Modifier.height(24.dp))

                Text("Harvested By", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                Spacer(Modifier.height(12.dp))
                if (artisan != null) {
                    ArtisanCard(artisan = artisan!!)
                } else {
                    ArtisanCard(Artisan(name = product.familyName, tribeName = product.tribeName, villageName = product.village))
                }

                Spacer(Modifier.height(24.dp))

                Text("Collected from Source", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                Text("Region: ${product.forestRegion}", style = MaterialTheme.typography.bodySmall, color = EarthBrown)
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    val location = LatLng(product.locationLat, product.locationLng)
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(location, 11f)
                        },
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false)
                    ) {
                        Marker(state = MarkerState(position = location), title = product.forestRegion)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Product Story", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen, modifier = Modifier.weight(1f))
                    // Audio Description Toggle (Requirement 9)
                    IconButton(onClick = { voiceViewModel.speak(product.description) }) {
                        Icon(Icons.Default.VolumeUp, "Listen to Story", tint = ForestGreen)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(24.dp))

                // Reviews Section
                ReviewSection(reviews = reviews, onAddReview = { rating, comment ->
                    productViewModel.addReview(product.productId, rating, comment)
                })

                Spacer(Modifier.height(24.dp))

                if (productHistory.isNotEmpty()) {
                    Text("Harvest Source History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                    Spacer(Modifier.height(16.dp))
                    productHistory.take(3).forEachIndexed { index, entry ->
                        HarvestTimelineItem(
                            date = entry.harvestDate,
                            quantity = "${entry.quantity} Kg",
                            artisan = entry.artisanName,
                            isFirst = index == 0,
                            isLast = index == productHistory.size - 1 || index == 2
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
                TribalWelfareCard()
                Spacer(Modifier.height(100.dp))
            }
        }

        // Floating Bottom Actions
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            color = Color.White
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Total Price", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹${product.pricePerKg.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = ForestGreen)
                }
                
                Button(
                    onClick = { onNavigateToConfirm(1.0, product.isPreOrder) },
                    modifier = Modifier.height(56.dp).weight(1.5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (product.isPreOrder) AccentOrange else ForestGreen),
                    enabled = !product.isPreOrder || (product.preorderCount < product.preorderStock)
                ) {
                    Icon(if (product.isPreOrder) Icons.Default.CalendarMonth else Icons.Default.ShoppingCart, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (product.isPreOrder) "Pre-Order Now" else "Add to Sante Cart",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewSection(reviews: List<ReviewEntity>, onAddReview: (Double, String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
            TextButton(onClick = { showDialog = true }) {
                Text("Write Review", color = AccentOrange)
            }
        }
        
        if (reviews.isEmpty()) {
            Text("No reviews yet. Be the first to share your experience!", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                reviews.take(3).forEach { review ->
                    ReviewItem(review)
                }
            }
        }
    }

    if (showDialog) {
        AddReviewDialog(onDismiss = { showDialog = false }, onConfirm = onAddReview)
    }
}

@Composable
fun ReviewItem(review: ReviewEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                repeat(5) { index ->
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = if (index < review.rating) TribalGold else Color.LightGray
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(review.comment, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}

@Composable
fun AddReviewDialog(onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var rating by remember { mutableStateOf(5.0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = (index + 1).toDouble() }) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = if (index < rating) TribalGold else Color.LightGray
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your experience") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(rating, comment); onDismiss() }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StockProgress(count: Int, total: Int) {
    Column {
        LinearProgressIndicator(
            progress = { if (total > 0) count.toFloat() / total.toFloat() else 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = AccentOrange,
            trackColor = AccentOrange.copy(alpha = 0.2f)
        )
        Text(
            "Stock reserved: $count/$total",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrown,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
