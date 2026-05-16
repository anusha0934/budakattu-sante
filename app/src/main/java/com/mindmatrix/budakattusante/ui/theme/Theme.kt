package com.mindmatrix.budakattusante.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors: ColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    secondary = EarthBrown,
    onSecondary = Color.White,
    tertiary = AccentOrange,
    background = Cream,
    surface = Color.White,
    onSurface = Color(0xFF2D2D2D),
    surfaceVariant = Color(0xFFFDF8F1), // Using a very light cream for variants
    onSurfaceVariant = Color(0xFF4A4A4A),
    outline = Color(0xFFD1C4B1)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF9BC7A7),
    secondary = Color(0xFFE8B06B),
    tertiary = Color(0xFF9EC3DF),
    background = Color(0xFF171915),
    surface = Color(0xFF20251F),
    surfaceVariant = Color(0xFF3E463B)
)

@Composable
fun BudakattuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
