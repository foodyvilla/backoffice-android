package com.jp.foodyvilla_backoffice.domain.security

object AdminPermission {
    const val ANALYTICS_READ = "analytics:read"
    const val REPORTS_VIEW = "REPORTS_VIEW"
    const val INVENTORY_READ = "inventory:read"
    const val INVENTORY_WRITE = "inventory:write"
    const val OUTLET_EDIT = "OUTLET_EDIT"
    const val ORDERS_READ = "orders:read"
    const val ORDERS_WRITE = "orders:write"
    const val ATTENDANCE_READ = "attendance:read"
    const val ATTENDANCE_WRITE = "attendance:write"
    const val HR_ATTENDANCE_READ = "HR_ATTENDANCE_READ"
    const val WHATSAPP_CAMPAIGNS_READ = "whatsapp_campaigns:read"
    const val WHATSAPP_CAMPAIGNS_WRITE = "whatsapp_campaigns:write"
    const val CAMPAIGN_WRITE = "CAMPAIGN_WRITE"
    const val EMPLOYEES_READ = "employees:read"
    const val EMPLOYEES_WRITE = "employees:write"
    const val EMPLOYEE_EDIT = "EMPLOYEE_EDIT"
    const val CUSTOMERS_READ = "customers:read"
    const val REVIEWS_MODERATE = "reviews:moderate"
    const val SETTINGS_WRITE = "settings:write"
}
