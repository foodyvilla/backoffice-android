package com.jp.foodyvilla_backoffice.data.new_backoffice.models

import kotlinx.serialization.Serializable

// --- OUTLET SCHEMA DATA BOUNDS ---
@Serializable
data class OutletUiModel(
    val id: Long = 0L,
    val name: String,
    val address: String,
    val city: String,
    val phone: String,
    val email: String,
    val radiusKm: Double = 5.0,
    val isActive: Boolean = true
)

@Serializable
data class OutletResponse(
    val id: Long = 0L,
    val name: String,
    val address: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val radius_km: Double = 5.0,
    val is_active: Boolean = true
)

fun OutletResponse.toUiModel() = OutletUiModel(
    id = id, name = name, address = address.orEmpty(),
    city = city.orEmpty(), phone = phone.orEmpty(), email = email.orEmpty(),
    radiusKm = radius_km, isActive = is_active
)

// --- OUTLET MENU ITEM DATA BOUNDS ---
@Serializable
data class OutletMenuItemUiModel(
    val id: Long = 0L,
    val outletId: Long,
    val productId: Long,
    val price: Double,
    val discount: Long = 0L,
    val isAvailable: Boolean = true,
    val isOutOfStock: Boolean = false,
    val productName: String = "",         // Join Helper
    val productCategoryName: String = "", // Join Helper
    val isProductVeg: Boolean = true      // Join Helper
)

@Serializable
data class OutletMenuItemResponse(
    val id: Long = 0L,
    val outlet_id: Long,
    val product_id: Long,
    val price: Double = 0.0,
    val discount: Long = 0L,
    val is_available: Boolean = true,
    val is_out_of_stock: Boolean = false,
    val product_catalog: InnerProductCatalogJoin? = null
)

@Serializable
data class InnerProductCatalogJoin(
    val name: String,
    val is_veg: Boolean = true,
    val categories: InnerCategoryJoin? = null
)

@Serializable
data class InnerCategoryJoin(val name: String)

fun OutletMenuItemResponse.toUiModel() = OutletMenuItemUiModel(
    id = id, outletId = outlet_id, productId = product_id,
    price = price, discount = discount, isAvailable = is_available,
    isOutOfStock = is_out_of_stock, productName = product_catalog?.name ?: "Unknown Product",
    productCategoryName = product_catalog?.categories?.name ?: "Unassigned",
    isProductVeg = product_catalog?.is_veg ?: true
)