package com.jp.foodyvilla_backoffice.data.repo.backoffice

import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toDisplayText
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import kotlinx.serialization.json.JsonObject

class BackOfficeFinanceRepository(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {
    suspend fun getPayments(): List<JsonObject> {
        val session = authRepository.currentSession.value ?: return emptyList()
        val rows = supabase.from("payments")
            .select(Columns.raw("*, orders(customer_name, phone, status, outlet_id), users(name, phone)")) {
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<JsonObject>()
        
        return if (session.isOwner()) rows 
        else {
            rows.filter { 
                it["orders"]?.let { o -> (o as? JsonObject)?.get("outlet_id").toDisplayText() == session.outletId.toString() } ?: false 
            }
        }
    }

    suspend fun getCartItems(): List<JsonObject> {
        val session = authRepository.currentSession.value ?: return emptyList()
        val rows = supabase.from("cart")
            .select(Columns.raw("*, users(name, phone), outlet_menu_items(id, price, product_catalog(name, category)), outlets(name, city)")) {
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<JsonObject>()
        
        return if (session.isOwner()) rows 
        else rows.filter { it["outlet_id"].toDisplayText() == session.outletId.toString() }
    }

    private fun com.jp.foodyvilla_backoffice.domain.security.UserSession.isOwner(): Boolean {
        return roleOrNull() == com.jp.foodyvilla_backoffice.domain.security.OutletRole.OWNER
    }

    private fun com.jp.foodyvilla_backoffice.domain.security.UserSession.roleOrNull(): com.jp.foodyvilla_backoffice.domain.security.OutletRole? {
        return when (this) {
            is com.jp.foodyvilla_backoffice.domain.security.UserSession.OutletSession -> role
            is com.jp.foodyvilla_backoffice.domain.security.UserSession.EmployeeSession -> role
        }
    }
}
