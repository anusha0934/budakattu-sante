package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.ui.components.BudakattuBottomNav
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.OrderViewModel

/**
 * Requirement 10 & 19: Production-ready Cart with Offline Support.
 * Added: Quantity update, Coupon support, Shipping fee, and Empty State (Requirement 10).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    onHome: () -> Unit,
    onCategories: () -> Unit,
    onOrders: () -> Unit,
    onProfile: () -> Unit,
    onAddAddress: () -> Unit,
    orderViewModel: OrderViewModel
) {
    val uiState by orderViewModel.uiState.collectAsStateWithLifecycle()
    val cartItems = uiState.cartItems
    val subtotal = cartItems.sumOf { it.pricePerKg * it.quantity }
    val shippingFee = if (subtotal > 0 && subtotal < 1000) 50.0 else 0.0
    val total = subtotal + shippingFee - uiState.discountAmount
    
    var couponCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Tribal Basket", fontWeight = FontWeight.Bold) },
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
                currentScreen = "Cart",
                onHome = onHome,
                onCategories = onCategories,
                onOrders = onOrders,
                onCart = { /* Already here */ },
                onProfile = onProfile
            )
        },
        containerColor = Cream
    ) { padding ->
        if (cartItems.isEmpty()) {
            EmptyCartView(onHome, padding)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemRow(
                            name = item.name,
                            qty = item.quantity,
                            price = (item.pricePerKg * item.quantity).toInt(),
                            imageUrl = item.imageUrl,
                            onRemove = { orderViewModel.removeFromCart(item.productId) },
                            onIncrease = { orderViewModel.updateCartQuantity(item.productId, item.quantity + 1) },
                            onDecrease = { orderViewModel.updateCartQuantity(item.productId, item.quantity - 1) }
                        )
                    }

                    item {
                        CouponSection(
                            code = couponCode,
                            onCodeChange = { couponCode = it },
                            appliedCoupon = uiState.appliedCoupon,
                            onApply = { orderViewModel.applyCoupon(couponCode, subtotal) }
                        )
                    }
                    
                    item {
                        OrderSummarySection(subtotal, shippingFee, uiState.discountAmount, total)
                    }
                    
                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Proceed to Checkout", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(onHome: () -> Unit, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ShoppingBasket,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = ForestGreen.copy(alpha = 0.1f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Your tribal basket is empty",
                style = MaterialTheme.typography.headlineSmall,
                color = EarthBrown,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Support forest artisans by adding pure organic products to your cart. Every purchase makes a difference.",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onHome,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp).fillMaxWidth().padding(horizontal = 32.dp)
            ) {
                Text("Explore the Sante", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CartItemRow(
    name: String,
    qty: Double,
    price: Int,
    imageUrl: String,
    onRemove: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                Text("₹$price", style = MaterialTheme.typography.titleMedium, color = EarthBrown, fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp).clickable { onDecrease() },
                        shape = CircleShape,
                        color = Cream,
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.padding(6.dp), tint = EarthBrown)
                    }
                    Text(
                        text = "${qty.toInt()}kg",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        modifier = Modifier.size(32.dp).clickable { onIncrease() },
                        shape = CircleShape,
                        color = ForestGreen,
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.padding(6.dp), tint = Color.White)
                    }
                }
            }
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun CouponSection(
    code: String,
    onCodeChange: (String) -> Unit,
    appliedCoupon: String?,
    onApply: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Apply Coupon", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EarthBrown)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("TRIBAL20 or HARVEST10") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = appliedCoupon == null
            )
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onApply,
                enabled = code.isNotBlank() && appliedCoupon == null,
                colors = ButtonDefaults.buttonColors(containerColor = if (appliedCoupon != null) ForestGreen else AccentOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text(if (appliedCoupon != null) "Applied" else "Apply")
            }
        }
    }
}

@Composable
fun OrderSummarySection(subtotal: Double, shipping: Double, discount: Double, total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryRow("Subtotal", "₹${subtotal.toInt()}")
            SummaryRow("Shipping Fee", if (shipping > 0) "₹${shipping.toInt()}" else "FREE", color = if (shipping == 0.0) ForestGreen else Color.Black)
            if (discount > 0) {
                SummaryRow("Coupon Discount", "-₹${discount.toInt()}", color = AccentOrange)
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Payable", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = ForestGreen)
                Text("₹${total.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = ForestGreen)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, color: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, color = color)
    }
}
