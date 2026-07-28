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
        val role = when (this) {
            is OutletSession -> role
            is EmployeeSession -> role
        }
        return role?.dbValue ?: "employee"
    }

    fun isHead(): Boolean = when (this) {
        is OutletSession -> role == OutletRole.HEAD || role == OutletRole.MANAGER || role == OutletRole.STORE_SUPERVISOR
        is EmployeeSession -> role == OutletRole.HEAD || role == OutletRole.MANAGER || role == OutletRole.STORE_SUPERVISOR
    }

    fun canAcceptOrders(): Boolean {
        if (isOwner()) return true
        val role = when (this) {
            is OutletSession -> role
            is EmployeeSession -> role
        } ?: return false
        return role == OutletRole.MANAGER || role == OutletRole.STORE_SUPERVISOR
    }

    fun canManageMenu(): Boolean {
        if (isOwner()) return true
        val role = when (this) {
            is OutletSession -> role
            is EmployeeSession -> role
        } ?: return false
        return role == OutletRole.MANAGER || role == OutletRole.STORE_SUPERVISOR
    }

    fun canUpdateOrderStatus(status: String): Boolean {
        if (isOwner()) return true
        val role = when (this) {
            is OutletSession -> role
            is EmployeeSession -> role
        } ?: return false
        
        if (role == OutletRole.DELIVERY_BOY) {
            return status.lowercase() == "completed" || status.lowercase() == "delivered" || status.lowercase() == "cancelled"
        }
        
        // Managers/Supervisors can do anything. 
        // Others might have limited access depending on app flow, 
        // but for now let's focus on the Delivery Boy restriction.
        if (role == OutletRole.MANAGER || role == OutletRole.STORE_SUPERVISOR) return true
        
        return true // Default allowed for others? Or maybe restricted to "accepted/preparing" for chefs?
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
