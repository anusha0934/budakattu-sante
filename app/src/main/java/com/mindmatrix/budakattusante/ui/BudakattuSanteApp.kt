package com.mindmatrix.budakattusante.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mindmatrix.budakattusante.ui.components.ConnectivityBanner
import com.mindmatrix.budakattusante.ui.screens.*
import com.mindmatrix.budakattusante.ui.viewmodel.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun BudakattuSanteApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
    val productViewModel: ProductViewModel = hiltViewModel()
    val orderViewModel: OrderViewModel = hiltViewModel()
    val vendorViewModel: VendorViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val voiceViewModel: VoiceViewModel = hiltViewModel()
    val addressViewModel: AddressViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val adminViewModel: AdminViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    
    val connectivityStatus by connectivityViewModel.connectivityStatus.collectAsStateWithLifecycle()
    val productState by productViewModel.uiState.collectAsStateWithLifecycle()
    val orderState by orderViewModel.uiState.collectAsStateWithLifecycle()
    val userProfile by profileViewModel.userProfile.collectAsStateWithLifecycle()
    val addresses by addressViewModel.addresses.collectAsStateWithLifecycle()
    val notifications by notificationViewModel.notifications.collectAsStateWithLifecycle()
    
    val userRole = userProfile?.role ?: UserRole.NONE.name

    // Requirement 10: AI Voice-to-Navigation Observer
    LaunchedEffect(Unit) {
        voiceViewModel.navigationIntent.collectLatest { route ->
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(orderState.message) {
        orderState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            orderViewModel.clearMessage()
        }
    }
    
    LaunchedEffect(productState.message) {
        productState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            productViewModel.clearMessage()
        }
    }

    LaunchedEffect(Firebase.auth.currentUser) {
        if (Firebase.auth.currentUser != null) {
            profileViewModel.loadProfile()
            addressViewModel.loadAddresses()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ConnectivityBanner(status = connectivityStatus)
        
        NavHost(
            navController = navController, 
            startDestination = "splash",
            modifier = Modifier.weight(1f)
        ) {
            composable("splash") {
                SplashScreen(onTimeout = {
                    val nextDest = when (userRole) {
                        UserRole.VENDOR.name -> "vendor_dashboard"
                        UserRole.CUSTOMER.name -> "home"
                        UserRole.ADMIN.name -> "admin_dashboard"
                        else -> "identity_gate"
                    }
                    navController.navigate(nextDest) {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }
            
            composable("identity_gate") {
                IdentityGateScreen(onRoleSelected = { role ->
                    scope.launch {
                        profileViewModel.updateRole(role)
                        navController.navigate("login/${role.name}")
                    }
                })
            }
            
            composable("login/{targetRole}", arguments = listOf(navArgument("targetRole") { type = NavType.StringType })) { bse ->
                val targetRole = bse.arguments?.getString("targetRole") ?: UserRole.CUSTOMER.name
                LoginScreen(navController = navController, targetRole = targetRole)
            }

            composable("home") {
                CustomerDashboard(
                    productViewModel = productViewModel,
                    onProductClick = { product -> navController.navigate("product_detail/${product.productId}") },
                    onAddToCart = { product -> orderViewModel.addToCart(product) },
                    onViewOrders = { navController.navigate("orders") },
                    onViewProfile = { navController.navigate("profile") },
                    onViewCategories = { navController.navigate("categories") },
                    onViewCart = { navController.navigate("cart") },
                    onViewMap = { navController.navigate("map") },
                    onViewNotifications = { navController.navigate("notifications") },
                    voiceViewModel = voiceViewModel,
                    userProfileImage = userProfile?.profileImageUrl,
                    userName = userProfile?.name ?: "Mallamma"
                )
            }
            
            composable("product_detail/{productId}") { bse ->
                val productId = bse.arguments?.getString("productId") ?: ""
                val product = productState.catalog.find { it.productId == productId }
                if (product != null) {
                    ProductDetailScreen(
                        product = product,
                        onBack = { navController.popBackStack() },
                        onNavigateToConfirm = { qty, isPre ->
                            orderViewModel.setPendingOrder(product, qty, userProfile?.name ?: "", userProfile?.phoneNumber ?: "", "", isPre)
                            navController.navigate("order_confirm")
                        },
                        productViewModel = productViewModel
                    )
                }
            }

            composable("order_confirm") {
                val pending = orderViewModel.pendingOrderDetails
                if (pending != null) {
                    OrderConfirmScreen(
                        product = pending.product,
                        quantity = pending.qty,
                        addresses = addresses,
                        onBack = { navController.popBackStack() },
                        onAddAddress = { navController.navigate("address_management") },
                        onProceedToPayment = { address ->
                            orderViewModel.setPendingOrder(pending.product, pending.qty, address.name, address.phone, "${address.street}, ${address.city}", false)
                            navController.navigate("payment")
                        },
                        onConfirmReservation = { address ->
                            orderViewModel.placeOrder(
                                Firebase.auth.currentUser?.uid ?: "",
                                address.name,
                                address.phone,
                                "${address.street}, ${address.city}",
                                "RESERVATION"
                            )
                            navController.navigate("order_success/true") {
                                popUpTo("home") { inclusive = false }
                            }
                        },
                        orderViewModel = orderViewModel
                    )
                }
            }

            composable("payment") {
                PaymentScreen(
                    onBack = { navController.popBackStack() },
                    onPaymentSuccess = { method ->
                        val pending = orderViewModel.pendingOrderDetails
                        if (pending != null) {
                            orderViewModel.placeOrder(
                                Firebase.auth.currentUser?.uid ?: "",
                                pending.name,
                                pending.phone,
                                pending.address,
                                method
                            )
                            navController.navigate("order_success/false") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    }
                )
            }

            composable("order_success/{isPreOrder}", arguments = listOf(navArgument("isPreOrder") { type = NavType.BoolType })) { bse ->
                val isPre = bse.arguments?.getBoolean("isPreOrder") ?: false
                OrderSuccessScreen(isPreOrder = isPre) {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }

            composable("orders") {
                MyOrdersScreen(
                    orders = orderState.orders,
                    onBack = { navController.popBackStack() },
                    onHome = { navController.navigate("home") },
                    onCategories = { navController.navigate("categories") },
                    onCart = { navController.navigate("cart") },
                    onProfile = { navController.navigate("profile") },
                    onTrackOrder = { orderId -> navController.navigate("track_order/$orderId") }
                )
            }

            composable("track_order/{orderId}") { bse ->
                val orderId = bse.arguments?.getString("orderId") ?: ""
                CustomerDeliveryTrackingScreen(orderId = orderId, onBack = { navController.popBackStack() })
            }

            composable("profile") {
                CustomerProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = { 
                        Firebase.auth.signOut()
                        navController.navigate("identity_gate") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSwitchRole = {
                        scope.launch {
                            profileViewModel.updateRole(UserRole.VENDOR)
                            navController.navigate("vendor_dashboard") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    onViewOrders = { navController.navigate("orders") },
                    onHome = { navController.navigate("home") },
                    onCategories = { navController.navigate("categories") },
                    onCart = { navController.navigate("cart") },
                    onViewAddresses = { navController.navigate("address_management") },
                    onViewNotifications = { navController.navigate("notifications") },
                    userProfileImage = userProfile?.profileImageUrl,
                    userName = userProfile?.name ?: "Mallamma"
                )
            }

            composable("cart") {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate("order_confirm") },
                    onHome = { navController.navigate("home") },
                    onCategories = { navController.navigate("categories") },
                    onOrders = { navController.navigate("orders") },
                    onProfile = { navController.navigate("profile") },
                    onAddAddress = { navController.navigate("address_management") },
                    orderViewModel = orderViewModel
                )
            }

            composable("categories") {
                CategoriesScreen(
                    onCategoryClick = { cat -> 
                        productViewModel.selectCategory(cat)
                        navController.navigate("home")
                    },
                    onBack = { navController.popBackStack() },
                    onHome = { navController.navigate("home") },
                    onOrders = { navController.navigate("orders") },
                    onCart = { navController.navigate("cart") },
                    onProfile = { navController.navigate("profile") }
                )
            }

            composable("address_management") {
                AddressManagementScreen(
                    addresses = addresses,
                    onAddAddress = { addressViewModel.addAddress(it) },
                    onDeleteAddress = { addressViewModel.deleteAddress(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("vendor_dashboard") {
                VendorDashboard(
                    inventory = productState.catalog.filter { it.vendorId == Firebase.auth.currentUser?.uid },
                    orders = orderState.orders.filter { it.vendorId == Firebase.auth.currentUser?.uid },
                    onSync = { vendorViewModel.triggerSync() },
                    onNavigate = { route -> navController.navigate(route) },
                    voiceViewModel = voiceViewModel,
                    vendorViewModel = vendorViewModel
                )
            }
            
            composable("vendor_add_product") {
                VendorProductFormScreen(
                    vendorViewModel = vendorViewModel,
                    onAddProduct = { batch ->
                        productViewModel.addBatch(batch)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("vendor_products") {
                VendorProductsScreen(
                    inventory = productState.catalog.filter { it.vendorId == Firebase.auth.currentUser?.uid },
                    onNavigate = { route -> navController.navigate(route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("vendor_orders") {
                VendorOrdersScreen(
                    orders = orderState.orders.filter { it.vendorId == Firebase.auth.currentUser?.uid },
                    onBack = { navController.popBackStack() },
                    onUpdateStatus = { orderId, status -> orderViewModel.updateOrderStatus(orderId, status) }
                )
            }
            
            composable("vendor_analytics") {
                VendorAnalyticsScreen(
                    vendorViewModel = vendorViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("vendor_payments") {
                PaymentScreen(
                    onBack = { navController.popBackStack() },
                    onPaymentSuccess = { method ->
                        Toast.makeText(context, "Payment processing enabled via $method", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }

            composable("vendor_profile") {
                VendorProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        Firebase.auth.signOut()
                        navController.navigate("identity_gate") { popUpTo(0) { inclusive = true } }
                    },
                    onSwitchRole = {
                        scope.launch {
                            profileViewModel.updateRole(UserRole.CUSTOMER)
                            navController.navigate("home") { popUpTo("vendor_dashboard") { inclusive = true } }
                        }
                    },
                    userProfile = userProfile
                )
            }

            composable("notifications") {
                NotificationScreen(
                    notifications = notifications,
                    onBack = { navController.popBackStack() },
                    onMarkAsRead = { notificationViewModel.markAsRead(it) }
                )
            }

            composable("vendor_notifications") {
                NotificationScreen(
                    notifications = notifications.filter { it.type.startsWith("VENDOR") || it.type == "ORDER_UPDATE" || it.type == "BATCH_APPROVAL" },
                    onBack = { navController.popBackStack() },
                    onMarkAsRead = { notificationViewModel.markAsRead(it) }
                )
            }

            composable("batch_details/{batchId}") { bse ->
                val batchId = bse.arguments?.getString("batchId") ?: ""
                BatchDetailsScreen(batchId = batchId, onBack = { navController.popBackStack() })
            }

            composable("admin_dashboard") {
                AdminDashboardScreen(
                    viewModel = adminViewModel,
                    onBack = { 
                        Firebase.auth.signOut()
                        navController.navigate("identity_gate") { popUpTo(0) { inclusive = true } }
                    }
                )
            }

            composable("tribal_ai") {
                ChatScreen(
                    viewModel = chatViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("map") {
                MapScreen(onBack = { navController.popBackStack() })
            }

            composable("faq") {
                FaqScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
