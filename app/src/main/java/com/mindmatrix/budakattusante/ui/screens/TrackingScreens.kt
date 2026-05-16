package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.ui.theme.Cream
import com.mindmatrix.budakattusante.ui.theme.ForestGreen
import com.mindmatrix.budakattusante.ui.theme.EarthBrown
import com.mindmatrix.budakattusante.ui.theme.TribalGold

/**
 * Requirement 9: Order tracking timeline and Delivery ETA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDeliveryTrackingScreen(orderId: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Your Pure Produce", fontWeight = FontWeight.Bold) },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Order #$orderId", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("Wild Forest Honey", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ForestGreen)
                    Text("Arriving by Friday, 24th April", style = MaterialTheme.typography.bodyLarge, color = EarthBrown, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Delivery Progress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreen)
            Spacer(Modifier.height(24.dp))

            val stages = listOf(
                TrackingStage("Order Placed", "We have received your order", "Today, 10:30 AM", true),
                TrackingStage("Harvest Verified", "Batch quality checked by tribal collective", "Today, 02:15 PM", true),
                TrackingStage("Packed at Source", "Eco-friendly packaging completed", "Pending", false),
                TrackingStage("In Transit", "Handed over to forest logistics fleet", "Expected Tomorrow", false),
                TrackingStage("Delivered", "Product reaching your doorstep", "Expected Friday", false)
            )

            stages.forEachIndexed { index, stage ->
                TrackingTimelineItem(
                    stage = stage,
                    isLast = index == stages.size - 1
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Support Action
            OutlinedButton(
                onClick = { /* Contact Support */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.SupportAgent, null)
                Spacer(Modifier.width(8.dp))
                Text("Need Help with this Order?")
            }
        }
    }
}

data class TrackingStage(
    val title: String,
    val description: String,
    val time: String,
    val isCompleted: Boolean
)

@Composable
fun TrackingTimelineItem(stage: TrackingStage, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = if (stage.isCompleted) ForestGreen else Color.LightGray.copy(alpha = 0.5f)
            ) {
                if (stage.isCompleted) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.padding(4.dp), tint = Color.White)
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (stage.isCompleted) ForestGreen else Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.padding(bottom = 32.dp)) {
            Text(
                stage.title, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = if (stage.isCompleted) Color.Black else Color.Gray
            )
            Text(stage.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(stage.time, style = MaterialTheme.typography.labelSmall, color = if (stage.isCompleted) ForestGreen else Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDeliveryTrackingScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logistics Hub", fontWeight = FontWeight.Bold) },
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
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text("Active Shipments", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = ForestGreen)
            Spacer(Modifier.height(16.dp))
            
            // Empty State Placeholder
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No tribal shipments currently in transit", color = Color.Gray)
                }
            }
        }
    }
}
