package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import android.content.Context
import android.net.Uri
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import com.jp.foodyvilla_backoffice.data.utils.compressImage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.ktor.client.request.setBody
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

class CustomerManagementRepository(private val supabase: SupabaseClient) {

    suspend fun fetchAllCustomers(): List<CustomerUiModel> =
        supabase.from("users").select().decodeList<UserResponse>().map { it.toUiModel() }

    suspend fun insertCustomer(customer: CustomerUiModel) {
        supabase.from("users").insert(buildJsonObject {
            put("name", customer.name.trim())
            put("phone", customer.phone.trim())
            put("email", customer.email.trim())
            put("address", customer.address.trim())
            put("is_verified", customer.isVerified)
        })
    }

    suspend fun updateCustomer(customer: CustomerUiModel) {
        supabase.from("users").update(buildJsonObject {
            put("name", customer.name.trim())
            put("phone", customer.phone.trim())
            put("email", customer.email.trim())
            put("address", customer.address.trim())
            put("is_verified", customer.isVerified)
        }) { filter { eq("id", customer.id) } }
    }

    suspend fun deleteCustomer(id: Long) {
        supabase.from("users").delete { filter { eq("id", id) } }
    }

    // --- ANALYTICS AND METRIC CALCULATION QUERIES ---
    suspend fun fetchDetailedAnalytics(customerId: Long): CustomerDetailedAnalytics {
        // Query payments data table for spend statistics calculations
        val paymentRows = supabase.from("payments")
            .select { filter { eq("customer_id", customerId) } }
            .decodeList<PaymentRecord>()
        
        val totalSpend = paymentRows.filter { it.payment_status == "captured" }.sumOf { it.amount.toDouble() }
        val totalOrders = paymentRows.distinctBy { it.order_id }.size.toLong()

        // Fetch customer reviews with relational items included
        val reviewsData = supabase.from("reviews")
            .select { filter { eq("customer_id", customerId) } }
            .decodeList<ReviewRecord>()

        val reviewMetrics = reviewsData.map {
            CustomerReviewMetric(it.id, it.rating, it.review_type, it.title.orEmpty(), it.description.orEmpty())
        }

        return CustomerDetailedAnalytics(
            totalSpend = totalSpend,
            totalOrders = totalOrders,
            reviewsList = reviewMetrics
        )
    }

    // --- UPLOAD CONTRACT AND EDGE FUNCTIONS CHANNELS ---
    suspend fun uploadWhatsAppImage(context: Context, uri: Uri): String {
        val bucketRef = supabase.storage.from("whatsapp_img")
        val compressed = compressImage(context, uri)
        val path = "broadcasts/${UUID.randomUUID()}.jpg"
        bucketRef.upload(path, compressed)
        return bucketRef.publicUrl(path)
    }

    @OptIn(InternalAPI::class)
    suspend fun invokeFcmNotificationEdgeFunction(token: String, title: String, desc: String, url: String) {
        try {
            supabase.functions.invoke(function = "send_fcm_to_token") {
                this.body = buildJsonObject {
                    put("token", token)
                    put("title", title)
                    put("body", desc)
                    put("imageUrl", url)
                }.toString()
            }
        } catch (e: Exception) {
            println("Sent failure $e")
        }
    }

    suspend fun invokeWhatsAppTemplateEdgeFunction(phone: String, message: String, imageUrl: String?) {
        supabase.functions.invoke("send_whatsapp_template") {
            setBody(buildJsonObject {
                put("to", phone)
                put("message", message)
                if (!imageUrl.isNullOrBlank()) put("image_url", imageUrl)
            })
        }
    }
}

// Temporary data parsing structures mapped internally for repository aggregations
@Serializable
private data class PaymentRecord(val amount: Long, val payment_status: String, val order_id: String)
@Serializable
private data class ReviewRecord(val id: Long, val rating: Long, val review_type: String, val title: String? = null, val description: String? = null)