package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

            PaymentMethodItem(
                title = "UPI (PhonePe/Google Pay)",
                icon = Icons.Default.QrCode,
                selected = selectedMethod == "UPI",
                onClick = { selectedMethod = "UPI" }
            )

            Spacer(Modifier.height(16.dp))

            PaymentMethodItem(
                title = "Credit / Debit Card",
                icon = Icons.Default.CreditCard,
                selected = selectedMethod == "CARD",
                onClick = { selectedMethod = "CARD" }
            )

            Spacer(Modifier.height(16.dp))

            PaymentMethodItem(
                title = "Cash on Delivery",
                icon = Icons.Default.Money,
                selected = selectedMethod == "COD",
                onClick = { selectedMethod = "COD" }
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    isProcessing = true
                    // Simulate payment processing
                    onPaymentSuccess(selectedMethod!!)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = selectedMethod != null && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Pay Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
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
