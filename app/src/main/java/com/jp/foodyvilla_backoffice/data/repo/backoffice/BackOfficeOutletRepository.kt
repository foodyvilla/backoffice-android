package com.jp.foodyvilla_backoffice.data.repo.backoffice

import com.jp.foodyvilla_backoffice.data.model.backoffice.adminTables
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import kotlinx.serialization.json.JsonObject

class BackOfficeOutletRepository(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {
    suspend fun getOutlets(): List<JsonObject> {
        return supabase.from("outlets")
            .select {
                order("name", SupabaseOrder.ASCENDING)
            }
            .decodeList<JsonObject>()
    }
}
