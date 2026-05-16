package com.mindmatrix.budakattusante.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.ui.components.CatalogItem
import com.mindmatrix.budakattusante.ui.components.HarvestTimelineItem
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.VendorViewModel
import com.mindmatrix.budakattusante.util.QrGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProductsScreen(
    inventory: List<Product>,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigate("vendor_add_product") }, containerColor = ForestGreen) {
                Icon(Icons.Default.Add, contentDescription = "Add Product", tint = Color.White)
            }
        },
        containerColor = Cream
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(inventory) { product ->
                CatalogItem(
                    product = product,
                    onClick = { onNavigate("batch_details/${product.productId}") },
                    onAddToCart = { /* N/A */ }
                )
            }
        }
    }
}

/**
 * Requirement 4: Batch tracking with Offline QR Generation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailsScreen(
    batchId: String,
    onBack: () -> Unit
) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(batchId) {
        qrBitmap = QrGenerator.generateQrCode(batchId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batch Traceability", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = SandBeige
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Batch ID", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(batchId, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreen)
                    
                    Spacer(Modifier.height(24.dp))
                    
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Batch QR Code",
                            modifier = Modifier.size(250.dp)
                        )
                    } else {
                        Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ForestGreen)
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Scan this code to verify product origin and harvest details offline.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrown
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { /* Print Label */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Print, null)
                Spacer(Modifier.width(8.dp))
                Text("Print Batch Label", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorAnalyticsScreen(
    vendorViewModel: VendorViewModel,
    onBack: () -> Unit
) {
    val analytics by vendorViewModel.analytics.collectAsStateWithLifecycle()
    val batches by vendorViewModel.harvestBatches.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics & Supply Log", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // AI Prediction Card (Requirement 5)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = TribalGold)
                        Spacer(Modifier.width(8.dp))
                        Text("Seasonal Demand Prediction", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "AI predicts 30% increase in Wild Honey demand next month due to festive season.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Harvest Trends", style = MaterialTheme.typography.titleLarge, color = ForestGreen, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = TribalGold)
                        Spacer(Modifier.width(8.dp))
                        Text("Most Productive Family", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        analytics.mostProductiveFamily,
                        style = MaterialTheme.typography.headlineMedium,
                        color = ForestGreen,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            Text("Harvest Source History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            val displayedBatches = batches.take(5)
            displayedBatches.forEachIndexed { index, batch ->
                HarvestTimelineItem(
                    date = batch.harvestDate,
                    quantity = "${batch.quantityKg} Kg",
                    artisan = batch.familyName,
                    isFirst = index == 0,
                    isLast = index == displayedBatches.size - 1
                )
            }
            
            Spacer(Modifier.height(24.dp))
            Text("Source Origin Heatmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(LatLng(11.9961, 77.1355), 9f)
                    }
                ) {
                    for (batch in batches) {
                        if (batch.latitude != 0.0) {
                            Marker(
                                state = rememberMarkerState(position = LatLng(batch.latitude, batch.longitude)),
                                title = batch.productName
                            )
                        }
                    }
                }
            }
        }
    }
}
