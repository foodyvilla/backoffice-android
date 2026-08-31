package com.jp.foodyvilla_backoffice.data.repository

import android.util.Log
import com.jp.foodyvilla_backoffice.data.model.*
import com.jp.foodyvilla_backoffice.domain.repository.OrderManagementRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.call.body
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class OrderManagementRepositoryImpl(private val supabase: SupabaseClient) : OrderManagementRepository {

    private companion object {
        const val TAG = "OrdersManagementRepo"
    }

    override suspend fun resolveEmployeeRowId(authUserId: String): Long? {
        return try {
            supabase.from("employee").select {
                filter { eq("auth_user_id", authUserId) }
            }.decodeSingleOrNull<EmployeeIdLookupResponse>()?.id
        } catch (e: Exception) {
            Log.e(TAG, "Error matching auth_user_id to employee tracking identifier: ${e.message}")
            null
        }
    }

    override suspend fun fetchOutletOrdersSnapshot(
        outletId: Long?,
        isOwner: Boolean,
        filterDate: LocalDate
    ): List<NewDetailedOrderUiModel> {
        if (!isOwner && outletId == null) {
            Log.w(TAG, "Gating boundary triggered: Employee profile lacks assigned branch ID.")
            return emptyList()
        }

        return try {
            val localZoneId = ZoneId.systemDefault()
            val startUtcInstant = filterDate.atStartOfDay(localZoneId).toInstant()
            val endUtcInstant = filterDate.plusDays(1).atStartOfDay(localZoneId).toInstant()

            val isoStartInUtcString = DateTimeFormatter.ISO_INSTANT.format(startUtcInstant)
            val isoEndInUtcString = DateTimeFormatter.ISO_INSTANT.format(endUtcInstant)

            Log.d(TAG, "Querying window: Local Date $filterDate -> UTC Bounds: [$isoStartInUtcString] to [$isoEndInUtcString]")

            supabase.from("orders").select(
                Columns.raw(
                    """
                *,
                restaurant_tables:restaurant_tables!orders_table_id_fkey ( table_number ),
                order_items (
                    menu_item_id, qty, price_per_item, total_price,
                    outlet_menu_items ( product_catalog ( name ) )
                )
                """.trimIndent()
                )
            ) {
                if (outletId != null) {
                    filter { eq("outlet_id", outletId) }
                }
                filter {
                    gte("created_at", isoStartInUtcString)
                    lt("created_at", isoEndInUtcString)
                }
                order("created_at", order = Order.DESCENDING)
            }.decodeList<OrderListResponse>().map { it.toUiModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error running snapshot postgrest statement extraction execution: ${e.message}", e)
            emptyList()
        }
    }

    override fun observeOutletOrdersRealTime(
        outletId: Long?,
        isOwner: Boolean,
        filterDate: LocalDate
    ): Flow<List<NewDetailedOrderUiModel>> = callbackFlow {
        val initialData = fetchOutletOrdersSnapshot(outletId, isOwner, filterDate)
        trySend(initialData)

        if (!isOwner && outletId == null) {
            awaitClose { }
            return@callbackFlow
        }

        val uniqueChannelNameIdentifier = if (outletId != null) "orders_outlet_${outletId}_${UUID.randomUUID()}" else "orders_master_stream_${UUID.randomUUID()}"
        val realtimeChannelWS = supabase.channel(uniqueChannelNameIdentifier)

        val mutationsChangeFlow = realtimeChannelWS.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "orders"
        }

        val scopeJob = launch {
            mutationsChangeFlow.collectLatest { postgresAction ->
                Log.d(TAG, "Intercepted remote public table database mutate change event: ${postgresAction::class.simpleName}")
                val freshRefetchedSnapshot = fetchOutletOrdersSnapshot(outletId, isOwner, filterDate)
                trySend(freshRefetchedSnapshot)
            }
        }

        realtimeChannelWS.subscribe()

        awaitClose {
            scopeJob.cancel()
            launch {
                runCatching { realtimeChannelWS.unsubscribe() }
                runCatching { supabase.realtime.removeChannel(realtimeChannelWS) }
            }
        }
    }

    override suspend fun fetchActiveOutletsList(): List<OutletDropdownUiModel> {
        return try {
            supabase.from("outlets").select().decodeList<OutletListResponse>().map {
                OutletDropdownUiModel(
                    id = it.id,
                    name = it.name
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving outlets drop down items: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getOutletMenu(outletId: Long): List<NewOutletMenuUiModel> {
        return try {
            supabase.from("outlet_menu_items").select(Columns.raw("*, product_catalog(*)")) {
                filter {
                    eq("outlet_id", outletId)
                }
            }.decodeList<NewOutletMenuResponse>().map { it.toUiModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving items configuration matrices: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getCategories(): List<NewCategoryUiModel> {
        return try {
            supabase.from("categories").select {
                filter { eq("is_active", true) }
            }.decodeList<NewCategoryResponse>().map {
                NewCategoryUiModel(
                    id = it.id,
                    name = it.name,
                    emoji = it.emoji
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun findCustomerByPhone(phone: String): NewCustomerUiModel? {
        return try {
            supabase.from("users").select {
                filter { eq("phone", phone) }
            }.decodeSingleOrNull<NewCustomerResponse>()?.toUiModel()
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(InternalAPI::class)
    override suspend fun updateOrderDetails(
        orderId: String, status: String, address: String, instruction: String,
        orderType: String, outletId: Long, customerPhone: String, internalEmpId: Long?,
        tableId: Long?
    ) {
        val trimmedId = orderId.trim()
        val dbStatus = when (val s = status.trim().lowercase().replace(" ", "_")) {
            "placed" -> "pending"
            "delivered" -> "completed"
            else -> s
        }

        try {
            Log.d(TAG, "Initiating order update for ID: $trimmedId, Target Status: $dbStatus (orig: $status)")
            val payload = buildJsonObject {
                put("status", dbStatus)
                put("address", address)
                put("instruction", instruction)
                put("order_type", orderType.lowercase().trim().replace(" ", "_"))
                if (internalEmpId != null) put("accepted_by", internalEmpId)
                if (tableId != null) put("table_id", tableId)
            }

            val result = supabase.from("orders").update(payload) {
                filter {
                    eq("id", trimmedId)
                    eq("outlet_id", outletId)
                }
                select()
            }.decodeSingleOrNull<JsonObject>()

            if (result == null) {
                val actualOrder = supabase.from("orders").select(columns = Columns.raw("id, outlet_id")) {
                    filter { eq("id", trimmedId) }
                }.decodeSingleOrNull<JsonObject>()

                val diagnosticMsg = when {
                    actualOrder == null -> "Order ID $trimmedId not found in database."
                    else -> {
                        val actualOutletIdStr = actualOrder["outlet_id"]?.toString()
                        "Order found but belongs to outlet $actualOutletIdStr, not $outletId. Permission denied or mismatch."
                    }
                }
                Log.e(TAG, "Order update failed for ID $trimmedId: $diagnosticMsg")
                throw IllegalStateException("Order update failed in database: $diagnosticMsg")
            }

            Log.d(TAG, "Database update successful for Order ID: $trimmedId. Proceeding to notifications.")

            runCatching {
                triggerOutletEdgeNotification(outletId, trimmedId, "Administrative override changes saved. Status: ${dbStatus.uppercase()}")
            }.onFailure { e ->
                Log.e(TAG, "Outlet notification failed but database update was successful: ${e.message}")
            }

            runCatching {
                launchEdgeCustomerNotificationPipeline(customerPhone, "Order Profile Updates 📦", "Your Order #${trimmedId.take(8)} configuration shifts to: ${dbStatus.uppercase()}")
            }.onFailure { e ->
                Log.e(TAG, "Customer FCM notification failed but database update was successful: ${e.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error executing mutations overrides for Order ID $orderId: ${e.message}", e)
            throw e
        }
    }

    @OptIn(InternalAPI::class)
    override suspend fun triggerFcmUpdateNotification(token: String, title: String, body: String) {
        runCatching {
            supabase.functions.invoke(function = "send_fcm_to_token") {
                this.body = buildJsonObject {
                    put("token", token)
                    put("title", title)
                    put("body", body)
                }.toString()
            }
        }
    }

    @OptIn(InternalAPI::class)
    override suspend fun triggerOutletEdgeNotification(outletId: Long, orderId: String, alertMessage: String) {
        runCatching {
            supabase.functions.invoke(function = "notify_outlet") {
                this.body = buildJsonObject {
                    put("outletId", outletId)
                    put("order_id", orderId)
                    put("message", alertMessage)
                }.toString()
            }
        }
    }

    private suspend fun launchEdgeCustomerNotificationPipeline(phone: String, title: String, msgBody: String) {
        runCatching {
            findCustomerByPhone(phone)?.fcmToken?.let { clientToken ->
                triggerFcmUpdateNotification(clientToken, title, msgBody)
            }
        }
    }

    @OptIn(InternalAPI::class)
    override suspend fun placeOrder(
        outletId: Long, customer: NewCustomerUiModel?, phone: String,
        address: String, orderType: NewOrderType, instruction: String,
        items: List<NewSelectedMenuItem>, internalEmpId: Long?,
        tableId: Long?
    ): NewOrderUiModel {
        val orderId = UUID.randomUUID().toString()
        val total = items.sumOf { it.totalPrice }
        val scope = CoroutineScope(Dispatchers.Default)

        return try {
            supabase.from("orders").insert(buildJsonObject {
                put("id", orderId)
                put("outlet_id", outletId)
                put("customer_id", customer?.id)
                put("customer_name", customer?.name ?: "Walk-in Customer")
                put("phone", phone)
                put("status", "placed")
                put("order_type", orderType.name.lowercase())
                put("address", address)
                put("instruction", instruction)
                if (internalEmpId != null) put("accepted_by", internalEmpId)
                if (tableId != null) put("table_id", tableId)
            })

            items.forEachIndexed { index, item ->
                supabase.from("order_items").insert(buildJsonObject {
                    put("order_id", orderId)
                    put("menu_item_id", item.menuItemId)
                    put("qty", item.qty)
                    put("price_per_item", item.price)
                    put("total_price", item.totalPrice)
                    put("total_discount", 0)
                })
            }

            val packagingFee = 15.0
            val gstCalculatedTax = total * 0.05
            val riderLogisticsFee = if (orderType == NewOrderType.DELIVERY) 30.0 else 0.0
            val grandTotalInRupees = total + packagingFee + gstCalculatedTax + riderLogisticsFee
            val totalAmountInPaisa = (grandTotalInRupees * 100).toLong()

            supabase.from("payments").insert(buildJsonObject {
                put("order_id", orderId)
                put("customer_id", customer?.id)
                put("amount", totalAmountInPaisa)
                put("payment_status", "created")
                put("payment_method", "cash")
            })

            scope.launch {
                runCatching {
                    triggerOutletEdgeNotification(outletId, orderId, "New counter POS placement receipt recorded.")
                }
            }

            customer?.fcmToken?.let { token ->
                scope.launch {
                    runCatching {
                        triggerFcmUpdateNotification(token, "Order Received! 🍔", "Your counter checkout order has cleared successfully.")
                    }
                }
            }

            NewOrderUiModel(
                orderId = orderId,
                customerName = customer?.name ?: "Walk-in Customer",
                phone = phone,
                orderType = orderType.name,
                items = items,
                totalAmount = grandTotalInRupees
            )
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL ERROR: Transaction sequence rolled back/failed for Order ID: $orderId. Reason: ${e.message}", e)
            throw e
        }
    }

    @OptIn(InternalAPI::class)
    override suspend fun getAnalyticsSummary(startDate: LocalDate, endDate: LocalDate): AnalyticsResponse {
        return try {
            val response = supabase.functions.invoke(function = "get_analytics") {
                this.body = buildJsonObject {
                    put("start_date", startDate.toString())
                    put("end_date", endDate.toString())
                }.toString()
            }
            response.body<AnalyticsResponse>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching analytics from edge function: ${e.message}")
            AnalyticsResponse(success = false, error = e.localizedMessage)
        }
    }
}
