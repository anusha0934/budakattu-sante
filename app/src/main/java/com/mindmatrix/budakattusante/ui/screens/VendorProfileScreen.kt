package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.data.model.UserProfile
import com.mindmatrix.budakattusante.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onSwitchRole: () -> Unit,
    userProfile: UserProfile?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = SandBeige
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(4.dp, ForestGreen, CircleShape)
            ) {
                if (!userProfile?.profileImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = userProfile?.profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = ForestGreen,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .background(Color.White)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = userProfile?.name ?: "Artisan",
                style = MaterialTheme.typography.headlineMedium,
                color = ForestGreen,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Certified Forest Producer",
                style = MaterialTheme.typography.bodyMedium,
                color = EarthBrown
            )

            Spacer(Modifier.height(32.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Products",
                    value = "12",
                    icon = Icons.Default.Inventory2
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Rating",
                    value = "4.9",
                    icon = Icons.Default.Star
                )
            }

            Spacer(Modifier.height(32.dp))

            // Options
            ProfileOptionItem(
                title = "Business Details",
                subtitle = "Manage your shop and tribal certification",
                icon = Icons.Default.Business,
                onClick = { /* Navigate to Edit Business */ }
            )
            ProfileOptionItem(
                title = "Switch to Buyer Mode",
                subtitle = "Shop for pure forest products",
                icon = Icons.Default.ShoppingBag,
                tint = AccentOrange,
                onClick = onSwitchRole
            )
            ProfileOptionItem(
                title = "Settings",
                subtitle = "App preferences and language",
                icon = Icons.Default.Settings,
                onClick = { /* Settings */ }
            )
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EarthBrown),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = ForestGreen, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
