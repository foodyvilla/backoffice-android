package com.jp.foodyvilla_backoffice.domain.security

enum class OutletRole(val dbValue: String) {
    OWNER("owner"),
    HEAD_CHEF("head_chef"),
    CHEF("chef"),
    HELPER("helper"),
    WAITER("waiter"),
    MANAGER("manager"),
    STORE_SUPERVISOR("store_supervisor"),
    DELIVERY_BOY("delivery_boy"),
    KITCHEN("kitchen"),
    EMPLOYEE("employee"),
    CASHIER("cashier"),
    HEAD("head");

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
                "staff" to EMPLOYEE,
                "supervisor" to STORE_SUPERVISOR,
                "delivery" to DELIVERY_BOY
            )
            aliases[normalized]?.let { return it }
            return entries.firstOrNull { role ->
                role.dbValue.equals(normalized, ignoreCase = true)
            }
        }
    }
}
