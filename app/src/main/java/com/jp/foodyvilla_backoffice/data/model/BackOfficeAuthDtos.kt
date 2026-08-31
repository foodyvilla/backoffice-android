package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackOfficeLoginResponseDto(
    val success: Boolean,
    val message: String? = null,
    val session: BackOfficeSessionDto? = null,
    val employee: BackOfficeEmployeeDto? = null,
    val token: BackOfficeTokenDto? = null
)

@Serializable
data class BackOfficeSessionDto(
    val type: String = "",
    @SerialName("outlet_id")
    val outletId: Long,
    val username: String? = null,
    val role: String? = null,
    val name: String? = null,
    val contact: String? = null,
    @SerialName("emp_id")
    val empId: String? = null,
    @SerialName("designation_id")
    val designationId: Long? = null,
    val permissions: List<String> = emptyList()
)

@Serializable
data class BackOfficeEmployeeDto(
    val id: String,
    val name: String? = null,
    val role: String? = null,
    @SerialName("outlet_id")
    val outletId: Long,
    val contact: String? = null
)

@Serializable
data class BackOfficeTokenDto(
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("expires_at")
    val expiresAt: Long? = null
)
