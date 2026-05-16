package com.mindmatrix.budakattusante.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.ui.theme.*

/**
 * BudakattuEmblem - A detailed tribal emblem for the Budakattu Sante app.
 * Refined with the 'Modern Ethnic' color palette and typography.
 */
@Composable
fun BudakattuEmblem(
    modifier: Modifier = Modifier,
    textColor: Color = ForestGreen,
    iconColor: Color? = null,
    showText: Boolean = true
) {
    val woodBrown = EarthBrown
    val forestGreen = iconColor ?: ForestGreen
    val accentOrange = AccentOrange
    val glowingGreen = Color(0xFF00FF00)
    val leafGreen = ForestGreen.copy(alpha = 0.8f)
    val earthYellow = TribalGold

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center, 
            modifier = if (showText) Modifier.size(320.dp) else Modifier.fillMaxHeight()
        ) {
            Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
                val center = center
                val radius = size.minDimension / 2

                // 1. Rising Sun with Sharp Rays (High Contrast)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentOrange.copy(alpha = 0.3f), Color.Transparent),
                        center = center.copy(y = center.y - 20f * (size.minDimension / 320f)),
                        radius = radius * 0.8f
                    ),
                    radius = radius * 0.8f,
                    center = center.copy(y = center.y - 20f * (size.minDimension / 320f))
                )
                
                for (i in 0 until 18) {
                    rotate(i * 20f, pivot = center.copy(y = center.y - 20f * (size.minDimension / 320f))) {
                        val rayPath = Path().apply {
                            moveTo(center.x, center.y - 20f * (size.minDimension / 320f) - radius * 0.7f)
                            lineTo(center.x - 12f * (size.minDimension / 320f), center.y - 20f * (size.minDimension / 320f) - radius * 1.0f)
                            lineTo(center.x + 12f * (size.minDimension / 320f), center.y - 20f * (size.minDimension / 320f) - radius * 1.0f)
                            close()
                        }
                        drawPath(rayPath, color = accentOrange.copy(alpha = 0.4f))
                    }
                }

                // 2. Rugged Mountains (Folk Art Style)
                val mountainPath = Path().apply {
                    moveTo(center.x - radius * 0.95f, center.y + 40f * (size.minDimension / 320f))
                    lineTo(center.x - radius * 0.5f, center.y - 60f * (size.minDimension / 320f))
                    lineTo(center.x - radius * 0.2f, center.y + 20f * (size.minDimension / 320f))
                    lineTo(center.x + radius * 0.35f, center.y - 80f * (size.minDimension / 320f))
                    lineTo(center.x + radius * 0.6f, center.y - 10f * (size.minDimension / 320f))
                    lineTo(center.x + radius * 0.85f, center.y - 110f * (size.minDimension / 320f))
                    lineTo(center.x + radius * 1.0f, center.y + 40f * (size.minDimension / 320f))
                    close()
                }
                drawPath(mountainPath, color = forestGreen.copy(alpha = 0.2f))
                drawPath(mountainPath, color = forestGreen.copy(alpha = 0.4f), style = Stroke(width = 2f * (size.minDimension / 320f)))

                // 3. Crossed Bamboo Stalks (Bottom)
                val bambooPivot = center.copy(y = center.y + 100f * (size.minDimension / 320f))
                listOf(40f, -40f).forEach { angle ->
                    rotate(angle, pivot = bambooPivot) {
                        drawRoundRect(
                            color = woodBrown,
                            topLeft = Offset(center.x - 8f * (size.minDimension / 320f), center.y + 20f * (size.minDimension / 320f)),
                            size = Size(16f * (size.minDimension / 320f), radius * 0.9f),
                            cornerRadius = CornerRadius(8f * (size.minDimension / 320f), 8f * (size.minDimension / 320f))
                        )
                        // Bamboo notches
                        for (j in 1..4) {
                            drawLine(
                                color = Color.White.copy(0.3f),
                                start = Offset(center.x - 8f * (size.minDimension / 320f), center.y + 20f * (size.minDimension / 320f) + (j * 40f * (size.minDimension / 320f))),
                                end = Offset(center.x + 8f * (size.minDimension / 320f), center.y + 20f * (size.minDimension / 320f) + (j * 40f * (size.minDimension / 320f))),
                                strokeWidth = 2f * (size.minDimension / 320f)
                            )
                        }
                    }
                }

                // 4. Central Detailed Tribal Mask
                val maskWidth = 110f * (size.minDimension / 320f)
                val maskHeight = 160f * (size.minDimension / 320f)
                val maskRect = Rect(center.x - maskWidth / 2, center.y - maskHeight / 2, center.x + maskWidth / 2, center.y + maskHeight / 2)
                
                // Shadow
                drawOval(
                    color = Color.Black.copy(0.2f),
                    topLeft = maskRect.topLeft.copy(x = maskRect.left + 5f * (size.minDimension / 320f), y = maskRect.top + 5f * (size.minDimension / 320f)),
                    size = maskRect.size
                )
                
                // Mask Base (Grainy wood effect simulation)
                drawOval(color = woodBrown, topLeft = maskRect.topLeft, size = maskRect.size)
                
                // Glowing Green Eyes
                val eyeY = center.y - 30f * (size.minDimension / 320f)
                drawCircle(color = glowingGreen, radius = 9f * (size.minDimension / 320f), center = Offset(center.x - 26f * (size.minDimension / 320f), eyeY), style = Stroke(width = 2f * (size.minDimension / 320f)))
                drawCircle(color = glowingGreen, radius = 9f * (size.minDimension / 320f), center = Offset(center.x + 26f * (size.minDimension / 320f), eyeY), style = Stroke(width = 2f * (size.minDimension / 320f)))
                drawCircle(color = glowingGreen.copy(alpha = 0.8f), radius = 5f * (size.minDimension / 320f), center = Offset(center.x - 26f * (size.minDimension / 320f), eyeY))
                drawCircle(color = glowingGreen.copy(alpha = 0.8f), radius = 5f * (size.minDimension / 320f), center = Offset(center.x + 26f * (size.minDimension / 320f), eyeY))

                // Intricate Tribal Face Patterns
                val patternPath = Path().apply {
                    // Forehead patterns
                    moveTo(center.x - 30f * (size.minDimension / 320f), center.y - 60f * (size.minDimension / 320f))
                    lineTo(center.x + 30f * (size.minDimension / 320f), center.y - 60f * (size.minDimension / 320f))
                    // Nose line
                    moveTo(center.x, center.y - 50f * (size.minDimension / 320f))
                    lineTo(center.x, center.y + 30f * (size.minDimension / 320f))
                    // Cheek scrolls
                    addArc(Rect(center.x - 45f * (size.minDimension / 320f), center.y - 10f * (size.minDimension / 320f), center.x - 15f * (size.minDimension / 320f), center.y + 20f * (size.minDimension / 320f)), 0f, 180f)
                    addArc(Rect(center.x + 15f * (size.minDimension / 320f), center.y - 10f * (size.minDimension / 320f), center.x + 45f * (size.minDimension / 320f), center.y + 20f * (size.minDimension / 320f)), 0f, 180f)
                }
                drawPath(patternPath, color = earthYellow.copy(alpha = 0.4f), style = Stroke(width = 2f * (size.minDimension / 320f)))
                
                // 5. Green Leaf Crown
                for (i in -4..4) {
                    rotate(i * 22f, pivot = center.copy(y = center.y - maskHeight / 2 + 20f * (size.minDimension / 320f))) {
                        val leafPath = Path().apply {
                            moveTo(center.x, center.y - maskHeight / 2 - 60f * (size.minDimension / 320f))
                            quadraticTo(center.x - 20f * (size.minDimension / 320f), center.y - maskHeight / 2 - 30f * (size.minDimension / 320f), center.x, center.y - maskHeight / 2)
                            quadraticTo(center.x + 20f * (size.minDimension / 320f), center.y - maskHeight / 2 - 30f * (size.minDimension / 320f), center.x, center.y - maskHeight / 2 - 60f * (size.minDimension / 320f))
                        }
                        drawPath(leafPath, color = leafGreen)
                        drawPath(leafPath, color = Color.White.copy(0.2f), style = Stroke(width = 1f * (size.minDimension / 320f)))
                    }
                }

                // 6. Flanking Elements
                // Flowering Plant in Pot (Left)
                val potX = center.x - radius * 0.8f
                val potY = center.y + 60f * (size.minDimension / 320f)
                // Pot
                val potPath = Path().apply {
                    moveTo(potX - 25f * (size.minDimension / 320f), potY)
                    lineTo(potX + 25f * (size.minDimension / 320f), potY)
                    lineTo(potX + 20f * (size.minDimension / 320f), potY + 40f * (size.minDimension / 320f))
                    lineTo(potX - 20f * (size.minDimension / 320f), potY + 40f * (size.minDimension / 320f))
                    close()
                }
                drawPath(potPath, color = accentOrange)
                // Plant
                drawLine(color = ForestGreen, start = Offset(potX, potY), end = Offset(potX, potY - 50f * (size.minDimension / 320f)), strokeWidth = 4f * (size.minDimension / 320f))
                drawCircle(color = accentOrange, radius = 12f * (size.minDimension / 320f), center = Offset(potX, potY - 55f * (size.minDimension / 320f)))
                drawCircle(color = earthYellow, radius = 5f * (size.minDimension / 320f), center = Offset(potX, potY - 55f * (size.minDimension / 320f)))

                // Mortar & Pestle (Right)
                val mortarX = center.x + radius * 0.8f
                val mortarY = center.y + 70f * (size.minDimension / 320f)
                // Mortar
                drawArc(
                    color = woodBrown,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(mortarX - 30f * (size.minDimension / 320f), mortarY - 10f * (size.minDimension / 320f)),
                    size = Size(60f * (size.minDimension / 320f), 50f * (size.minDimension / 320f))
                )
                // Pestle
                rotate(-20f, pivot = Offset(mortarX, mortarY)) {
                    drawRoundRect(color = woodBrown, topLeft = Offset(mortarX - 5f * (size.minDimension / 320f), mortarY - 30f * (size.minDimension / 320f)), size = Size(10f * (size.minDimension / 320f), 40f * (size.minDimension / 320f)), cornerRadius = CornerRadius(5f * (size.minDimension / 320f), 5f * (size.minDimension / 320f)))
                }
                // Glass Oil Bottle
                drawRoundRect(
                    color = Color(0xFFB2DFDB).copy(alpha = 0.6f),
                    topLeft = Offset(mortarX + 40f * (size.minDimension / 320f), mortarY - 20f * (size.minDimension / 320f)),
                    size = Size(20f * (size.minDimension / 320f), 45f * (size.minDimension / 320f)),
                    cornerRadius = CornerRadius(6f * (size.minDimension / 320f), 6f * (size.minDimension / 320f))
                )
                drawRect(color = woodBrown, topLeft = Offset(mortarX + 44f * (size.minDimension / 320f), mortarY - 28f * (size.minDimension / 320f)), size = Size(12f * (size.minDimension / 320f), 8f * (size.minDimension / 320f))) // Cork
            }
        }
        
        if (showText) {
            Spacer(Modifier.height(24.dp))
            
            // Clean bold typography (Modern Ethnic)
            Text(
                text = "BUDAKATTU SANTE",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = "TRADITIONAL TRIBAL HARVEST",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentOrange,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
fun TribalTextureBackground(modifier: Modifier = Modifier) {
    val lightBackground = Cream
    val patternColor = ForestGreen.copy(alpha = 0.04f)

    Canvas(modifier = modifier.fillMaxSize().background(lightBackground)) {
        val w = size.width
        val h = size.height

        // 1. Bamboo Silhouettes (Subtle vertical depth)
        for (i in 0..6) {
            val x = (i * (w / 6)) + 20f
            drawLine(
                color = patternColor,
                start = Offset(x, -50f),
                end = Offset(x + 40f, h + 50f),
                strokeWidth = 35f
            )
            // Bamboo notches
            for (j in 0..12) {
                val y = j * (h / 12)
                rotate(5f, pivot = Offset(x, y)) {
                    drawLine(
                        color = patternColor.copy(alpha = 0.08f),
                        start = Offset(x - 20f, y),
                        end = Offset(x + 60f, y + 5f),
                        strokeWidth = 4f
                    )
                }
            }
        }

        // 2. Organic Leaf Patterns (Scattered)
        val leafPositions = listOf(
            Offset(0.1f, 0.1f), Offset(0.8f, 0.15f), Offset(0.3f, 0.4f),
            Offset(0.7f, 0.6f), Offset(0.2f, 0.8f), Offset(0.9f, 0.9f),
            Offset(0.5f, 0.25f), Offset(0.4f, 0.75f), Offset(0.85f, 0.45f)
        )

        leafPositions.forEach { pos ->
            val lx = pos.x * w
            val ly = pos.y * h
            
            rotate(45f, pivot = Offset(lx, ly)) {
                val leafPath = Path().apply {
                    moveTo(lx, ly)
                    quadraticTo(lx + 40f, ly - 60f, lx + 80f, ly)
                    quadraticTo(lx + 40f, ly + 60f, lx, ly)
                    close()
                }
                drawPath(leafPath, color = patternColor)
                // Leaf vein
                drawLine(
                    color = patternColor.copy(alpha = 0.06f),
                    start = Offset(lx, ly),
                    end = Offset(lx + 80f, ly),
                    strokeWidth = 2f
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudakattuEmblemPreview() {
    BudakattuTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream),
            contentAlignment = Alignment.Center
        ) {
            BudakattuEmblem()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TribalTextureBackgroundPreview() {
    BudakattuTheme {
        TribalTextureBackground(Modifier.fillMaxSize())
    }
}
