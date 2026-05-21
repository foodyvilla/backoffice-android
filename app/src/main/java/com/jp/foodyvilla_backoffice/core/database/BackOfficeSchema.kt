package com.jp.foodyvilla_backoffice.core.database

object BackOfficeSchema {
    object Tables {
        const val Attendance = "attendance"
        const val Banners = "banners"
        const val Employee = "employee"
        const val Offers = "offers"
        const val OrderItems = "order_items"
        const val Orders = "orders"
        const val OutletMenuItems = "outlet_menu_items"
        const val Outlets = "outlets"
        const val Payments = "payments"
        const val ProductCatalog = "product_catalog"
        const val Reviews = "reviews"
        const val Users = "users"
    }

    object Employee {
        const val Id = "id"
        const val OutletId = "outlet_id"
        const val Name = "name"
        const val Address = "address"
        const val Contact = "contact"
        const val AadharNo = "aadhar_no"
        const val Role = "role"
        const val AuthUserId = "auth_user_id"
        const val IsActive = "is_active"
    }

    object Attendance {
        const val Id = "id"
        const val EmpId = "emp_id"
        const val Status = "status"
        const val InTime = "in_time"
        const val OutTime = "out_time"
        const val InLat = "in_lat"
        const val InLng = "in_lng"
        const val OutLat = "out_lat"
        const val OutLng = "out_lng"
    }

    object Orders {
        const val Id = "id"
        const val CreatedAt = "created_at"
        const val OutletId = "outlet_id"
        const val CustomerId = "customer_id"
        const val CustomerName = "customer_name"
        const val Phone = "phone"
        const val Status = "status"
        const val OrderType = "order_type"
        const val Address = "address"
        const val DeliveryLat = "delivery_lat"
        const val DeliveryLong = "delivery_long"
        const val Instruction = "instruction"
        const val TransactionId = "transaction_id"
    }

    object OrderItems {
        const val Id = "id"
        const val OrderId = "order_id"
        const val MenuItemId = "menu_item_id"
        const val Qty = "qty"
        const val PricePerItem = "price_per_item"
        const val TotalPrice = "total_price"
        const val TotalDiscount = "total_discount"
    }
}

