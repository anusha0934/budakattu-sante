package com.mindmatrix.budakattusante.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.data.model.UserProfile
import com.mindmatrix.budakattusante.ui.theme.Cream
import com.mindmatrix.budakattusante.ui.theme.ForestGreen
import com.mindmatrix.budakattusante.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val message by profileViewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var tribeName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name
            businessName = it.businessName
            tribeName = it.tribeName
            phone = it.phoneNumber
            upiId = it.upiId
            address = it.address
            village = it.village
            district = it.district
            description = it.description
            categories = it.categories.joinToString(", ")
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.clearMessage()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Business Profile", fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ForestGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Image Picker
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, ForestGreen, CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null || userProfile?.profileImageUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = imageUri ?: userProfile?.profileImageUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = ForestGreen.copy(0.3f))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(ForestGreen, CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                }

                Text("Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)

                ProfileTextField(value = name, onValueChange = { name = it }, label = "Full Name", icon = Icons.Default.Person)
                ProfileTextField(value = businessName, onValueChange = { businessName = it }, label = "Business Name", icon = Icons.Default.Store)
                ProfileTextField(value = tribeName, onValueChange = { tribeName = it }, label = "Tribe Name", icon = Icons.Default.Groups)
                ProfileTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", icon = Icons.Default.Phone)
                ProfileTextField(value = upiId, onValueChange = { upiId = it }, label = "UPI ID (for payments)", icon = Icons.Default.AccountBalanceWallet)

                Text("Location Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)

                ProfileTextField(value = address, onValueChange = { address = it }, label = "Full Address", icon = Icons.Default.LocationOn)
                ProfileTextField(value = village, onValueChange = { village = it }, label = "Village", icon = Icons.Default.Home)
                ProfileTextField(value = district, onValueChange = { district = it }, label = "District", icon = Icons.Default.Map)

                Text("Business Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)

                ProfileTextField(value = categories, onValueChange = { categories = it }, label = "Product Categories (comma separated)", icon = Icons.Default.Category)
                ProfileTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Business Description",
                    icon = Icons.Default.Description,
                    modifier = Modifier.height(120.dp),
                    singleLine = false
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || businessName.isBlank() || phone.isBlank() || upiId.isBlank()) {
                            profileViewModel.loadProfile() // Hack to trigger something or just show error
                            return@Button
                        }
                        userProfile?.let {
                            val updatedProfile = it.copy(
                                name = name,
                                businessName = businessName,
                                tribeName = tribeName,
                                phoneNumber = phone,
                                upiId = upiId,
                                address = address,
                                village = village,
                                district = district,
                                description = description,
                                categories = categories.split(",").map { cat -> cat.trim() }.filter { cat -> cat.isNotEmpty() }
                            )
                            profileViewModel.updateProfile(updatedProfile, imageUri)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(icon, null) },
        singleLine = singleLine
    )
}
