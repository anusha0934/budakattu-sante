package com.mindmatrix.budakattusante.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mindmatrix.budakattusante.ui.theme.ForestGreen

@Composable
fun BudakattuBottomNav(
    currentScreen: String,
    onHome: () -> Unit,
    onCategories: () -> Unit,
    onOrders: () -> Unit,
    onCart: () -> Unit,
    onProfile: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == "Home",
            onClick = onHome,
            icon = { Icon(if (currentScreen == "Home") Icons.Filled.Home else Icons.Outlined.Home, null) },
            label = { Text("Sante") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ForestGreen,
                selectedTextColor = ForestGreen,
                indicatorColor = ForestGreen.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == "Categories",
            onClick = onCategories,
            icon = { Icon(if (currentScreen == "Categories") Icons.Filled.Category else Icons.Outlined.Category, null) },
            label = { Text("Explore") }
        )
        NavigationBarItem(
            selected = currentScreen == "Orders",
            onClick = onOrders,
            icon = { Icon(if (currentScreen == "Orders") Icons.Filled.Assignment else Icons.Outlined.Assignment, null) },
            label = { Text("Orders") }
        )
        NavigationBarItem(
            selected = currentScreen == "Cart",
            onClick = onCart,
            icon = { Icon(if (currentScreen == "Cart") Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart, null) },
            label = { Text("Basket") }
        )
        NavigationBarItem(
            selected = currentScreen == "Profile",
            onClick = onProfile,
            icon = { Icon(if (currentScreen == "Profile") Icons.Filled.Person else Icons.Outlined.Person, null) },
            label = { Text("Profile") }
        )
    }
}
