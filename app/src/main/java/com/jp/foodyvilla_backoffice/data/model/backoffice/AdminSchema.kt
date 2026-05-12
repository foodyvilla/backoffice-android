package com.jp.foodyvilla_backoffice.data.model.backoffice

enum class AdminColumnType {
    Text,
    LongNumber,
    DecimalNumber,
    Boolean,
    Timestamp,
    Date,
    Uuid,
    TextArray,
    Json
}

data class AdminColumn(
    val name: String,
    val label: String = name.replace("_", " ").replaceFirstChar { it.uppercase() },
    val type: AdminColumnType = AdminColumnType.Text,
    val editable: Boolean = true,
    val required: Boolean = false,
    val multiline: Boolean = false,
    val helper: String? = null
)

data class AdminTable(
    val name: String,
    val title: String,
    val description: String,
    val primaryKey: String = "id",
    val primaryKeyType: AdminColumnType = AdminColumnType.LongNumber,
    val orderBy: String = "created_at",
    val columns: List<AdminColumn>,
    val displayColumns: List<String>,
    val createLabel: String = "New row"
) {
    val editableColumns: List<AdminColumn> = columns.filter { it.editable }
}

val adminTables = listOf(
    AdminTable(
        name = "orders",
        title = "Orders",
        description = "Customer orders, delivery details, payments, and status updates.",
        primaryKeyType = AdminColumnType.Uuid,
        displayColumns = listOf("customer_name", "phone", "status", "order_type", "created_at"),
        createLabel = "New order",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.Uuid, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("instruction", multiline = true),
            AdminColumn("phone"),
            AdminColumn("delivery_lat", type = AdminColumnType.DecimalNumber),
            AdminColumn("delivery_long", type = AdminColumnType.DecimalNumber),
            AdminColumn("address", multiline = true),
            AdminColumn("customer_name"),
            AdminColumn("status", required = true, helper = "PLACED, ACCEPTED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED"),
            AdminColumn("customer_id", type = AdminColumnType.LongNumber),
            AdminColumn("order_type"),
            AdminColumn("transaction_id")
        )
    ),
    AdminTable(
        name = "products",
        title = "Products",
        description = "Menu products, pricing, ratings, diet flags, media, and nutrition.",
        displayColumns = listOf("name", "category", "price", "discount", "isBestSeller"),
        createLabel = "New product",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("name", required = true),
            AdminColumn("description", multiline = true),
            AdminColumn("image", type = AdminColumnType.TextArray, helper = "Use comma separated image URLs or a JSON array."),
            AdminColumn("rating", type = AdminColumnType.DecimalNumber),
            AdminColumn("price", type = AdminColumnType.DecimalNumber, required = true),
            AdminColumn("discount", type = AdminColumnType.LongNumber),
            AdminColumn("review", type = AdminColumnType.Json, multiline = true, helper = "JSON array"),
            AdminColumn("category"),
            AdminColumn("reviewsCount", type = AdminColumnType.LongNumber),
            AdminColumn("prepTime"),
            AdminColumn("nutritionalInfo", type = AdminColumnType.Json, multiline = true),
            AdminColumn("isVeg", type = AdminColumnType.Boolean),
            AdminColumn("isVegan", type = AdminColumnType.Boolean),
            AdminColumn("isBestSeller", type = AdminColumnType.Boolean)
        )
    ),
    AdminTable(
        name = "users",
        title = "Users",
        description = "Customer profiles, contact information, addresses, and verification state.",
        displayColumns = listOf("name", "phone", "email", "is_verified", "created_at"),
        createLabel = "New user",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("name"),
            AdminColumn("email"),
            AdminColumn("phone"),
            AdminColumn("fcm_token", multiline = true),
            AdminColumn("address", multiline = true),
            AdminColumn("lat", type = AdminColumnType.DecimalNumber),
            AdminColumn("long", type = AdminColumnType.DecimalNumber),
            AdminColumn("auth_user_id", type = AdminColumnType.Uuid),
            AdminColumn("updated_at", type = AdminColumnType.Timestamp),
            AdminColumn("is_verified", type = AdminColumnType.Boolean)
        )
    ),
    AdminTable(
        name = "banners",
        title = "Banners",
        description = "Home screen promotional carousel banners.",
        displayColumns = listOf("title", "img_url", "created_at"),
        createLabel = "New banner",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("title"),
            AdminColumn("img_url", multiline = true)
        )
    ),
    AdminTable(
        name = "offers",
        title = "Offers",
        description = "Offer cards and linked campaign destinations.",
        primaryKeyType = AdminColumnType.Uuid,
        displayColumns = listOf("title", "desc", "linked_url", "created_at"),
        createLabel = "New offer",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.Uuid, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("title"),
            AdminColumn("desc", multiline = true),
            AdminColumn("img_url", multiline = true),
            AdminColumn("linked_url", multiline = true)
        )
    ),
    AdminTable(
        name = "reviews",
        title = "Reviews",
        description = "Order and product reviews with ratings and media.",
        displayColumns = listOf("customer_id", "order_id", "rating", "title", "created_at"),
        createLabel = "New review",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("customer_id", type = AdminColumnType.LongNumber),
            AdminColumn("order_id", type = AdminColumnType.Uuid),
            AdminColumn("is_product_review", type = AdminColumnType.Boolean),
            AdminColumn("title"),
            AdminColumn("desc", multiline = true),
            AdminColumn("img_url", type = AdminColumnType.Json, multiline = true),
            AdminColumn("rating", type = AdminColumnType.LongNumber)
        )
    ),
    AdminTable(
        name = "employee",
        title = "Employees",
        description = "Staff records, salary, role, joining date, and attendance punch location.",
        displayColumns = listOf("name", "contact", "role", "salary", "joining_date"),
        createLabel = "New employee",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("name", required = true),
            AdminColumn("address", multiline = true),
            AdminColumn("contact"),
            AdminColumn("aadhar_no"),
            AdminColumn("emergency_contact"),
            AdminColumn("salary", type = AdminColumnType.LongNumber),
            AdminColumn("profile_img", multiline = true),
            AdminColumn("joining_date", type = AdminColumnType.Date),
            AdminColumn("punch_lat", type = AdminColumnType.DecimalNumber),
            AdminColumn("punch_lang", type = AdminColumnType.DecimalNumber),
            AdminColumn("role")
        )
    ),
    AdminTable(
        name = "attendance",
        title = "Attendance",
        description = "Employee in/out punch records and status.",
        displayColumns = listOf("emp_id", "status", "in_time", "out_time", "created_at"),
        createLabel = "New attendance",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("emp_id", type = AdminColumnType.LongNumber),
            AdminColumn("status"),
            AdminColumn("in_time", type = AdminColumnType.Timestamp),
            AdminColumn("out_time", type = AdminColumnType.Timestamp)
        )
    ),
    AdminTable(
        name = "auth_otp",
        title = "Auth OTP",
        description = "Phone OTP records used by the custom login flow.",
        displayColumns = listOf("phone", "otp", "created_at"),
        createLabel = "New OTP",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("phone", required = true),
            AdminColumn("otp", required = true)
        )
    )
)
