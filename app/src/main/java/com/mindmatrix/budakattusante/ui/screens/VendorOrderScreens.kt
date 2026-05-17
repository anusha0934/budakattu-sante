package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.data.local.entity.OrderEntity
import com.mindmatrix.budakattusante.ui.components.StatusChip
import com.mindmatrix.budakattusante.ui.theme.*

/**
 * Requirement 11: Production-ready Vendor Order Management with Tabs.
 * Implements filtering for New, Pre-Orders, and Shipped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorOrdersScreen(
    orders: List<OrderEntity>,
    onBack: () -> Unit,
    onUpdateStatus: (String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("New", "Pre-Orders", "Shipped")

    val filteredOrders = remember(orders, selectedTab) {
        when (selectedTab) {
            0 -> orders.filter { !it.isPreOrder && it.orderStatus == "PENDING" }
            1 -> orders.filter { it.isPreOrder }
            2 -> orders.filter { it.orderStatus == "SHIPPED" || it.orderStatus == "DELIVERED" }
            else -> orders
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Management", fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = ForestGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ForestGreen
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
                        Text("No ${tabs[selectedTab].lowercase()} orders", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredOrders) { order ->
                        VendorOrderCard(order, onUpdateStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun VendorOrderCard(order: OrderEntity, onUpdateStatus: (String, String) -> Unit) {
    var showStatusDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = order.productImageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(order.productName, fontWeight = FontWeight.Bold, color = ForestGreen, fontSize = 18.sp)
                    Text("Customer: ${order.buyerName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    Text("Qty: ${order.quantityOrdered.toInt()} Kg • ₹${order.totalAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = EarthBrown)
                }
                StatusChip(status = order.orderStatus)
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.2f))
            Spacer(Modifier.height(16.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Delivery Address", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(order.buyerAddress, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = EarthBrown)
                    Spacer(Modifier.width(4.dp))
                    Text(order.buyerPhone, style = MaterialTheme.typography.bodySmall, color = EarthBrown, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("Payment: ${order.paymentMethod}", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { /* TODO: Open tracking update */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ForestGreen)
                ) {
                    Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Track", color = ForestGreen)
                }
                
                Button(
                    onClick = { showStatusDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Update Status")
                }
            }
        }
    }

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Update Order Status", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val statuses = listOf("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED")
                    statuses.forEach { status ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateStatus(order.orderId, status)
                                    showStatusDialog = false
                                }
                                .padding(vertical = 12.dp),
                            color = Color.Transparent
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = status == order.orderStatus, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(status, fontWeight = if (status == order.orderStatus) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) { Text("Cancel") }
            }
        )
    }
}
