package com.mindmatrix.budakattusante.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mindmatrix.budakattusante.R
import com.mindmatrix.budakattusante.ui.components.BudakattuEmblem
import com.mindmatrix.budakattusante.ui.theme.Cream
import com.mindmatrix.budakattusante.ui.theme.EarthBrown
import com.mindmatrix.budakattusante.ui.theme.ForestGreen
import java.util.concurrent.TimeUnit

enum class UserRole {
    VENDOR, CUSTOMER, ADMIN, NONE
}

@Composable
fun IdentityGateScreen(onRoleSelected: (UserRole) -> Unit) {
    var selectedRole by remember { mutableStateOf(UserRole.NONE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ForestGreen, ForestGreen.copy(alpha = 0.7f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Waves,
                null,
                modifier = Modifier.fillMaxSize().alpha(0.1f),
                tint = Color.White
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BudakattuEmblem(
                    modifier = Modifier.scale(0.8f),
                    textColor = Color.White
                )
                
                Spacer(Modifier.height(12.dp))
                Text(
                    "Local. Natural. Our Culture.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
                .offset(y = (-30).dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Cream)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome to the Sante",
                style = MaterialTheme.typography.headlineLarge,
                color = ForestGreen,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IdentityCard(
                    title = "I want to Shop",
                    subtitle = "Buy pure forest products",
                    icon = Icons.Default.ShoppingBag,
                    color = EarthBrown,
                    selected = selectedRole == UserRole.CUSTOMER,
                    onClick = { selectedRole = UserRole.CUSTOMER }
                )
                
                IdentityCard(
                    title = "I want to Sell",
                    subtitle = "Join our tribal collective",
                    icon = Icons.Default.Storefront,
                    color = ForestGreen,
                    selected = selectedRole == UserRole.VENDOR,
                    onClick = { selectedRole = UserRole.VENDOR }
                )

                IdentityCard(
                    title = "Internal Admin",
                    subtitle = "Approve and manage Sante",
                    icon = Icons.Default.AdminPanelSettings,
                    color = Color.DarkGray,
                    selected = selectedRole == UserRole.ADMIN,
                    onClick = { selectedRole = UserRole.ADMIN }
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onRoleSelected(selectedRole) },
                enabled = selectedRole != UserRole.NONE,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get Started", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun IdentityCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) color.copy(alpha = 0.05f) else Color.White,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) color else Color.LightGray.copy(alpha = 0.3f)
        ),
        shadowElevation = if (selected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (selected) color else Color.Gray.copy(alpha = 0.1f)
            ) {
                Icon(
                    icon,
                    null,
                    tint = if (selected) Color.White else color,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (selected) color else Color.Black
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.weight(1f))
            if (selected) {
                Icon(Icons.Default.CheckCircle, null, tint = color)
            }
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    targetRole: String = UserRole.CUSTOMER.name
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = Firebase.auth

    var selectedTab by remember { mutableStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                loading = true
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { taskResult ->
                        loading = false
                        if (taskResult.isSuccessful) {
                            val nextDest = when(targetRole) {
                                UserRole.VENDOR.name -> "vendor_dashboard"
                                UserRole.ADMIN.name -> "admin_dashboard"
                                else -> "home"
                            }
                            navController.navigate(nextDest) {
                                popUpTo("identity_gate") { inclusive = true }
                            }
                        } else {
                            errorMessage = taskResult.exception?.message ?: "Google Login Failed"
                        }
                    }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Google Sign In Error"
            }
        }
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            loading = true
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                loading = false
                if (task.isSuccessful) {
                    val nextDest = when(targetRole) {
                        UserRole.VENDOR.name -> "vendor_dashboard"
                        UserRole.ADMIN.name -> "admin_dashboard"
                        else -> "home"
                    }
                    navController.navigate(nextDest) {
                        popUpTo("identity_gate") { inclusive = true }
                    }
                } else {
                    errorMessage = task.exception?.message ?: "Verification Failed"
                }
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            loading = false
            errorMessage = e.message ?: "Verification Failed"
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            loading = false
            verificationId = id
            isOtpSent = true
            Toast.makeText(context, "OTP Sent", Toast.LENGTH_SHORT).show()
        }
    }

    Box(Modifier.fillMaxSize().background(Cream)) {
        if (loading) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.3f)).zIndex(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ForestGreen)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BudakattuEmblem(modifier = Modifier.scale(0.8f))
            Spacer(Modifier.height(32.dp))
            
            Text("Welcome to Budakattu", style = MaterialTheme.typography.headlineMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
            Text("Sign in to continue as ${targetRole.lowercase().replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            
            Spacer(Modifier.height(24.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = ForestGreen,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ForestGreen
                        )
                    }
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; isOtpSent = false; errorMessage = "" }) {
                    Text("Email", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; errorMessage = "" }) {
                    Text("Phone", modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            if (selectedTab == 0) {
                // Email Login
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please fill all fields"
                            return@Button
                        }
                        loading = true
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { task ->
                                loading = false
                                if (task.isSuccessful) {
                                    val nextDest = when(targetRole) {
                                        UserRole.VENDOR.name -> "vendor_dashboard"
                                        UserRole.ADMIN.name -> "admin_dashboard"
                                        else -> "home"
                                    }
                                    navController.navigate(nextDest) {
                                        popUpTo("identity_gate") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = task.exception?.message ?: "Login Failed"
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Login with Email")
                }
            } else {
                // Phone Login
                if (!isOtpSent) {
                    OutlinedTextField(
                        value = phoneNumber, onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (+91...)") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (phoneNumber.isBlank()) {
                                errorMessage = "Please enter phone number"
                                return@Button
                            }
                            loading = true
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(phoneNumber.trim())
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(activity!!)
                                .setCallbacks(callbacks)
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Send OTP")
                    }
                } else {
                    OutlinedTextField(
                        value = otp, onValueChange = { otp = it },
                        label = { Text("Enter OTP") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (otp.isBlank()) {
                                errorMessage = "Please enter OTP"
                                return@Button
                            }
                            val credential = PhoneAuthProvider.getCredential(verificationId, otp.trim())
                            loading = true
                            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                                loading = false
                                if (task.isSuccessful) {
                                    val nextDest = when(targetRole) {
                                        UserRole.VENDOR.name -> "vendor_dashboard"
                                        UserRole.ADMIN.name -> "admin_dashboard"
                                        else -> "home"
                                    }
                                    navController.navigate(nextDest) {
                                        popUpTo("identity_gate") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = task.exception?.message ?: "OTP Verification Failed"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Verify OTP")
                    }
                    TextButton(onClick = { isOtpSent = false }) {
                        Text("Change Phone Number", color = ForestGreen)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Button(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Continue with Google", color = Color.Black)
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(errorMessage, color = Color.Red, textAlign = TextAlign.Center)
            }
        }
    }
}
