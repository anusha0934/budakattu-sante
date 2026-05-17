@file:OptIn(ExperimentalMaterial3Api::class)

package com.mindmatrix.budakattusante.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.data.model.SupplyLog
import com.mindmatrix.budakattusante.ui.components.*
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.VendorViewModel
import com.mindmatrix.budakattusante.ui.viewmodel.VoiceViewModel
import com.mindmatrix.budakattusante.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Requirement 1 & 4: Production-ready Vendor Dashboard.
 * Includes Stats, Quick Actions, Analytics Preview, Recent Orders, and Low Stock Alerts.
 */
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
                onClick = { onNavigate("vendor_add_product") },
                containerColor = AccentOrange, 
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(20.dp)),
                icon = { Icon(Icons.Default.Add, "Add Product") },
                text = { Text("Log Harvest", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) }
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
                walletBalance = vendorViewModel.walletBalance.collectAsStateWithLifecycle().value, 
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
fun VendorAnalyticsGrid(
    walletBalance: Double,
    tribalEarnings: Double,
    preOrderCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VendorAnalyticsCard(
            title = "Wallet Balance",
            value = "₹${walletBalance.toInt()}",
            growth = "Settled weekly",
            icon = Icons.Default.AccountBalanceWallet,
            gradient = listOf(ForestGreen, ForestGreen.copy(0.8f)),
            modifier = Modifier.weight(1f)
        )

        VendorAnalyticsCard(
            title = "Tribal Earnings",
            value = "₹${tribalEarnings.toInt()}",
            growth = "Direct Impact",
            icon = Icons.Default.VolunteerActivism,
            gradient = listOf(AccentOrange, AccentOrange.copy(0.8f)),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Requirement 7: Product Upload / Publish Harvest Screen.
 * Fully functional with validation, image upload, map coordinates, and voice guidance.
 */
@Composable
fun VendorProductFormScreen(
    vendorViewModel: VendorViewModel,
    productViewModel: ProductViewModel,
    onBack: () -> Unit,
    voiceViewModel: VoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var certificationId by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("B.R. Hills") }
    val harvestDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    
    var isPreOrder by remember { mutableStateOf(false) }
    var preorderStock by remember { mutableStateOf("") }
    var expectedHarvestDate by remember { mutableStateOf("") }
    
    var pickedLocation by remember { mutableStateOf(LatLng(11.9961, 77.1355)) }
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val artisans by vendorViewModel.artisans.collectAsStateWithLifecycle()
    var selectedArtisan by remember { mutableStateOf<Artisan?>(null) }
    var manualFamilyName by remember { mutableStateOf("") }
    var showArtisanPicker by remember { mutableStateOf(false) }

    val vendorId = Firebase.auth.currentUser?.uid ?: ""
    val uiState by productViewModel.uiState.collectAsStateWithLifecycle()
    val isPublishing = uiState.isPublishing

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // Requirement 7: Improved Validation Logic - Made photo optional for better usability
    val missingFields = remember(name, category, village, stock, selectedArtisan, price, isPreOrder, expectedHarvestDate, preorderStock, manualFamilyName) {
        buildList {
            if (name.isBlank()) add("Product Name")
            if (category.isBlank()) add("Category")
            if (selectedArtisan == null && manualFamilyName.isBlank()) add("Artisan Family")
            if (price.isBlank()) add("Price")
            if (stock.isBlank()) add("Stock")
            if (village.isBlank()) add("Village")
            if (isPreOrder) {
                if (expectedHarvestDate.isBlank()) add("Expected Harvest Date")
                if (preorderStock.isBlank()) add("Pre-order limit")
            }
        }
    }
    
    val isValid = missingFields.isEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Fresh Harvest", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { voiceViewModel.speak("Logging new harvest. Please add product name, price, and select the artisan family.") }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, "Help", tint = ForestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(Cream).padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (isPublishing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ForestGreen)
                Text("Publishing harvest to Sante market...", style = MaterialTheme.typography.labelSmall, color = ForestGreen)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(if (selectedImageUri == null) BorderStroke(2.dp, ForestGreen.copy(0.5f)) else BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(24.dp))
                    .clickable(enabled = !isPublishing) { 
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, Modifier.size(48.dp), tint = ForestGreen)
                        Text("Add Harvest Photo (Optional)", color = ForestGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Harvested By (Artisan Family)*", style = MaterialTheme.typography.labelSmall, color = if (selectedArtisan == null && manualFamilyName.isBlank()) Color.Red else Color.Gray)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    onClick = { if (!isPublishing) showArtisanPicker = true }
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, null, tint = EarthBrown)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(selectedArtisan?.name ?: "Select from Collective", fontWeight = FontWeight.Bold, color = if (selectedArtisan == null) Color.Gray else Color.Black)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                    }
                }
                
                if (selectedArtisan == null) {
                    Text("OR Enter Name Manually", style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = manualFamilyName,
                        onValueChange = { manualFamilyName = it },
                        label = { Text("Family/Artisan Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isPublishing,
                        isError = manualFamilyName.isBlank()
                    )
                } else {
                    TextButton(onClick = { selectedArtisan = null }) {
                        Text("Clear selection", color = Color.Red)
                    }
                }
            }

            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Product Name*") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp), 
                enabled = !isPublishing,
                isError = name.isBlank()
            )
            
            OutlinedTextField(
                value = category, 
                onValueChange = { category = it }, 
                label = { Text("Category (e.g. Honey, Grains)*") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp), 
                enabled = !isPublishing,
                isError = category.isBlank()
            )
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Harvest Details & Story") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), minLines = 3, enabled = !isPublishing)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { vendorViewModel.generateAiDescription(name, category, region) }, enabled = !isPublishing && name.isNotBlank()) {
                    Icon(Icons.Default.AutoAwesome, "AI Help", tint = ForestGreen)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price, 
                    onValueChange = { price = it }, 
                    label = { Text("Price/Kg*") }, 
                    modifier = Modifier.weight(1f), 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                    shape = RoundedCornerShape(16.dp), 
                    enabled = !isPublishing,
                    isError = price.isBlank()
                )
                OutlinedTextField(
                    value = stock, 
                    onValueChange = { stock = it }, 
                    label = { Text("Stock (Kg)*") }, 
                    modifier = Modifier.weight(1f), 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                    shape = RoundedCornerShape(16.dp), 
                    enabled = !isPublishing,
                    isError = stock.isBlank()
                )
            }

            // Pre-Order Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isPreOrder) ForestGreen.copy(0.05f) else Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPreOrder, onCheckedChange = { isPreOrder = it }, enabled = !isPublishing)
                        Text("Enable Fair-Trade Pre-Order", fontWeight = FontWeight.Bold, color = ForestGreen)
                    }
                    if (isPreOrder) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = expectedHarvestDate, 
                            onValueChange = { expectedHarvestDate = it }, 
                            label = { Text("Expected Harvest Date (YYYY-MM-DD)*") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            shape = RoundedCornerShape(12.dp),
                            isError = expectedHarvestDate.isBlank()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = preorderStock, 
                            onValueChange = { preorderStock = it }, 
                            label = { Text("Pre-order Reservation Limit (Kg)*") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                            shape = RoundedCornerShape(12.dp),
                            isError = preorderStock.isBlank()
                        )
                    }
                }
            }

            Text("Source & Traceability", style = MaterialTheme.typography.titleMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = certificationId, onValueChange = { certificationId = it }, label = { Text("Certification ID") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), enabled = !isPublishing)
            OutlinedTextField(
                value = village, 
                onValueChange = { village = it }, 
                label = { Text("Village/Podu Name*") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp), 
                enabled = !isPublishing,
                isError = village.isBlank()
            )
            
            // Map Location Picker
            Text("Harvest Location (Tap to pick)*", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ForestGreen.copy(0.2f))
            ) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(pickedLocation, 12f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { pickedLocation = it }
                ) {
                    Marker(state = MarkerState(position = pickedLocation), title = "Source Point")
                }
            }

            if (!isValid && !isPublishing) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Required: ${missingFields.joinToString(", ")}", 
                        color = Color.Red, 
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Button(
                onClick = {
                    if (isValid) {
                        scope.launch {
                            val batchId = "BATCH-${UUID.randomUUID().toString().take(8).uppercase()}"
                            val batch = InventoryBatchEntity(
                                batchId = batchId,
                                vendorId = vendorId,
                                artisanId = selectedArtisan?.artisanId ?: "",
                                productName = name,
                                category = category,
                                quantityKg = stock.toDoubleOrNull() ?: 0.0,
                                pricePerKg = price.toDoubleOrNull() ?: 0.0,
                                mspPrice = (price.toDoubleOrNull() ?: 0.0) * 0.75,
                                certificationId = certificationId,
                                familyId = selectedArtisan?.artisanId ?: UUID.randomUUID().toString(),
                                familyName = selectedArtisan?.name ?: manualFamilyName,
                                sellerPhone = Firebase.auth.currentUser?.phoneNumber ?: "",
                                description = description,
                                harvestDate = harvestDate,
                                village = village,
                                forestRegion = region,
                                latitude = pickedLocation.latitude,
                                longitude = pickedLocation.longitude,
                                localImagePath = selectedImageUri?.toString() ?: "",
                                isPreOrder = isPreOrder,
                                preorderStock = preorderStock.toIntOrNull() ?: 0,
                                expectedHarvestDate = expectedHarvestDate
                            )
                            val success = productViewModel.addBatch(batch)
                            if (success) {
                                voiceViewModel.speak("Harvest details saved successfully.")
                                onBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                enabled = isValid && !isPublishing
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isPreOrder) "Launch Pre-Order" else "Publish Harvest", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showArtisanPicker) {
        ModalBottomSheet(onDismissRequest = { showArtisanPicker = false }) {
            Column(Modifier.padding(24.dp)) {
                Text("Select Artisan Family", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
    
    val filteredLogs = remember(uiState.supplyLogs, searchQuery) {
        uiState.supplyLogs.filter { log ->
            log.productName.contains(searchQuery, ignoreCase = true)
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
            SupplyLogStats(uiState.supplyLogs)

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by product name...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredLogs) { log ->
                    SupplyLogItem(log)
                }
                
                if (filteredLogs.isEmpty()) {
                    item {
                        EmptyState(message = "No harvest logs found", onAction = { productViewModel.refreshCatalog() })
                    }
                }
            }
        }
    }
}

@Composable
fun SupplyLogStats(logs: List<SupplyLog>) {
    val totalQty = logs.sumOf { it.totalQuantityKg }
    val totalEarnings = logs.sumOf { it.earnings }
    
    Row(Modifier.fillMaxWidth().padding(16.dp).background(ForestGreen, RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Collected", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
            Text("${totalQty.toInt()} Kg", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
        VerticalDivider(color = Color.White.copy(0.2f), modifier = Modifier.height(40.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Estimated Earnings", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
            Text("₹${totalEarnings.toInt()}", color = TribalGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun SupplyLogItem(log: SupplyLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = Cream) {
                Icon(Icons.Default.Eco, null, modifier = Modifier.padding(12.dp), tint = ForestGreen)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(log.productName, fontWeight = FontWeight.Bold, color = ForestGreen)
                Text("Harvested: ${log.harvestDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("Family: ${log.familyName}", style = MaterialTheme.typography.bodySmall, color = EarthBrown)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${log.totalQuantityKg.toInt()} Kg", fontWeight = FontWeight.Black)
                Text("₹${log.earnings.toInt()}", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
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

@Composable
fun VendorTopBar(onProfile: () -> Unit, onNotifications: () -> Unit, onSettings: () -> Unit) {
    TopAppBar(
        title = { Image(painter = painterResource(id = R.drawable.budakatu_logo), contentDescription = null, modifier = Modifier.height(36.dp)) },
        actions = {
            IconButton(onClick = onNotifications) { Icon(Icons.Default.NotificationsNone, null, tint = ForestGreen) }
            IconButton(onClick = onProfile) { Icon(Icons.Default.AccountCircle, null, tint = ForestGreen) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
    )
}

@Composable
fun VendorBottomNav(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = currentRoute == "vendor_dashboard", onClick = { onNavigate("vendor_dashboard") }, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Home") })
        NavigationBarItem(selected = currentRoute == "vendor_orders", onClick = { onNavigate("vendor_orders") }, icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) }, label = { Text("Orders") })
        NavigationBarItem(selected = currentRoute == "vendor_products", onClick = { onNavigate("vendor_products") }, icon = { Icon(Icons.Default.Inventory2, null) }, label = { Text("Inventory") })
    }
}

@Composable
fun VendorMarketsBanner(onNavigate: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(24.dp).height(120.dp).clickable { onNavigate("map") }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ForestGreen)) {
        Row(Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tribal Sante Markets", color = TribalGold, style = MaterialTheme.typography.titleMedium)
                Text("Find local artisan collective hubs and upcoming markets", color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
        }
    }
}

@Composable
fun VendorAnalyticsCard(
    title: String,
    value: String,
    growth: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.height(180.dp), shape = RoundedCornerShape(20.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradient)).padding(20.dp)) {
            Column {
                Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(title, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyLarge, fontSize = 12.sp)
                Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text(growth, color = TribalGold, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun VendorHeaderSection(onSync: () -> Unit, profileImageUrl: String, ownerName: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = EarthBrown, shadowElevation = 4.dp) {
            if (profileImageUrl.isNotBlank()) {
                AsyncImage(model = profileImageUrl, contentDescription = "Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(12.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Namaskara, $ownerName", style = MaterialTheme.typography.titleLarge, color = ForestGreen)
            Text("Shop Dashboard", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
        IconButton(onClick = onSync) {
            Icon(Icons.Default.Sync, "Sync Data", tint = ForestGreen)
        }
    }
}

@Composable
fun VendorQuickActionsSection(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text("Quick Actions", style = MaterialTheme.typography.titleLarge, color = ForestGreen)
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            VendorQuickActionButton("Add Product", Icons.Default.Add, AccentOrange) { onNavigate("vendor_add_product") }
            VendorQuickActionButton("Orders", Icons.AutoMirrored.Filled.Assignment, ForestGreen) { onNavigate("vendor_orders") }
            VendorQuickActionButton("Supply Log", Icons.Default.History, EarthBrown) { onNavigate("vendor_supply_log") }
            VendorQuickActionButton("Analytics", Icons.Default.BarChart, ForestGreen) { onNavigate("vendor_analytics") }
        }
    }
}

@Composable
fun VendorQuickActionButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Surface(modifier = Modifier.size(60.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
            Icon(icon, null, tint = color, modifier = Modifier.padding(16.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun VendorRecentOrdersList(orders: List<OrderEntity>, onOrderClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (orders.isEmpty()) {
            Text("No recent orders", color = Color.Gray)
        } else {
            orders.take(3).forEach { order ->
                VendorOrderCardPremium(
                    customer = order.buyerName, 
                    product = order.productName, 
                    amount = "₹${order.totalAmount.toInt()}", 
                    status = order.orderStatus, 
                    onClick = onOrderClick
                )
            }
        }
    }
}

@Composable
fun VendorOrderCardPremium(customer: String, product: String, amount: String, status: String, onClick: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(40.dp), shape = CircleShape, color = Cream) {
                Icon(Icons.Default.Person, null, tint = EarthBrown, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(customer, fontWeight = FontWeight.Bold, color = ForestGreen)
                Text(product, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(amount, fontWeight = FontWeight.Bold, color = ForestGreen)
                StatusChip(status)
            }
        }
    }
}

@Composable
fun VendorInventoryPreviewList(inventory: List<Product>) {
    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(inventory) { product ->
            Card(modifier = Modifier.width(150.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.height(100.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
                    Column(Modifier.padding(12.dp)) {
                        Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, color = ForestGreen)
                        Text(if (product.isPreOrder) "Pre-Order" else "Stock: ${product.availableKg}kg", style = MaterialTheme.typography.labelSmall, color = if (product.isPreOrder) AccentOrange else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun VendorFairTradeAnalyticsSection(inventory: List<Product>, analytics: com.mindmatrix.budakattusante.ui.viewmodel.VendorAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, null, tint = ForestGreen)
                Spacer(Modifier.width(8.dp))
                Text("Collective Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
            }
            Spacer(Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Direct Tribal Share", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹${analytics.totalTribalEarnings.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = EarthBrown)
                    Text("Direct Support", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Certification", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${analytics.activeBatches} Batches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                    Surface(color = ForestGreen.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text("PURE", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = ForestGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VendorSectionHeader(title: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = ForestGreen, fontWeight = FontWeight.Bold)
        TextButton(onClick = onAction) {
            Text("See All", color = AccentOrange, fontWeight = FontWeight.Bold)
        }
    }
}
