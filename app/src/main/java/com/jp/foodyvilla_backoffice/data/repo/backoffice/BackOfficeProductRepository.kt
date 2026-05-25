package com.jp.foodyvilla_backoffice.data.repo.backoffice

import com.jp.foodyvilla_backoffice.data.model.backoffice.adminTables
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toDisplayText
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import kotlinx.serialization.json.JsonObject

class BackOfficeProductRepository(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {
    private val productTable = adminTables.first { it.name == "product_catalog" }

    suspend fun getProducts(): List<JsonObject> {
        val rows = supabase.from("product_catalog")
            .select {
                order("name", SupabaseOrder.ASCENDING)
            }
            .decodeList<JsonObject>()
        return rows
    }

    suspend fun getCategories(): List<JsonObject> {
        return supabase.from("categories")
            .select {
                order("name", SupabaseOrder.ASCENDING)
            }
            .decodeList<JsonObject>()
    }

    suspend fun getOutletMenu(): List<JsonObject> {
        val session = authRepository.currentSession.value ?: return emptyList()
        val rows = supabase.from("outlet_menu_items")
            .select(Columns.raw("*, product_catalog(*)")) {
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<JsonObject>()
        
        return if (session.isOwner()) rows 
        else rows.filter { it["outlet_id"].toDisplayText() == session.outletId.toString() }
    }
}
