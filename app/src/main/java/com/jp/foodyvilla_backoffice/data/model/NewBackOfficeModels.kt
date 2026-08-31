package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NewDetailedOrderUiModel(
    val id: String,
    val outletId: Long,
    val customerId: Long?,
    val customerName: String,
    val phone: String,
    val status: String,
    val orderType: String,
    val address: String,
    val instruction: String,
    val createdAt: String,
    val acceptedBy: Long?,
    val tableId: Long? = null,
    val tableNumber: String? = null,
    val items: List<NewSelectedMenuItem> = emptyList(),
    val totalAmount: Double = 0.0
)

@Serializable
data class NewSelectedMenuItem(
    val menuItemId: Long,
    val name: String,
    val qty: Int,
    val price: Double,
    val totalPrice: Double,
    val image: String?
)

@Serializable
data class NewOutletMenuUiModel(
    val id: Long,
    val outletId: Long,
    val productId: Long,
    val name: String,
    val description: String,
    val category: String,
    val image: String?,
    val price: Double,
    val discount: Long,
    val finalPrice: Double,
    val isVeg: Boolean,
    val isAvailable: Boolean,
    val isOutOfStock: Boolean
)

@Serializable
data class NewCategoryUiModel(
    val id: Long,
    val name: String,
    val emoji: String
)

@Serializable
data class NewCustomerUiModel(
    val id: Long,
    val name: String,
    val phone: String,
    val address: String?,
    val lat: Double?,
    val lng: Double?,
    val fcmToken: String?
)

@Serializable
data class OutletDropdownUiModel(
    val id: Long,
    val name: String
)

enum class NewOrderType {
    DELIVERY, PICKUP, DINE_IN
}

// ==========================================
// Network Serialization DTO Packages
// ==========================================

@Serializable
data class OrderListResponse(
    val id: String? = null,
    val outlet_id: Long? = null,
    val customer_id: Long? = null,
    val customer_name: String? = null,
    val phone: String? = null,
    val status: String? = null,
    val order_type: String? = null,
    val address: String? = null,
    val instruction: String? = null,
    val created_at: String? = null,
    val accepted_by: Long? = null,
    val table_id: Long? = null,
    val restaurant_tables: TableNumberContainer? = null,
    val order_items: List<OrderItemDetailResponse>? = emptyList()
)

@Serializable
data class TableNumberContainer(
    val table_number: String
)

@Serializable
data class OrderItemDetailResponse(
    val menu_item_id: Long? = null,
    val qty: Int? = null,
    val price_per_item: Double? = null,
    val total_price: Double? = null,
    val outlet_menu_items: CatalogNameContainer? = null
)

@Serializable
data class CatalogNameContainer(
    val product_catalog: CatalogNameSpec? = null
)

@Serializable
data class CatalogNameSpec(
    val name: String
)

@Serializable
data class OutletListResponse(
    val id: Long,
    val name: String
)

@Serializable
data class NewOutletMenuResponse(
    val id: Long? = null,
    val outlet_id: Long? = null,
    val product_id: Long? = null,
    val image: List<String>? = null,
    val price: Double? = null,
    val discount: Long? = null,
    val is_available: Boolean? = null,
    val is_out_of_stock: Boolean? = null,
    val product_catalog: NewProductCatalogResponse? = null
)

@Serializable
data class NewProductCatalogResponse(
    val id: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val is_veg: Boolean? = null
)

@Serializable
data class NewCategoryResponse(
    val id: Long,
    val name: String,
    val emoji: String
)

@Serializable
data class NewCustomerResponse(
    val id: Long,
    val name: String? = null,
    val phone: String,
    val address: String? = null,
    val lat: Double? = null,
    val long: Double? = null,
    val fcm_token: String? = null
)

@Serializable
data class EmployeeIdLookupResponse(
    val id: Long
)

// ==========================================
// Extension Structural Mapping Layer
// ==========================================

fun OrderListResponse.toUiModel(): NewDetailedOrderUiModel {
    val itemsList = (order_items ?: emptyList()).map { item ->
        NewSelectedMenuItem(
            menuItemId = item.menu_item_id ?: 0L,
            name = item.outlet_menu_items?.product_catalog?.name ?: "Item #${item.menu_item_id}",
            qty = item.qty ?: 0, price = item.price_per_item ?: 0.0, totalPrice = item.total_price ?: 0.0, image = null
        )
    }
    return NewDetailedOrderUiModel(
        id = id.orEmpty(), outletId = outlet_id ?: 0L, customerId = customer_id,
        customerName = customer_name ?: "Walk-in Customer", phone = phone.orEmpty(),
        status = status ?: "pending", orderType = (order_type ?: "dine_in").uppercase(), address = address.orEmpty(),
        instruction = instruction.orEmpty(), createdAt = created_at.orEmpty(), acceptedBy = accepted_by,
        tableId = table_id, tableNumber = restaurant_tables?.table_number,
        items = itemsList, totalAmount = itemsList.sumOf { it.totalPrice }
    )
}

fun NewOutletMenuResponse.toUiModel(): NewOutletMenuUiModel = NewOutletMenuUiModel(
    id = id ?: 0L, outletId = outlet_id ?: 0L, productId = product_id ?: 0L, 
    name = product_catalog?.name ?: "Unknown Item",
    description = product_catalog?.description.orEmpty(), category = product_catalog?.category.orEmpty(),
    image = image?.firstOrNull(), price = price ?: 0.0, discount = discount ?: 0L, 
    finalPrice = ((price ?: 0.0) - (discount ?: 0L)).coerceAtLeast(0.0),
    isVeg = product_catalog?.is_veg ?: true, isAvailable = is_available ?: true, isOutOfStock = is_out_of_stock ?: false
)

fun NewCustomerResponse.toUiModel(): NewCustomerUiModel = NewCustomerUiModel(
    id = id, name = name.orEmpty().ifBlank { "Walk-in Customer" }, phone = phone,
    address = address, lat = lat, lng = long, fcmToken = fcm_token
)




/**
 * Summary data class returned immediately after successfully recording an order transaction.
 * Used for dialog confirmations and type-safe cross-screen navigation parameters.
 */
@Serializable
data class NewOrderUiModel(
    val orderId: String,
    val customerName: String,
    val phone: String,
    val orderType: String,
    val items: List<NewSelectedMenuItem> = emptyList(),
    val totalAmount: Double
)