package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No addresses saved yet", color = Color.Gray)
                }
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Cream
            ) {
                Icon(Icons.Default.LocationOn, null, tint = ForestGreen, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(address.name, fontWeight = FontWeight.Bold, color = ForestGreen)
                Text(address.street, style = MaterialTheme.typography.bodySmall)
                if (address.landmark.isNotBlank()) {
                    Text("Landmark: ${address.landmark}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Text("${address.city}, ${address.state} - ${address.zipCode}", style = MaterialTheme.typography.bodySmall)
                Text(address.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = { onDelete(address) }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun AddAddressDialog(onDismiss: () -> Unit, onConfirm: (Address) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Delivery Address", fontWeight = FontWeight.Bold, color = ForestGreen) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name*") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number*") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street Address*") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark (Optional)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City*") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = zip, onValueChange = { zip = it }, label = { Text("Pincode*") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && street.isNotBlank() && city.isNotBlank() && zip.isNotBlank()) {
                        onConfirm(
                            Address(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                phone = phone,
                                street = street,
                                landmark = landmark,
                                city = city,
                                state = "Karnataka",
                                zipCode = zip,
                                type = AddressType.HOME
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) { Text("Save Address") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
