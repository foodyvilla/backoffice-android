package com.jp.foodyvilla_backoffice.data.repository

import com.jp.foodyvilla_backoffice.core.database.BackOfficeSchema
import com.jp.foodyvilla_backoffice.data.domain.model.BackOfficeOrder
import com.jp.foodyvilla_backoffice.data.domain.model.BackOfficeOrderItem
import com.jp.foodyvilla_backoffice.data.domain.model.OrderStatus
import com.jp.foodyvilla_backoffice.data.domain.repository.OrderAlert
import com.jp.foodyvilla_backoffice.data.domain.repository.OrderWorkflowRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

class SupabaseOrderWorkflowRepository(
    private val supabase: SupabaseClient
) : OrderWorkflowRepository {

    override fun observeIncomingOrders(outletId: Long): Flow<Result<OrderAlert>> = callbackFlow {
        val channel = supabase.realtime.channel("orders-alert-${UUID.randomUUID()}")

        val insertJob = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = BackOfficeSchema.Tables.Orders
        }.onEach {
            val latest = runCatching { loadLatestPendingOrder(outletId) }
            latest.getOrNull()?.let { trySend(Result.success(it)) }
            latest.exceptionOrNull()?.let { trySend(Result.failure(it)) }
        }.launchIn(this)

        channel.subscribe()

        awaitClose {
            insertJob.cancel()
            launch {
                runCatching { channel.unsubscribe() }
                runCatching { supabase.realtime.removeChannel(channel) }
            }
        }
    }

    override suspend fun loadOrder(orderId: String): Result<OrderAlert> = runCatching {
        val order = supabase.from(BackOfficeSchema.Tables.Orders)
            .select {
                filter { eq(BackOfficeSchema.Orders.Id, orderId) }
            }
            .decodeSingle<BackOfficeOrder>()
        OrderAlert(order = order, items = loadItems(orderId))
    }

    override suspend fun transitionOrder(orderId: String, status: OrderStatus): Result<Unit> = runCatching {
        val result = supabase.from(BackOfficeSchema.Tables.Orders).update(
            buildJsonObject {
                put(BackOfficeSchema.Orders.Status, status.dbValue)
            }
        ) {
            filter { eq(BackOfficeSchema.Orders.Id, orderId) }
            select()
        }.decodeSingleOrNull<kotlinx.serialization.json.JsonObject>()
        
        if (result == null) throw IllegalStateException("Order status transition failed. No row found for $orderId")
    }

    override suspend fun rejectOrder(orderId: String, reason: String): Result<Unit> {
        // The attached DB export does not include rejected_by, rejected_at, or rejected_reason yet.
        // Keep the mutation RLS-safe by writing only schema-backed columns.
        return transitionOrder(orderId, OrderStatus.Rejected)
    }

    private suspend fun loadLatestPendingOrder(outletId: Long): OrderAlert? {
        val order = supabase.from(BackOfficeSchema.Tables.Orders)
            .select {
                filter {
                    eq(BackOfficeSchema.Orders.OutletId, outletId)
                    eq(BackOfficeSchema.Orders.Status, OrderStatus.Pending.dbValue)
                }
                order(BackOfficeSchema.Orders.CreatedAt, Order.DESCENDING)
                limit(1)
            }
            .decodeList<BackOfficeOrder>()
            .firstOrNull()
            ?: return null

        return OrderAlert(order = order, items = loadItems(order.id))
    }

    private suspend fun loadItems(orderId: String): List<BackOfficeOrderItem> {
        return supabase.from(BackOfficeSchema.Tables.OrderItems)
            .select {
                filter { eq(BackOfficeSchema.OrderItems.OrderId, orderId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList()
    }
}

