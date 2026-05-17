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
import com.mindmatrix.budakattusante.data.model.UserRole
import com.mindmatrix.budakattusante.ui.components.ConnectivityBanner
import com.mindmatrix.budakattusante.ui.screens.*
import com.mindmatrix.budakattusante.ui.viewmodel.*
import kotlinx.coroutines.launch

/**
 * Main Application Navigation Graph for Budakattu Sante.
 * Implements MVVM Architecture with Hilt Injection.
 * All features: Customer Dashboard, Vendor Management, Admin Controls, AI Voice, and Offline Support.
 */
@Composable
fun BudakattuSanteApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // ViewModels for global state and feature-specific logic
    val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
    val productViewModel: ProductViewModel = hiltViewModel()
    val orderViewModel: OrderViewModel = hiltViewModel()
    val vendorViewModel: VendorViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val voiceViewModel: VoiceViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val addressViewModel: AddressViewModel = hiltViewModel()
    val adminViewModel: AdminViewModel = hiltViewModel()
    
    // UI State Observers
    val connectivityStatus by connectivityViewModel.connectivityStatus.collectAsStateWithLifecycle()
    val productState by productViewModel.uiState.collectAsStateWithLifecycle()
    val orderState by orderViewModel.uiState.collectAsStateWithLifecycle()
    val userProfile by profileViewModel.userProfile.collectAsStateWithLifecycle()
    val notifications by notificationViewModel.notifications.collectAsStateWithLifecycle()
    val addresses by addressViewModel.addresses.collectAsStateWithLifecycle()
    
    val userRole = userProfile?.role ?: UserRole.NONE.name

    // Global Notification Handler for Repository messages
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

    // Refresh profile on app launch if authenticated
    LaunchedEffect(Firebase.auth.currentUser) {
        if (Firebase.auth.currentUser != null) {
            profileViewModel.loadProfile()
        }
    }

    // AI Voice-assisted navigation observer
    LaunchedEffect(Unit) {
        voiceViewModel.navigationIntent.collect { route ->
            if (route.isNotBlank()) {
                navController.navigate(route)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ConnectivityBanner(status = connectivityStatus)
        
        NavHost(
            navController = navController, 
            startDestination = "splash",
            modifier = Modifier.weight(1f)
        ) {
            // Requirement 2: Animated Splash Screen
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
            
            // Requirement 3: Role Selection Screen
            composable("identity_gate") {
                IdentityGateScreen(onRoleSelected = { role ->
                    scope.launch {
                        profileViewModel.updateRole(role)
                        navController.navigate("login/${role.name}")
                    }
                })
            }
            
            // Login Screen with Firebase Auth
            composable("login/{targetRole}", arguments = listOf(navArgument("targetRole") { type = NavType.StringType })) { bse ->
                val targetRole = bse.arguments?.getString("targetRole") ?: UserRole.CUSTOMER.name
                LoginScreen(navController = navController, targetRole = targetRole)
            }

            // Requirement 1: Customer Home Dashboard
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

            // Requirement 10: Product Details with Traceability & Reviews
            composable("product_detail/{productId}") { bse ->
                val productId = bse.arguments?.getString("productId") ?: ""
                val product = productState.catalog.find { it.productId == productId }
                if (product != null) {
                    ProductDetailScreen(
                        product = product,
                        onBack = { navController.popBackStack() },
                        onNavigateToConfirm = { qty, _ -> 
                            navController.navigate("order_confirm/$productId/$qty")
                        },
                        productViewModel = productViewModel,
                        voiceViewModel = voiceViewModel
                    )
                }
            }

            // Requirement 12: Checkout Confirmation
            composable("order_confirm/{productId}/{quantity}", arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("quantity") { type = NavType.FloatType }
            )) { bse ->
                val productId = bse.arguments?.getString("productId") ?: ""
                val quantity = bse.arguments?.getFloat("quantity")?.toDouble() ?: 1.0
                val product = productState.catalog.find { it.productId == productId }
                if (product != null) {
                    OrderConfirmScreen(
                        product = product,
                        quantity = quantity,
                        addresses = addresses,
                        onBack = { navController.popBackStack() },
                        onAddAddress = { navController.navigate("address_management") },
                        onProceedToPayment = { _ ->
                            navController.navigate("payment")
                        },
                        onConfirmReservation = { address ->
                            orderViewModel.placeOrder(
                                buyerId = Firebase.auth.currentUser?.uid ?: "",
                                buyerName = address.name,
                                buyerPhone = address.phone,
                                buyerAddress = "${address.houseNumber}, ${address.street}, ${address.village}, ${address.district}, ${address.state} - ${address.zipCode}",
                                paymentMethod = "RESERVATION"
                            )
                            navController.navigate("order_success/true")
                        },
                        orderViewModel = orderViewModel,
                        voiceViewModel = voiceViewModel
                    )
                }
            }

            // Requirement 13: Integrated Payment System
            composable("payment") {
                PaymentScreen(
                    onBack = { navController.popBackStack() },
                    onPaymentSuccess = { method ->
                        val pending = orderViewModel.pendingOrderDetails
                        if (pending != null) {
                            if (orderViewModel.isCartCheckout) {
                                orderViewModel.placeCartOrders(
                                    buyerId = Firebase.auth.currentUser?.uid ?: "",
                                    buyerName = pending.name,
                                    buyerPhone = pending.phone,
                                    buyerAddress = pending.address,
                                    paymentMethod = method
                                )
                            } else {
                                orderViewModel.placeOrder(
                                    buyerId = Firebase.auth.currentUser?.uid ?: "",
                                    buyerName = pending.name,
                                    buyerPhone = pending.phone,
                                    buyerAddress = pending.address,
                                    paymentMethod = method
                                )
                            }
                        }
                        navController.navigate("order_success/false")
                    }
                )
            }

            // Order Success Page
            composable("order_success/{isPreOrder}", arguments = listOf(
                navArgument("isPreOrder") { type = NavType.BoolType }
            )) { bse ->
                val isPreOrder = bse.arguments?.getBoolean("isPreOrder") ?: false
                OrderSuccessScreen(
                    isPreOrder = isPreOrder,
                    onHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    voiceViewModel = voiceViewModel
                )
            }

            // Requirement 11: Order Management (Buyer)
            composable("orders") {
                MyOrdersScreen(
                    orders = orderState.orders,
                    onBack = { navController.popBackStack() },
                    onHome = { navController.navigate("home") },
                    onCategories = { navController.navigate("categories") },
                    onCart = { navController.navigate("cart") },
                    onProfile = { navController.navigate("profile") },
                    onTrackOrder = { orderId -> navController.navigate("tracking/$orderId") }
                )
            }

            // Requirement 10: Live Delivery Tracking
            composable("tracking/{orderId}") { bse ->
                val orderId = bse.arguments?.getString("orderId") ?: ""
                CustomerDeliveryTrackingScreen(orderId = orderId, onBack = { navController.popBackStack() })
            }

            // Shopping Cart / Basket
            composable("cart") {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate("cart_checkout") },
                    onHome = { navController.navigate("home") },
                    onCategories = { navController.navigate("categories") },
                    onOrders = { navController.navigate("orders") },
                    onProfile = { navController.navigate("profile") },
                    onAddAddress = { navController.navigate("address_management") },
                    orderViewModel = orderViewModel
                )
            }

            composable("cart_checkout") {
                CartCheckoutScreen(
                    addresses = addresses,
                    onBack = { navController.popBackStack() },
                    onAddAddress = { navController.navigate("address_management") },
                    onProceedToPayment = { _ -> navController.navigate("payment") },
                    orderViewModel = orderViewModel
                )
            }

            // Profile & Settings
            composable("profile") {
                CustomerProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        Firebase.auth.signOut()
                        navController.navigate("identity_gate") { popUpTo(0) }
                    },
                    onSwitchRole = { 
                        if (userRole == UserRole.VENDOR.name) navController.navigate("vendor_dashboard") 
                        else navController.navigate("identity_gate")
                    },
                    onViewOrders = { navController.navigate("orders") },
                    onHome = { navController.navigate("home") },
                    onCategories = { navController.navigate("categories") },
                    onCart = { navController.navigate("cart") },
                    onViewAddresses = { navController.navigate("address_management") },
                    onViewNotifications = { navController.navigate("notifications") },
                    onEditProfile = { navController.navigate("vendor_edit_business") },
                    userProfileImage = userProfile?.profileImageUrl,
                    userName = userProfile?.name ?: "Mallamma"
                )
            }

            // Address Book Management
            composable("address_management") {
                AddressManagementScreen(
                    addresses = addresses,
                    onAddAddress = { address -> addressViewModel.addAddress(address) },
                    onDeleteAddress = { address -> addressViewModel.deleteAddress(address) },
                    onBack = { navController.popBackStack() }
                )
            }

            // Seasonal Categories Explorer
            composable("categories") {
                CategoriesScreen(
                    onCategoryClick = { category ->
                        productViewModel.selectCategory(category)
                        navController.navigate("home")
                    },
                    onBack = { navController.popBackStack() },
                    onHome = { navController.navigate("home") },
                    onOrders = { navController.navigate("orders") },
                    onCart = { navController.navigate("cart") },
                    onProfile = { navController.navigate("profile") }
                )
            }

            // Requirement 1 & 4: Vendor Dashboard
            composable("vendor_dashboard") {
                VendorDashboard(
                    inventory = productState.catalog.filter { it.vendorId == Firebase.auth.currentUser?.uid },
                    orders = orderState.orders,
                    onSync = { vendorViewModel.triggerSync() },
                    onNavigate = { route -> navController.navigate(route) },
                    voiceViewModel = voiceViewModel,
                    vendorViewModel = vendorViewModel
                )
            }
            
            // Requirement 7: Publish Harvest Screen
            composable("vendor_add_product") {
                VendorProductFormScreen(
                    vendorViewModel = vendorViewModel,
                    productViewModel = productViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Requirement 4 & 5: Vendor Profile & Business Details
            composable("vendor_profile") {
                VendorProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        Firebase.auth.signOut()
                        navController.navigate("identity_gate") { popUpTo(0) }
                    },
                    onSwitchRole = { navController.navigate("home") },
                    onEditBusiness = { navController.navigate("vendor_edit_business") },
                    onManageArtisans = { /* TODO */ },
                    onViewPayments = { navController.navigate("vendor_payments") },
                    onViewAnalytics = { navController.navigate("vendor_analytics") },
                    userProfile = userProfile
                )
            }

            composable("vendor_edit_business") {
                EditProfileScreen(
                    profileViewModel = profileViewModel,
                    onBack = { navController.popBackStack() },
                    voiceViewModel = voiceViewModel
                )
            }

            // Requirement 11: Vendor Order Management
            composable("vendor_orders") {
                VendorOrdersScreen(
                    orders = orderState.orders.filter { it.vendorId == Firebase.auth.currentUser?.uid },
                    onBack = { navController.popBackStack() },
                    onUpdateStatus = { id, status -> orderViewModel.updateOrderStatus(id, status) }
                )
            }

            // Requirement 6: Inventory Management
            composable("vendor_products") {
                VendorProductsScreen(
                    inventory = productState.catalog.filter { it.vendorId == Firebase.auth.currentUser?.uid },
                    onNavigate = { route -> navController.navigate(route) },
                    onBack = { navController.popBackStack() }
                )
            }

            // Requirement 10: Vendor Analytics, Heatmap & Supply Log
            composable("vendor_analytics") {
                VendorAnalyticsScreen(
                    vendorViewModel = vendorViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("vendor_supply_log") {
                SupplyLogScreen(
                    productViewModel = productViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // QR Traceability for Batches
            composable("batch_details/{batchId}") { bse ->
                val batchId = bse.arguments?.getString("batchId") ?: ""
                BatchDetailsScreen(batchId = batchId, onBack = { navController.popBackStack() })
            }

            // Admin Control Center
            composable("admin_dashboard") {
                AdminDashboardScreen(
                    viewModel = adminViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Requirement 15: Tribal Markets & Shops Map
            composable("map") {
                MapScreen(onBack = { navController.popBackStack() })
            }

            // Notifications Explorer
            composable("notifications") {
                NotificationScreen(
                    notifications = notifications,
                    onBack = { navController.popBackStack() },
                    onMarkAsRead = { id -> notificationViewModel.markAsRead(id) }
                )
            }
            
            // Vendor Notifications
            composable("vendor_notifications") {
                NotificationScreen(
                    notifications = notifications,
                    onBack = { navController.popBackStack() },
                    onMarkAsRead = { id -> notificationViewModel.markAsRead(id) }
                )
            }
            
            // Vendor Wallet & Payments
            composable("vendor_payments") {
                PaymentScreen(onBack = { navController.popBackStack() }, onPaymentSuccess = { /* Handle vendor cash-out */ })
            }
        }
    }
}
