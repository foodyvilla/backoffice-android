package com.jp.foodyvilla_backoffice.data.new_backoffice.models

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long = 0L,
    val created_at: String? = null,
    val updated_at: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val fcm_token: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val long: Double? = null,
    val auth_user_id: String? = null,
    val is_verified: Boolean = false
)

@Serializable
data class CustomerUiModel(
    val id: Long = 0L,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val fcmToken: String = "",
    val isVerified: Boolean = false
)

fun UserResponse.toUiModel() = CustomerUiModel(
    id = id,
    name = name.orEmpty(),
    email = email.orEmpty(),
    phone = phone.orEmpty(),
    address = address.orEmpty(),
    fcmToken = fcm_token.orEmpty(),
    isVerified = is_verified
)

// Analytics wrapper model for customer depth calculations
data class CustomerDetailedAnalytics(
    val totalSpend: Double = 0.0,
    val totalOrders: Long = 0L,
    val favoriteItemName: String = "N/A",
    val favoriteOutletName: String = "N/A",
    val reviewsList: List<CustomerReviewMetric> = emptyList()
)

data class CustomerReviewMetric(
    val id: Long,
    val rating: Long,
    val type: String,
    val title: String,
    val description: String
)