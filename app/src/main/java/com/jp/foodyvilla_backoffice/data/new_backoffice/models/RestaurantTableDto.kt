package com.jp.foodyvilla_backoffice.data.new_backoffice.models

import kotlinx.serialization.Serializable

// ---------- Supabase DTOs (mirror table columns 1:1) ----------

@Serializable
data class RestaurantTableDto(
    val id: Long = 0,
    val outlet_id: Long = 0,
    val table_number: String = "",
    val capacity: Int = 4,
    val status: String = "available" // available | occupied | reserved
)

@Serializable
data class OrderDto(
    val id: String = "",
    val outlet_id: Long = 0,
    val table_id: Long? = null,
    val customer_name: String? = null,
    val phone: String? = null,
    val status: String = "pending", // pending | preparing | served | paid | cancelled
    val order_type: String? = "dine_in"
)

@Serializable
data class OrderItemInsertDto(
    val order_id: String,
    val menu_item_id: Long,
    val qty: Long,
    val price_per_item: Double,
    val total_price: Double,
    val total_discount: Float = 0f
)

@Serializable
data class ProductCatalogDto(
    val id: Long = 0,
    val name: String = "",
    val category_id: Long? = null
)

@Serializable
data class CategoryDto(
    val id: Long = 0,
    val name: String = "",
    val emoji: String = "",
    val is_active: Boolean = true
)

// outlet_menu_items row with its parent product embedded via FK join
@Serializable
data class OutletMenuItemWithProductDto(
    val id: Long = 0,
    val outlet_id: Long = 0,
    val product_id: Long = 0,
    val price: Double = 0.0,
    val discount: Long = 0,
    val is_available: Boolean = true,
    val is_out_of_stock: Boolean = false,
    val product_catalog: ProductCatalogDto? = null
)

// nested join used only to pull the product name onto a bill line
@Serializable
data class OutletMenuItemJoinDto(
    val id: Long = 0,
    val product_catalog: ProductCatalogDto? = null
)

@Serializable
data class OrderItemWithMenuDto(
    val id: Long = 0,
    val order_id: String = "",
    val menu_item_id: Long = 0,
    val qty: Long = 1,
    val price_per_item: Double = 0.0,
    val total_price: Double = 0.0,
    val total_discount: Float = 0f,
    val kot_printed: Boolean = false,
    val outlet_menu_items: OutletMenuItemJoinDto? = null
)

// ---------- UI models ----------

data class TableUiModel(
    val id: Long,
    val tableNumber: String,
    val capacity: Int,
    val status: String
)

data class MenuItemUiModel(
    val menuItemId: Long,
    val productId: Long,
    val name: String,
    val categoryId: Long?,
    val price: Double,
    val discount: Long,
    val isAvailable: Boolean
)

// a line the waiter has added but not yet saved to the order
data class CartLineUiModel(
    val menuItemId: Long,
    val name: String,
    val price: Double,
    val qty: Long
)

// a line that already exists on the order in the DB
data class BillLineUiModel(
    val orderItemId: Long,
    val menuItemId: Long,
    val name: String,
    val qty: Long,
    val pricePerItem: Double,
    val totalPrice: Double,
    val kotPrinted: Boolean
)

// hook for whatever thermal-printer SDK you wire up later (Bluetooth/USB ESC-POS etc.)
// suspend because real printing does blocking socket I/O — implementations must hop to Dispatchers.IO themselves
interface ThermalPrinterBridge {
    suspend fun printKot(context: android.content.Context, tableNumber: String, lines: List<BillLineUiModel>)
    suspend fun printInvoice(context: android.content.Context, tableNumber: String, lines: List<BillLineUiModel>, grandTotal: Double)
}