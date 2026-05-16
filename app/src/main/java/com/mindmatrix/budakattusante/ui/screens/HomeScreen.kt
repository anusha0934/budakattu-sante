package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val products = remember {
        mutableStateListOf(
            "Bamboo Basket",
            "Organic Honey",
            "Tribal Necklace",
            "Handmade Pot"
        )
    }

    Scaffold(

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = {
                        Icon(Icons.Default.Home, contentDescription = null)
                    },
                    label = {
                        Text("Home")
                    }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {

                    },
                    icon = {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    },
                    label = {
                        Text("Cart")
                    }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {

                    },
                    icon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    label = {
                        Text("Profile")
                    }
                )
            }
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDF8F1))
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Text(
                text = "Budakattu Santé",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = {
                    Text("Search Products")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D5A27)
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Support Local Tribal Communities",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {

                    navController.navigate("vendor")

                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(text = "Vendor")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Products",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2)
            ) {

                items(products) { product ->

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Text(
                                text = product,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "₹350",
                                color = Color(0xFF2D5A27),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}