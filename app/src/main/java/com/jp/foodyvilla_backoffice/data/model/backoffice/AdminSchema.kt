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

data class AdminReference(
    val table: String,
    val valueColumn: String = "id",
    val labelColumns: List<String>
)

data class AdminColumn(
    val name: String,
    val label: String = name.replace("_", " ").replaceFirstChar { it.uppercase() },
    val type: AdminColumnType = AdminColumnType.Text,
    val editable: Boolean = true,
    val required: Boolean = false,
    val multiline: Boolean = false,
    val helper: String? = null,
    val reference: AdminReference? = null
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
        name = "outlets",
        title = "Outlets",
        description = "Branches, geo-fence settings, operating hours, and payment keys.",
        displayColumns = listOf("name", "city", "phone", "is_active", "created_at"),
        createLabel = "New outlet",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("name", required = true),
            AdminColumn("address", multiline = true),
            AdminColumn("city"),
            AdminColumn("phone"),
            AdminColumn("email"),
            AdminColumn("logo_url", multiline = true),
            AdminColumn("lat", type = AdminColumnType.DecimalNumber, required = true),
            AdminColumn("lng", type = AdminColumnType.DecimalNumber, required = true),
            AdminColumn("radius_km", type = AdminColumnType.DecimalNumber),
            AdminColumn("is_active", type = AdminColumnType.Boolean),
            AdminColumn("opens_at"),
            AdminColumn("closes_at"),
            AdminColumn("fcm_tokens", type = AdminColumnType.TextArray),
            AdminColumn("banner_url", multiline = true),
            AdminColumn("razor_pay_key")
        )
    ),
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
            AdminColumn("status", required = true, helper = "pending, accepted, preparing, ready, completed, rejected"),
            AdminColumn("outlet_id", type = AdminColumnType.LongNumber, reference = AdminReference("outlets", labelColumns = listOf("name", "city"))),
            AdminColumn("customer_id", type = AdminColumnType.LongNumber, reference = AdminReference("users", labelColumns = listOf("name", "phone"))),
            AdminColumn("order_type", helper = "delivery, pickup, dine_in"),
            AdminColumn("transaction_id")
        )
    ),
    AdminTable(
        name = "product_catalog",
        title = "Products",
        description = "Product catalog, diet flags, categories, prep time, and nutrition.",
        displayColumns = listOf("name", "category", "prep_time", "is_bestseller"),
        createLabel = "New product",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("name", required = true),
            AdminColumn("description", multiline = true),
            AdminColumn("review", type = AdminColumnType.Json, multiline = true, helper = "JSON array"),
            AdminColumn("category"),
            AdminColumn("prep_time"),
            AdminColumn("nutritional_info", type = AdminColumnType.Json, multiline = true),
            AdminColumn("is_veg", type = AdminColumnType.Boolean),
            AdminColumn("is_vegan", type = AdminColumnType.Boolean),
            AdminColumn("is_bestseller", type = AdminColumnType.Boolean)
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
        name = "cart",
        title = "Cart",
        description = "Customer cart rows by outlet and menu item.",
        displayColumns = listOf("customer_id", "outlet_id", "menu_item_id", "qty", "created_at"),
        createLabel = "New cart row",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("customer_id", type = AdminColumnType.LongNumber, required = true, reference = AdminReference("users", labelColumns = listOf("name", "phone"))),
            AdminColumn("outlet_id", type = AdminColumnType.LongNumber, required = true, reference = AdminReference("outlets", labelColumns = listOf("name", "city"))),
            AdminColumn("menu_item_id", type = AdminColumnType.LongNumber, required = true, reference = AdminReference("outlet_menu_items", labelColumns = listOf("product_name", "price"))),
            AdminColumn("qty", type = AdminColumnType.LongNumber, required = true)
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
            AdminColumn("outlet_id", type = AdminColumnType.LongNumber, reference = AdminReference("outlets", labelColumns = listOf("name", "city"))),
            AdminColumn("title"),
            AdminColumn("img_url", multiline = true),
            AdminColumn("display_order", type = AdminColumnType.LongNumber)
        )
    ),
    AdminTable(
        name = "offers",
        title = "Offers",
        description = "Offer cards and linked campaign destinations.",
        primaryKeyType = AdminColumnType.Uuid,
        displayColumns = listOf("title", "description", "linked_url", "created_at"),
        createLabel = "New offer",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.Uuid, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("outlet_id", type = AdminColumnType.LongNumber, reference = AdminReference("outlets", labelColumns = listOf("name", "city"))),
            AdminColumn("title"),
            AdminColumn("description", multiline = true),
            AdminColumn("img_url", multiline = true),
            AdminColumn("linked_url", multiline = true),
            AdminColumn("expires_at", type = AdminColumnType.Timestamp)
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
            AdminColumn("customer_id", type = AdminColumnType.LongNumber, reference = AdminReference("users", labelColumns = listOf("name", "phone"))),
            AdminColumn("review_type", helper = "order, product, outlet"),
            AdminColumn("order_id", type = AdminColumnType.Uuid, reference = AdminReference("orders", labelColumns = listOf("customer_name", "status"))),
            AdminColumn("menu_item_id", type = AdminColumnType.LongNumber, reference = AdminReference("outlet_menu_items", labelColumns = listOf("product_name", "price"))),
            AdminColumn("outlet_id", type = AdminColumnType.LongNumber, reference = AdminReference("outlets", labelColumns = listOf("name", "city"))),
            AdminColumn("title"),
            AdminColumn("description", multiline = true),
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
            AdminColumn("punch_lng", type = AdminColumnType.DecimalNumber),
            AdminColumn("outlet_id", type = AdminColumnType.LongNumber, reference = AdminReference("outlets", labelColumns = listOf("name", "city"))),
            AdminColumn("role", helper = "head, owner, chef, employee"),
            AdminColumn("auth_user_id", type = AdminColumnType.Uuid),
            AdminColumn("is_active", type = AdminColumnType.Boolean)
        )
    ),
    AdminTable(
        name = "attendance",
        title = "Attendance",
        description = "Employee in/out punch records and status.",
        displayColumns = listOf("employee_name", "status", "in_time", "out_time", "created_at"),
        createLabel = "New attendance",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("emp_id", type = AdminColumnType.LongNumber, reference = AdminReference("employee", labelColumns = listOf("name", "role", "contact"))),
            AdminColumn("status"),
            AdminColumn("in_time", type = AdminColumnType.Timestamp),
            AdminColumn("out_time", type = AdminColumnType.Timestamp),
            AdminColumn("in_lat", type = AdminColumnType.DecimalNumber),
            AdminColumn("in_lng", type = AdminColumnType.DecimalNumber),
            AdminColumn("out_lat", type = AdminColumnType.DecimalNumber),
            AdminColumn("out_lng", type = AdminColumnType.DecimalNumber)
        )
    ),
    AdminTable(
        name = "order_items",
        title = "Order Items",
        description = "Line items linked to orders and outlet-specific menu entries.",
        displayColumns = listOf("order_label", "product_name", "qty", "total_price", "created_at"),
        createLabel = "New order item",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("order_id", type = AdminColumnType.Uuid, required = true, reference = AdminReference("orders", labelColumns = listOf("customer_name", "phone", "status"))),
            AdminColumn("menu_item_id", type = AdminColumnType.LongNumber, required = true, reference = AdminReference("outlet_menu_items", labelColumns = listOf("product_name", "price"))),
            AdminColumn("qty", type = AdminColumnType.LongNumber, required = true),
            AdminColumn("price_per_item", type = AdminColumnType.DecimalNumber, required = true),
            AdminColumn("total_price", type = AdminColumnType.DecimalNumber, required = true),
            AdminColumn("total_discount", type = AdminColumnType.DecimalNumber)
        )
    ),
    AdminTable(
        name = "outlet_menu_items",
        title = "Outlet Menu",
        description = "Branch-specific product pricing, stock state, and availability.",
        displayColumns = listOf("product_name", "product_category", "price", "is_available", "is_out_of_stock"),
        createLabel = "New menu item",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("outlet_id", type = AdminColumnType.LongNumber, required = true, reference = AdminReference("outlets", labelColumns = listOf("name", "city"))),
            AdminColumn("product_id", type = AdminColumnType.LongNumber, required = true, reference = AdminReference("product_catalog", labelColumns = listOf("name", "category"))),
            AdminColumn("image", type = AdminColumnType.TextArray, multiline = true),
            AdminColumn("price", type = AdminColumnType.DecimalNumber, required = true),
            AdminColumn("discount", type = AdminColumnType.DecimalNumber),
            AdminColumn("is_available", type = AdminColumnType.Boolean),
            AdminColumn("is_out_of_stock", type = AdminColumnType.Boolean),
            AdminColumn("rating", type = AdminColumnType.DecimalNumber),
            AdminColumn("reviews_count", type = AdminColumnType.LongNumber)
        )
    ),
    AdminTable(
        name = "payments",
        title = "Payments",
        description = "Razorpay settlement records and payment status tracking.",
        primaryKeyType = AdminColumnType.LongNumber,
        displayColumns = listOf("order_customer", "amount", "payment_status", "payment_method", "created_at"),
        createLabel = "New payment",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("updated_at", type = AdminColumnType.Timestamp),
            AdminColumn("order_id", type = AdminColumnType.Uuid, reference = AdminReference("orders", labelColumns = listOf("customer_name", "status"))),
            AdminColumn("customer_id", type = AdminColumnType.LongNumber, reference = AdminReference("users", labelColumns = listOf("name", "phone"))),
            AdminColumn("razorpay_order_id"),
            AdminColumn("razorpay_payment_id"),
            AdminColumn("razorpay_signature", multiline = true),
            AdminColumn("amount", type = AdminColumnType.DecimalNumber),
            AdminColumn("amount_due", type = AdminColumnType.DecimalNumber),
            AdminColumn("amount_refunded", type = AdminColumnType.DecimalNumber),
            AdminColumn("currency"),
            AdminColumn("payment_status"),
            AdminColumn("payment_method"),
            AdminColumn("razorpay_response", type = AdminColumnType.Json, multiline = true),
            AdminColumn("refund_id"),
            AdminColumn("refund_reason", multiline = true),
            AdminColumn("refunded_at", type = AdminColumnType.Timestamp),
            AdminColumn("error_code"),
            AdminColumn("error_description", multiline = true)
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
    ),
    AdminTable(
        name = "categories",
        title = "Categories",
        description = "Product categories for the catalog.",
        displayColumns = listOf("emoji", "name", "is_active", "created_at"),
        createLabel = "New category",
        columns = listOf(
            AdminColumn("id", type = AdminColumnType.LongNumber, editable = false),
            AdminColumn("created_at", type = AdminColumnType.Timestamp, editable = false),
            AdminColumn("name", required = true),
            AdminColumn("emoji"),
            AdminColumn("is_active", type = AdminColumnType.Boolean)
        )
    )
)
