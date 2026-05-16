package com.mindmatrix.budakattusante.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.R // Ensure this import is correct

// 1. Define the Poppins Font Family
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_semibold, FontWeight.SemiBold)
)

// 2. Set up the Typography to use Poppins
val Typography = Typography(
    // Large Bold Greeting Text style
    displayLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    // Standard Header style
    headlineMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    // Standard Body style
    bodyLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    // Small Label/Location style
    labelMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)