package com.mindmatrix.budakattusante.data.model

import java.util.UUID

data class PaymentRecord(
    val transactionId: String = UUID.randomUUID().toString(),
    val vendorId: String = "",
    val amount: Double = 0.0,
    val type: String = "CREDIT", // "CREDIT" for sales, "DEBIT" for withdrawals
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    val method: String = "UPI" // "UPI", "WALLET", "BANK"
)
