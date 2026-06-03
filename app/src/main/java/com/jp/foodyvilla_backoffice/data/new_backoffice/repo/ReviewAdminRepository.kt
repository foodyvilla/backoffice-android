package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put

class ReviewAdminRepository(private val supabase: SupabaseClient) {

    suspend fun fetchAllUsersMinimal(): List<UserResponse> =
        supabase.from("users").select().decodeList()

    suspend fun fetchRawReviewsAdmin(): List<ReviewAdminResponse> =
        supabase.from("reviews").select {
            order("created_at", order = Order.DESCENDING)
        }.decodeList()

    suspend fun createReviewRecord(model: ReviewAdminUiModel) {
        supabase.from("reviews").insert(buildJsonObject {
            put("customer_id", model.customerId)
            put("review_type", model.reviewType.name.lowercase())
            if (model.orderId.isNotBlank()) put("order_id", model.orderId.trim())
            put("menu_item_id", model.menuItemId.toLongOrNull())
            put("outlet_id", model.outletId.toLongOrNull())
            put("rating", model.rating)
            put("title", model.title.trim())
            put("description", model.description.trim())
            put("img_url", buildJsonArray {
                model.images.forEach { add(it.trim()) }
            })
        })
    }

    suspend fun updateReviewRecord(model: ReviewAdminUiModel) {
        supabase.from("reviews").update(buildJsonObject {
            put("customer_id", model.customerId)
            put("review_type", model.reviewType.name.lowercase())
            put("order_id", model.orderId.trim().ifBlank { null })
            put("menu_item_id", model.menuItemId.toLongOrNull())
            put("outlet_id", model.outletId.toLongOrNull())
            put("rating", model.rating)
            put("title", model.title.trim())
            put("description", model.description.trim())
            put("img_url", buildJsonArray {
                model.images.forEach { add(it.trim()) }
            })
        }) { filter { eq("id", model.id) } }
    }

    suspend fun purgeReviewLogRecord(id: Long) {
        supabase.from("reviews").delete { filter { eq("id", id) } }
    }
}