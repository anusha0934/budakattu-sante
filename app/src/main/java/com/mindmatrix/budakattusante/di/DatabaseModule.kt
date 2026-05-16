package com.mindmatrix.budakattusante.di

import android.content.Context
import com.mindmatrix.budakattusante.data.local.BudakattuDatabase
import com.mindmatrix.budakattusante.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BudakattuDatabase {
        return BudakattuDatabase.getInstance(context)
    }

    @Provides
    fun provideProductDao(db: BudakattuDatabase): ProductDao = db.productDao()

    @Provides
    fun provideOrderDao(db: BudakattuDatabase): OrderDao = db.orderDao()

    @Provides
    fun provideArtisanDao(db: BudakattuDatabase): ArtisanDao = db.artisanDao()

    @Provides
    fun provideSyncQueueDao(db: BudakattuDatabase): SyncQueueDao = db.syncQueueDao()

    @Provides
    fun providePendingUploadDao(db: BudakattuDatabase): PendingUploadDao = db.pendingUploadDao()

    @Provides
    fun provideInventoryBatchDao(db: BudakattuDatabase): InventoryBatchDao = db.inventoryBatchDao()

    @Provides
    fun provideProductHistoryDao(db: BudakattuDatabase): ProductHistoryDao = db.productHistoryDao()

    @Provides
    fun provideSourceLocationDao(db: BudakattuDatabase): SourceLocationDao = db.sourceLocationDao()

    @Provides
    fun provideSyncMetadataDao(db: BudakattuDatabase): SyncMetadataDao = db.syncMetadataDao()
    
    @Provides
    fun provideUserProfileDao(db: BudakattuDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideAddressDao(db: BudakattuDatabase): AddressDao = db.addressDao()

    @Provides
    fun provideBusinessDetailsDao(db: BudakattuDatabase): BusinessDetailsDao = db.businessDetailsDao()

    @Provides
    fun provideMspDao(db: BudakattuDatabase): MspDao = db.mspDao()

    @Provides
    fun provideReviewDao(db: BudakattuDatabase): ReviewDao = db.reviewDao()
    
    @Provides
    fun provideAiChatDao(db: BudakattuDatabase): AiChatDao = db.aiChatDao()

    @Provides
    fun provideDraftDao(db: BudakattuDatabase): DraftDao = db.draftDao()

    @Provides
    fun provideCartDao(db: BudakattuDatabase): CartDao = db.cartDao()

    @Provides
    fun provideNotificationDao(db: BudakattuDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideWishlistDao(db: BudakattuDatabase): WishlistDao = db.wishlistDao()
}
