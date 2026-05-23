package com.jp.foodyvilla_backoffice.data.model.user

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val auth_user_id: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val address: String? ,
    val lat: Double?,
    val long: Double?,
    val is_verified: Boolean
)