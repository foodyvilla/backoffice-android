package com.jp.foodyvilla_backoffice.data.new_backoffice.repo
import android.util.Log

import com.jp.foodyvilla_backoffice.data.new_backoffice.models.NewBannerUiModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import  com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import io.github.jan.supabase.functions.functions
import io.ktor.utils.io.InternalAPI

class MarketingRepository(private val supabase: SupabaseClient) {

    private companion object {
        const val TAG = "MarketingRepository"
    }

    suspend fun getBanners(outletId: Long?): List<NewBannerUiModel> {
        return try {
            supabase.from("banners").select {
                if (outletId != null) {
                    filter { eq("outlet_id", outletId) }
                }
                order(column = "display_order", order = Order.ASCENDING)
            }.decodeList<BannerResponse>().map { it.toUiModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching banners for outletId=$outletId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getActiveOffers(outletId: Long?): List<NewOfferUiModel> {
        return try {
            supabase.from("offers").select {
                if (outletId != null) {
                    filter { eq("outlet_id", outletId) }
                }
                order(column = "created_at", order = Order.DESCENDING)
            }.decodeList<OfferResponse>().map { it.toUiModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active offers for outletId=$outletId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchOutletsDropdown(): List<OutletDropdownUiModel> {
        return try {
            supabase.from("outlets").select {
                filter { eq("is_active", true) }
            }.decodeList<OutletResponse>().map { OutletDropdownUiModel(id = it.id, name = it.name) }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching outlets dropdown listings: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun uploadMarketingImage(bucketName: String, fileBytes: ByteArray, fileName: String): String {
        val bucket = supabase.storage.from(bucketName)
        val path = "uploads/$fileName"
        bucket.upload(path, fileBytes) { upsert = true }
        return bucket.publicUrl(path)
    }

    suspend fun upsertBanner(id: Long?, outletId: Long?, title: String?, imageUrl: String, displayOrder: Int) {
        try {
            if (id == null) {
                // 1. FOR INSERT OPERATIONS: Create a payload completely omitting the id field context
                val insertPayload = buildJsonObject {
                    put("outlet_id", outletId)
                    put("title", title)
                    put("img_url", imageUrl)
                    put("display_order", displayOrder)
                }
                // Explicit insert removes 'id' from the generated columns query string parameter
                supabase.from("banners").insert(insertPayload)
            } else {
                // 2. FOR UPDATE OPERATIONS: Exclude 'id' from the payload. 
                // Identity columns defined as GENERATED ALWAYS cannot be updated.
                val updatePayload = buildJsonObject {
                    put("outlet_id", outletId)
                    put("title", title)
                    put("img_url", imageUrl)
                    put("display_order", displayOrder)
                }
                supabase.from("banners").update(updatePayload) {
                    filter { eq("id", id) }
                }
                invokeFcmNotificationEdgeFunction(topic = "banners", title = title ?: "New Offer", desc =  "Taste the difference and enjoy your meal", url = imageUrl ?: "")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing banner upsert execution: ${e.message}", e)
            throw e
        }
    }

    suspend fun upsertOffer(
        id: String?, outletId: Long?, title: String?, description: String?,
        imageUrl: String?, linkedUrl: String?, expiresAt: String?
    ) {
        try {
            val payload = buildJsonObject {
                put("id", id ?: UUID.randomUUID().toString())
                put("outlet_id", outletId)
                put("title", title)
                put("description", description)
                put("img_url", imageUrl)
                put("linked_url", linkedUrl)
                put("expires_at", expiresAt)
            }
            supabase.from("offers").upsert(payload)
            invokeFcmNotificationEdgeFunction(topic = "offers", title = title ?: "New Offer", desc = description ?: "Taste the difference and enjoy your meal", url = imageUrl ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error performing offer upsert execution: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteBanner(id: Long) {
        try {
            supabase.from("banners").delete { filter { eq("id", id) } }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting banner configuration with id=$id: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteOffer(id: String) {
        try {
            supabase.from("offers").delete { filter { eq("id", id) } }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting promotional offer with id=$id: ${e.message}", e)
            throw e
        }
    }


    @OptIn(InternalAPI::class)
    suspend fun invokeFcmNotificationEdgeFunction(topic: String, title: String, desc: String, url: String) {
        try {
            supabase.functions.invoke(function = "send_fcm_to_token") {
                this.body = buildJsonObject {
                    put("topic", topic)
                    put("title", title)
                    put("body", desc)
                    put("imageUrl", url)
                }.toString()
            }
        } catch (e: Exception) {
            println("Sent failure $e")
        }
    }
}
