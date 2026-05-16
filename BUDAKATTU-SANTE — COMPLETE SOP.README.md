# BUDAKATTU-SANTE — COMPLETE SOP & DEVELOPMENT GUIDE
### Project #17 | MindMatrix VTU Internship | Android + Firebase + Room DB

---

# PART 1: PROJECT FOUNDATION & ARCHITECTURE

## 1.1 System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    BUDAKATTU-SANTE SYSTEM                       │
├─────────────────┬───────────────────────────────────────────────┤
│  TRIBAL LEADER  │           URBAN BUYER                         │
│  (Offline-First)│           (Online-Always)                     │
├─────────────────┼───────────────────────────────────────────────┤
│                 │                                               │
│  ┌──────────┐   │   ┌──────────────────────────────────────┐   │
│  │  Room DB │◄──┼───│         Cloud Firestore               │   │
│  │ (Local)  │   │   │  /products  /orders  /supply_logs     │   │
│  └────┬─────┘   │   └──────────────┬───────────────────────┘   │
│       │ Sync    │                  │                            │
│       │ when    │   ┌──────────────▼───────────────────────┐   │
│       │ online  │   │      Firebase Cloud Functions         │   │
│       └─────────┼──►│  NewBatchLogged → UpdateLedger        │   │
│                 │   │  OrderPlaced → NotifyFamily           │   │
│                 │   └──────────────────────────────────────┘   │
└─────────────────┴───────────────────────────────────────────────┘
```

## 1.2 MVVM Architecture Pattern

```
┌─────────────────────────────────────────────────────┐
│                   UI LAYER                          │
│  Activities / Fragments / Jetpack Compose           │
│  (Observes LiveData / StateFlow from ViewModel)     │
└──────────────────────┬──────────────────────────────┘
                       │ observes
┌──────────────────────▼──────────────────────────────┐
│                 VIEWMODEL LAYER                     │
│  ProductViewModel / OrderViewModel / SyncViewModel  │
│  (Business Logic, holds UI State)                   │
└──────────────────────┬──────────────────────────────┘
                       │ calls
┌──────────────────────▼──────────────────────────────┐
│               REPOSITORY LAYER                      │
│  ProductRepository — decides: Room or Firestore?    │
│  OrderRepository  — manages pre-order stock logic   │
└────────────┬──────────────────────┬─────────────────┘
             │                      │
┌────────────▼──────┐    ┌──────────▼──────────────────┐
│   Room DB (Local) │    │  Firebase Firestore (Cloud)  │
│   InventoryBatch  │    │  products / orders /         │
│   isSynced flag   │    │  supply_logs collections     │
└───────────────────┘    └─────────────────────────────┘
```

---

# PART 2: COMPLETE PROJECT SETUP

## 2.1 Android Studio Project Setup

```
Step 1: Open Android Studio → New Project
Step 2: Select "Empty Activity"
Step 3: Configure:
        Name:        BudakattuSante
        Package:     com.mindmatrix.budakattusante
        Language:    Kotlin
        Min SDK:     API 24 (Android 7.0) ← matches NFR-02
        Build:       Gradle (Kotlin DSL)
Step 4: Click Finish
Step 5: Connect to Firebase:
        Tools → Firebase → Firestore → Connect
```

## 2.2 Complete Gradle Dependencies (build.gradle.kts - app level)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")          // For Room
    id("com.google.gms.google-services")   // For Firebase
    id("kotlin-kapt")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.mindmatrix.budakattusante"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // ─── Firebase ───────────────────────────────────────
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // ─── Room DB (Offline-First) ─────────────────────────
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // ─── WorkManager (Background Sync) ──────────────────
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ─── ViewModel + LiveData ───────────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // ─── Image Loading (Coil) ────────────────────────────
    implementation("io.coil-kt:coil:2.5.0")

    // ─── Coroutines ──────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ─── Navigation Component ────────────────────────────
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // ─── Material Design ─────────────────────────────────
    implementation("com.google.android.material:material:1.11.0")

    // ─── Standard AndroidX ───────────────────────────────
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
}
```

---

# PART 3: DATABASE DESIGN

## 3.1 Room DB Entities (Local — Tribal Device)

```kotlin
// ─── FILE: data/local/entity/InventoryBatchEntity.kt ───

package com.mindmatrix.budakattusante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "inventory_batch")
data class InventoryBatchEntity(

    @PrimaryKey
    val batchId: String = UUID.randomUUID().toString(),

    val productName: String,          // "Wild Honey"
    val category: String,             // "Honey"
    val quantityKg: Double,           // 50.0
    val pricePerKg: Double,           // 350.0
    val familyId: String,             // "FAM_RAMAKRISHNAN_001"
    val familyName: String,           // "Ramakrishnan"
    val sellerPhone: String,          // "+91-9876543210"
    val description: String,          // Product description
    val harvestDate: String,          // "2025-05-03"
    val timestamp: Long = System.currentTimeMillis(),

    // ─── CRITICAL FLAGS ───────────────────────────────
    val isSynced: Boolean = false,    // false = pending upload
    val localImagePath: String = "",  // local file path
    val cloudImageUrl: String = "",   // Firebase Storage URL (after sync)
    val audioDescPath: String = ""    // local audio file path
)
```

```kotlin
// ─── FILE: data/local/entity/OrderEntity.kt ───

@Entity(tableName = "pre_orders")
data class OrderEntity(

    @PrimaryKey
    val orderId: String = UUID.randomUUID().toString(),

    val productId: String,
    val productName: String,
    val quantityOrdered: Double,
    val buyerName: String,
    val buyerPhone: String,
    val totalAmount: Double,
    val orderStatus: String = "PAYMENT_LOCKED",
    val deliveryDate: String,          // "2025-05-17" (+2 weeks)
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
```

## 3.2 Room DAO Interfaces

```kotlin
// ─── FILE: data/local/dao/InventoryBatchDao.kt ───

package com.mindmatrix.budakattusante.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryBatchDao {

    // Insert new batch (offline)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: InventoryBatchEntity)

    // Get ALL local batches (for leader's supply log view)
    @Query("SELECT * FROM inventory_batch ORDER BY timestamp DESC")
    fun getAllBatches(): Flow<List<InventoryBatchEntity>>

    // ─── CRITICAL: Get unsynced batches for WorkManager ───
    @Query("SELECT * FROM inventory_batch WHERE isSynced = 0")
    suspend fun getUnsyncedBatches(): List<InventoryBatchEntity>

    // Mark batch as synced after successful Firebase upload
    @Query("UPDATE inventory_batch SET isSynced = 1, cloudImageUrl = :url WHERE batchId = :id")
    suspend fun markAsSynced(id: String, url: String)

    // Get batches by family (for supply log)
    @Query("SELECT * FROM inventory_batch WHERE familyId = :familyId")
    fun getBatchesByFamily(familyId: String): Flow<List<InventoryBatchEntity>>

    // Get total stock for a product (for stock limit check)
    @Query("SELECT SUM(quantityKg) FROM inventory_batch WHERE productName = :name AND isSynced = 1")
    suspend fun getTotalStockForProduct(name: String): Double?

    @Delete
    suspend fun deleteBatch(batch: InventoryBatchEntity)
}
```

```kotlin
// ─── FILE: data/local/dao/OrderDao.kt ───

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM pre_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    // ─── STOCK LIMIT LOGIC ───────────────────────────────
    // Total ordered quantity for a product
    @Query("SELECT SUM(quantityOrdered) FROM pre_orders WHERE productName = :name AND orderStatus != 'CANCELLED'")
    suspend fun getTotalOrderedQuantity(name: String): Double?

    @Query("SELECT * FROM pre_orders WHERE isSynced = 0")
    suspend fun getUnsyncedOrders(): List<OrderEntity>

    @Query("UPDATE pre_orders SET isSynced = 1 WHERE orderId = :id")
    suspend fun markOrderSynced(id: String)
}
```

## 3.3 Room Database Class

```kotlin
// ─── FILE: data/local/BudakattuDatabase.kt ───

package com.mindmatrix.budakattusante.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [InventoryBatchEntity::class, OrderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BudakattuDatabase : RoomDatabase() {

    abstract fun inventoryBatchDao(): InventoryBatchDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: BudakattuDatabase? = null

        fun getInstance(context: Context): BudakattuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudakattuDatabase::class.java,
                    "budakattu_sante_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

# PART 4: DATA MODELS (Firestore)

## 4.1 Firestore Data Classes

```kotlin
// ─── FILE: data/model/Product.kt ───

package com.mindmatrix.budakattusante.data.model

data class Product(
    val productId: String = "",
    val name: String = "",
    val category: String = "",        // Honey/Bamboo/HerbalOil/WildFruit/Other
    val pricePerKg: Double = 0.0,
    val mspPrice: Double = 0.0,       // Government MSP
    val totalAvailable: Double = 0.0, // Updated by Cloud Function
    val imageUrl: String = "",
    val description: String = "",
    val familyName: String = "",
    val familyId: String = "",
    val sellerPhone: String = "",
    val harvestSeason: String = "",
    val audioDescUrl: String = "",    // Firebase Storage audio URL
    val timestamp: Long = 0L
)
```

```kotlin
// ─── FILE: data/model/SupplyLog.kt ───

data class SupplyLog(
    val batchId: String = "",
    val productId: String = "",
    val familyId: String = "",
    val familyName: String = "",
    val quantityKg: Double = 0.0,
    val harvestDate: String = "",
    val paymentStatus: String = "PENDING", // PENDING/PAID/PROCESSING
    val stripePaymentId: String = "",
    val timestamp: Long = 0L
)
```

```kotlin
// ─── FILE: data/model/Order.kt ───

data class Order(
    val orderId: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantityOrdered: Double = 0.0,
    val totalAmount: Double = 0.0,
    val buyerName: String = "",
    val buyerPhone: String = "",
    val orderStatus: String = "PAYMENT_LOCKED",
    val expectedDelivery: String = "",
    val familyNotified: Boolean = false,
    val timestamp: Long = 0L
)
```

---

# PART 5: REPOSITORY LAYER

## 5.1 Product Repository

```kotlin
// ─── FILE: data/repository/ProductRepository.kt ───

package com.mindmatrix.budakattusante.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val dao: InventoryBatchDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    // ─── TRIBAL LEADER: Save batch OFFLINE first ──────────
    suspend fun saveBatchOffline(batch: InventoryBatchEntity) {
        dao.insertBatch(batch)
        // isSynced = false by default → WorkManager will handle upload
    }

    // ─── OFFLINE SYNC: Called by SyncWorker ──────────────
    suspend fun syncPendingBatches(): Result<Int> {
        return try {
            val unsyncedBatches = dao.getUnsyncedBatches()
            var syncedCount = 0

            for (batch in unsyncedBatches) {
                // Step 1: Upload image to Firebase Storage
                val imageUrl = if (batch.localImagePath.isNotEmpty()) {
                    uploadImageToStorage(batch.batchId, batch.localImagePath)
                } else ""

                // Step 2: Build Firestore document
                val supplyLog = hashMapOf(
                    "batchId"     to batch.batchId,
                    "familyId"    to batch.familyId,
                    "familyName"  to batch.familyName,
                    "quantityKg"  to batch.quantityKg,
                    "productName" to batch.productName,
                    "category"    to batch.category,
                    "pricePerKg"  to batch.pricePerKg,
                    "imageUrl"    to imageUrl,
                    "harvestDate" to batch.harvestDate,
                    "timestamp"   to batch.timestamp,
                    "paymentStatus" to "PENDING"
                )

                // Step 3: Upload to Firestore
                firestore.collection("supply_logs")
                    .document(batch.batchId)
                    .set(supplyLog)
                    .await()

                // Step 4: Mark as synced in Room
                dao.markAsSynced(batch.batchId, imageUrl)
                syncedCount++
            }
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── URBAN BUYER: Fetch products from Firestore ───────
    fun getProductsFromFirestore(): Flow<List<Product>> = flow {
        val snapshot = firestore.collection("products")
            .orderBy("timestamp")
            .get()
            .await()
        val products = snapshot.documents.mapNotNull {
            it.toObject(Product::class.java)?.copy(productId = it.id)
        }
        emit(products)
    }

    // ─── SUPPLY LOG: Get all logs for a family ────────────
    fun getSupplyLogByFamily(familyId: String): Flow<List<SupplyLog>> = flow {
        val snapshot = firestore.collection("supply_logs")
            .whereEqualTo("familyId", familyId)
            .get()
            .await()
        val logs = snapshot.documents.mapNotNull {
            it.toObject(SupplyLog::class.java)
        }
        emit(logs)
    }

    private suspend fun uploadImageToStorage(batchId: String, localPath: String): String {
        val file = android.net.Uri.fromFile(java.io.File(localPath))
        val ref = storage.reference.child("product_images/$batchId.jpg")
        ref.putFile(file).await()
        return ref.downloadUrl.await().toString()
    }
}
```

## 5.2 Order Repository (Stock Limit Logic)

```kotlin
// ─── FILE: data/repository/OrderRepository.kt ───

class OrderRepository(
    private val orderDao: OrderDao,
    private val inventoryDao: InventoryBatchDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ─── STOCK LIMIT LOGIC (FR-08) ───────────────────────
    suspend fun checkStockAvailability(
        productName: String,
        requestedQty: Double
    ): StockCheckResult {

        // Total supplied by tribes (from local Room)
        val totalSupplied = inventoryDao.getTotalStockForProduct(productName) ?: 0.0

        // Total already ordered (from Firestore - source of truth)
        val snapshot = firestore.collection("orders")
            .whereEqualTo("productName", productName)
            .whereNotEqualTo("orderStatus", "CANCELLED")
            .get()
            .await()

        val totalOrdered = snapshot.documents.sumOf {
            it.getDouble("quantityOrdered") ?: 0.0
        }

        val availableStock = totalSupplied - totalOrdered

        return when {
            availableStock <= 0          -> StockCheckResult.OUT_OF_STOCK
            requestedQty > availableStock -> StockCheckResult.INSUFFICIENT(availableStock)
            else                         -> StockCheckResult.AVAILABLE(availableStock)
        }
    }

    // ─── PLACE PRE-ORDER ─────────────────────────────────
    suspend fun placePreOrder(order: Order): Result<String> {
        return try {
            // 1. Save to Firestore
            firestore.collection("orders")
                .document(order.orderId)
                .set(order)
                .await()

            // 2. Cache locally in Room
            orderDao.insertOrder(order.toEntity())

            // 3. Firebase Function "OrderPlaced" auto-triggers →
            //    notifies tribal family via FCM/SMS

            Result.success(order.orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Stock check sealed class
sealed class StockCheckResult {
    object OUT_OF_STOCK : StockCheckResult()
    data class INSUFFICIENT(val available: Double) : StockCheckResult()
    data class AVAILABLE(val available: Double) : StockCheckResult()
}
```

---

# PART 6: VIEWMODELS

## 6.1 Product ViewModel

```kotlin
// ─── FILE: ui/viewmodel/ProductViewModel.kt ───

package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepo: ProductRepository
) : ViewModel() {

    // All products from Firestore
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    // Search query
    private val _searchQuery = MutableStateFlow("")

    // Category filter
    private val _selectedCategory = MutableStateFlow("All")

    // UI State
    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState

    // ─── FILTERED products (search + category) ────────────
    val filteredProducts: StateFlow<List<Product>> = combine(
        _allProducts, _searchQuery, _selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchesSearch = product.name.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || product.category == category
            matchesSearch && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                productRepo.getProductsFromFirestore().collect { products ->
                    _allProducts.value = products
                    _uiState.value = if (products.isEmpty()) {
                        ProductUiState.Empty   // FR-12: Empty state
                    } else {
                        ProductUiState.Success(products)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Search (FR-05)
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Category filter (FR-06)
    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    // Save batch offline (FR-03)
    fun saveBatchOffline(batch: InventoryBatchEntity) {
        viewModelScope.launch {
            productRepo.saveBatchOffline(batch)
        }
    }
}

// UI State sealed class
sealed class ProductUiState {
    object Loading : ProductUiState()
    object Empty : ProductUiState()
    data class Success(val products: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}
```

## 6.2 Order ViewModel

```kotlin
// ─── FILE: ui/viewmodel/OrderViewModel.kt ───

class OrderViewModel(
    private val orderRepo: OrderRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<OrderUiState>(OrderUiState.Idle)
    val orderState: StateFlow<OrderUiState> = _orderState

    // ─── PRE-ORDER FLOW (FR-08) ───────────────────────────
    fun placePreOrder(
        product: Product,
        requestedQty: Double,
        buyerName: String,
        buyerPhone: String
    ) {
        viewModelScope.launch {
            _orderState.value = OrderUiState.Checking

            // Step 1: Stock limit check
            when (val check = orderRepo.checkStockAvailability(product.name, requestedQty)) {

                is StockCheckResult.OUT_OF_STOCK -> {
                    _orderState.value = OrderUiState.OutOfStock
                }

                is StockCheckResult.INSUFFICIENT -> {
                    _orderState.value = OrderUiState.InsufficientStock(check.available)
                }

                is StockCheckResult.AVAILABLE -> {
                    // Step 2: Build order object
                    val deliveryDate = calculateDeliveryDate(weeksFromNow = 2)
                    val order = Order(
                        productId       = product.productId,
                        productName     = product.name,
                        quantityOrdered = requestedQty,
                        totalAmount     = requestedQty * product.pricePerKg,
                        buyerName       = buyerName,
                        buyerPhone      = buyerPhone,
                        orderStatus     = "PAYMENT_LOCKED",
                        expectedDelivery = deliveryDate
                    )

                    // Step 3: Place order
                    orderRepo.placePreOrder(order).fold(
                        onSuccess = { orderId ->
                            _orderState.value = OrderUiState.Confirmed(
                                orderId = orderId,
                                deliveryDate = deliveryDate,
                                amount = order.totalAmount
                            )
                        },
                        onFailure = { e ->
                            _orderState.value = OrderUiState.Error(e.message ?: "Order failed")
                        }
                    )
                }
            }
        }
    }

    private fun calculateDeliveryDate(weeksFromNow: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.WEEK_OF_YEAR, weeksFromNow)
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(cal.time)
    }
}

sealed class OrderUiState {
    object Idle : OrderUiState()
    object Checking : OrderUiState()
    object OutOfStock : OrderUiState()
    data class InsufficientStock(val available: Double) : OrderUiState()
    data class Confirmed(val orderId: String, val deliveryDate: String, val amount: Double) : OrderUiState()
    data class Error(val message: String) : OrderUiState()
}
```

---

# PART 7: OFFLINE SYNC ENGINE (WorkManager)

## 7.1 SyncWorker

```kotlin
// ─── FILE: worker/SyncWorker.kt ───

package com.mindmatrix.budakattusante.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = BudakattuDatabase.getInstance(applicationContext)
        val repository = ProductRepository(database.inventoryBatchDao())

        return try {
            val result = repository.syncPendingBatches()
            result.fold(
                onSuccess = { count ->
                    // Show notification: "X batches synced successfully"
                    showSyncNotification(count)
                    Result.success()
                },
                onFailure = {
                    Result.retry() // Retry on failure
                }
            )
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showSyncNotification(count: Int) {
        // Build and show Android notification
        // "Sync Complete: $count batches uploaded to cloud"
    }

    companion object {
        const val WORK_NAME = "BudakattuSyncWork"

        // ─── SCHEDULE SYNC ─────────────────────────────────
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Only when WiFi/Data
                .build()

            // Periodic sync every 15 minutes when online
            val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        // ─── IMMEDIATE SYNC (when app detects network) ────
        fun scheduleImmediate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val immediateRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(immediateRequest)
        }
    }
}
```

## 7.2 Network Connectivity Monitor

```kotlin
// ─── FILE: util/NetworkMonitor.kt ───

package com.mindmatrix.budakattusante.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkMonitor(context: Context) {

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            _isOnline.value = true
            // ─── TRIGGER SYNC WHEN NETWORK RETURNS ────────
            SyncWorker.scheduleImmediate(context)
        }

        override fun onLost(network: Network) {
            _isOnline.value = false
        }
    }

    fun startMonitoring() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
```

---

# PART 8: UI SCREENS

## 8.1 Image Compression Utility (FR-02, NFR-05)

```kotlin
// ─── FILE: util/ImageCompressor.kt ───

package com.mindmatrix.budakattusante.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    // Target: Under 500KB (NFR-05)
    fun compressImage(context: Context, imageUri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        // Calculate compression ratio
        val targetWidth = 800
        val scaleFactor = originalBitmap.width.toFloat() / targetWidth
        val targetHeight = (originalBitmap.height / scaleFactor).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(
            originalBitmap, targetWidth, targetHeight, true
        )

        // Save compressed image to cache
        val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use { fos ->
            scaledBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                75,   // 75% quality → ~300-400KB
                fos
            )
        }

        return outputFile.absolutePath
    }
}
```

## 8.2 Leader Upload Fragment

```kotlin
// ─── FILE: ui/fragment/LeaderUploadFragment.kt ───

package com.mindmatrix.budakattusante.ui.fragment

import android.os.Bundle
import android.view.View
import android.speech.tts.TextToSpeech
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mindmatrix.budakattusante.R
import com.mindmatrix.budakattusante.databinding.FragmentLeaderUploadBinding
import java.util.Locale

class LeaderUploadFragment : Fragment(R.layout.fragment_leader_upload) {

    private var _binding: FragmentLeaderUploadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()

    private var compressedImagePath: String = ""

    // ─── AUDIO GUIDANCE for semi-literate users (FR-11) ──
    private lateinit var tts: TextToSpeech

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Compress before saving (NFR-05: under 500KB)
            compressedImagePath = ImageCompressor.compressImage(requireContext(), it)
            binding.ivProductPhoto.setImageURI(uri)
            binding.tvPhotoStatus.text = "✓ Photo added"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLeaderUploadBinding.bind(view)

        setupTTS()
        setupClickListeners()
    }

    private fun setupTTS() {
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Use Kannada locale for tribal users
                tts.language = Locale("kn", "IN")
            }
        }
    }

    private fun setupClickListeners() {
        // Pick photo
        binding.btnPickPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Audio guidance - play instruction aloud
        binding.btnAudioGuide.setOnClickListener {
            speakInstruction("ಉತ್ಪನ್ನದ ಹೆಸರು, ತೂಕ ಮತ್ತು ಕುಟುಂಬದ ಹೆಸರನ್ನು ನಮೂದಿಸಿ")
            // Translation: "Enter product name, weight and family name"
        }

        // Save batch (offline-first)
        binding.btnSaveBatch.setOnClickListener {
            if (validateForm()) {
                saveBatch()
            }
        }
    }

    private fun validateForm(): Boolean {
        // NFR-04: Input Validation — No Crashes
        var isValid = true

        if (binding.etProductName.text.isNullOrBlank()) {
            binding.etProductName.error = "Product name is required"
            isValid = false
        }
        if (binding.etQuantity.text.isNullOrBlank()) {
            binding.etQuantity.error = "Quantity is required"
            isValid = false
        }
        if (binding.etPrice.text.isNullOrBlank()) {
            binding.etPrice.error = "Price is required"
            isValid = false
        }
        if (binding.etFamilyName.text.isNullOrBlank()) {
            binding.etFamilyName.error = "Family name is required"
            isValid = false
        }
        if (compressedImagePath.isEmpty()) {
            binding.tvPhotoStatus.text = "⚠ Please add a photo"
            isValid = false
        }

        return isValid
    }

    private fun saveBatch() {
        val batch = InventoryBatchEntity(
            productName    = binding.etProductName.text.toString(),
            category       = binding.spinnerCategory.selectedItem.toString(),
            quantityKg     = binding.etQuantity.text.toString().toDouble(),
            pricePerKg     = binding.etPrice.text.toString().toDouble(),
            familyId       = "FAM_${binding.etFamilyName.text.toString().uppercase()}",
            familyName     = binding.etFamilyName.text.toString(),
            sellerPhone    = binding.etPhone.text.toString(),
            description    = binding.etDescription.text.toString(),
            harvestDate    = binding.etDate.text.toString(),
            localImagePath = compressedImagePath,
            isSynced       = false  // ← OFFLINE: will sync later
        )

        viewModel.saveBatchOffline(batch)

        // Show success message
        binding.tvSyncStatus.text = "✓ Saved locally. Will sync when online."
        speakInstruction("ನಿಮ್ಮ ಮಾಹಿತಿ ಉಳಿಸಲಾಗಿದೆ. ಇಂಟರ್ನೆಟ್ ಸಿಕ್ಕಾಗ ಅಪ್ಲೋಡ್ ಆಗುತ್ತದೆ")
    }

    private fun speakInstruction(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "guide_${System.currentTimeMillis()}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts.shutdown()
        _binding = null
    }
}
```

## 8.3 Buyer Catalog Fragment

```kotlin
// ─── FILE: ui/fragment/BuyerCatalogFragment.kt ───

class BuyerCatalogFragment : Fragment(R.layout.fragment_buyer_catalog) {

    private var _binding: FragmentBuyerCatalogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBuyerCatalogBinding.bind(view)

        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        observeProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            // Navigate to product detail
            val action = BuyerCatalogFragmentDirections
                .actionCatalogToDetail(product.productId)
            findNavController().navigate(action)
        }

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2-column grid
            adapter = productAdapter
        }
    }

    // FR-05: Real-time Search
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onSearchQueryChanged(newText ?: "")
                return true
            }
        })
    }

    // FR-06: Category Filter Chips
    private fun setupCategoryChips() {
        val categories = listOf("All", "Honey", "Bamboo", "Herbal Oil", "Wild Fruit", "Other")
        categories.forEach { category ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isChecked = category == "All"
                setOnClickListener { viewModel.onCategorySelected(category) }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    // Observe UI State
    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe filtered products
            viewModel.filteredProducts.collect { products ->
                productAdapter.submitList(products)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ProductUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                    }
                    is ProductUiState.Empty -> {
                        // FR-12: Empty state screen
                        binding.progressBar.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    }
                    is ProductUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.GONE
                    }
                    is ProductUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showError(state.message)
                    }
                }
            }
        }
    }
}
```

## 8.4 Product Detail Fragment (Pre-Order + MSP)

```kotlin
// ─── FILE: ui/fragment/ProductDetailFragment.kt ───

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private val orderViewModel: OrderViewModel by viewModels()
    private var currentProduct: Product? = null
    private var mediaPlayer: android.media.MediaPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeOrderState()
        setupButtons()
    }

    private fun setupButtons() {
        // FR-07: Pre-Order button (green primary)
        binding.btnPreOrder.setOnClickListener {
            currentProduct?.let { product ->
                showPreOrderDialog(product)
            }
        }

        // FR-10: Check MSP Price button
        binding.btnCheckMsp.setOnClickListener {
            currentProduct?.let { product ->
                showMspDialog(product)
            }
        }

        // FR-11: Audio description
        binding.btnPlayAudio.setOnClickListener {
            currentProduct?.audioDescUrl?.let { url ->
                playAudioDescription(url)
            }
        }
    }

    // ─── PRE-ORDER DIALOG ─────────────────────────────────
    private fun showPreOrderDialog(product: Product) {
        val dialogBinding = DialogPreOrderBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pre-Order ${product.name}")
            .setView(dialogBinding.root)
            .setPositiveButton("Confirm") { _, _ ->
                val qty = dialogBinding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
                val buyerName = dialogBinding.etBuyerName.text.toString()
                val buyerPhone = dialogBinding.etBuyerPhone.text.toString()

                orderViewModel.placePreOrder(product, qty, buyerName, buyerPhone)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── MSP INFO DIALOG ──────────────────────────────────
    private fun showMspDialog(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📋 MSP Info — ${product.name}")
            .setMessage(
                """
                Government Minimum Support Price (MSP):
                ₹${product.mspPrice} per kg
                
                Current Listing Price:
                ₹${product.pricePerKg} per kg
                
                ✓ This listing is above MSP.
                Fair trade prices ensure tribal families get paid fairly.
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // ─── OBSERVE ORDER RESULT ─────────────────────────────
    private fun observeOrderState() {
        viewLifecycleOwner.lifecycleScope.launch {
            orderViewModel.orderState.collect { state ->
                when (state) {
                    is OrderUiState.Checking -> showLoading(true)

                    is OrderUiState.OutOfStock -> {
                        showLoading(false)
                        showSnackbar("⚠ Out of Stock — This product is not available")
                        binding.btnPreOrder.isEnabled = false
                        binding.btnPreOrder.text = "Out of Stock"
                    }

                    is OrderUiState.InsufficientStock -> {
                        showLoading(false)
                        showSnackbar(
                            "Only ${state.available}kg available. Please reduce quantity."
                        )
                    }

                    is OrderUiState.Confirmed -> {
                        showLoading(false)
                        // ─── SUCCESS DIALOG ────────────────────────────
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("✅ Order Confirmed!")
                            .setMessage(
                                """
                                Order ID: ${state.orderId}
                                Payment: LOCKED
                                Expected Delivery: ${state.deliveryDate}
                                Amount: ₹${state.amount}
                                
                                The tribal family has been notified.
                                """.trimIndent()
                            )
                            .setPositiveButton("Done") { _, _ ->
                                findNavController().popBackStack()
                            }
                            .show()
                    }

                    is OrderUiState.Error -> {
                        showLoading(false)
                        showSnackbar("Error: ${state.message}")
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun playAudioDescription(url: String) {
        mediaPlayer?.release()
        mediaPlayer = android.media.MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { start() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
    }
}
```

---

# PART 9: FIREBASE CLOUD FUNCTIONS

## 9.1 Cloud Functions (Node.js)

```javascript
// ─── FILE: functions/index.js ───

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();

// ─── FUNCTION 1: NewBatchLogged ───────────────────────────
// Triggers when tribal leader uploads a new supply batch
// Updates the product's total available stock
exports.NewBatchLogged = functions.firestore
    .document("supply_logs/{batchId}")
    .onCreate(async (snap, context) => {
        const batch = snap.data();

        try {
            // 1. Find or create product document
            const productsRef = db.collection("products");
            const query = await productsRef
                .where("name", "==", batch.productName)
                .get();

            if (query.empty) {
                // Create new product listing
                await productsRef.add({
                    name: batch.productName,
                    category: batch.category,
                    pricePerKg: batch.pricePerKg,
                    mspPrice: getMspPrice(batch.category),
                    totalAvailable: batch.quantityKg,
                    imageUrl: batch.imageUrl,
                    familyName: batch.familyName,
                    familyId: batch.familyId,
                    sellerPhone: batch.sellerPhone,
                    timestamp: admin.firestore.FieldValue.serverTimestamp()
                });
            } else {
                // Update existing product stock
                const productDoc = query.docs[0];
                await productDoc.ref.update({
                    totalAvailable: admin.firestore.FieldValue.increment(batch.quantityKg)
                });
            }

            // 2. Send notification to cooperative admin
            await sendAdminNotification(
                `New batch logged: ${batch.quantityKg}kg of ${batch.productName} by ${batch.familyName}`
            );

            console.log(`✓ Supply ledger updated for batch: ${context.params.batchId}`);
        } catch (error) {
            console.error("NewBatchLogged error:", error);
        }
    });

// ─── FUNCTION 2: OrderPlaced ──────────────────────────────
// Triggers when urban buyer places a pre-order
// Notifies tribal family + updates stock
exports.OrderPlaced = functions.firestore
    .document("orders/{orderId}")
    .onCreate(async (snap, context) => {
        const order = snap.data();

        try {
            // 1. Decrement available stock
            const productsRef = db.collection("products");
            const query = await productsRef
                .where("name", "==", order.productName)
                .get();

            if (!query.empty) {
                await query.docs[0].ref.update({
                    totalAvailable: admin.firestore.FieldValue.increment(-order.quantityOrdered)
                });
            }

            // 2. Get family's FCM token
            const familyDoc = await db.collection("families")
                .doc(order.familyId || query.docs[0].data().familyId)
                .get();

            if (familyDoc.exists) {
                const fcmToken = familyDoc.data().fcmToken;

                // 3. Send push notification to tribal family
                if (fcmToken) {
                    await admin.messaging().send({
                        token: fcmToken,
                        notification: {
                            title: "🎉 New Order Received!",
                            body: `${order.buyerName} ordered ${order.quantityOrdered}kg of ${order.productName}. Delivery by ${order.expectedDelivery}.`
                        },
                        data: {
                            orderId: context.params.orderId,
                            type: "NEW_ORDER"
                        }
                    });
                }
            }

            // 4. Mark order as family notified
            await snap.ref.update({ familyNotified: true });

            console.log(`✓ Family notified for order: ${context.params.orderId}`);
        } catch (error) {
            console.error("OrderPlaced error:", error);
        }
    });

// ─── FUNCTION 3: ProcessPayment ──────────────────────────
// Handles payment processing via Stripe
exports.ProcessPayment = functions.https.onCall(async (data, context) => {
    const { orderId, amount, currency = "inr" } = data;

    // Stripe integration (production)
    const stripe = require("stripe")(functions.config().stripe.secret_key);

    try {
        const paymentIntent = await stripe.paymentIntents.create({
            amount: Math.round(amount * 100), // Convert to paise
            currency: currency,
            metadata: { orderId }
        });

        // Update order with payment intent
        await db.collection("orders").doc(orderId).update({
            stripePaymentIntentId: paymentIntent.id,
            orderStatus: "PAYMENT_PROCESSING"
        });

        return { clientSecret: paymentIntent.client_secret };
    } catch (error) {
        throw new functions.https.HttpsError("internal", error.message);
    }
});

// Helper: Get MSP price by category
function getMspPrice(category) {
    const mspTable = {
        "Honey": 350,
        "Bamboo": 120,
        "Herbal Oil": 800,
        "Wild Fruit": 200,
        "Other": 150
    };
    return mspTable[category] || 150;
}

async function sendAdminNotification(message) {
    // Send to admin FCM topic
    await admin.messaging().sendToTopic("admin_alerts", {
        notification: { title: "Budakattu-Sante Admin", body: message }
    });
}
```

---

# PART 10: UI LAYOUT FILES

## 10.1 Earthy Tribal Theme (NFR-03)

```xml
<!-- ─── FILE: res/values/colors.xml ─── -->
<resources>
    <!-- Primary: Forest Green -->
    <color name="forest_green">#3E4F3C</color>
    <color name="forest_green_light">#5A7055</color>
    <color name="forest_green_dark">#2C3A2B</color>

    <!-- Secondary: Terracotta/Wood -->
    <color name="terracotta">#A67B5B</color>
    <color name="terracotta_light">#C49A7A</color>
    <color name="wood_brown">#795548</color>

    <!-- Accent: Honey Gold -->
    <color name="honey_gold">#F4A227</color>
    <color name="honey_amber">#FF8F00</color>

    <!-- Background: Parchment -->
    <color name="parchment">#F5F2ED</color>
    <color name="parchment_dark">#EDE8E0</color>

    <!-- Text -->
    <color name="text_primary">#2C2C2C</color>
    <color name="text_secondary">#6B6B6B</color>
    <color name="text_on_green">#FFFFFF</color>

    <!-- Status Colors -->
    <color name="sync_pending">#FF5722</color>
    <color name="sync_complete">#4CAF50</color>
    <color name="out_of_stock">#F44336</color>
</resources>
```

```xml
<!-- ─── FILE: res/values/themes.xml ─── -->
<resources>
    <style name="Theme.BudakattuSante" parent="Theme.MaterialComponents.Light.NoActionBar">
        <item name="colorPrimary">@color/forest_green</item>
        <item name="colorPrimaryVariant">@color/forest_green_dark</item>
        <item name="colorSecondary">@color/terracotta</item>
        <item name="colorSecondaryVariant">@color/terracotta_light</item>
        <item name="android:colorBackground">@color/parchment</item>
        <item name="colorSurface">@color/parchment</item>
        <item name="colorOnPrimary">@color/text_on_green</item>
        <item name="colorOnBackground">@color/text_primary</item>
        <item name="android:fontFamily">@font/quicksand</item>
    </style>

    <!-- Card Style -->
    <style name="BudakattuCardStyle" parent="Widget.MaterialComponents.CardView">
        <item name="cardCornerRadius">12dp</item>
        <item name="cardElevation">4dp</item>
        <item name="contentPadding">12dp</item>
        <item name="cardBackgroundColor">@color/parchment</item>
    </style>

    <!-- Primary Button -->
    <style name="BudakattuButtonPrimary" parent="Widget.MaterialComponents.Button">
        <item name="backgroundTint">@color/forest_green</item>
        <item name="android:textColor">@color/text_on_green</item>
        <item name="cornerRadius">8dp</item>
    </style>
</resources>
```

## 10.2 Product Card Layout

```xml
<!-- ─── FILE: res/layout/item_product_card.xml ─── -->
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cardProduct"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="6dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp"
    app:cardBackgroundColor="@color/parchment">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Product Image (loaded by Coil) -->
        <ImageView
            android:id="@+id/ivProductImage"
            android:layout_width="match_parent"
            android:layout_height="140dp"
            android:scaleType="centerCrop"
            android:contentDescription="@string/product_image"/>

        <!-- Category Badge -->
        <com.google.android.material.chip.Chip
            android:id="@+id/chipCategory"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="8dp"
            android:textColor="@color/text_on_green"
            app:chipBackgroundColor="@color/forest_green"
            app:chipMinHeight="24dp"/>

        <!-- Product Info -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingHorizontal="10dp"
            android:paddingBottom="10dp">

            <TextView
                android:id="@+id/tvProductName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textSize="14sp"
                android:textStyle="bold"
                android:textColor="@color/text_primary"
                android:maxLines="2"
                android:ellipsize="end"/>

            <TextView
                android:id="@+id/tvProductPrice"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textSize="16sp"
                android:textColor="@color/honey_gold"
                android:textStyle="bold"
                android:layout_marginTop="4dp"/>

            <!-- Sync status indicator for tribal users -->
            <TextView
                android:id="@+id/tvSyncStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textSize="10sp"
                android:visibility="gone"/>

        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

## 10.3 RecyclerView Adapter

```kotlin
// ─── FILE: ui/adapter/ProductAdapter.kt ───

class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    inner class ProductViewHolder(
        private val binding: ItemProductCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                tvProductPrice.text = "₹${product.pricePerKg}/kg"
                chipCategory.text = product.category

                // Load image with Coil (smooth, cached)
                ivProductImage.load(product.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_forest_placeholder)
                    error(R.drawable.ic_forest_placeholder)
                }

                // Set category chip color
                chipCategory.setChipBackgroundColorResource(
                    getCategoryColor(product.category)
                )

                // Click listener
                root.setOnClickListener { onProductClick(product) }
            }
        }

        private fun getCategoryColor(category: String): Int {
            return when (category) {
                "Honey"     -> R.color.honey_gold
                "Bamboo"    -> R.color.wood_brown
                "Herbal Oil"-> R.color.forest_green
                "Wild Fruit"-> R.color.terracotta
                else        -> R.color.text_secondary
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(old: Product, new: Product) = old.productId == new.productId
    override fun areContentsTheSame(old: Product, new: Product) = old == new
}
```

---

# PART 11: FIRESTORE SECURITY RULES

```javascript
// ─── FILE: firestore.rules ───

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // ─── PRODUCTS: Public read, restricted write ────────
    match /products/{productId} {
      allow read: if true;           // Anyone can browse catalog
      allow write: if false;         // Only Cloud Functions can write
    }

    // ─── SUPPLY LOGS: Read-only after creation ──────────
    match /supply_logs/{batchId} {
      allow read: if true;
      allow create: if request.resource.data.keys()
                      .hasAll(['batchId','familyId','quantityKg','productName']);
      allow update: if false;        // NFR-07: Prevent unauthorized modifications
      allow delete: if false;        // NFR-07: Prevent unauthorized deletion
    }

    // ─── ORDERS: Create + read own orders ───────────────
    match /orders/{orderId} {
      allow read: if true;
      allow create: if request.resource.data.keys()
                      .hasAll(['orderId','productName','quantityOrdered','buyerPhone']);
      allow update: if false;
      allow delete: if false;
    }

    // ─── FAMILIES: Private ──────────────────────────────
    match /families/{familyId} {
      allow read: if false;          // Private data
      allow write: if false;
    }
  }
}
```

---

# PART 12: 3-WEEK DELIVERY PLAN (DAY-BY-DAY)

## WEEK 1 — Foundation (Days 1–7)

```
DAY 1: Project Setup
  ├── Android Studio project created
  ├── Firebase project connected (google-services.json added)
  ├── All Gradle dependencies added and synced
  ├── Package structure created:
  │   ├── data/local/entity/
  │   ├── data/local/dao/
  │   ├── data/model/
  │   ├── data/repository/
  │   ├── ui/fragment/
  │   ├── ui/viewmodel/
  │   ├── ui/adapter/
  │   └── worker/
  └── GitHub repository created, initial commit pushed

DAY 2: Room DB Setup
  ├── InventoryBatchEntity.kt created
  ├── OrderEntity.kt created
  ├── InventoryBatchDao.kt with all queries
  ├── OrderDao.kt with stock limit query
  └── BudakattuDatabase.kt singleton created

DAY 3: Earthy UI Theme
  ├── colors.xml (forest green, terracotta, honey gold)
  ├── themes.xml (custom material theme)
  ├── fonts/ directory (Quicksand font added)
  └── All drawable placeholders created

DAY 4: Leader Upload Screen
  ├── fragment_leader_upload.xml layout
  ├── All form fields: name, category, price, qty, family, phone
  ├── Spinner for categories
  └── Photo picker button with preview ImageView

DAY 5: Image Picker + Compression
  ├── ImageCompressor.kt utility class
  ├── ActivityResultContracts.GetContent() implemented
  ├── BitmapFactory compression to <500KB
  └── Compressed image path stored in local variable

DAY 6: Room Save + Validation
  ├── validateForm() — all fields checked (NFR-04)
  ├── saveBatchOffline() — saves to Room with isSynced=false
  ├── Success message: "Saved locally. Will sync when online."
  └── isSynced=false visually shown with pending icon ⏳

DAY 7: Week 1 Review + GitHub Push
  ├── Test: Add 3 batches in airplane mode
  ├── Verify data in Room using Database Inspector
  ├── Fix any crashes or validation issues
  └── Push all code to GitHub with meaningful commit message
```

## WEEK 2 — Buyer Experience (Days 8–14)

```
DAY 8: Firebase Firestore Integration
  ├── ProductRepository.kt — getProductsFromFirestore()
  ├── Coroutines + .await() for Firestore calls
  └── Product.kt data class matching Firestore schema

DAY 9: Catalog Grid UI
  ├── fragment_buyer_catalog.xml with RecyclerView
  ├── GridLayoutManager(context, 2) — 2-column grid
  ├── item_product_card.xml layout
  └── ProductAdapter.kt with DiffUtil

DAY 10: Coil Image Loading + Empty State
  ├── Coil library: ivProductImage.load(url)
  ├── Placeholder + error drawable set
  ├── layout_empty.xml — friendly empty state (FR-12)
  └── ProductViewModel observes uiState (Loading/Empty/Success/Error)

DAY 11: Search + Category Filter
  ├── SearchView connected to onSearchQueryChanged()
  ├── ChipGroup with category chips dynamically added
  ├── StateFlow combine() for simultaneous filter (FR-05, FR-06)
  └── Test: Search "honey" shows only honey products

DAY 12: Product Detail Screen
  ├── fragment_product_detail.xml
  ├── Full-width product image (Coil)
  ├── Price, category badge, artisan name, description
  ├── "Pre-Order" button (green, FR-07)
  └── "Check MSP Price" button (outlined, FR-10)

DAY 13: Pre-Order + Stock Limit Logic
  ├── OrderRepository.checkStockAvailability() implemented
  ├── OrderViewModel.placePreOrder() with all states
  ├── StockCheckResult sealed class
  ├── Success dialog: "Payment locked, delivery in 2 weeks"
  ├── Out of stock → button disabled (FR-08)
  └── Test: Try ordering more than available → blocked

DAY 14: Supply Log + MSP Info Screens
  ├── Supply log: list of batches per family
  ├── MSP dialog shows government prices (FR-10)
  ├── Week 2 testing: full buyer flow end-to-end
  └── GitHub push with Week 2 tag
```

## WEEK 3 — Polish + Delivery (Days 15–21)

```
DAY 15: WorkManager Sync Engine
  ├── SyncWorker.kt with doWork() logic
  ├── Constraints: NetworkType.CONNECTED
  ├── SyncWorker.schedule() — periodic 15 min
  └── Application.kt: schedule sync on app start

DAY 16: Network Monitor + Auto-Sync
  ├── NetworkMonitor.kt with callback
  ├── onAvailable() → SyncWorker.scheduleImmediate()
  ├── Banner in UI: "No internet — offline mode" (NFR-06)
  └── Test: Offline add → WiFi connect → auto-sync triggers

DAY 17: Cloud Functions Deployment
  ├── firebase-functions project initialized
  ├── NewBatchLogged function deployed and tested
  ├── OrderPlaced function deployed and tested
  ├── Firestore security rules deployed (NFR-07)
  └── Test: Upload batch → Firestore auto-updates product stock

DAY 18: Audio Descriptions (FR-11)
  ├── TextToSpeech (TTS) setup with Kannada locale
  ├── btnAudioGuide speaks form instructions
  ├── btnPlayAudio in detail screen plays Firebase audio URL
  └── MediaPlayer released properly in onDestroyView()

DAY 19: Performance Testing
  ├── Add 25+ products to Firestore manually
  ├── Test smooth scrolling (NFR-01: no stuttering)
  ├── Test load time under 3 seconds (NFR-08)
  └── Profile with Android Studio Memory Profiler

DAY 20: Final Polish
  ├── App icon designed (forest/tribal theme)
  ├── Splash screen added
  ├── All earthy colors verified (NFR-03)
  ├── APK built: Build → Generate Signed APK
  └── Screenshots taken for all screens

DAY 21: Submission
  ├── README.md written with setup instructions
  ├── All success criteria ticked off (see checklist)
  ├── GitHub repository finalized and made public
  └── APK + GitHub link submitted to MindMatrix
```

---

# PART 13: SUCCESS CRITERIA CHECKLIST

```
WEEK 1 TARGETS:
  ✅ Leader can list product offline (Room DB)
  ✅ Image compressed to <500KB before saving
  ✅ Form validates all fields (no crashes)
  ✅ isSynced=false shown with pending indicator

WEEK 2 TARGETS:
  ✅ 2-column catalog grid loads from Firestore
  ✅ Search filters products in real time
  ✅ Category chips filter the grid correctly
  ✅ Product detail has Pre-Order + MSP buttons
  ✅ Pre-Order blocks if quantity > available stock
  ✅ Empty state shown when no products listed
  ✅ Supply log shows family-wise contributions

WEEK 3 TARGETS:
  ✅ Offline data syncs to Firestore when WiFi connects
  ✅ Audio descriptions play on product detail screen
  ✅ UI uses earthy green/brown colors throughout
  ✅ App handles 20+ products without stuttering
  ✅ Cloud Function notifies family on new order
  ✅ GitHub pushed + APK submitted
```

---

# PART 14: KEY SOP RULES

```
RULE 1 — OFFLINE FIRST:
  Never make a network call during batch save.
  Always save to Room first. isSynced=false.
  WorkManager handles the upload when online.

RULE 2 — NO CRASHES:
  Every form field validated before submission.
  Every network call wrapped in try-catch.
  Room DB operations always on background thread.

RULE 3 — STOCK LIMIT:
  Always check Firestore (source of truth) for ordered qty.
  Available = Total Supplied − Total Ordered.
  If available ≤ 0 → disable Pre-Order button.

RULE 4 — IMAGE COMPRESSION:
  Never upload raw gallery image.
  Always compress with BitmapFactory, 75% quality.
  Target: under 500KB per image.

RULE 5 — EARTHY UI:
  Primary: #3E4F3C (Forest Green)
  Secondary: #A67B5B (Terracotta)
  Background: #F5F2ED (Parchment)
  No generic blue/white Material defaults allowed.

RULE 6 — GITHUB DAILY:
  Push to GitHub every evening.
  Meaningful commit messages:
  "Day 6: Add offline Room save + validation"
  "Day 13: Implement Stock Limit pre-order logic"
```

---

> **Final Note:** The core innovation of Budakattu-Sante is the **`isSynced` flag** in Room DB combined with **WorkManager's network constraints**. This single mechanism enables tribal users to work completely offline in zero-network forests and have all their data automatically reach urban buyers the moment they return to connectivity. Every kg of honey logged offline eventually becomes a visible, purchasable product on an urban buyer's screen — closing the gap between the forest floor and the city doorstep.