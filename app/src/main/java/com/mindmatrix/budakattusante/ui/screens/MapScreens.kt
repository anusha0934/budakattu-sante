package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.mindmatrix.budakattusante.ui.theme.Cream
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val brHills = LatLng(11.9961, 77.1355)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(brHills, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tribal Markets", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { padding ->
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(padding),
            cameraPositionState = cameraPositionState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorNearbyMarketsScreen(onBack: () -> Unit) {
    MapScreen(onBack = onBack)
}
