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
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.ui.components.CatalogItem
import com.mindmatrix.budakattusante.ui.theme.*
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
