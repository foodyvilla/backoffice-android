package com.jp.foodyvilla_backoffice.domain.security

sealed class AppFeature(
    val title: String,
    private val allowedOutletRoles: Set<OutletRole> = emptySet(),
    private val requiredPermissions: Set<String> = emptySet()
) {
    data object Dashboard : AppFeature(
        title = "Dashboard",
        allowedOutletRoles = setOf(OutletRole.OWNER, OutletRole.HEAD, OutletRole.MANAGER, OutletRole.CHEF, OutletRole.EMPLOYEE),
        requiredPermissions = setOf(AdminPermission.ANALYTICS_READ, AdminPermission.REPORTS_VIEW)
    )

    data object InventoryManagement : AppFeature(
        title = "Inventory Management",
        allowedOutletRoles = setOf(OutletRole.OWNER, OutletRole.HEAD, OutletRole.MANAGER, OutletRole.CHEF, OutletRole.EMPLOYEE),
        requiredPermissions = setOf(AdminPermission.INVENTORY_READ, AdminPermission.INVENTORY_WRITE, AdminPermission.OUTLET_EDIT)
    )

    data object OrderProcessing : AppFeature(
        title = "Order Processing",
        allowedOutletRoles = setOf(OutletRole.OWNER, OutletRole.HEAD, OutletRole.MANAGER, OutletRole.CHEF, OutletRole.EMPLOYEE, OutletRole.KITCHEN, OutletRole.CASHIER, OutletRole.WAITER),
        requiredPermissions = setOf(AdminPermission.ORDERS_READ, AdminPermission.ORDERS_WRITE)
    )

    data object BiometricAttendance : AppFeature(
        title = "Biometric Attendance",
        allowedOutletRoles = OutletRole.entries.toSet(),
        requiredPermissions = setOf(AdminPermission.ATTENDANCE_READ, AdminPermission.ATTENDANCE_WRITE, AdminPermission.HR_ATTENDANCE_READ)
    )

    data object WhatsAppCampaigns : AppFeature(
        title = "WhatsApp Campaigns",
        allowedOutletRoles = setOf(OutletRole.OWNER, OutletRole.HEAD, OutletRole.MANAGER),
        requiredPermissions = setOf(AdminPermission.WHATSAPP_CAMPAIGNS_READ, AdminPermission.WHATSAPP_CAMPAIGNS_WRITE, AdminPermission.CAMPAIGN_WRITE)
    )

    data object EmployeeManagement : AppFeature(
        title = "Employee Management",
        allowedOutletRoles = setOf(OutletRole.OWNER, OutletRole.HEAD),
        requiredPermissions = setOf(AdminPermission.EMPLOYEES_READ, AdminPermission.EMPLOYEES_WRITE, AdminPermission.EMPLOYEE_EDIT)
    )

    data object CustomerManagement : AppFeature(
        title = "Customer Management",
        allowedOutletRoles = setOf(OutletRole.OWNER, OutletRole.HEAD, OutletRole.MANAGER, OutletRole.CHEF, OutletRole.CASHIER),
        requiredPermissions = setOf(AdminPermission.CUSTOMERS_READ, AdminPermission.REPORTS_VIEW)
    )

    data object Settings : AppFeature(
        title = "Settings",
        allowedOutletRoles = setOf(OutletRole.OWNER, OutletRole.HEAD),
        requiredPermissions = setOf(AdminPermission.SETTINGS_WRITE, AdminPermission.OUTLET_EDIT)
    )

    fun isAccessibleBy(session: UserSession): Boolean {
        return when (session) {
            is UserSession.OutletSession -> session.role in allowedOutletRoles
            is UserSession.EmployeeSession -> session.role in allowedOutletRoles ||
                requiredPermissions.any { permission ->
                    session.permissions.any { userPermission ->
                        userPermission.equals(permission, ignoreCase = true)
                    }
                }
        }
    }

    companion object {
        val primaryFeatures = listOf(
            Dashboard,
            InventoryManagement,
            OrderProcessing,
            BiometricAttendance,
            WhatsAppCampaigns,
            EmployeeManagement,
            CustomerManagement,
            Settings
        )
    }
}
