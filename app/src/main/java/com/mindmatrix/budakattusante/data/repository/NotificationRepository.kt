package com.mindmatrix.budakattusante.data.repository

import com.mindmatrix.budakattusante.data.local.dao.NotificationDao
import com.mindmatrix.budakattusante.data.local.entity.NotificationEntity
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val firebaseGateway: FirebaseGateway
) {
    /**
     * Requirement 9: Notifications.
     * Requirement 14: Offline notifications queue.
     */
    val notifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun addNotification(notification: NotificationEntity) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotification(id)
    }

    /**
     * Requirement 9: Notify artisan when batch is approved.
     * In a real app, this would use FCM. Here we simulate local and remote push.
     */
    suspend fun sendBatchApprovalNotification(artisanId: String, batchId: String) {
        val notification = NotificationEntity(
            id = "APPROVAL_${batchId}",
            title = "Batch Approved!",
            message = "Your harvest batch $batchId has been approved and is now live for customers.",
            type = "BATCH_APPROVAL"
        )
        notificationDao.insertNotification(notification)
        // Future: Integration with firebaseGateway.sendPushNotification(artisanId, ...)
    }
}
