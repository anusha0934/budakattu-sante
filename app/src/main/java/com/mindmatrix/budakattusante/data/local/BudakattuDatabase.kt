package com.mindmatrix.budakattusante.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mindmatrix.budakattusante.data.local.dao.*
import com.mindmatrix.budakattusante.data.local.entity.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        InventoryBatchEntity::class, 
        OrderEntity::class, 
        AddressEntity::class,
        ProductEntity::class,
        CartEntity::class,
        ArtisanEntity::class,
        NotificationEntity::class,
        BusinessDetailsEntity::class,
        UserProfileEntity::class,
        MspEntity::class,
        ReviewEntity::class,
        AiChatEntity::class,
        SyncQueueEntity::class,
        PendingUploadEntity::class,
        ProductHistoryEntity::class,
        SourceLocationEntity::class,
        SyncMetadataEntity::class,
        DraftEntity::class,
        WishlistEntity::class
    ],
    version = 17,
    exportSchema = false
)
abstract class BudakattuDatabase : RoomDatabase() {
    abstract fun inventoryBatchDao(): InventoryBatchDao
    abstract fun orderDao(): OrderDao
    abstract fun addressDao(): AddressDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun artisanDao(): ArtisanDao
    abstract fun notificationDao(): NotificationDao
    abstract fun businessDetailsDao(): BusinessDetailsDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun mspDao(): MspDao
    abstract fun reviewDao(): ReviewDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun pendingUploadDao(): PendingUploadDao
    abstract fun productHistoryDao(): ProductHistoryDao
    abstract fun sourceLocationDao(): SourceLocationDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun draftDao(): DraftDao
    abstract fun wishlistDao(): WishlistDao

    companion object {
        @Volatile
        private var instance: BudakattuDatabase? = null

        fun getInstance(context: Context): BudakattuDatabase =
            instance ?: synchronized(this) {
                // Requirement 15: Encrypt Room database using SQLCipher
                SQLiteDatabase.loadLibs(context)
                val passphrase = SQLiteDatabase.getBytes("BUDAKATTU_SANTE_SECURE_KEY_2026".toCharArray())
                val factory = SupportFactory(passphrase)

                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BudakattuDatabase::class.java,
                    "budakattu_sante_secure.db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
            }
    }
}
