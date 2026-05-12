package com.jp.foodyvilla_backoffice.data.model.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val access_token: String? = null,
    val refresh_token: String? = null,
    val expires_in: Long? = null,
    val expires_at: Long? = null,
    val is_new_user: Boolean? = null,
    val user: UserDto? = null
)

@Serializable
data class UserDto(
    val id: Int,
    val phone: String,
    val name: String? = null,
    val email: String? = null,
    val auth_user_id: String,
    val created_at: String? = null,
    val updated_at: String? = null,
    val is_verified: Boolean? = null
)