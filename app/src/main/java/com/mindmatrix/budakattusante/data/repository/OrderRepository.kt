package com.mindmatrix.budakattusante.data.repository

import com.google.gson.Gson
import com.mindmatrix.budakattusante.data.local.dao.CartDao
import com.mindmatrix.budakattusante.data.local.dao.OrderDao
import com.mindmatrix.budakattusante.data.local.dao.SyncQueueDao
import com.mindmatrix.budakattusante.data.local.dao.NotificationDao
import com.mindmatrix.budakattusante.data.local.entity.CartEntity
import com.mindmatrix.budakattusante.data.local.entity.OrderEntity
import com.mindmatrix.budakattusante.data.local.entity.SyncQueueEntity
import com.mindmatrix.budakattusante.data.local.entity.NotificationEntity
import com.mindmatrix.budakattusante.data.model.PaymentRecord
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val syncQueueDao: SyncQueueDao,
    private val notificationDao: NotificationDao,
    private val firebaseGateway: FirebaseGateway,
    private val gson: Gson
) {
    val orders: Flow<List<OrderEntity>> = orderDao.getOrders()
    val localCartItems: Flow<List<CartEntity>> = cartDao.getCartItems()

    suspend fun addToCart(product: Product) = withContext(Dispatchers.IO) {
        val cartItem = CartEntity(
            productId = product.productId,
            name = product.name,
            pricePerKg = product.pricePerKg,
            imageUrl = product.imageUrl,
            quantity = 1.0,
            vendorId = product.vendorId,
            isPreOrder = product.isPreOrder
        )
        cartDao.addToCart(cartItem)
    }

    suspend fun updateCartQuantity(productId: String, quantity: Double) = withContext(Dispatchers.IO) {
        cartDao.updateQuantity(productId, quantity)
    }

    suspend fun removeFromCart(productId: String) = withContext(Dispatchers.IO) {
        cartDao.removeFromCart(productId)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }

    suspend fun placeOrder(
        product: Product,
        quantityKg: Double,
        buyerName: String,
        buyerPhone: String,
        buyerAddress: String,
        buyerId: String,
        paymentMethod: String,
        totalAmount: Double,
        isPreOrder: Boolean = false
    ): OrderEntity = withContext(Dispatchers.IO) {
        val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val order = OrderEntity(
            orderId = orderId,
            productId = product.productId,
            vendorId = product.vendorId,
            buyerId = buyerId,
            productName = product.name,
            productImageUrl = product.imageUrl,
            quantityOrdered = quantityKg,
            buyerName = buyerName.trim(),
            buyerPhone = buyerPhone.trim(),
            buyerAddress = buyerAddress.trim(),
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            deliveryDate = if (isPreOrder) product.expectedHarvestDate else LocalDate.now().plusDays(7).toString(),
            isPreOrder = isPreOrder,
            harvestDate = product.expectedHarvestDate,
            orderStatus = if (isPreOrder) "RESERVED" else "PENDING",
            estimatedDeliveryDate = if (isPreOrder) {
                runCatching { LocalDate.parse(product.expectedHarvestDate).plusDays(5).toString() }.getOrDefault("")
            } else {
                LocalDate.now().plusDays(7).toString()
            },
            isSynced = false
        )
        
        orderDao.insertOrder(order)
        
        queueSyncItem("ORDER", order.orderId, "INSERT", order)
        
        notificationDao.insertNotification(NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = if (isPreOrder) "Pre-Order Reserved" else "Order Placed",
            message = "Your request for ${product.name} is saved. We will sync it shortly.",
            type = "ORDER_UPDATE"
        ))

        if (product.vendorId.isNotBlank()) {
            val paymentRecord = PaymentRecord(
                transactionId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
                vendorId = product.vendorId,
                amount = order.totalAmount,
                type = "CREDIT",
                description = "${if (isPreOrder) "Pre-Order" else "Sale"}: ${product.name}",
                method = paymentMethod,
                status = "SUCCESS",
                timestamp = System.currentTimeMillis()
            )
            queueSyncItem("PAYMENT", paymentRecord.transactionId, "INSERT", paymentRecord)
        }
        
        order
    }

    suspend fun updateOrderStatus(orderId: String, status: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, status)
        queueSyncItem("ORDER", orderId, "UPDATE", mapOf("orderStatus" to status))
    }

    suspend fun cancelOrder(orderId: String, productId: String, isPreOrder: Boolean) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, "CANCELLED")
        queueSyncItem("ORDER", orderId, "UPDATE", mapOf("orderStatus" to "CANCELLED"))
    }

    private suspend fun queueSyncItem(type: String, id: String, op: String, payload: Any) {
        val syncItem = SyncQueueEntity(
            entityType = type,
            entityId = id,
            operation = op,
            payloadJson = gson.toJson(payload),
            priority = if (type == "ORDER") 10 else 1
        )
        syncQueueDao.insert(syncItem)
    }

    /**
     * Requirement 5: Sync pending orders to Firebase.
     */
    suspend fun syncPendingOrders(): Int = withContext(Dispatchers.IO) {
        val pendingItems = syncQueueDao.getPendingItemsByType("ORDER")
        var count = 0
        for (item in pendingItems) {
            try {
                val order = gson.fromJson(item.payloadJson, OrderEntity::class.java)
                firebaseGateway.publishOrder(order)
                syncQueueDao.delete(item)
                orderDao.markAsSynced(order.orderId)
                count++
            } catch (e: Exception) {
                // Skip for now
            }
        }
        count
    }
}
