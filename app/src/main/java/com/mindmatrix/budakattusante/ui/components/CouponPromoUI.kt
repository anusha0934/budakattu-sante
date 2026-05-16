package com.mindmatrix.budakattusante.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.ui.theme.BudakattuTheme
import com.mindmatrix.budakattusante.ui.theme.ForestGreen
import com.mindmatrix.budakattusante.ui.theme.LightMintGreen

/**
 * CouponPromoUI - A stylish UI element for the checkout screen.
 * Includes a dashed-border input field, a forest green apply button,
 * and a list of available offers with leaf icons.
 */
@Composable
fun CouponPromoUI(
    modifier: Modifier = Modifier,
    onApplyCoupon: (String) -> Unit = {}
) {
    var couponCode by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LightMintGreen, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "Have a Coupon?",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Input with Dashed Border
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 12.dp)
                    .drawBehindDashedBorder(color = ForestGreen.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (couponCode.isEmpty()) {
                    Text(
                        text = "Enter Coupon Code",
                        color = ForestGreen.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                BasicTextField(
                    value = couponCode,
                    onValueChange = { couponCode = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = ForestGreen,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Vibrant Apply Button
            Button(
                onClick = { onApplyCoupon(couponCode) },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Text(
                    text = "Apply",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Available Offers",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ForestGreen.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // List of Offers
        val offers = listOf(
            "TRIBAL20" to "Get 20% off on all tribal crafts",
            "HARVEST10" to "Extra 10% off on forest honey",
            "FREESHIP" to "Free shipping on orders above ₹999"
        )

        offers.forEach { (code, description) ->
            OfferItem(code = code, description = description)
        }
    }
}

@Composable
fun OfferItem(code: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leaf-themed icon
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = ForestGreen
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = code,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ForestGreen.copy(alpha = 0.6f)
                )
            )
        }
    }
}

/**
 * Extension to draw a dashed border behind the content.
 */
fun Modifier.drawBehindDashedBorder(color: Color) = this.then(
    Modifier.drawWithContent {
        drawContent()
        val stroke = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
        )
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(12.dp.toPx())
        )
    }
)

@Preview(showBackground = true)
@Composable
fun CouponPromoUIPreview() {
    BudakattuTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            CouponPromoUI()
        }
    }
}
