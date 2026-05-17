package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.data.model.Address
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.ui.components.*
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.OrderViewModel
import com.mindmatrix.budakattusante.ui.viewmodel.VoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmScreen(
    product: Product,
    quantity: Double,
    addresses: List<Address>,
    onBack: () -> Unit,
    onAddAddress: () -> Unit,
    onProceedToPayment: (Address) -> Unit,
    onConfirmReservation: (Address) -> Unit,
    orderViewModel: OrderViewModel,
    voiceViewModel: VoiceViewModel = hiltViewModel()
) {
    var currentQuantity by remember { mutableStateOf(quantity) }
    var selectedAddress by remember { mutableStateOf<Address?>(addresses.firstOrNull()) }
    
    val subtotal = product.pricePerKg * currentQuantity
    val shipping = if (subtotal < 1000 && subtotal > 0) 50.0 else 0.0
    val total = subtotal + shipping

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (product.isPreOrder) "Confirm Reservation" else "Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { voiceViewModel.speak("Checkout page. Please review your order and delivery address.") }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, "Listen", tint = ForestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Product Summary & Quantity Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, color = ForestGreen)
                            Text("₹${product.pricePerKg.toInt()}/kg", color = EarthBrown)
                        }
                        IconButton(onClick = { voiceViewModel.speak("Ordering ${currentQuantity.toInt()} kg of ${product.name}") }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Quantity (Kg)", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (currentQuantity > 1) currentQuantity-- },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Cream)
                            ) {
                                Icon(Icons.Default.Remove, null, tint = ForestGreen)
                            }
                            Text(
                                currentQuantity.toInt().toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { currentQuantity++ },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Cream)
                            ) {
                                Icon(Icons.Default.Add, null, tint = ForestGreen)
                            }
                        }
                    }
                }
            }

            AddressSelectionSection(addresses, selectedAddress, onAddAddress) { selectedAddress = it }

            // Order Summary
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CheckoutSummaryRow("Subtotal", "₹${subtotal.toInt()}")
                        CheckoutSummaryRow("Delivery Charges", if (shipping == 0.0) "FREE" else "₹${shipping.toInt()}")
                        if (product.isPreOrder) {
                            CheckoutSummaryRow("Estimated Harvest", product.expectedHarvestDate, isHighlight = true)
                        }
                        CheckoutSummaryRow("Estimated Delivery", "7-10 days after harvest", isHighlight = true)
                        
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("₹${total.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { 
                    selectedAddress?.let { 
                        orderViewModel.setPendingOrder(product, currentQuantity, it.name, it.phone, "${it.street}, ${it.city}, ${it.zipCode}", product.isPreOrder)
                        if (product.isPreOrder) onConfirmReservation(it) else onProceedToPayment(it)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = selectedAddress != null,
                colors = ButtonDefaults.buttonColors(containerColor = if (product.isPreOrder) AccentOrange else ForestGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (product.isPreOrder) "Confirm Reservation" else "Proceed to Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutScreen(
    addresses: List<Address>,
    onBack: () -> Unit,
    onAddAddress: () -> Unit,
    onProceedToPayment: (Address) -> Unit,
    orderViewModel: OrderViewModel,
    voiceViewModel: VoiceViewModel = hiltViewModel()
) {
    val uiState by orderViewModel.uiState.collectAsStateWithLifecycle()
    var selectedAddress by remember { mutableStateOf<Address?>(addresses.firstOrNull()) }
    
    val subtotal = uiState.cartItems.sumOf { it.pricePerKg * it.quantity }
    val shipping = uiState.shippingFee
    val total = subtotal + shipping - uiState.discountAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cart Items Summary
            Text("Items in Basket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.cartItems.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Medium, color = ForestGreen, fontSize = 14.sp)
                                Text("${item.quantity.toInt()}kg × ₹${item.pricePerKg.toInt()}", color = Color.Gray, fontSize = 12.sp)
                            }
                            Text("₹${(item.pricePerKg * item.quantity).toInt()}", fontWeight = FontWeight.Bold, color = EarthBrown)
                        }
                    }
                }
            }

            AddressSelectionSection(addresses, selectedAddress, onAddAddress) { selectedAddress = it }

            // Order Summary
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Payment Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CheckoutSummaryRow("Subtotal", "₹${subtotal.toInt()}")
                        CheckoutSummaryRow("Delivery Charges", if (shipping == 0.0) "FREE" else "₹${shipping.toInt()}")
                        if (uiState.discountAmount > 0) {
                            CheckoutSummaryRow("Discount", "-₹${uiState.discountAmount.toInt()}", isHighlight = true)
                        }
                        CheckoutSummaryRow("Estimated Delivery", "3-5 business days", isHighlight = true)
                        
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("₹${total.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { 
                    selectedAddress?.let { 
                        orderViewModel.setPendingCartCheckout(it.name, it.phone, "${it.street}, ${it.city}, ${it.zipCode}")
                        onProceedToPayment(it)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = selectedAddress != null,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Choose Payment Method", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun AddressSelectionSection(
    addresses: List<Address>,
    selectedAddress: Address?,
    onAddAddress: () -> Unit,
    onAddressSelected: (Address) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
            TextButton(onClick = onAddAddress) {
                Text("+ Add New", color = AccentOrange, fontWeight = FontWeight.Bold)
            }
        }
        
        if (addresses.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onAddAddress() },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddLocation, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No addresses found. Tap to add.", color = Color.Gray)
                }
            }
        } else {
            addresses.forEach { address ->
                AddressOption(
                    address = address,
                    isSelected = selectedAddress?.id == address.id,
                    onClick = { onAddressSelected(address) }
                )
            }
        }
    }
}

@Composable
fun CheckoutSummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (isHighlight) ForestGreen else Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal, color = if (isHighlight) ForestGreen else Color.Black)
    }
}

@Composable
fun AddressOption(address: Address, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ForestGreen.copy(0.05f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) ForestGreen else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = ForestGreen))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(address.name, fontWeight = FontWeight.Bold, color = if (isSelected) ForestGreen else Color.Black)
                Text("${address.street}, ${address.landmark}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("${address.city}, ${address.state} - ${address.zipCode}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(address.phone, style = MaterialTheme.typography.bodySmall, color = EarthBrown, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OrderSuccessScreen(isPreOrder: Boolean, onHome: () -> Unit, voiceViewModel: VoiceViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        voiceViewModel.speak(if (isPreOrder) "Your pre-order has been reserved successfully! We will notify you when it's ready." else "Order placed successfully! Thank you for supporting tribal livelihoods.")
    }

    Column(
        modifier = Modifier.fillMaxSize().background(SandBeige).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = ForestGreen
        ) {
            Icon(Icons.Default.Check, null, modifier = Modifier.padding(32.dp).size(60.dp), tint = Color.White)
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            if (isPreOrder) "Pre-Order Reserved!" else "Order Placed Successfully!",
            style = MaterialTheme.typography.headlineLarge,
            color = ForestGreen,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            if (isPreOrder) "Your reservation is confirmed. We will notify you once the harvest is complete. Support pure tribal livelihoods!"
            else "Your pure forest produce will reach you soon. Thank you for supporting the tribal community of B.R. Hills.",
            textAlign = TextAlign.Center,
            color = EarthBrown,
            lineHeight = 24.sp
        )
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Text("Back to Sante", fontWeight = FontWeight.Bold)
        }
    }
}
