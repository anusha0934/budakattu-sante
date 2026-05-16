package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import com.mindmatrix.budakattusante.data.local.entity.WishlistEntity
import com.mindmatrix.budakattusante.ui.theme.Cream
import com.mindmatrix.budakattusante.ui.theme.ForestGreen
import com.mindmatrix.budakattusante.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val uiState by productViewModel.uiState.collectAsStateWithLifecycle()
    val wishlist = uiState.wishlist

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wishlist", fontWeight = FontWeight.Bold) },
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
        if (wishlist.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Favorite, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
                    Text("Your wishlist is empty", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(wishlist) { item ->
                    WishlistItemRow(item, onProductClick)
                }
            }
        }
    }
}

@Composable
fun WishlistItemRow(item: WishlistEntity, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(item.productId) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
                Text("₹${item.price.toInt()}", color = ForestGreen, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { /* TODO: Implement quick add to cart from wishlist */ }) {
                Icon(Icons.Default.ShoppingCart, null, tint = ForestGreen)
            }
        }
    }
}
