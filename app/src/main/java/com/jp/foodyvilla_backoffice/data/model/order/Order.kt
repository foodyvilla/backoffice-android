package com.jp.foodyvilla_backoffice.data.model.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderModel(
    val id: String,
    val customer_id: Int,
    val status: String,
    val created_at: String,

    val address: String? = null,
    val customer_name: String? = null,
    val phone: String? = null,

    val instruction: String? = null,
    val delivery_lat: Double? = null,
    val delivery_long: Double? = null,
    val order_type: String? = null,
    val transaction_id: String?  = null,
    val order_items : List<OrderItem> = emptyList()
)

// ───── Insert Models (separate from response models) ─────

@Serializable
data class OrderInsert(
    val customer_id: Int,
    val status: String,
    val address: String,
    val phone: String,
    val transaction_id   : String ?= null,
    val customer_name: String,
    val order_type: String? = null,
    val instruction: String? = null,
    val delivery_lat: Double? = null,
    val delivery_long: Double? = null
)

@Serializable
data class OrderItemInsert(
    val order_id: String,
    val productid: Int,
    val qty: Int,
    val price_per_item: Double,
    val total_price: Double,
    val total_discount: Double = 0.0
)