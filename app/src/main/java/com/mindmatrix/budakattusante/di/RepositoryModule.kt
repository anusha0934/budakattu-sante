package com.mindmatrix.budakattusante.di

import android.content.Context
import com.google.gson.Gson
import com.mindmatrix.budakattusante.data.local.dao.*
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import com.mindmatrix.budakattusante.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(
        batchDao: InventoryBatchDao,
        productDao: ProductDao,
        artisanDao: ArtisanDao,
        syncQueueDao: SyncQueueDao,
        productHistoryDao: ProductHistoryDao,
        sourceLocationDao: SourceLocationDao,
        pendingUploadDao: PendingUploadDao,
        syncMetadataDao: SyncMetadataDao,
        wishlistDao: WishlistDao,
        notificationDao: NotificationDao,
        firebaseGateway: FirebaseGateway,
        gson: Gson
    ): ProductRepository {
        return ProductRepository(
            batchDao, productDao, artisanDao, syncQueueDao,
            productHistoryDao, sourceLocationDao, pendingUploadDao,
            syncMetadataDao, wishlistDao, notificationDao, firebaseGateway, gson
        )
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        orderDao: OrderDao,
        cartDao: CartDao,
        syncQueueDao: SyncQueueDao,
        notificationDao: NotificationDao,
        firebaseGateway: FirebaseGateway,
        gson: Gson
    ): OrderRepository {
        return OrderRepository(orderDao, cartDao, syncQueueDao, notificationDao, firebaseGateway, gson)
    }

    @Provides
    @Singleton
    fun provideVendorRepository(
        businessDetailsDao: BusinessDetailsDao,
        firebaseGateway: FirebaseGateway
    ): VendorRepository {
        return VendorRepository(businessDetailsDao, firebaseGateway)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        syncQueueDao: SyncQueueDao,
        @ApplicationContext context: Context
    ): SyncRepository {
        return SyncRepository(syncQueueDao, context)
    }

    @Provides
    @Singleton
    fun provideMspRepository(mspDao: MspDao): MspRepository {
        return MspRepository(mspDao)
    }

    @Provides
    @Singleton
    fun provideReviewRepository(
        reviewDao: ReviewDao,
        syncQueueDao: SyncQueueDao,
        firebaseGateway: FirebaseGateway
    ): ReviewRepository {
        return ReviewRepository(reviewDao, syncQueueDao, firebaseGateway)
    }
    
    @Provides
    @Singleton
    fun provideAddressRepository(addressDao: AddressDao): AddressRepository {
        return AddressRepository(addressDao)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationDao: NotificationDao,
        firebaseGateway: FirebaseGateway
    ): NotificationRepository {
        return NotificationRepository(notificationDao, firebaseGateway)
    }

    @Provides
    @Singleton
    fun provideDraftRepository(draftDao: DraftDao): DraftRepository {
        return DraftRepository(draftDao)
    }
    
    @Provides
    @Singleton
    fun provideAiChatRepository(
        aiChatDao: AiChatDao,
        aiRepository: AiRepository
    ): AiChatRepository {
        return AiChatRepository(aiChatDao, aiRepository)
    }

    @Provides
    @Singleton
    fun provideAdminRepository(
        productRepository: ProductRepository,
        firebaseGateway: FirebaseGateway,
        syncQueueDao: SyncQueueDao,
        gson: Gson
    ): AdminRepository {
        return AdminRepository(productRepository, firebaseGateway, syncQueueDao, gson)
    }
}
