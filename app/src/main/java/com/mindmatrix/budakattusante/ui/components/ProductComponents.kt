package com.mindmatrix.budakattusante.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.VoiceViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun CatalogItem(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
                
                if (product.isPreOrder) {
                    Surface(
                        modifier = Modifier.padding(12.dp),
                        color = AccentOrange,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "PRE-ORDER",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "₹${product.pricePerKg.toInt()}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    product.tribeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrown
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = TribalGold, modifier = Modifier.size(14.dp))
                    Text(" ${product.rating}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(Modifier.weight(1f))
                    
                    if (!product.isPreOrder) {
                        IconButton(
                            onClick = onAddToCart,
                            modifier = Modifier.size(32.dp).background(ForestGreen, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Icon(Icons.Default.CalendarMonth, null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PreOrderCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = ForestGreen,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(color = AccentOrange.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "₹${product.pricePerKg.toInt()}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = AccentOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                HarvestCountdown(product.expectedHarvestDate)
                
                Spacer(Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, tint = EarthBrown, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Reserved: ${product.preorderCount}/${product.preorderStock}",
                        style = MaterialTheme.typography.labelMedium,
                        color = EarthBrown
                    )
                }
            }
        }
    }
}

@Composable
fun HarvestCountdown(targetDate: String) {
    val daysLeft = try {
        val target = LocalDate.parse(targetDate, DateTimeFormatter.ISO_LOCAL_DATE)
        ChronoUnit.DAYS.between(LocalDate.now(), target)
    } catch (e: Exception) {
        15L
    }

    Surface(
        color = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AccentOrange.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Timer, null, tint = AccentOrange, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Harvesting in $daysLeft days",
                style = MaterialTheme.typography.labelLarge,
                color = AccentOrange,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HarvestChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(name) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ForestGreen,
            selectedLabelColor = Color.White,
            containerColor = Color.White,
            labelColor = ForestGreen
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = ForestGreen,
            selectedBorderColor = ForestGreen
        )
    )
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = ForestGreen
        )
        TextButton(onClick = onSeeAll) {
            Text("See All", color = AccentOrange, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantSheet(
    onDismiss: () -> Unit,
    voiceViewModel: VoiceViewModel
) {
    val voiceState by voiceViewModel.voiceState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ForestGreen,
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Tribal Voice Assistant",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "How can I help you in the Sante today?",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(0.7f)
            )
            
            Spacer(Modifier.height(48.dp))
            
            Box(contentAlignment = Alignment.Center) {
                // Pulse Animation (Conceptual)
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {}
                
                IconButton(
                    onClick = { 
                        if (voiceState.isSpeaking) voiceViewModel.stopListening() 
                        else voiceViewModel.startListening()
                    },
                    modifier = Modifier.size(80.dp).background(Color.White, CircleShape)
                ) {
                    Icon(
                        if (voiceState.isSpeaking) Icons.Default.Stop else Icons.Default.Mic,
                        null,
                        tint = ForestGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                voiceState.spokenText.ifBlank { "Listening..." },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(48.dp))
            
            Text(
                "Try saying: \"Show me wild honey\" or \"Check my orders\"",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.5f)
            )
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun EmptyState(message: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Inventory,
            null,
            modifier = Modifier.size(100.dp),
            tint = ForestGreen.copy(0.1f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = EarthBrown
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Refresh Sante")
        }
    }
}

@Composable
fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Oops! Something went wrong while connecting to the forest.")
        TextButton(onClick = onRetry) {
            Text("Try Again", color = ForestGreen)
        }
    }
}

@Composable
fun StockProgress(current: Int, total: Int) {
    val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Pre-order Progress",
                style = MaterialTheme.typography.labelLarge,
                color = ForestGreen,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$current / $total Reserved",
                style = MaterialTheme.typography.labelMedium,
                color = EarthBrown
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = AccentOrange,
            trackColor = AccentOrange.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.uppercase()) {
        "DELIVERED" -> Color(0xFF4CAF50)
        "SHIPPED" -> Color(0xFF2196F3)
        "RESERVED", "PENDING_HARVEST" -> AccentOrange
        "CANCELLED" -> Color.Red
        else -> EarthBrown
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
