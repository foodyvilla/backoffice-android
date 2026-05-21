package com.jp.foodyvilla_backoffice.data.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: Long,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("outlet_id") val outletId: Long,
    val name: String? = null,
    val address: String? = null,
    val contact: String? = null,
    @SerialName("aadhar_no") val aadharNo: String? = null,
    val role: String,
    @SerialName("auth_user_id") val authUserId: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

@Serializable
data class AttendanceLog(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("emp_id") val employeeId: Long,
    val status: String,
    @SerialName("in_time") val inTime: String? = null,
    @SerialName("out_time") val outTime: String? = null,
    @SerialName("in_lat") val inLat: Double? = null,
    @SerialName("in_lng") val inLng: Double? = null,
    @SerialName("out_lat") val outLat: Double? = null,
    @SerialName("out_lng") val outLng: Double? = null
)

@Serializable
data class BackOfficeOrder(
    val id: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("outlet_id") val outletId: Long,
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("customer_name") val customerName: String? = null,
    val phone: String? = null,
    val status: String = OrderStatus.Pending.dbValue,
    @SerialName("order_type") val orderType: String? = null,
    val address: String? = null,
    @SerialName("delivery_lat") val deliveryLat: Double? = null,
    @SerialName("delivery_long") val deliveryLong: Double? = null,
    val instruction: String? = null,
    @SerialName("transaction_id") val transactionId: String? = null
)

@Serializable
data class BackOfficeOrderItem(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("order_id") val orderId: String,
    @SerialName("menu_item_id") val menuItemId: Long,
    val qty: Int,
    @SerialName("price_per_item") val pricePerItem: Double,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("total_discount") val totalDiscount: Double? = null
)

enum class OrderStatus(val dbValue: String) {
    Pending("pending"),
    Accepted("accepted"),
    Preparing("preparing"),
    Ready("ready"),
    Completed("completed"),
    Rejected("rejected");

    companion object {
        fun fromDb(value: String): OrderStatus {
            val normalized = value.trim().lowercase().replace(" ", "_")
            return entries.firstOrNull { it.dbValue == normalized }
                ?: when {
                    normalized == "placed" -> Pending
                    normalized == "delivered" -> Completed
                    normalized == "cancelled" -> Rejected
                    normalized == "out_for_delivery" -> Ready
                    else -> Pending
                }
        }
    }
}

