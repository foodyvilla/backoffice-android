package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AdminPaymentStatus {
    CREATED, AUTHORIZED, CAPTURED, FAILED, REFUNDED
}

@Serializable
enum class AdminPaymentMethod {
    UPI, CARD, NETBANKING, WALLET, EMI, UNKNOWN
}

@Serializable
data class PaymentAdminResponse(
    val id: Long? = null,
    val created_at: String? = null,
    val order_id: String? = null, // UUID String mapping representation
    val customer_id: Long? = null,
    val razorpay_order_id: String? = null,
    val razorpay_payment_id: String? = null,
    val amount: Long? = null, // Amount in Paisa or direct currency units
    val payment_status: String? = null,
    val payment_method: String? = null,
    val error_description: String? = null
)

data class PaymentAdminUiModel(
    val id: Long = 0L,
    val createdAtDate: String = "",
    val orderId: String,
    val customerId: Long,
    val customerName: String = "Walk-in / Unknown",
    val customerContact: String = "N/A",
    val razorpayOrderId: String = "",
    val razorpayPaymentId: String = "",
    val amountDisplay: Double = 0.0,
    val status: AdminPaymentStatus = AdminPaymentStatus.CREATED,
    val method: AdminPaymentMethod = AdminPaymentMethod.UNKNOWN,
    val errorDescription: String = ""
)

fun PaymentAdminResponse.toUiModel(customerName: String, contact: String) = PaymentAdminUiModel(
    id = id ?: 0L,
    createdAtDate = created_at.orEmpty().take(10), // Extracts basic YYYY-MM-DD template indices
    orderId = order_id.orEmpty(),
    customerId = customer_id ?: 0L,
    customerName = customerName,
    customerContact = contact,
    razorpayOrderId = razorpay_order_id.orEmpty(),
    razorpayPaymentId = razorpay_payment_id.orEmpty(),
    amountDisplay = (amount ?: 0L).toDouble() / 100.0, // Assuming Paisa format; adjust multiplier conversion as needed
    status = when (payment_status?.lowercase()) {
        "authorized" -> AdminPaymentStatus.AUTHORIZED
        "captured" -> AdminPaymentStatus.CAPTURED
        "failed" -> AdminPaymentStatus.FAILED
        "refunded" -> AdminPaymentStatus.REFUNDED
        else -> AdminPaymentStatus.CREATED
    },
    method = when (payment_method?.lowercase()) {
        "upi" -> AdminPaymentMethod.UPI
        "card" -> AdminPaymentMethod.CARD
        "netbanking" -> AdminPaymentMethod.NETBANKING
        "wallet" -> AdminPaymentMethod.WALLET
        "emi" -> AdminPaymentMethod.EMI
        else -> AdminPaymentMethod.UNKNOWN
    },
    errorDescription = error_description.orEmpty()
)