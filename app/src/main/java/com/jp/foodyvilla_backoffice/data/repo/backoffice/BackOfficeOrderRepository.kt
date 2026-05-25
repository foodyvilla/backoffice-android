package com.jp.foodyvilla_backoffice.data.repo.backoffice

import com.jp.foodyvilla_backoffice.data.model.backoffice.adminTables
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toDisplayText
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.util.UUID

class BackOfficeOrderRepository(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {
    private val orderTable = adminTables.first { it.name == "orders" }

    suspend fun getOrders(): List<JsonObject> {
        val session = authRepository.currentSession.value ?: return emptyList()
        val rows = supabase.from("orders")
            .select(orderTable.selectColumns()) {
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<JsonObject>()
        
        return if (session.isOwner()) rows 
        else rows.filter { it["outlet_id"].toDisplayText() == session.outletId.toString() }
    }

    fun observeOrders(): Flow<Result<List<JsonObject>>> = callbackFlow {
        val channel = supabase.realtime.channel("backoffice-orders-${UUID.randomUUID()}")
        
        suspend fun push() {
            trySend(runCatching { getOrders() })
        }

        val jobs = listOf(
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") { table = "orders" }.onEach { push() }.launchIn(this),
            channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") { table = "orders" }.onEach { push() }.launchIn(this),
            channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") { table = "orders" }.onEach { push() }.launchIn(this)
        )

        channel.subscribe()
        push()

        awaitClose {
            jobs.forEach { it.cancel() }
            launch {
                runCatching { channel.unsubscribe() }
                runCatching { supabase.realtime.removeChannel(channel) }
            }
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): JsonObject {
        val dbStatus = status
        val updated = supabase.from("orders").update({
            "status" to dbStatus
        }) {
            filter { eq("id", orderId) }
            select()
        }.decodeSingle<JsonObject>()
        return updated
    }

    suspend fun deleteOrder(orderId: String) {
        supabase.from("orders").delete {
            filter { eq("id", orderId) }
        }
    }

    suspend fun getOrderItems(orderIds: Set<String>): List<JsonObject> {
        val table = adminTables.first { it.name == "order_items" }
        return supabase.from("order_items")
            .select(table.selectColumns()) {
                filter { isIn("order_id", orderIds.toList()) }
                order("created_at", SupabaseOrder.ASCENDING)
            }
            .decodeList<JsonObject>()
    }

    suspend fun getAllOrderItems(): List<JsonObject> {
        val session = authRepository.currentSession.value ?: return emptyList()
        val table = adminTables.first { it.name == "order_items" }
        val rows = supabase.from("order_items")
            .select(table.selectColumns()) {
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<JsonObject>()
        
        return if (session.isOwner()) rows 
        else rows.filter { it["orders"]?.let { o -> (o as? JsonObject)?.get("outlet_id").toDisplayText() == session.outletId.toString() } ?: false }
    }

    suspend fun getProducts(): List<JsonObject> {
        return supabase.from("product_catalog")
            .select {
                order("name", SupabaseOrder.ASCENDING)
            }
            .decodeList<JsonObject>()
    }

    private fun com.jp.foodyvilla_backoffice.data.model.backoffice.AdminTable.selectColumns(): Columns {
        return when (name) {
            "orders" -> Columns.raw("*, outlets(name, city), users(name, phone), payments(amount)")
            "order_items" -> Columns.raw("*, orders(customer_name, phone, status, outlet_id), outlet_menu_items(id, image, price, product_catalog(name, category, description))")
            else -> Columns.ALL
        }
    }

    private fun String.toOrderDbStatus(): String = when (val s = trim().lowercase().replace(" ", "_")) {
        "placed" -> "pending"
        "delivered" -> "completed"
        else -> s
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
