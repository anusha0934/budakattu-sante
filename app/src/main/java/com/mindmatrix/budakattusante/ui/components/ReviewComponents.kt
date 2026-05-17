package com.mindmatrix.budakattusante.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.data.local.entity.ReviewEntity
import com.mindmatrix.budakattusante.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewSection(
    reviews: List<ReviewEntity>,
    onAddReview: (Double, String) -> Unit
) {
    var showAddReviewDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Community Reviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            TextButton(onClick = { showAddReviewDialog = true }) {
                Text("Write a Review", color = AccentOrange, fontWeight = FontWeight.Bold)
            }
        }

        if (reviews.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = ForestGreen.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "No reviews yet. Be the first to share your experience with this forest treasure!",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EarthBrown,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reviews) { review ->
                    ReviewCard(review)
                }
            }
        }
    }

    if (showAddReviewDialog) {
        AddReviewDialog(
            onDismiss = { showAddReviewDialog = false },
            onSubmit = { rating, comment ->
                onAddReview(rating, comment)
                showAddReviewDialog = false
            }
        )
    }
}

@Composable
fun ReviewCard(review: ReviewEntity) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .heightIn(min = 120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.userName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
                RatingBar(rating = review.rating, size = 14.dp)
            }
            
            Spacer(Modifier.height(4.dp))
            
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(review.timestamp))
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = EarthBrown,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RatingBar(
    rating: Double,
    maxRating: Int = 5,
    size: androidx.compose.ui.unit.Dp = 18.dp
) {
    Row {
        for (i in 1..maxRating) {
            val icon = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder
            val color = if (i <= rating) TribalGold else Color.LightGray
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(size)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Double, String) -> Unit
) {
    var rating by remember { mutableStateOf(5.0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Your Experience", color = ForestGreen, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("How would you rate this product?", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i.toDouble() }) {
                            Icon(
                                if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (i <= rating) TribalGold else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Review") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        focusedLabelColor = ForestGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                enabled = comment.isNotBlank()
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
