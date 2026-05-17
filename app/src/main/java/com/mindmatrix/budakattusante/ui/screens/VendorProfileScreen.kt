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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.data.model.UserProfile
import com.mindmatrix.budakattusante.ui.components.ProfileOptionItem
import com.mindmatrix.budakattusante.ui.components.StatCard
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.VoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onSwitchRole: () -> Unit,
    onEditBusiness: () -> Unit,
    onManageArtisans: () -> Unit,
    onViewPayments: () -> Unit,
    onViewAnalytics: () -> Unit,
    userProfile: UserProfile?,
    voiceViewModel: VoiceViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { voiceViewModel.speak("Your shop profile. Manage your business details, artisans, and payments here.") }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, "Help", tint = ForestGreen)
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
                text = userProfile?.businessName?.ifBlank { "Tribal Shop" } ?: "Tribal Shop",
                style = MaterialTheme.typography.headlineMedium,
                color = ForestGreen,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Community: ${userProfile?.tribeName?.ifBlank { "B.R. Hills" } ?: "B.R. Hills"}",
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
                    label = "Items",
                    value = "12",
                    icon = Icons.Default.Inventory2
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Trust",
                    value = "4.9",
                    icon = Icons.Default.Verified
                )
            }

            Spacer(Modifier.height(32.dp))

            // Options
            ProfileOptionItem(
                title = "Edit Business Details",
                subtitle = "Shop name, location, and UPI info",
                icon = Icons.Default.Edit,
                onClick = onEditBusiness
            )
            
            ProfileOptionItem(
                title = "Manage Artisans",
                subtitle = "Add family members and collectors",
                icon = Icons.Default.Groups,
                onClick = onManageArtisans
            )
            
            ProfileOptionItem(
                title = "Payments & Wallet",
                subtitle = "Earnings and payout history",
                icon = Icons.Default.AccountBalanceWallet,
                onClick = onViewPayments
            )
            
            ProfileOptionItem(
                title = "Analytics & Supply Log",
                subtitle = "Track your sales and harvest history",
                icon = Icons.Default.BarChart,
                onClick = onViewAnalytics
            )

            ProfileOptionItem(
                title = "Switch to Customer Mode",
                subtitle = "Shop for pure forest products",
                icon = Icons.Default.ShoppingBag,
                tint = AccentOrange,
                onClick = onSwitchRole
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
                Text("Logout from Sante", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}
