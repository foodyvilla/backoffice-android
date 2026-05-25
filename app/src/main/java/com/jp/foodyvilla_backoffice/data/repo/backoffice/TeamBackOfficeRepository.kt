package com.jp.foodyvilla_backoffice.data.repo.backoffice

import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toDisplayText
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import kotlinx.serialization.json.JsonObject

class TeamBackOfficeRepository(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {
    suspend fun getEmployees(): List<JsonObject> {
        val session = authRepository.currentSession.value ?: return emptyList()
        val rows = supabase.from("employee").select {
            order("name", SupabaseOrder.ASCENDING)
        }.decodeList<JsonObject>()
        
        return if (session.isOwner()) rows 
        else rows.filter { it["outlet_id"].toDisplayText() == session.outletId.toString() }
    }

    suspend fun getAttendance(): List<JsonObject> {
        val session = authRepository.currentSession.value ?: return emptyList()
        val rows = supabase.from("attendance")
            .select(Columns.raw("*, employee(name, role, contact, outlet_id)")) {
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<JsonObject>()
        
        return if (session.isOwner()) rows 
        else rows.filter { 
            it["outlet_id"].toDisplayText() == session.outletId.toString() || 
            (it["employee"] as? JsonObject)?.get("outlet_id").toDisplayText() == session.outletId.toString() 
        }
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
