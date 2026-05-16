package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.data.local.entity.OrderEntity
import com.mindmatrix.budakattusante.ui.components.BudakattuBottomNav
import com.mindmatrix.budakattusante.ui.components.StatusChip
import com.mindmatrix.budakattusante.ui.theme.*

/**
 * Requirement 9: Order tracking and history.
 * Requirement 10: Empty state for orders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    orders: List<OrderEntity>,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onCategories: () -> Unit,
    onCart: () -> Unit,
    onProfile: () -> Unit,
    onTrackOrder: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tribal Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        bottomBar = {
            BudakattuBottomNav(
                currentScreen = "Orders",
                onHome = onHome,
                onCategories = onCategories,
                onOrders = { /* Already here */ },
                onCart = onCart,
                onProfile = onProfile
            )
        },
        containerColor = Cream
    ) { padding ->
        if (orders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Default.ListAlt, null, modifier = Modifier.size(100.dp), tint = ForestGreen.copy(0.1f))
                    Spacer(Modifier.height(16.dp))
                    Text("No orders yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = EarthBrown)
                    Text("Your pure forest products will appear here once you make a purchase.", textAlign = TextAlign.Center, color = Color.Gray)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onHome, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)) {
                        Text("Start Shopping")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders.sortedByDescending { it.timestamp }) { order ->
                    CustomerOrderCard(order = order, onTrack = { onTrackOrder(order.orderId) })
                }
            }
        }
    }
}

@Composable
fun CustomerOrderCard(order: OrderEntity, onTrack: () -> Unit) {
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
                    Text("₹${order.totalAmount.toInt()} • ${order.quantityOrdered.toInt()} Kg", style = MaterialTheme.typography.bodySmall, color = EarthBrown)
                }
                StatusChip(status = order.orderStatus)
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.2f))
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Order ID", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("#${order.orderId.takeLast(8).uppercase()}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onTrack,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen.copy(0.1f), contentColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Track Order", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/**
 * Requirement 13: Customer Profile with Address Management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onSwitchRole: () -> Unit,
    onViewOrders: () -> Unit,
    onHome: () -> Unit,
    onCategories: () -> Unit,
    onCart: () -> Unit,
    onViewAddresses: () -> Unit,
    onViewNotifications: () -> Unit,
    onEditProfile: () -> Unit,
    userProfileImage: String?,
    userName: String
) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        bottomBar = {
            BudakattuBottomNav(
                currentScreen = "Profile",
                onHome = onHome,
                onCategories = onCategories,
                onOrders = onViewOrders,
                onCart = onCart,
                onProfile = { /* Already here */ }
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState)
        ) {
            // Profile Header
            Box(
                modifier = Modifier.fillMaxWidth().background(ForestGreen).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).border(3.dp, Color.White, CircleShape)
                    ) {
                        AsyncImage(
                            model = userProfileImage,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(userName, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
                    Text("Premium Sante Member", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
                }
            }

            // Profile Options
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileOptionItem(Icons.Default.Person, "Edit Profile", "Name, Email, Phone", onClick = onEditProfile)
                ProfileOptionItem(Icons.Default.LocationOn, "Delivery Addresses", "Manage your shipping locations", onClick = onViewAddresses)
                ProfileOptionItem(Icons.Default.History, "Order History", "Track and view past orders", onClick = onViewOrders)
                ProfileOptionItem(Icons.Default.Notifications, "Notifications", "Alerts and updates", onClick = onViewNotifications)
                ProfileOptionItem(Icons.Default.Store, "Switch to Seller Mode", "Become a tribal artisan partner", onClick = onSwitchRole, tint = Color(0xFFE65100))
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.1f), contentColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout from Sante", fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ProfileOptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit, tint: Color = ForestGreen) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), shape = CircleShape, color = tint.copy(alpha = 0.1f)) {
                Icon(icon, null, modifier = Modifier.padding(12.dp), tint = tint)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = ForestGreen)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
