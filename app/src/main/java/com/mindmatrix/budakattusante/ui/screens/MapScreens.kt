package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mindmatrix.budakattusante.ui.theme.Cream
import com.mindmatrix.budakattusante.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val uiState by productViewModel.uiState.collectAsStateWithLifecycle()
    val brHills = LatLng(11.9961, 77.1355)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(brHills, 11f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tribal Sante Markets", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                // Display all product source locations as markers
                uiState.catalog.forEach { product ->
                    Marker(
                        state = MarkerState(position = LatLng(product.locationLat, product.locationLng)),
                        title = product.name,
                        snippet = "Harvested in ${product.forestRegion} by ${product.tribeName}",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
                        )
                    )
                }
            }
            
            if (uiState.catalog.isEmpty()) {
                Surface(
                    modifier = Modifier.padding(16.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Loading nearby tribal markets...",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
