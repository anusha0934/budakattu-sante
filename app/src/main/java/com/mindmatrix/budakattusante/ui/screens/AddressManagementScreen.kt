package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindmatrix.budakattusante.data.model.Address
import com.mindmatrix.budakattusante.data.model.AddressType
import com.mindmatrix.budakattusante.ui.theme.Cream
import com.mindmatrix.budakattusante.ui.theme.ForestGreen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressManagementScreen(
    addresses: List<Address>,
    onAddAddress: (Address) -> Unit,
    onDeleteAddress: (Address) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Addresses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ForestGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Address", tint = Color.White)
            }
        },
        containerColor = Cream
    ) { padding ->
        if (addresses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No addresses saved yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(addresses) { address ->
                    AddressItem(address, onDeleteAddress)
                }
            }
        }
    }

    if (showAddDialog) {
        AddAddressDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { address ->
                onAddAddress(address)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddressItem(address: Address, onDelete: (Address) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, null, tint = ForestGreen)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(address.name, fontWeight = FontWeight.Bold)
                Text(address.street, style = MaterialTheme.typography.bodySmall)
                Text("${address.city}, ${address.state} - ${address.zipCode}", style = MaterialTheme.typography.bodySmall)
                Text(address.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = { onDelete(address) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun AddAddressDialog(onDismiss: () -> Unit, onConfirm: (Address) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Address") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street Address") })
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") })
                OutlinedTextField(value = zip, onValueChange = { zip = it }, label = { Text("Zip Code") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    Address(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        phone = phone,
                        street = street,
                        city = city,
                        state = "Karnataka",
                        zipCode = zip,
                        type = AddressType.HOME
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
