package com.jp.foodyvilla_backoffice.data.repo.backoffice

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import kotlinx.serialization.json.JsonObject

class MarketingBackOfficeRepository(private val supabase: SupabaseClient) {
    suspend fun getBanners(): List<JsonObject> {
        return supabase.from("banners").select {
            order("created_at", SupabaseOrder.DESCENDING)
        }.decodeList<JsonObject>()
    }

    suspend fun getOffers(): List<JsonObject> {
        return supabase.from("offers").select {
            order("created_at", SupabaseOrder.DESCENDING)
        }.decodeList<JsonObject>()
    }
}
