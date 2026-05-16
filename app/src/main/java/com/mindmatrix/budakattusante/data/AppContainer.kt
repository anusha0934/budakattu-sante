package com.mindmatrix.budakattusante.data

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.mindmatrix.budakattusante.data.local.BudakattuDatabase
import com.mindmatrix.budakattusante.data.local.UserPreferences
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import com.mindmatrix.budakattusante.data.repository.*
import com.mindmatrix.budakattusante.ui.VoiceToTextParser
import com.mindmatrix.budakattusante.util.NetworkConnectivityObserver

class AppContainer(context: Context) {
    private val database = BudakattuDatabase.getInstance(context)
    val firebaseGateway = FirebaseGateway()
    val userPreferences = UserPreferences(context)
    val connectivityObserver = NetworkConnectivityObserver(context)
    val gson = Gson()
    
    val voiceToTextParser = VoiceToTextParser(context.applicationContext as Application)

    // Gemini API Key Integrated
    val aiRepository = AiRepository(apiKey = "AIzaSyA94wgc2wP-9Q6Z_gMIcQhL4y8hEi2TUgI")

    val productRepository = ProductRepository(
        batchDao = database.inventoryBatchDao(),
        productDao = database.productDao(),
        artisanDao = database.artisanDao(),
        syncQueueDao = database.syncQueueDao(),
        productHistoryDao = database.productHistoryDao(),
        sourceLocationDao = database.sourceLocationDao(),
        pendingUploadDao = database.pendingUploadDao(),
        syncMetadataDao = database.syncMetadataDao(),
        wishlistDao = database.wishlistDao(),
        notificationDao = database.notificationDao(),
        firebaseGateway = firebaseGateway,
        gson = gson
    )

    val orderRepository = OrderRepository(
        orderDao = database.orderDao(),
        cartDao = database.cartDao(),
        syncQueueDao = database.syncQueueDao(),
        notificationDao = database.notificationDao(),
        firebaseGateway = firebaseGateway,
        gson = gson
    )

    val addressRepository = AddressRepository(
        addressDao = database.addressDao()
    )

    val vendorRepository = VendorRepository(
        businessDetailsDao = database.businessDetailsDao(),
        firebaseGateway = firebaseGateway
    )

    val mspRepository = MspRepository(
        mspDao = database.mspDao()
    )

    val reviewRepository = ReviewRepository(
        reviewDao = database.reviewDao(),
        syncQueueDao = database.syncQueueDao(),
        firebaseGateway = firebaseGateway
    )

    val aiChatRepository = AiChatRepository(
        aiChatDao = database.aiChatDao(),
        aiRepository = aiRepository
    )

    val syncRepository = SyncRepository(
        syncQueueDao = database.syncQueueDao(),
        context = context
    )

    // Expose userProfileDao for the ProfileViewModel
    val userProfileDao = database.userProfileDao()
    
    val draftRepository = DraftRepository(database.draftDao())
    val notificationRepository = NotificationRepository(database.notificationDao(), firebaseGateway)
    
    val adminRepository = AdminRepository(
        productRepository = productRepository,
        firebaseGateway = firebaseGateway,
        syncQueueDao = database.syncQueueDao(),
        gson = gson
    )
}
