package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.budakattusante.data.local.entity.CartEntity
import com.mindmatrix.budakattusante.data.local.entity.OrderEntity
import com.mindmatrix.budakattusante.data.model.Product
import com.mindmatrix.budakattusante.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderUiState(
    val orders: List<OrderEntity> = emptyList(),
    val cartItems: List<CartEntity> = emptyList(),
    val latestOrder: OrderEntity? = null,
    val placing: Boolean = false,
    val message: String? = null,
    val discountAmount: Double = 0.0,
    val appliedCoupon: String? = null,
    val shippingFee: Double = 0.0
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {
    private val _placing = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)
    private val _latestOrder = MutableStateFlow<OrderEntity?>(null)
    private val _discountAmount = MutableStateFlow(0.0)
    private val _appliedCoupon = MutableStateFlow<String?>(null)

    val uiState: StateFlow<OrderUiState> = combine(
        repository.orders,
        repository.localCartItems,
        _latestOrder,
        _placing,
        _message,
        _discountAmount,
        _appliedCoupon
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val orders = flows[0] as List<OrderEntity>
        @Suppress("UNCHECKED_CAST")
        val cartItems = flows[1] as List<CartEntity>
        val latestOrder = flows[2] as? OrderEntity
        val placing = flows[3] as Boolean
        val message = flows[4] as? String
        val discount = flows[5] as Double
        val coupon = flows[6] as? String

        val subtotal = cartItems.sumOf { it.pricePerKg * it.quantity }
        val shipping = if (subtotal > 0 && subtotal < 1000) 50.0 else 0.0

        OrderUiState(
            orders = orders,
            cartItems = cartItems,
            latestOrder = latestOrder,
            placing = placing,
            message = message,
            discountAmount = discount,
            appliedCoupon = coupon,
            shippingFee = shipping
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OrderUiState()
    )

    var pendingOrderDetails by mutableStateOf<PendingOrder?>(null)
        private set

    data class PendingOrder(
        val product: Product,
        val qty: Double,
        val name: String,
        val phone: String,
        val address: String,
        val isPreOrder: Boolean = false
    )

    fun addToCart(product: Product) {
        viewModelScope.launch {
            repository.addToCart(product)
            _message.value = "Added to Cart: ${product.name} (Offline)"
        }
    }

    /**
     * Requirement 19: Quantity update in cart.
     */
    fun updateCartQuantity(productId: String, quantity: Double) {
        viewModelScope.launch {
            if (quantity <= 0) {
                repository.removeFromCart(productId)
            } else {
                repository.updateCartQuantity(productId, quantity)
            }
            recalculateCoupon()
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
            recalculateCoupon()
        }
    }

    fun setPendingOrder(product: Product, qty: Double, name: String, phone: String, address: String, isPreOrder: Boolean = false) {
        pendingOrderDetails = PendingOrder(product, qty, name, phone, address, isPreOrder)
        _discountAmount.value = 0.0
        _appliedCoupon.value = null
    }

    private fun recalculateCoupon() {
        val currentCoupon = _appliedCoupon.value ?: return
        val subtotal = uiState.value.cartItems.sumOf { it.pricePerKg * it.quantity }
        applyCoupon(currentCoupon, subtotal, silent = true)
    }

    /**
     * Requirement 19: Coupon support.
     */
    fun applyCoupon(code: String, subtotal: Double, silent: Boolean = false) {
        if (code.isBlank()) {
            if (!silent) _message.value = "Please enter a coupon code"
            return
        }
        when (code.uppercase()) {
            "TRIBAL20" -> {
                _discountAmount.value = subtotal * 0.20
                _appliedCoupon.value = "TRIBAL20"
                if (!silent) _message.value = "TRIBAL20 applied! 20% off"
            }
            "HARVEST10" -> {
                _discountAmount.value = subtotal * 0.10
                _appliedCoupon.value = "HARVEST10"
                if (!silent) _message.value = "HARVEST10 applied! 10% off"
            }
            else -> {
                if (!silent) _message.value = "Invalid coupon code"
                _discountAmount.value = 0.0
                _appliedCoupon.value = null
            }
        }
    }

    fun placeOrder(
        buyerId: String,
        buyerName: String,
        buyerPhone: String,
        buyerAddress: String,
        paymentMethod: String
    ) {
        val pending = pendingOrderDetails ?: return
        viewModelScope.launch {
            _placing.value = true
            try {
                val subtotal = pending.product.pricePerKg * pending.qty
                val shipping = if (subtotal < 1000) 50.0 else 0.0
                val totalWithDiscount = subtotal + shipping - _discountAmount.value

                val order = repository.placeOrder(
                    product = pending.product,
                    quantityKg = pending.qty,
                    buyerName = buyerName,
                    buyerPhone = buyerPhone,
                    buyerAddress = buyerAddress,
                    buyerId = buyerId,
                    paymentMethod = paymentMethod,
                    totalAmount = totalWithDiscount,
                    isPreOrder = pending.isPreOrder
                )
                
                _latestOrder.value = order
                _message.value = if (pending.isPreOrder) "Pre-order reserved offline!" else "Order placed offline!"
                pendingOrderDetails = null
            } catch (e: Exception) {
                _message.value = e.message ?: "Could not place order"
            } finally {
                _placing.value = false
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, status)
                _message.value = "Order status updated to $status (Sync queued)"
            } catch (e: Exception) {
                _message.value = "Failed to update status: ${e.message}"
            }
        }
    }

    fun cancelOrder(orderId: String, productId: String, isPreOrder: Boolean) {
        viewModelScope.launch {
            try {
                repository.cancelOrder(orderId, productId, isPreOrder)
                _message.value = "Order cancelled offline"
            } catch (e: Exception) {
                _message.value = "Failed to cancel order: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun resetLatestOrder() {
        _latestOrder.value = null
    }
}
