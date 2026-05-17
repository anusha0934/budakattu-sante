package com.mindmatrix.budakattusante.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.budakattusante.data.local.entity.InventoryBatchEntity
import com.mindmatrix.budakattusante.data.local.entity.MspEntity
import com.mindmatrix.budakattusante.data.local.entity.OrderEntity
import com.mindmatrix.budakattusante.data.model.UserProfile
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Products", "Vendors", "Orders", "MSP")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Control Center", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream),
                actions = {
                    IconButton(onClick = { /* Refresh */ }) {
                        Icon(Icons.Default.Refresh, null, tint = ForestGreen)
                    }
                }
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = ForestGreen,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> PendingApprovalsList(uiState.pendingBatches, viewModel)
                1 -> VendorApprovalsSection(viewModel)
                2 -> OrderMonitoringSection(viewModel)
                3 -> MspManagementSection(uiState.mspData, viewModel)
            }
        }
    }
}

@Composable
fun PendingApprovalsList(batches: List<InventoryBatchEntity>, viewModel: AdminViewModel) {
    if (batches.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending products to approve", color = Color.Gray)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(batches) { batch ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(batch.productName, style = MaterialTheme.typography.titleLarge, color = ForestGreen, fontWeight = FontWeight.Bold)
                                Text("Artisan: ${batch.familyName}", color = EarthBrown, style = MaterialTheme.typography.bodySmall)
                                Text("Village: ${batch.village}", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                            Text("₹${batch.pricePerKg.toInt()}/kg", fontWeight = FontWeight.Black, color = ForestGreen, fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(batch.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3, color = Color.DarkGray)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.rejectBatch(batch.batchId) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) { Text("Reject") }
                            Button(
                                onClick = { viewModel.approveBatch(batch.batchId) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                            ) { Text("Approve") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorApprovalsSection(viewModel: AdminViewModel) {
    // Implementation for vendor registration approvals
    // Placeholder UI
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Store, null, modifier = Modifier.size(64.dp), tint = ForestGreen.copy(alpha = 0.2f))
        Text("Vendor Registration Approvals", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text("Feature coming in next sync...", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
    }
}

@Composable
fun OrderMonitoringSection(viewModel: AdminViewModel) {
    // Implementation for monitoring all orders
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Assignment, null, modifier = Modifier.size(64.dp), tint = ForestGreen.copy(alpha = 0.2f))
        Text("Order Monitoring Dashboard", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
    }
}

@Composable
fun MspManagementSection(mspList: List<MspEntity>, viewModel: AdminViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var editingMsp by remember { mutableStateOf<MspEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(mspList) { msp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    ListItem(
                        headlineContent = { Text(msp.category, fontWeight = FontWeight.Bold, color = ForestGreen) },
                        supportingContent = { Text("MSP: ₹${msp.approvedMsp} • Market: ₹${msp.marketPrice}") },
                        trailingContent = {
                            IconButton(onClick = { editingMsp = msp; showDialog = true }) {
                                Icon(Icons.Default.Edit, null, tint = ForestGreen)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
        Button(
            onClick = { editingMsp = null; showDialog = true },
            modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add New MSP Record", fontWeight = FontWeight.Bold)
        }
    }

    if (showDialog) {
        UpdateMspDialog(
            msp = editingMsp,
            onDismiss = { showDialog = false },
            onSave = { viewModel.updateMsp(it); showDialog = false }
        )
    }
}

@Composable
fun UpdateMspDialog(msp: MspEntity?, onDismiss: () -> Unit, onSave: (MspEntity) -> Unit) {
    var category by remember { mutableStateOf(msp?.category ?: "") }
    var price by remember { mutableStateOf(msp?.approvedMsp?.toString() ?: "") }
    var marketPrice by remember { mutableStateOf(msp?.marketPrice?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (msp == null) "New MSP Entry" else "Update MSP", fontWeight = FontWeight.Bold, color = ForestGreen) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Approved MSP (₹)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = marketPrice, onValueChange = { marketPrice = it }, label = { Text("Market Price (₹)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(MspEntity(category, price.toDoubleOrNull() ?: 0.0, marketPrice.toDoubleOrNull() ?: 0.0, "2026-01-01")) 
            }, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
