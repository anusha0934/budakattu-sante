package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.budakattusante.ui.theme.Cream
import com.mindmatrix.budakattusante.ui.theme.ForestGreen
import com.mindmatrix.budakattusante.ui.viewmodel.ChatMessage
import com.mindmatrix.budakattusante.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tribal AI Assistant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Online • Powered by Gemini", style = MaterialTheme.typography.labelSmall, color = ForestGreen)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.messages.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Namaskara! I am your Tribal Assistant.", color = ForestGreen, fontWeight = FontWeight.Bold)
                                Text("Ask me about forest products, prices, or app help.", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }

                items(uiState.messages) { message ->
                    ChatBubble(message)
                }

                if (uiState.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.CenterStart) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = ForestGreen)
                        }
                    }
                }

                uiState.error?.let { error ->
                    item {
                        Text(error, color = Color.Red, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Input Area
            Surface(
                tonalElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type your question...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        maxLines = 3
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !uiState.isLoading,
                        modifier = Modifier
                            .background(if (inputText.isNotBlank()) ForestGreen else Color.LightGray, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) ForestGreen else Color.White
    val contentColor = if (message.isUser) Color.White else Color.Black
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = shape,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
