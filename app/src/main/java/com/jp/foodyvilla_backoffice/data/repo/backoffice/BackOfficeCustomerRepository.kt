package com.jp.foodyvilla_backoffice.data.repo.backoffice

import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toDisplayText
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import kotlinx.serialization.json.JsonObject

class BackOfficeCustomerRepository(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {
    suspend fun getCustomers(): List<JsonObject> {
        val session = authRepository.currentSession.value ?: return emptyList()
        val rows = supabase.from("users")
            .select {
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<JsonObject>()
        
        return if (session.isOwner()) rows 
        else {
            val customerIdsForOutlet = supabase.from("orders")
                .select { 
                    filter { eq("outlet_id", session.outletId) } 
                }
                .decodeList<JsonObject>()
                .mapNotNull { it["customer_id"].toDisplayText().takeIf { v -> v != "-" } }
                .toSet()
            rows.filter { it["id"].toDisplayText() in customerIdsForOutlet }
        }
    }

    suspend fun getCustomerOrders(customerId: String): List<JsonObject> {
        return supabase.from("orders")
            .select {
                filter { eq("customer_id", customerId) }
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<JsonObject>()
    }

    suspend fun getCustomerCart(customerId: String): List<JsonObject> {
        return supabase.from("cart")
            .select {
                filter { eq("customer_id", customerId) }
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<JsonObject>()
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
