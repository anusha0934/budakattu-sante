package com.mindmatrix.budakattusante.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.util.ConnectivityObserver

/**
 * Requirement 7 & 12: Online/Offline Banner.
 * Displays the current connectivity status and sync progress.
 */
@Composable
fun ConnectivityBanner(status: ConnectivityObserver.Status) {
    val (backgroundColor, text, icon) = when (status) {
        ConnectivityObserver.Status.Wifi, ConnectivityObserver.Status.Mobile, ConnectivityObserver.Status.Available -> 
            Triple(Color(0xFF4CAF50), "Online", Icons.Default.Wifi)
        ConnectivityObserver.Status.Unavailable, ConnectivityObserver.Status.Lost -> 
            Triple(Color(0xFFF44336), "Offline Mode Active", Icons.Default.CloudOff)
        ConnectivityObserver.Status.Losing -> 
            Triple(Color(0xFFFFC107), "Connection Unstable...", Icons.Default.Sync)
    }

    AnimatedVisibility(
        visible = status != ConnectivityObserver.Status.Wifi && status != ConnectivityObserver.Status.Mobile && status != ConnectivityObserver.Status.Available,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 4.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Requirement 12: Sync Progress Indicator.
 * Shown on dashboards when items are pending upload.
 */
@Composable
fun SyncStatusBadge(pendingCount: Int, isSyncing: Boolean) {
    if (pendingCount > 0 || isSyncing) {
        Surface(
            color = if (isSyncing) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFEF6C00)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isSyncing) "Syncing..." else "$pendingCount pending",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSyncing) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
