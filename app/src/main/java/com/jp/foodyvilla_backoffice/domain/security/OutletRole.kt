package com.jp.foodyvilla_backoffice.domain.security

enum class OutletRole(val dbValue: String) {
    OWNER("owner"),
    HEAD("head"),
    MANAGER("manager"),
    CHEF("chef"),
    KITCHEN("kitchen"),
    EMPLOYEE("employee"),
    WAITER("waiter"),
    CASHIER("cashier");

    companion object {
        fun fromDbValue(value: String): OutletRole? {
            val normalized = value.trim().lowercase().replace(" ", "_")
            val aliases = mapOf(
                "admin" to OWNER,
                "outlet_head" to HEAD,
                "store_head" to HEAD,
                "head_manager" to HEAD,
                "cook" to CHEF,
                "kitchen_staff" to CHEF,
                "staff" to EMPLOYEE
            )
            aliases[normalized]?.let { return it }
            return entries.firstOrNull { role ->
                role.dbValue.equals(normalized, ignoreCase = true)
            }
        }
    }
}
