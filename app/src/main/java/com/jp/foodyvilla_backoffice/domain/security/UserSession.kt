package com.jp.foodyvilla_backoffice.domain.security

sealed interface UserSession {
    val outletId: Long

    data class OutletSession(
        override val outletId: Long,
        val username: String,
        val role: OutletRole
    ) : UserSession

    data class EmployeeSession(
        val empId: Long,
        override val outletId: Long,
        val designationId: Long?,
        val permissions: Set<String>,
        val role: OutletRole? = null,
        val name: String? = null,
        val contact: String? = null
    ) : UserSession
}
