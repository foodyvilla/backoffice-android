package com.jp.foodyvilla_backoffice.data.model.backoffice

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskCategory(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val name: String = "",
    val description: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
) : AdminModel
