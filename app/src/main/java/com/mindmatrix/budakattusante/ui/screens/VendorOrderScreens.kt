package com.mindmatrix.budakattusante.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorOrdersScreen(
    orders: List<OrderEntity>,
    onBack: () -> Unit,
    onUpdateStatus: (String, String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Orders", fontWeight = FontWeight.Bold) },
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
        if (orders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
                    Text("No orders received yet", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    VendorOrderCard(order, onUpdateStatus)
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
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = order.productImageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(order.productName, fontWeight = FontWeight.Bold, color = ForestGreen)
                    Text("Customer: ${order.buyerName}", style = MaterialTheme.typography.bodySmall)
                    Text("Qty: ${order.quantityOrdered.toInt()} Kg", style = MaterialTheme.typography.bodySmall, color = EarthBrown)
                }
                StatusChip(status = order.orderStatus)
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.2f))
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Delivery Address", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(order.buyerAddress, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                Button(
                    onClick = { showStatusDialog = true },
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
            title = { Text("Update Order Status") },
            text = {
                Column {
                    val statuses = listOf("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED")
                    statuses.forEach { status ->
                        TextButton(
                            onClick = {
                                onUpdateStatus(order.orderId, status)
                                showStatusDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(status, color = if (status == order.orderStatus) ForestGreen else Color.Black)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) { Text("Close") }
            }
        )
    }
}
