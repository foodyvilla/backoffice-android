package com.jp.foodyvilla_backoffice.domain.security

sealed interface UserSession {
    val outletId: Long

    fun isOwner(): Boolean = when (this) {
        is OutletSession -> role == OutletRole.OWNER
        is EmployeeSession -> role == OutletRole.OWNER
    }


    fun role() : String{
        if (isHead()) return "head"
        if (isOwner()) return "owner"
        else return "employee"
    }
    fun isHead(): Boolean = when (this) {
        is OutletSession -> role == OutletRole.HEAD || role == OutletRole.MANAGER
        is EmployeeSession -> role == OutletRole.HEAD || role == OutletRole.MANAGER
    }

    fun canManageEmployees(): Boolean = isOwner()

    fun canCreate(tableName: String): Boolean {
        if (isOwner()) return true
        val role = when (this) {
            is OutletSession -> role
            is EmployeeSession -> role
        } ?: return false

        return when (role) {
            OutletRole.HEAD, OutletRole.MANAGER -> tableName != "outlets" && tableName != "categories" && tableName != "employee"
            OutletRole.CHEF, OutletRole.EMPLOYEE, OutletRole.KITCHEN, OutletRole.WAITER, OutletRole.CASHIER -> {
                tableName == "orders" || tableName == "order_items" || tableName == "attendance"
            }
            else -> false
        }
    }

    fun canEdit(tableName: String): Boolean {
        if (isOwner()) return true
        val role = when (this) {
            is OutletSession -> role
            is EmployeeSession -> role
        } ?: return false

        return when (role) {
            OutletRole.HEAD, OutletRole.MANAGER -> tableName != "categories" && tableName != "employee"
            OutletRole.CHEF, OutletRole.EMPLOYEE, OutletRole.KITCHEN, OutletRole.WAITER, OutletRole.CASHIER -> {
                tableName == "orders" || tableName == "order_items" || tableName == "attendance"
            }
            else -> false
        }
    }

    fun canDelete(tableName: String): Boolean {
        if (isOwner()) return true
        val role = when (this) {
            is OutletSession -> role
            is EmployeeSession -> role
        } ?: return false

        return when (role) {
            OutletRole.HEAD, OutletRole.MANAGER -> {
                tableName != "outlets" && tableName != "categories" && tableName != "employee" && tableName != "payments"
            }
            else -> false
        }
    }

    data class OutletSession(
        override val outletId: Long,
        val username: String,
        val role: OutletRole
    ) : UserSession

    data class EmployeeSession(
        val empId: String,
        override val outletId: Long,
        val designationId: Long?,
        val permissions: Set<String>,
        val role: OutletRole? = null,
        val name: String? = null,
        val contact: String? = null
    ) : UserSession
}
