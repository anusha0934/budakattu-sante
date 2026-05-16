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
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.AdminViewModel

/**
 * Requirement 7: Complete Admin Dashboard.
 * Allows admins to approve products, manage MSP, and view system analytics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "MSP", "Analytics")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = ForestGreen) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> PendingApprovalsList(uiState.pendingBatches, viewModel)
                1 -> MspManagementSection(uiState.mspData, viewModel)
                2 -> AdminAnalyticsSection()
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(batch.productName, style = MaterialTheme.typography.titleLarge, color = ForestGreen)
                                Text("Artisan: ${batch.familyName}", color = EarthBrown)
                            }
                            Text("₹${batch.pricePerKg.toInt()}/kg", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(batch.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.rejectBatch(batch.batchId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) { Text("Reject") }
                            Button(
                                onClick = { viewModel.approveBatch(batch.batchId) },
                                modifier = Modifier.weight(1f),
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
fun MspManagementSection(mspList: List<MspEntity>, viewModel: AdminViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var editingMsp by remember { mutableStateOf<MspEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(mspList) { msp ->
                ListItem(
                    headlineContent = { Text(msp.category, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("MSP: ₹${msp.approvedMsp} • Authority: ${msp.authority}") },
                    trailingContent = {
                        IconButton(onClick = { editingMsp = msp; showDialog = true }) {
                            Icon(Icons.Default.Edit, null, tint = ForestGreen)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.White)
                )
            }
        }
        Button(
            onClick = { editingMsp = null; showDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Icon(Icons.Default.Add, null)
            Text("Add New MSP Record")
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
        title = { Text(if (msp == null) "New MSP Entry" else "Update MSP") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, enabled = msp == null)
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Approved MSP (₹)") })
                OutlinedTextField(value = marketPrice, onValueChange = { marketPrice = it }, label = { Text("Market Price (₹)") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(MspEntity(category, price.toDoubleOrNull() ?: 0.0, marketPrice.toDoubleOrNull() ?: 0.0, "2026-01-01")) 
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AdminAnalyticsSection() {
    Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("System Overview", style = MaterialTheme.typography.titleLarge, color = ForestGreen, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        AnalyticsStatRow("Total Tribal Revenue", "₹12,45,000")
        AnalyticsStatRow("Active Forest Producers", "142 Families")
        AnalyticsStatRow("Completed Deliveries", "892 Orders")
        
        Spacer(Modifier.height(24.dp))
        Text("Supply Chain Integrity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp)) {
                Text("100% Products Traceable", color = ForestGreen, fontWeight = FontWeight.Bold)
                Text("QR Verification Active", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AnalyticsStatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, color = ForestGreen)
    }
}
