package com.jp.foodyvilla_backoffice.data.new_backoffice.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- Supabase DTOs (mirror table columns 1:1) ----------

@Serializable
data class RestaurantTableDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("outlet_id") val outlet_id: Long = 0,
    @SerialName("table_number") val table_number: String = "",
    @SerialName("capacity") val capacity: Int = 4,
    @SerialName("status") val status: String = "available" // available | occupied | reserved
)

@Serializable
data class OrderDto(
    @SerialName("id") val id: String = "",
    @SerialName("outlet_id") val outlet_id: Long = 0,
    @SerialName("table_id") val table_id: Long? = null,
    @SerialName("customer_name") val customer_name: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("status") val status: String = "pending", // pending | preparing | served | paid | cancelled | completed
    @SerialName("order_type") val order_type: String? = "dine_in"
)

@Serializable
data class OrderItemInsertDto(
    @SerialName("order_id") val order_id: String,
    @SerialName("menu_item_id") val menu_item_id: Long,
    @SerialName("qty") val qty: Long,
    @SerialName("price_per_item") val price_per_item: Double,
    @SerialName("total_price") val total_price: Double,
    @SerialName("total_discount") val total_discount: Float = 0f
)

@Serializable
data class ProductCatalogDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("name") val name: String = "",
    @SerialName("category_id") val category_id: Long? = null
)

@Serializable
data class CategoryDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("name") val name: String = "",
    @SerialName("emoji") val emoji: String = "",
    @SerialName("is_active") val is_active: Boolean = true
)

// outlet_menu_items row with its parent product embedded via FK join
@Serializable
data class OutletMenuItemWithProductDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("outlet_id") val outlet_id: Long = 0,
    @SerialName("product_id") val product_id: Long = 0,
    @SerialName("price") val price: Double = 0.0,
    @SerialName("discount") val discount: Long = 0,
    @SerialName("is_available") val is_available: Boolean = true,
    @SerialName("is_out_of_stock") val is_out_of_stock: Boolean = false,
    @SerialName("product_catalog") val product_catalog: ProductCatalogDto? = null
)

// nested join used only to pull the product name onto a bill line
@Serializable
data class OutletMenuItemJoinDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("product_catalog") val product_catalog: ProductCatalogDto? = null
)

@Serializable
data class OrderItemWithMenuDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("order_id") val order_id: String = "",
    @SerialName("menu_item_id") val menu_item_id: Long = 0,
    @SerialName("qty") val qty: Long = 1,
    @SerialName("price_per_item") val price_per_item: Double = 0.0,
    @SerialName("total_price") val total_price: Double = 0.0,
    @SerialName("total_discount") val total_discount: Float = 0f,
    @SerialName("kot_printed") val kot_printed: Boolean = false,
    @SerialName("outlet_menu_items") val outlet_menu_items: OutletMenuItemJoinDto? = null
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
