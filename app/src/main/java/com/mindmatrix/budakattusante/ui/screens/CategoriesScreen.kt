package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindmatrix.budakattusante.ui.components.BudakattuBottomNav
import com.mindmatrix.budakattusante.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onOrders: () -> Unit,
    onCart: () -> Unit,
    onProfile: () -> Unit
) {
    val categories = listOf(
        "Raw Honey", "Wild Berries", "Tribal Ragi", 
        "Forest Tubers", "Handicrafts", "Medicinal Herbs"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        bottomBar = {
            BudakattuBottomNav(
                currentScreen = "Categories",
                onHome = onHome,
                onCategories = { /* Already here */ },
                onOrders = onOrders,
                onCart = onCart,
                onProfile = onProfile
            )
        },
        containerColor = Cream
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(categories) { category ->
                CategoryCard(category) { onCategoryClick(category) }
            }
        }
    }
}

@Composable
fun CategoryCard(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
        }
    }
}
