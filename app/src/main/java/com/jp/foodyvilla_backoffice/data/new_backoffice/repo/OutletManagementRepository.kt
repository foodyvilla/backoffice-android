package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class OutletManagementRepository(private val supabase: SupabaseClient) {

    // --- OUTLET REST DATA WRITE OPS ---
    suspend fun fetchAllOutlets(): List<OutletUiModel> {
        return supabase.from("outlets").select { order("name", order = Order.ASCENDING) }
            .decodeList<OutletResponse>().map { it.toUiModel() }
    }

    suspend fun insertNewOutlet(outlet: OutletUiModel) {
        supabase.from("outlets").insert(buildJsonObject {
            put("name", outlet.name.trim())
            put("address", outlet.address.trim())
            put("city", outlet.city.trim())
            put("phone", outlet.phone.trim())
            put("email", outlet.email.trim())
            put("radius_km", outlet.radiusKm)
            put("is_active", outlet.isActive)
            put("lat", 21.1983) // Safe operational fallback coordinate points
            put("lng", 81.9614)
        })
    }

    suspend fun updateOutletRow(outlet: OutletUiModel) {
        supabase.from("outlets").update(buildJsonObject {
            put("name", outlet.name.trim())
            put("address", outlet.address.trim())
            put("city", outlet.city.trim())
            put("phone", outlet.phone.trim())
            put("email", outlet.email.trim())
            put("radius_km", outlet.radiusKm)
            put("is_active", outlet.isActive)
        }) { filter { eq("id", outlet.id) } }
    }

    // --- JUNCTION MENU ITEMS DATA WRITE OPS ---
    suspend fun fetchMenuForOutlet(outletId: Long): List<OutletMenuItemUiModel> {
        return supabase.from("outlet_menu_items")
            .select(Columns.raw("*, product_catalog(*, categories(name))")) {
                filter { eq("outlet_id", outletId) }
            }.decodeList<OutletMenuItemResponse>().map { it.toUiModel() }
    }

    suspend fun addProductToOutletMenu(item: OutletMenuItemUiModel) {
        supabase.from("outlet_menu_items").insert(buildJsonObject {
            put("outlet_id", item.outletId)
            put("product_id", item.productId)
            put("price", item.price)
            put("discount", item.discount)
            put("is_available", item.isAvailable)
            put("is_out_of_stock", item.isOutOfStock)
        })
    }

    suspend fun updateOutletMenuRow(item: OutletMenuItemUiModel) {
        supabase.from("outlet_menu_items").update(buildJsonObject {
            put("price", item.price)
            put("discount", item.discount)
            put("is_available", item.isAvailable)
            put("is_out_of_stock", item.isOutOfStock)
        }) { filter { eq("id", item.id) } }
    }

    suspend fun removeProductFromOutletMenu(id: Long) {
        supabase.from("outlet_menu_items").delete { filter { eq("id", id) } }
    }
}