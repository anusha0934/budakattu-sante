@file:OptIn(ExperimentalMaterial3Api::class)

package com.mindmatrix.budakattusante.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import com.mindmatrix.budakattusante.R
import com.mindmatrix.budakattusante.data.local.entity.InventoryBatchEntity
import com.mindmatrix.budakattusante.data.local.entity.OrderEntity
import com.mindmatrix.budakattusante.data.model.Artisan
import com.mindmatrix.budakattusante.data.model.BusinessDetails
import com.mindmatrix.budakattusante.data.model.PaymentRecord
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.ui.components.*
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.VendorViewModel
import com.mindmatrix.budakattusante.ui.viewmodel.VoiceViewModel
import com.mindmatrix.budakattusante.ui.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun VendorDashboard(
    inventory: List<Product>,
    orders: List<OrderEntity>,
    onSync: () -> Unit,
    onNavigate: (String) -> Unit,
    voiceViewModel: VoiceViewModel,
    vendorViewModel: VendorViewModel
) {
    val vendorId = Firebase.auth.currentUser?.uid ?: ""
    val businessDetails by vendorViewModel.businessDetails.collectAsStateWithLifecycle()
    val analytics by vendorViewModel.analytics.collectAsStateWithLifecycle()
    val syncStatus by vendorViewModel.syncStatus.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val preOrders = orders.filter { it.isPreOrder }
    val regularOrders = orders.filter { !it.isPreOrder }

    LaunchedEffect(vendorId) {
        if (vendorId.isNotBlank()) {
            vendorViewModel.loadVendorData(vendorId)
        }
    }

    Scaffold(
        topBar = {
            VendorTopBar(
                onProfile = { onNavigate("vendor_profile") },
                onNotifications = { onNavigate("vendor_notifications") },
                onSettings = { /* Open settings */ }
            )
        },
        bottomBar = {
            VendorBottomNav(
                currentRoute = "vendor_dashboard",
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate("tribal_ai") },
                containerColor = AccentOrange, 
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(20.dp)),
                icon = { Icon(Icons.Default.AutoAwesome, "AI Assistant") },
                text = { Text("Ask Tribal AI", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Cream) 
                .verticalScroll(scrollState)
        ) {
            VendorHeaderSection(
                onSync = { vendorViewModel.triggerSync() },
                profileImageUrl = businessDetails.profileImageUrl,
                ownerName = if (businessDetails.ownerName.isNotBlank()) businessDetails.ownerName else "Artisan"
            )

            SyncStatusBadge(
                pendingCount = syncStatus.pendingUploads,
                isSyncing = syncStatus.isSyncing
            )

            VendorAnalyticsGrid(
                walletBalance = 0.0, 
                tribalEarnings = analytics.totalTribalEarnings,
                preOrderCount = preOrders.size
            )

            VendorQuickActionsSection(onNavigate)

            VendorFairTradeAnalyticsSection(inventory, analytics)

            if (preOrders.isNotEmpty()) {
                VendorSectionHeader("Upcoming Harvest Pre-Orders", onAction = { onNavigate("vendor_orders") })
                VendorRecentOrdersList(orders = preOrders, onOrderClick = { onNavigate("vendor_orders") })
                Spacer(Modifier.height(16.dp))
            }

            VendorSectionHeader("Regular Orders", onAction = { onNavigate("vendor_orders") })
            VendorRecentOrdersList(orders = regularOrders, onOrderClick = { onNavigate("vendor_orders") })

            VendorSectionHeader("Low Stock Alerts", onAction = { onNavigate("vendor_products") })
            VendorInventoryPreviewList(inventory.filter { it.availableKg < 5 && !it.isPreOrder })

            VendorSectionHeader("Current Inventory", onAction = { onNavigate("vendor_products") })
            VendorInventoryPreviewList(inventory.filter { !it.isPreOrder }.take(5))

            VendorMarketsBanner(onNavigate)

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun VendorProductFormScreen(
    vendorViewModel: VendorViewModel,
    productViewModel: ProductViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var msp by remember { mutableStateOf("") }
    var marketPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPreOrder by remember { mutableStateOf(false) }
    var harvestDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var village by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("B.R. Hills") }
    
    val artisans by vendorViewModel.artisans.collectAsStateWithLifecycle()
    var selectedArtisan by remember { mutableStateOf<Artisan?>(null) }
    var showArtisanPicker by remember { mutableStateOf(false) }

    val vendorId = Firebase.auth.currentUser?.uid ?: ""
    val isUploading by productViewModel.uiState.map { it.loading }.collectAsState(initial = false)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    val isValid = remember(name, category, village, stock, selectedImageUri, selectedArtisan, price) {
        name.isNotBlank() && category.isNotBlank() && village.isNotBlank() && 
        stock.isNotBlank() && selectedImageUri != null && selectedArtisan != null && price.isNotBlank()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Fresh Harvest", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(Cream).padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (isUploading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ForestGreen)
                Text("Uploading harvest details...", style = MaterialTheme.typography.labelSmall, color = ForestGreen)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(if (selectedImageUri == null) BorderStroke(2.dp, ForestGreen.copy(0.2f)) else BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(24.dp))
                    .clickable(enabled = !isUploading) { 
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, Modifier.size(48.dp), tint = ForestGreen)
                        Text("Add Harvest Photo*", color = ForestGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                onClick = { if (!isUploading) showArtisanPicker = true }
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, null, tint = EarthBrown)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Sourced By*", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(selectedArtisan?.name ?: "Select Tribal Artisan/Family", fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                }
            }

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), enabled = !isUploading)
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), enabled = !isUploading)
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Harvest Details") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), minLines = 3, enabled = !isUploading)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { vendorViewModel.generateAiDescription(name, category, region) }, enabled = !isUploading && name.isNotBlank()) {
                    Icon(Icons.Default.AutoAwesome, "AI Generate", tint = ForestGreen)
                }
            }
            
            val aiDesc by vendorViewModel.aiDescription.collectAsStateWithLifecycle()
            if (aiDesc.isNotBlank() && aiDesc != description) {
                Card(colors = CardDefaults.cardColors(containerColor = TribalGold.copy(0.1f)), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("AI Suggested Description:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(aiDesc, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { description = aiDesc }) { Text("Use AI Text", color = ForestGreen) }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price/Kg*") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp), enabled = !isUploading)
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Quantity (Kg)*") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp), enabled = !isUploading)
            }

            Text("Traceability & Origin", style = MaterialTheme.typography.titleMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village/Podu Name*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), enabled = !isUploading)
            
            Button(
                onClick = {
                    if (isValid) {
                        val batchId = "BATCH-${UUID.randomUUID().toString().take(8).uppercase()}"
                        val batch = InventoryBatchEntity(
                            batchId = batchId,
                            vendorId = vendorId,
                            artisanId = selectedArtisan?.artisanId ?: "",
                            productName = name,
                            category = category,
                            quantityKg = stock.toDoubleOrNull() ?: 0.0,
                            pricePerKg = price.toDoubleOrNull() ?: 0.0,
                            mspPrice = msp.toDoubleOrNull() ?: 0.0,
                            familyName = selectedArtisan?.familyName ?: "",
                            sellerPhone = Firebase.auth.currentUser?.phoneNumber ?: "",
                            description = description,
                            harvestDate = harvestDate,
                            village = village,
                            forestRegion = region,
                            localImagePath = selectedImageUri?.toString() ?: ""
                        )
                        productViewModel.addBatch(batch)
                        Toast.makeText(context, "Publishing harvest...", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                enabled = isValid && !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Publish Harvest", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    if (showArtisanPicker) {
        ModalBottomSheet(onDismissRequest = { showArtisanPicker = false }) {
            Column(Modifier.padding(24.dp)) {
                Text("Select Tribal Producer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(artisans) { artisan ->
                        Card(modifier = Modifier.fillMaxWidth(), onClick = { selectedArtisan = artisan; showArtisanPicker = false }) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(model = artisan.profileImageUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(artisan.name, fontWeight = FontWeight.Bold)
                                    Text("${artisan.tribeName} • ${artisan.villageName}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SupplyLogScreen(
    productViewModel: ProductViewModel,
    onBack: () -> Unit
) {
    val uiState by productViewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf<String?>(null) }
    
    val filteredLogs = remember(uiState.supplyLogs, searchQuery, selectedMonth) {
        uiState.supplyLogs.filter { log ->
            val matchesSearch = log.productName.contains(searchQuery, ignoreCase = true)
            val matchesMonth = selectedMonth == null || log.harvestDate.contains(selectedMonth!!)
            matchesSearch && matchesMonth
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harvest Supply Log", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Stats Header
            SupplyLogStats(uiState.supplyLogs)

            // Filters
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search product...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                // Simplified Month Picker
                IconButton(onClick = { /* Open month picker */ }) {
                    Icon(Icons.Default.FilterList, null, tint = ForestGreen)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredLogs) { log ->
                    SupplyLogItem(log)
                }
                
                if (filteredLogs.isEmpty()) {
                    item {
                        EmptyState(title = "No harvest logs found", description = "Try changing your search or filter.")
                    }
                }
            }
        }
    }
}

@Composable
fun SupplyLogStats(logs: List<com.mindmatrix.budakattusante.data.model.SupplyLog>) {
    val totalQty = logs.sumOf { it.quantityKg }
    val totalEarnings = logs.sumOf { it.quantityKg * it.pricePerKg }
    
    Row(Modifier.fillMaxWidth().padding(16.dp).background(ForestGreen, RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Harvest", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
            Text("${totalQty.toInt()} Kg", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
        VerticalDivider(color = Color.White.copy(0.2f), modifier = Modifier.height(40.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Earnings", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
            Text("₹${totalEarnings.toInt()}", color = TribalGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun SupplyLogItem(log: com.mindmatrix.budakattusante.data.model.SupplyLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = Cream) {
                Icon(Icons.Default.History, null, modifier = Modifier.padding(12.dp), tint = ForestGreen)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(log.productName, fontWeight = FontWeight.Bold, color = ForestGreen)
                Text("Harvested on: ${log.harvestDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("By: ${log.familyName}", style = MaterialTheme.typography.bodySmall, color = EarthBrown)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${log.quantityKg.toInt()} Kg", fontWeight = FontWeight.Black)
                Text("₹${(log.quantityKg * log.pricePerKg).toInt()}", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                SyncStatusIcon(log.synced)
            }
        }
    }
}

@Composable
fun SyncStatusIcon(synced: Boolean) {
    Icon(
        imageVector = if (synced) Icons.Default.CloudDone else Icons.Default.CloudUpload,
        contentDescription = null,
        tint = if (synced) ForestGreen else AccentOrange,
        modifier = Modifier.size(16.dp)
    )
}

// ... rest of the existing components (VendorTopBar, VendorBottomNav, etc.)
