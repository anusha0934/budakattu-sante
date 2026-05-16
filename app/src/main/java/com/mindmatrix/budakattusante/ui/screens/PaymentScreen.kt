package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onPaymentSuccess: (String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    
    // UPI Fields
    var upiId by remember { mutableStateOf("") }
    
    // Card Fields
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Payment", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Text(
                "How would you like to pay?",
                style = MaterialTheme.typography.headlineSmall,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Supporting tribal communities with every purchase.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // UPI Option
            PaymentMethodItem(
                title = "UPI (PhonePe/Google Pay/Paytm)",
                icon = Icons.Default.QrCode,
                selected = selectedMethod == "UPI",
                onClick = { selectedMethod = "UPI" }
            )
            
            AnimatedVisibility(visible = selectedMethod == "UPI") {
                Column(Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("Enter UPI ID (e.g., name@okaxis)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Text(
                        "A payment request will be sent to this ID.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Card Option
            PaymentMethodItem(
                title = "Credit / Debit Card",
                icon = Icons.Default.CreditCard,
                selected = selectedMethod == "CARD",
                onClick = { selectedMethod = "CARD" }
            )

            AnimatedVisibility(visible = selectedMethod == "CARD") {
                Column(Modifier.padding(top = 16.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = cardHolder,
                        onValueChange = { cardHolder = it },
                        label = { Text("Card Holder Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { if (it.length <= 16) cardNumber = it },
                        label = { Text("Card Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { if (it.length <= 5) expiryDate = it },
                            label = { Text("MM/YY") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { if (it.length <= 3) cvv = it },
                            label = { Text("CVV") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PasswordVisualTransformation()
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // COD Option
            PaymentMethodItem(
                title = "Cash on Delivery (COD)",
                icon = Icons.Default.Payments,
                selected = selectedMethod == "COD",
                onClick = { selectedMethod = "COD" }
            )
            
            if (selectedMethod == "COD") {
                Text(
                    "Pay with cash when your forest produce is delivered.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestGreen,
                    modifier = Modifier.padding(top = 8.dp, start = 12.dp)
                )
            }

            Spacer(Modifier.height(40.dp))

            val isFormValid = when (selectedMethod) {
                "UPI" -> upiId.contains("@") && upiId.length > 3
                "CARD" -> cardNumber.length == 16 && cvv.length == 3 && expiryDate.length == 5 && cardHolder.isNotBlank()
                "COD" -> true
                else -> false
            }

            Button(
                onClick = {
                    isProcessing = true
                    onPaymentSuccess(selectedMethod!!)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = isFormValid && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (selectedMethod == "COD") "Confirm Order" else "Pay Securely",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun PaymentMethodItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) ForestGreen.copy(alpha = 0.05f) else Color.White,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) ForestGreen else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (selected) ForestGreen else Color.Gray.copy(alpha = 0.1f)
            ) {
                Icon(
                    icon,
                    null,
                    tint = if (selected) Color.White else ForestGreen,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) ForestGreen else Color.Black
            )
            Spacer(Modifier.weight(1f))
            if (selected) {
                Icon(Icons.Default.CheckCircle, null, tint = ForestGreen)
            }
        }
    }
}
