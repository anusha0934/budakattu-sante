package com.mindmatrix.budakattusante.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.data.model.Artisan
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.ui.theme.*

@Composable
fun ArtisanCard(artisan: Artisan) {
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
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, ForestGreen, CircleShape)
            ) {
                if (artisan.profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = artisan.profileImageUrl,
                        contentDescription = artisan.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        tint = ForestGreen
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(artisan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreen)
                Text("${artisan.tribeName} Tribe • ${artisan.yearsOfExperience} yrs experience", style = MaterialTheme.typography.bodyMedium, color = EarthBrown)
                Text("Specializes in: ${artisan.specialization}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = AccentOrange, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(artisan.villageName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TraceabilitySection(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(ForestGreen.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .border(1.dp, ForestGreen.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VerifiedUser, null, tint = ForestGreen)
            Spacer(Modifier.width(8.dp))
            Text("Know Your Product Source", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = ForestGreen)
        }
        Spacer(Modifier.height(16.dp))
        
        TraceItem(Icons.Default.Fingerprint, "Batch ID", product.batchId.ifBlank { "BATCH-TRIBAL-DEFAULT" })
        TraceItem(Icons.Default.CalendarToday, "Harvest Date", product.harvestDate)
        TraceItem(Icons.Default.Eco, "Collected from", product.forestRegion)
        TraceItem(Icons.Default.GpsFixed, "Collection Zone", product.collectionZone.ifBlank { "Western Ghats Buffer Zone" })
        
        Spacer(Modifier.height(12.dp))
        Text(
            "This product is 100% traceable to the source. Collected using traditional sustainable methods by the ${product.tribeName} families.",
            style = MaterialTheme.typography.bodySmall,
            color = EarthBrown.copy(alpha = 0.8f),
            lineHeight = 16.sp
        )
    }
}

/**
 * Requirement 3: MSP Transparency Section.
 * Shows comparison between Government MSP and Budakattu Sante Fair Trade price.
 */
@Composable
fun FairTradePricingSection(product: Product) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(TribalGold.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .border(1.dp, TribalGold.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Balance, null, tint = EarthBrown)
                Spacer(Modifier.width(8.dp))
                Text("Fair Trade & MSP Pricing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = EarthBrown)
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = EarthBrown)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PricingMetric("Tribal MSP", "₹${product.mspPrice.toInt()}", "Minimum Support")
            PricingMetric("Market Price", "₹${product.marketPrice.toInt()}", "External Value")
            PricingMetric("Customer Price", "₹${product.pricePerKg.toInt()}", "Budakattu Sante")
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Tribal Earnings Highlight
        Surface(
            color = ForestGreen,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.VolunteerActivism, null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "₹${product.tribalEarnings.toInt()} goes directly to tribal artisans",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                HorizontalDivider(color = EarthBrown.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))
                
                Text("Price Breakdown (per Kg)", fontWeight = FontWeight.Bold, color = EarthBrown)
                Spacer(Modifier.height(8.dp))
                
                PriceBreakdownItem("Artisan Direct Pay (MSP)", "₹${product.mspPrice.toInt()}", ForestGreen)
                PriceBreakdownItem("Community Welfare Fund", "₹${(product.pricePerKg * 0.05).toInt()}", TribalGold)
                PriceBreakdownItem("Logistics & Platform", "₹${(product.pricePerKg - product.mspPrice - (product.pricePerKg * 0.05)).toInt()}", EarthBrown)
                
                Spacer(Modifier.height(16.dp))
                Text("Pricing History", fontWeight = FontWeight.Bold, color = EarthBrown)
                Spacer(Modifier.height(12.dp))
                PricingHistoryGraph(basePrice = product.mspPrice)
                
                Spacer(Modifier.height(16.dp))
                Text("Why MSP Matters?", fontWeight = FontWeight.Bold, color = EarthBrown)
                Text(
                    "Minimum Support Price (MSP) ensures that tribal collectors receive a guaranteed minimum income for their forest produce, protecting them from exploitation by middlemen and market fluctuations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                
                Spacer(Modifier.height(12.dp))
                
                if (product.governmentApproved) {
                    GovernmentApprovalCard(
                        certificationId = product.certificationId,
                        authority = product.pricingAuthority,
                        lastUpdated = product.mspLastUpdated
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                Text(
                    "This purchase supports tribal livelihoods and helps preserve ancient forest wisdom.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PriceBreakdownItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun PricingHistoryGraph(basePrice: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val points = listOf(0.85f, 0.75f, 0.92f, 1.0f, 1.08f, 1.05f)
            val path = Path()
            val stepX = size.width / (points.size - 1)
            
            points.forEachIndexed { index, point ->
                val x = index * stepX
                val y = size.height * (1f - point / 1.5f)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            
            drawPath(
                path = path,
                color = ForestGreen,
                style = Stroke(width = 2.dp.toPx())
            )
            
            // Draw points
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = ForestGreen,
                    radius = 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(index * stepX, size.height * (1f - point / 1.5f))
                )
            }
        }
        Text(
            "Historical MSP Trend (Last 6 Months)",
            modifier = Modifier.align(Alignment.BottomEnd),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 8.sp
        )
    }
}

@Composable
fun GovernmentApprovalCard(certificationId: String, authority: String = "TRIFED", lastUpdated: String = "N/A") {
    Surface(
        color = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Verified, null, tint = ForestGreen, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Government Supported Pricing", fontWeight = FontWeight.ExtraBold, color = ForestGreen, fontSize = 14.sp)
                Text("Cert ID: $certificationId • Pricing Authority: $authority", style = MaterialTheme.typography.labelSmall, color = ForestGreen.copy(alpha = 0.8f))
                Text("Last Updated: $lastUpdated", style = MaterialTheme.typography.labelSmall, color = ForestGreen.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun TribalWelfareCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = EarthBrown),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, null, tint = TribalGold)
                Spacer(Modifier.width(8.dp))
                Text("Tribal Welfare Impact", color = TribalGold, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "By buying directly through Budakattu Sante, you eliminate 3-4 middle-layers. 80-90% of your payment reaches the tribal community directly, funding education and healthcare in B.R. Hills.",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "\"This purchase supports tribal livelihoods\"",
                style = MaterialTheme.typography.bodySmall,
                color = TribalGold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FairTradeBadge(modifier: Modifier = Modifier) {
    Surface(
        color = TribalGold,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "FAIR TRADE CERTIFIED",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun PricingMetric(label: String, value: String, subLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = EarthBrown)
        Text(subLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
    }
}

@Composable
private fun TraceItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = EarthBrown, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EarthBrown)
    }
}

@Composable
fun HarvestTimelineItem(date: String, quantity: String, artisan: String, isFirst: Boolean = false, isLast: Boolean = false) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(if (isFirst) Color.Transparent else Color.LightGray)
            )
            Surface(
                modifier = Modifier.size(12.dp),
                shape = CircleShape,
                color = ForestGreen,
                border = androidx.compose.foundation.BorderStroke(2.dp, Cream)
            ) {}
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(if (isLast) Color.Transparent else Color.LightGray)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.padding(bottom = 24.dp)) {
            Text(date, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text("$quantity harvested by $artisan", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = ForestGreen)
        }
    }
}
