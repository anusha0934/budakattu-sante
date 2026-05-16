package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.data.local.entity.NotificationEntity
import com.mindmatrix.budakattusante.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Requirement 18: Notification System.
 * Requirement 14: Offline Notifications Queue.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notifications: List<NotificationEntity>,
    onBack: () -> Unit,
    onMarkAsRead: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tribal Alerts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = SandBeige
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
                    Text("No new notifications", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification, onMarkAsRead)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationEntity, onMarkAsRead: (String) -> Unit) {
    val icon = when (notification.type) {
        "MSP_UPDATE" -> Icons.Default.TrendingUp
        "ORDER_UPDATE" -> Icons.Default.LocalShipping
        "SYNC_ALERT" -> Icons.Default.Sync
        "LOW_STOCK" -> Icons.Default.Warning
        else -> Icons.Default.Notifications
    }
    
    val color = if (notification.isRead) Color.Gray else ForestGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = { onMarkAsRead(notification.id) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(12.dp))
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    notification.title,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                    color = if (notification.isRead) Color.Gray else Color.Black
                )
                Text(
                    notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Text(
                    text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(notification.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
            
            if (!notification.isRead) {
                Box(modifier = Modifier.size(8.dp).background(ForestGreen, CircleShape))
            }
        }
    }
}
