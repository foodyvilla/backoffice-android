package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import android.content.Context
import android.net.Uri
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import com.jp.foodyvilla_backoffice.data.utils.compressImage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.util.UUID

class OutletManagementRepository(private val supabase: SupabaseClient) {

    // =====================================================================
    // CORE IMAGE UPLOAD PLUGINS (SUPABASE STORAGE)
    // =====================================================================

    // ✅ ADDED: Single Image Upload function following your exact compression pattern
    suspend fun uploadSingleImage(
        context: Context,
        uri: Uri,
        bucketName: String,     // Pass "outlet-logos" or "menu-items"
        pathPrefix: String      // Pass "logos" or "banners"
    ): String {
        val bucketRef = supabase.storage.from(bucketName)

        val compressed = compressImage(context, uri)
        val fileName = "${UUID.randomUUID()}.jpg"
        val fullPath = "$pathPrefix/$fileName"

        bucketRef.upload(
            path = fullPath,
            data = compressed
        )

        return bucketRef.publicUrl(fullPath)
    }

    suspend fun uploadMultipleImages(
        context: Context,
        uris: List<Uri>,
        bucketName: String,     // Pass "outlet-logos" or "menu-items"
        pathPrefix: String      // Pass "outlets" or "menu"
    ): List<String> {
        val bucketRef = supabase.storage.from(bucketName)

        return uris.map { uri ->
            val compressed = compressImage(context, uri)
            val fileName = "${UUID.randomUUID()}.jpg"
            val fullPath = "$pathPrefix/$fileName"

            bucketRef.upload(
                path = fullPath,
                data = compressed
            )

            bucketRef.publicUrl(fullPath)
        }
    }

    suspend fun uploadImages(
        context: Context,
        uris: List<Uri>
    ): List<String> {
        return uris.map { uri ->
            val compressed = compressImage(context, uri)
            val fileName = "${UUID.randomUUID()}.jpg"
            val path = "reviews/$fileName"

            supabase.storage.from("review").upload(
                path = path,
                data = compressed
            )

            supabase.storage.from("review").publicUrl(path)
        }
    }

    // =====================================================================
    // OUTLET BASE DATA WRITES (POSTGREST)
    // =====================================================================
    suspend fun fetchAllOutlets(): List<OutletUiModel> =
        supabase.from("outlets").select { order("name", order = Order.ASCENDING) }
            .decodeList<OutletResponse>().map { it.toUiModel() }

    suspend fun fetchOutletById(id: Long): OutletUiModel =
        supabase.from("outlets").select { filter { eq("id", id) } }
            .decodeSingle<OutletResponse>().toUiModel()

    suspend fun insertNewOutlet(outlet: OutletUiModel) {
        supabase.from("outlets").insert(buildJsonObject {
            put("name", outlet.name.trim())
            put("address", outlet.address.trim())
            put("city", outlet.city.trim())
            put("phone", outlet.phone.trim())
            put("email", outlet.email.trim())
            put("lat", outlet.lat)
            put("lng", outlet.lng)
            put("radius_km", outlet.radiusKm)
            put("is_active", outlet.isActive)
            put("attendance_radius_meters", outlet.attendanceRadius)
            if (!outlet.logoUrl.isNullOrBlank()) put("logo_url", outlet.logoUrl)
            if (!outlet.bannerUrl.isNullOrBlank()) put("banner_url", outlet.bannerUrl)
            if (!outlet.opensAt.isNullOrBlank()) put("opens_at", outlet.opensAt)
            if (!outlet.closesAt.isNullOrBlank()) put("closes_at", outlet.closesAt)
            put("razor_pay_key", outlet.razorPayKey.trim())
        })
    }

    suspend fun updateOutletRow(outlet: OutletUiModel) {
        supabase.from("outlets").update(buildJsonObject {
            put("name", outlet.name.trim())
            put("address", outlet.address.trim())
            put("city", outlet.city.trim())
            put("phone", outlet.phone.trim())
            put("email", outlet.email.trim())
            put("lat", outlet.lat)
            put("lng", outlet.lng)
            put("radius_km", outlet.radiusKm)
            put("is_active", outlet.isActive)
            put("attendance_radius_meters", outlet.attendanceRadius)
            put("logo_url", outlet.logoUrl)
            put("banner_url", outlet.bannerUrl)
            put("opens_at", outlet.opensAt)
            put("closes_at", outlet.closesAt)
            put("razor_pay_key", outlet.razorPayKey.trim())
        }) { filter { eq("id", outlet.id) } }
    }

    // =====================================================================
    // OUTLET MENU JUNCTION TIERS DATA WRITES (POSTGREST)
    // =====================================================================
    suspend fun fetchMenuForOutlet(outletId: Long): List<OutletMenuItemUiModel> =
        supabase.from("outlet_menu_items")
            .select(Columns.raw("*, product_catalog(*, categories(name))")) {
                filter { eq("outlet_id", outletId) }
            }.decodeList<OutletMenuItemResponse>().map { it.toUiModel() }

    suspend fun fetchMenuItemById(id: Long): OutletMenuItemUiModel =
        supabase.from("outlet_menu_items")
            .select(Columns.raw("*, product_catalog(*, categories(name))")) {
                filter { eq("id", id) }
            }.decodeSingle<OutletMenuItemResponse>().toUiModel()

    suspend fun addProductToOutletMenu(item: OutletMenuItemUiModel) {
        supabase.from("outlet_menu_items").insert(buildJsonObject {
            put("outlet_id", item.outletId)
            put("product_id", item.productId)
            put("price", item.price)
            put("discount", item.discount)
            put("is_available", item.isAvailable)
            put("is_out_of_stock", item.isOutOfStock)
            put("handling_charges", item.handlingCharges)
            put("delivery_charges", item.deliveryCharges)
            put("is_free_delivery", item.isFreeDelivery)
            putJsonArray("image") { item.imagesList.forEach { add(it) } }
        })
    }

    suspend fun updateOutletMenuRow(item: OutletMenuItemUiModel) {
        supabase.from("outlet_menu_items").update(buildJsonObject {
            put("price", item.price)
            put("discount", item.discount)
            put("is_available", item.isAvailable)
            put("is_out_of_stock", item.isOutOfStock)
            put("handling_charges", item.handlingCharges)
            put("delivery_charges", item.deliveryCharges)
            put("is_free_delivery", item.isFreeDelivery)
            putJsonArray("image") { item.imagesList.forEach { add(it) } }
        }) { filter { eq("id", item.id) } }
    }

    suspend fun removeProductFromMenu(id: Long) {
        supabase.from("outlet_menu_items").delete { filter { eq("id", id) } }
    }
}