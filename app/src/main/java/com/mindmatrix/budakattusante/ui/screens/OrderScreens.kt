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
import com.mindmatrix.budakattusante.data.model.Address
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.OrderViewModel

/**
 * Requirement 1, 9, 19: Checkout and Confirmation Workflow.
 */
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
    orderViewModel: OrderViewModel
) {
    var selectedAddress by remember { mutableStateOf<Address?>(addresses.firstOrNull()) }
    val subtotal = product.pricePerKg * quantity
    val shipping = if (subtotal < 1000) 50.0 else 0.0
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
                .verticalScroll(rememberScrollState())
        ) {
            // Product Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(product.name, fontWeight = FontWeight.Bold, color = ForestGreen)
                        Text("${quantity.toInt()} Kg • ₹${product.pricePerKg.toInt()}/kg", color = EarthBrown)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Delivery Address
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                TextButton(onClick = onAddAddress) {
                    Text("+ Add New", color = AccentOrange)
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
                        Icon(Icons.Default.AddLocation, null, tint = Color.LightGray)
                        Text("No addresses found. Tap to add.", color = Color.Gray)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    addresses.forEach { address ->
                        AddressOption(
                            address = address,
                            isSelected = selectedAddress?.id == address.id,
                            onClick = { selectedAddress = address }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Pricing Summary
            OrderSummarySection(subtotal, shipping, 0.0, total)

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { 
                    selectedAddress?.let { 
                        if (product.isPreOrder) onConfirmReservation(it) else onProceedToPayment(it)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = selectedAddress != null,
                colors = ButtonDefaults.buttonColors(containerColor = if (product.isPreOrder) AccentOrange else ForestGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (product.isPreOrder) "Reserve Now (No Pay)" else "Proceed to Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            if (product.isPreOrder) {
                Text(
                    "You will only pay once the harvest is ready and quality-checked.",
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
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
            Column {
                Text(address.name, fontWeight = FontWeight.Bold)
                Text("${address.street}, ${address.city}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(address.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun OrderSuccessScreen(isPreOrder: Boolean, onHome: () -> Unit) {
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
