package com.jp.foodyvilla_backoffice.data.new_backoffice.models

import kotlinx.serialization.Serializable

// --- OUTLETS MANAGEMENT DATA BOUNDS ---
@Serializable
data class OutletUiModel(
    val id: Long = 0L,
    val name: String,
    val address: String,
    val city: String,
    val phone: String,
    val email: String,
    val logoUrl: String? = null,
    val bannerUrl: String? = null,
    val lat: Double = 21.1983,
    val lng: Double = 81.9614,
    val radiusKm: Double = 5.0,
    val attendanceRadius : Int = 50,
    val isActive: Boolean = true,
    val opensAt: String? = null,  // Format: "HH:MM:SS"
    val closesAt: String? = null, // Format: "HH:MM:SS"
    val razorPayKey: String = "rzp_test_ShBw7mlCM6gT6y"
)

@Serializable
data class OutletResponse(
    val id: Long = 0L,
    val name: String,
    val address: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val logo_url: String? = null,
    val banner_url: String? = null,
    val lat: Double = 21.1983,
    val lng: Double = 81.9614,
    val radius_km: Double = 5.0,
    val is_active: Boolean = true,
    val attendance_radius_meters : Int = 50,
    val opens_at: String? = null,
    val closes_at: String? = null,
    val razor_pay_key: String? = null
)

fun OutletResponse.toUiModel() = OutletUiModel(
    id = id, name = name, address = address.orEmpty(), city = city.orEmpty(),
    phone = phone.orEmpty(), email = email.orEmpty(), logoUrl = logo_url,
    bannerUrl = banner_url, lat = lat, lng = lng, radiusKm = radius_km,
    isActive = is_active, opensAt = opens_at, closesAt = closes_at,
    attendanceRadius = attendance_radius_meters,
    razorPayKey = razor_pay_key ?: "rzp_test_ShBw7mlCM6gT6y"
)

// --- OUTLET MENU JUNCTION TIERS DATA BOUNDS ---
@Serializable
data class OutletMenuItemUiModel(
    val id: Long = 0L,
    val outletId: Long,
    val productId: Long,
    val imagesList: List<String> = emptyList(), // Maps back public image paths
    val price: Double,
    val discount: Long = 0L,
    val isAvailable: Boolean = true,
    val isOutOfStock: Boolean = false,
    val handlingCharges: Double = 0.0,
    val deliveryCharges: Double = 0.0,
    val isFreeDelivery: Boolean = false,
    val productName: String = "",
    val productCategoryName: String = "",
    val isProductVeg: Boolean = true
)

@Serializable
data class OutletMenuItemResponse(
    val id: Long = 0L,
    val outlet_id: Long,
    val product_id: Long,
    val image: List<String>? = emptyList(), // Maps text[] array column structure
    val price: Double = 0.0,
    val discount: Long = 0L,
    val is_available: Boolean = true,
    val is_out_of_stock: Boolean = false,
    val handling_charges: Double? = 0.0,
    val delivery_charges: Double? = 0.0,
    val is_free_delivery: Boolean? = false,
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
    imagesList = image.orEmpty(), price = price, discount = discount,
    isAvailable = is_available, isOutOfStock = is_out_of_stock,
    handlingCharges = handling_charges ?: 0.0, deliveryCharges = delivery_charges ?: 0.0,
    isFreeDelivery = is_free_delivery ?: false,
    productName = product_catalog?.name ?: "Unknown Product",
    productCategoryName = product_catalog?.categories?.name ?: "Unassigned",
    isProductVeg = product_catalog?.is_veg ?: true
)