package com.mindmatrix.budakattusante.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import com.mindmatrix.budakattusante.ui.theme.BudakattuTheme

/**
 * ForestTextureBackground - A high-quality seamless background texture.
 * Features a very light desaturated green base with subtle, semi-transparent 
 * organic patterns of leaves and bamboo silhouettes to create depth.
 */
@Composable
fun ForestTextureBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val lightDesaturatedGreen = Color(0xFFF1F8E9)
    val forestGreenPattern = Color(0xFF2D5A27).copy(alpha = 0.03f)
    val secondaryGreenPattern = Color(0xFF1B3022).copy(alpha = 0.02f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(lightDesaturatedGreen)
            .drawWithCache {
                onDrawBehind {
                    val w = size.width
                    val h = size.height

                    // 1. Subtle Bamboo Vertical Silhouettes
                    val bambooCount = 5
                    for (i in 0..bambooCount) {
                        val x = (i * (w / bambooCount)) + (w / 10)
                        val bambooWidth = 40f
                        
                        // Main stalk
                        drawLine(
                            color = forestGreenPattern,
                            start = Offset(x, -50f),
                            end = Offset(x + 20f, h + 50f),
                            strokeWidth = bambooWidth
                        )
                        
                        // Bamboo joints/notches
                        val notches = 8
                        for (j in 0..notches) {
                            val y = j * (h / notches)
                            rotate(degrees = 3f, pivot = Offset(x, y)) {
                                drawLine(
                                    color = forestGreenPattern.copy(alpha = 0.05f),
                                    start = Offset(x - 25f, y),
                                    end = Offset(x + 45f, y + 2f),
                                    strokeWidth = 3f
                                )
                            }
                        }
                    }

                    // 2. Organic Floating Leaves
                    val leafPositions = listOf(
                        Offset(0.15f, 0.12f), Offset(0.85f, 0.18f), 
                        Offset(0.40f, 0.45f), Offset(0.72f, 0.65f), 
                        Offset(0.25f, 0.82f), Offset(0.92f, 0.88f),
                        Offset(0.55f, 0.22f), Offset(0.12f, 0.55f),
                        Offset(0.88f, 0.40f)
                    )

                    leafPositions.forEachIndexed { index, pos ->
                        val lx = pos.x * w
                        val ly = pos.y * h
                        val color = if (index % 2 == 0) forestGreenPattern else secondaryGreenPattern
                        
                        rotate(degrees = (index * 45f) % 360f, pivot = Offset(lx, ly)) {
                            val leafPath = Path().apply {
                                moveTo(lx, ly)
                                quadraticTo(lx + 35f, ly - 50f, lx + 70f, ly)
                                quadraticTo(lx + 35f, ly + 50f, lx, ly)
                                close()
                            }
                            drawPath(leafPath, color = color)
                            
                            // Subtle vein
                            drawLine(
                                color = color.copy(alpha = 0.04f),
                                start = Offset(lx, ly),
                                end = Offset(lx + 70f, ly),
                                strokeWidth = 1.5f
                            )
                        }
                    }
                    
                    // 3. Very soft radial gradient for depth
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                            center = Offset(w * 0.5f, h * 0.3f),
                            radius = w * 0.8f
                        )
                    )
                }
            }
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun ForestTextureBackgroundPreview() {
    BudakattuTheme {
        ForestTextureBackground {
            // Content can go here
        }
    }
}
