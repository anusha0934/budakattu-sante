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

            // Requirement 12: Dashboard Sync Indicators
            SyncStatusBadge(
                pendingCount = syncStatus.pendingUploads,
                isSyncing = syncStatus.isSyncing
            )

            VendorAnalyticsGrid(
                walletBalance = 0.0, // Should come from VM
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
fun VendorFairTradeAnalyticsSection(inventory: List<Product>, analytics: com.mindmatrix.budakattusante.ui.viewmodel.VendorAnalytics) {
    val mspProductsCount = inventory.count { it.mspPrice > 0 }
    
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
                Text("Fair-Trade Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
            }
            Spacer(Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Direct Tribal Share", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹${analytics.totalTribalEarnings.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = EarthBrown)
                    Text("Livelihoods supported", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("MSP Protected", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("$mspProductsCount Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                    Surface(color = ForestGreen.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text("Certified", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = ForestGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.3f))
            Spacer(Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Fair-Trade Premium", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("+₹${analytics.fairTradePremiumGenerated.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentOrange)
                }
                Button(
                    onClick = { /* Detailed Analytics */ },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Full Report", style = MaterialTheme.typography.labelLarge)
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

@Composable
fun VendorAnalyticsGrid(walletBalance: Double, tribalEarnings: Double, preOrderCount: Int) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            VendorAnalyticsCard(
                title = "Wallet Balance",
                value = "₹${String.format(Locale.US, "%.0f", walletBalance)}",
                growth = "Available",
                icon = Icons.Default.AccountBalanceWallet,
                gradient = listOf(ForestGreen, ForestGreen.copy(alpha = 0.7f))
            )
        }
        item {
            VendorAnalyticsCard(
                title = "Tribal Payout",
                value = "₹${String.format(Locale.US, "%.0f", tribalEarnings)}",
                growth = "Total Share",
                icon = Icons.Default.VolunteerActivism,
                gradient = listOf(EarthBrown, EarthBrown.copy(alpha = 0.7f))
            )
        }
        item {
            VendorAnalyticsCard(
                title = "Pre-Orders",
                value = "$preOrderCount",
                growth = "Reserved Stock",
                icon = Icons.Default.CalendarToday,
                gradient = listOf(AccentOrange, AccentOrange.copy(alpha = 0.7f))
            )
        }
    }
}

@Composable
fun VendorProductFormScreen(
    vendorViewModel: VendorViewModel,
    onAddProduct: (InventoryBatchEntity) -> Unit, 
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var msp by remember { mutableStateOf("") }
    var marketPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPreOrder by remember { mutableStateOf(false) }
    var harvestDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var preorderStock by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var villageDraft by remember { mutableStateOf("") }
    
    // Artisan Selection
    val artisans by vendorViewModel.artisans.collectAsStateWithLifecycle()
    var selectedArtisan by remember { mutableStateOf<Artisan?>(null) }
    var showArtisanPicker by remember { mutableStateOf(false) }

    val vendorId = Firebase.auth.currentUser?.uid ?: ""

    // Requirement 14: Load draft if exists
    LaunchedEffect(Unit) {
        vendorViewModel.getProductDraft(vendorId)?.let { draft ->
            name = draft.productName
            category = draft.category
            description = draft.description
            price = draft.pricePerKg.toString()
            msp = draft.mspPrice.toString()
            villageDraft = draft.village
            harvestDate = draft.harvestDate
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // Requirement 14: Auto-save on change
    LaunchedEffect(name, category, description, price, msp, villageDraft, stock, harvestDate) {
        val draft = InventoryBatchEntity(
            batchId = "DRAFT",
            productName = name,
            category = category,
            description = description,
            quantityKg = stock.toDoubleOrNull() ?: 0.0,
            pricePerKg = price.toDoubleOrNull() ?: 0.0,
            mspPrice = msp.toDoubleOrNull() ?: 0.0,
            village = villageDraft,
            familyId = "",
            familyName = "",
            sellerPhone = "",
            harvestDate = harvestDate
        )
        vendorViewModel.saveProductDraft(vendorId, draft)
    }

    val isValid = remember(name, category, villageDraft, stock, preorderStock, isPreOrder, selectedImageUri, selectedArtisan, price) {
        name.isNotBlank() && category.isNotBlank() && villageDraft.isNotBlank() && 
        (if (isPreOrder) preorderStock.isNotBlank() else stock.isNotBlank()) &&
        selectedImageUri != null && selectedArtisan != null && price.isNotBlank()
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
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(if (selectedImageUri == null) BorderStroke(2.dp, ForestGreen.copy(0.2f)) else BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(24.dp))
                    .clickable { 
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

            // Artisan Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                onClick = { showArtisanPicker = true }
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

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Harvest Details") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), minLines = 3)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { vendorViewModel.generateAiDescription(name, category, "B.R. Hills") }) {
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

            Text("Fair Trade Pricing & MSP", style = MaterialTheme.typography.titleMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price/Kg*") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp))
                OutlinedTextField(value = msp, onValueChange = { msp = it }, label = { Text("Govt MSP/Kg") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp))
            }

            Text("Traceability & Origin", style = MaterialTheme.typography.titleMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = villageDraft, onValueChange = { villageDraft = it }, label = { Text("Village/Podu Name*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            
            Button(
                onClick = {
                    val batchId = "BATCH-${category.uppercase()}-${System.currentTimeMillis().toString().takeLast(6)}"
                    val batch = InventoryBatchEntity(
                        batchId = batchId,
                        vendorId = vendorId,
                        artisanId = selectedArtisan?.artisanId ?: "",
                        productName = name,
                        category = category,
                        quantityKg = stock.toDoubleOrNull() ?: 0.0,
                        pricePerKg = price.toDoubleOrNull() ?: 0.0,
                        mspPrice = msp.toDoubleOrNull() ?: 0.0,
                        marketPrice = marketPrice.toDoubleOrNull() ?: 0.0,
                        familyId = selectedArtisan?.familyName ?: "",
                        familyName = selectedArtisan?.familyName ?: "",
                        sellerPhone = Firebase.auth.currentUser?.phoneNumber ?: "",
                        description = description,
                        harvestDate = harvestDate,
                        processingStatus = "APPROVED",
                        village = villageDraft,
                        localImagePath = selectedImageUri?.toString() ?: ""
                    )
                    onAddProduct(batch)
                    vendorViewModel.clearProductDraft(vendorId)
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isPreOrder) AccentOrange else ForestGreen),
                enabled = isValid
            ) {
                Text("Publish Harvest", fontWeight = FontWeight.Bold, color = Color.White)
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
        NavigationBarItem(selected = currentRoute == "vendor_products", onClick = { onNavigate("vendor_products") }, icon = { Icon(Icons.Default.Inventory2, null) }, label = { Text("Stock") })
    }
}

@Composable
fun VendorMarketsBanner(onNavigate: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(24.dp).height(120.dp).clickable { onNavigate("map") }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ForestGreen)) {
        Row(Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tribal Sante Markets", color = TribalGold, style = MaterialTheme.typography.titleMedium)
                Text("Book your stall for the upcoming harvest festival", color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
        }
    }
}

@Composable
fun VendorAnalyticsCard(title: String, value: String, growth: String, icon: ImageVector, gradient: List<Color>) {
    Card(modifier = Modifier.width(160.dp).height(180.dp), shape = RoundedCornerShape(20.dp)) {
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
            Text("Fair-Trade Shop Dashboard", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
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
            VendorQuickActionButton("Payments", Icons.Default.AccountBalanceWallet, EarthBrown) { onNavigate("vendor_payments") }
            VendorQuickActionButton("Stock", Icons.Default.Inventory2, ForestGreen) { onNavigate("vendor_products") }
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
                    id = order.orderId.takeLast(6), 
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
fun VendorOrderCardPremium(id: String, customer: String, product: String, amount: String, status: String, onClick: () -> Unit = {}) {
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
                        if (product.mspPrice > 0) {
                            Text("MSP: ₹${product.mspPrice.toInt()}", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
