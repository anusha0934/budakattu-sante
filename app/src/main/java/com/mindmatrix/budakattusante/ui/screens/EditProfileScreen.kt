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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mindmatrix.budakattusante.ui.theme.Cream
import com.mindmatrix.budakattusante.ui.theme.ForestGreen
import com.mindmatrix.budakattusante.ui.viewmodel.ProfileViewModel
import com.mindmatrix.budakattusante.ui.viewmodel.VoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    voiceViewModel: VoiceViewModel = hiltViewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val message by profileViewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var tribeName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var forestRegion by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name
            businessName = it.businessName
            tribeName = it.tribeName
            phone = it.phoneNumber
            email = it.email
            upiId = it.upiId
            address = it.address
            village = it.village
            district = it.district
            state = it.state
            forestRegion = it.forestRegion
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
                title = { Text("Edit Profile & Business", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { voiceViewModel.speak("Update your name, business name, and tribal details here.") }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, "Help", tint = ForestGreen)
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
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                
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

                Text("Owner Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Person, null, tint = ForestGreen) })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Phone, null, tint = ForestGreen) })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Email, null, tint = ForestGreen) })
                
                Text("Business & Tribal Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("Business/Shop Name*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Store, null, tint = ForestGreen) })
                OutlinedTextField(value = tribeName, onValueChange = { tribeName = it }, label = { Text("Tribe/Community Name*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Groups, null, tint = ForestGreen) })
                OutlinedTextField(value = forestRegion, onValueChange = { forestRegion = it }, label = { Text("Forest Region (e.g. B.R. Hills)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Eco, null, tint = ForestGreen) })
                OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("UPI ID for Payments*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null, tint = ForestGreen) })

                Text("Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village/Podu Name*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Home, null, tint = ForestGreen) })
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Map, null, tint = ForestGreen) })
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Public, null, tint = ForestGreen) })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Full Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = ForestGreen) })

                Text("Bio & Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                OutlinedTextField(value = categories, onValueChange = { categories = it }, label = { Text("Product Categories (comma separated)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, tint = ForestGreen) })
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Business Description (Tell your story)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Description, null, tint = ForestGreen) },
                    singleLine = false
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || businessName.isBlank() || phone.isBlank() || upiId.isBlank() || tribeName.isBlank()) {
                            voiceViewModel.speak("Please fill all required fields marked with star.")
                            return@Button
                        }
                        userProfile?.let {
                            val updatedProfile = it.copy(
                                name = name,
                                businessName = businessName,
                                tribeName = tribeName,
                                phoneNumber = phone,
                                email = email,
                                upiId = upiId,
                                address = address,
                                village = village,
                                district = district,
                                state = state,
                                forestRegion = forestRegion,
                                description = description,
                                categories = categories.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            )
                            profileViewModel.updateProfile(updatedProfile, imageUri)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Save All Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
