package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.ui.components.BudakattuEmblem
import com.mindmatrix.budakattusante.ui.theme.ForestGreen
import com.mindmatrix.budakattusante.ui.theme.LightCream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 10000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
        delay(3500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Background decorative ring
                Surface(
                    modifier = Modifier
                        .size(320.dp)
                        .rotate(rotation.value),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = ForestGreen.copy(alpha = 0.1f)
                    )
                ) {}

                // THE NEW VECTOR LOGO
                BudakattuEmblem(
                    modifier = Modifier.padding(20.dp)
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "From our forest, for your wellness",
                fontSize = 16.sp,
                color = Color.Gray.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(48.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = ForestGreen.copy(alpha = 0.4f),
                strokeWidth = 3.dp
            )
        }
        
        // Forest Decoration at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.BottomCenter)
        ) {
             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(bottom = 30.dp),
                 horizontalArrangement = Arrangement.SpaceEvenly,
                 verticalAlignment = Alignment.Bottom
             ) {
                 Icon(Icons.Default.Forest, null, tint = ForestGreen.copy(alpha = 0.1f), modifier = Modifier.size(50.dp))
                 Icon(Icons.Default.Forest, null, tint = ForestGreen.copy(alpha = 0.2f), modifier = Modifier.size(90.dp))
                 Icon(Icons.Default.Forest, null, tint = ForestGreen.copy(alpha = 0.1f), modifier = Modifier.size(70.dp))
             }
        }
    }
}
