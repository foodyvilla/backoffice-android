package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.YearMonth
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PaymentAdminRepository(private val supabase: SupabaseClient) {

    suspend fun fetchAllUsersMinimal(): List<UserResponse> =
        supabase.from("users").select().decodeList()

    suspend fun fetchRawPaymentsAdmin(month: YearMonth): List<PaymentAdminResponse> {
        val startBoundary = month.atDay(1).atStartOfDay().toString()
        val endBoundary = month.atEndOfMonth().atTime(23, 59, 59).toString()
        
        return supabase.from("payments").select {
            filter {
                gte("created_at", startBoundary)
                lte("created_at", endBoundary)
            }
            order("created_at", order = Order.DESCENDING)
        }.decodeList()
    }

    suspend fun createPaymentRecord(model: PaymentAdminUiModel) {
        supabase.from("payments").insert(buildJsonObject {
            put("order_id", model.orderId.trim())
            if (model.customerId != 0L) put("customer_id", model.customerId)
            put("razorpay_order_id", model.razorpayOrderId.trim().ifBlank { null })
            put("razorpay_payment_id", model.razorpayPaymentId.trim().ifBlank { null })
            put("amount", (model.amountDisplay * 100).toLong())
            put("payment_status", model.status.name.lowercase())
            put("payment_method", model.method.name.lowercase())
        })
    }

    suspend fun updatePaymentRecord(model: PaymentAdminUiModel) {
        supabase.from("payments").update(buildJsonObject {
            put("order_id", model.orderId.trim())
            if (model.customerId != 0L) put("customer_id", model.customerId)
            put("razorpay_order_id", model.razorpayOrderId.trim().ifBlank { null })
            put("razorpay_payment_id", model.razorpayPaymentId.trim().ifBlank { null })
            put("amount", (model.amountDisplay * 100).toLong())
            put("payment_status", model.status.name.lowercase())
            put("payment_method", model.method.name.lowercase())
        }) { filter { eq("id", model.id) } }
    }

    suspend fun purgePaymentLogRecord(id: Long) {
        supabase.from("payments").delete { filter { eq("id", id) } }
    }
}