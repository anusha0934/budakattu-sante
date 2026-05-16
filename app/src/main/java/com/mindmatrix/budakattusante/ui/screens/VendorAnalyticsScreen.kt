package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mindmatrix.budakattusante.ui.components.HarvestTimelineItem
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.VendorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorAnalyticsScreen(
    vendorViewModel: VendorViewModel,
    onBack: () -> Unit
) {
    val analytics by vendorViewModel.analytics.collectAsStateWithLifecycle()
    val batches by vendorViewModel.harvestBatches.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(scrollState)
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

            Text("Sales Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreen)
            Spacer(Modifier.height(16.dp))
            
            // Stats Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AnalyticsStatCard("Total Revenue", "₹45,200", Modifier.weight(1f))
                AnalyticsStatCard("Orders", "128", Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AnalyticsStatCard("Avg. Order", "₹353", Modifier.weight(1f))
                AnalyticsStatCard("Growth", "+12%", Modifier.weight(1f), Color(0xFF2E7D32))
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

            Spacer(Modifier.height(32.dp))
            
            Text("Popular Products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreen)
            Spacer(Modifier.height(16.dp))
            
            PopularProductItem("Wild Honey", "45 Kg sold", "₹22,500")
            PopularProductItem("Bamboo Crafts", "12 units sold", "₹8,400")
            PopularProductItem("Tribal Grains", "80 Kg sold", "₹6,200")

            Spacer(Modifier.height(32.dp))
            
            // Placeholder for Chart
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(48.dp), tint = ForestGreen.copy(0.2f))
                        Text("Weekly Sales Chart", color = Color.Gray)
                        Text("(Visual Analytics coming soon)", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = ForestGreen) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = valueColor)
        }
    }
}

@Composable
fun PopularProductItem(name: String, stat: String, revenue: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold)
                Text(stat, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(revenue, fontWeight = FontWeight.Black, color = ForestGreen)
        }
    }
}
