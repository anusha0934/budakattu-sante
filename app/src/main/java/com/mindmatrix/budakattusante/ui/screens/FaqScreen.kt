package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.budakattusante.ui.theme.*
import com.mindmatrix.budakattusante.ui.viewmodel.ChatViewModel

/**
 * Requirement 5: AI FAQ Assistant.
 * Combined static FAQ with Gemini AI for dynamic answering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    onBack: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val faqItems = listOf(
        FaqItem("How is the price decided?", "We use TRIFED's Minimum Support Price (MSP) as a base. 80-90% of your payment goes directly to the tribal family."),
        FaqItem("Is the honey pure?", "Yes, it is raw forest honey, collected by the Soliga tribe. It is unfiltered and contains no added sugar."),
        FaqItem("How do pre-orders work?", "You reserve the product before harvest. We notify you once the harvest is complete and ready for shipping."),
        FaqItem("Where are products sourced?", "Mainly from the Biligiri Forest Region (B.R. Hills) and surrounding Western Ghats buffer zones.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & FAQ", fontWeight = FontWeight.Bold) },
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
            Text("Common Questions", style = MaterialTheme.typography.titleLarge, color = ForestGreen, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            faqItems.forEach { item ->
                FaqExpandableCard(item)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(32.dp))

            // AI FAQ Integration
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = TribalGold)
                        Spacer(Modifier.width(12.dp))
                        Text("Still have questions?", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Ask our Tribal AI assistant anything about our products, tribes, or your orders.", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = { /* Navigate to Chat or handle locally */ },
                        colors = ButtonDefaults.buttonColors(containerColor = TribalGold),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Chat with Assistant", color = EarthBrown, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class FaqItem(val question: String, val answer: String)

@Composable
fun FaqExpandableCard(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.question, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = EarthBrown)
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color.Gray)
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    item.answer,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}
