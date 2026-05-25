package com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class NewOrdersManagementRepository(private val supabase: SupabaseClient) {

    private companion object {
        const val TAG = "OrdersManagementRepo"
    }

    /**
     * Resolves the database internal row auto-increment primary key identifier index for
     * the active session user account profile.
     */
    suspend fun resolveEmployeeRowId(authUserId: String): Long? {
        return try {
            supabase.from("employee").select {
                filter { eq("auth_user_id", authUserId) }
            }.decodeSingleOrNull<EmployeeIdLookupResponse>()?.id
        } catch (e: Exception) {
            Log.e(TAG, "Error matching auth_user_id to employee tracking identifier: ${e.message}")
            null
        }
    }

    /**
     * Executes one-time snapshot query fetches for orders based on explicit roles, location locks, and date limits.
     * * Rules implemented:
     * 1. If isOwner == true and outletId == null -> Returns records across ALL active franchises.
     * 2. If outletId is explicitly given -> Restricts retrieval strictly to that outlet node context.
     * 3. If isOwner == false and outletId == null -> Returns empty dataset instantly (Employee without location bounds).
     */
    suspend fun fetchOutletOrdersSnapshot(
        outletId: Long?,
        isOwner: Boolean,
        filterDate: LocalDate = LocalDate.now()
    ): List<NewDetailedOrderUiModel> {
        // Enforce strict client-side role validation barrier gating
        if (!isOwner && outletId == null) {
            Log.w(TAG, "Gating boundary triggered: Employee profile lacks assigned branch ID location properties.")
            return emptyList()
        }

        return try {
            val isoStartOfDayString = filterDate.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
            val isoEndOfDayString = filterDate.plusDays(1).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"

            supabase.from("orders").select(
                Columns.raw(
                    """
                    *,
                    order_items (
                        menu_item_id, qty, price_per_item, total_price,
                        outlet_menu_items ( product_catalog ( name ) )
                    )
                    """.trimIndent()
                )
            ) {
                // Apply outlet constraint dynamically based on authorization layers
                if (outletId != null) {
                    filter { eq("outlet_id", outletId) }
                }

                // Enforce time boundary range parameters
                filter {
                    gte("created_at", isoStartOfDayString)
                    lt("created_at", isoEndOfDayString)
                }

                order("created_at", order = Order.DESCENDING)
            }.decodeList<OrderListResponse>().map { it.toUiModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error running snapshot postgrest statement extraction execution: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Establishes a highly reactive WebSocket real-time subscription flow pipeline.
     * Automatically emits newly modified matrices down to collectors whenever modifications clear on the database.
     */
    fun observeOutletOrdersRealTime(
        outletId: Long?,
        isOwner: Boolean,
        filterDate: LocalDate
    ): Flow<List<NewDetailedOrderUiModel>> = callbackFlow {
        // 1. Instantly dispatch initial state context snapshot
        val initialData = fetchOutletOrdersSnapshot(outletId, isOwner, filterDate)
        trySend(initialData)

        // Security fallback clearance checkpoint loops
        if (!isOwner && outletId == null) {
            awaitClose { /* Terminate execution stream context gracefully */ }
            return@callbackFlow
        }

        // 2. Setup structural listening channels channel properties
        val uniqueChannelNameIdentifier = if (outletId != null) "orders_outlet_${outletId}_${UUID.randomUUID()}" else "orders_master_stream_${UUID.randomUUID()}"
        val realtimeChannelWS = supabase.channel(uniqueChannelNameIdentifier)

        val mutationsChangeFlow = realtimeChannelWS.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "orders"
        }

        val scopeJob = launch {
            mutationsChangeFlow.collectLatest { postgresAction ->
                Log.d(TAG, "Intercepted remote public table database mutate change event: ${postgresAction::class.simpleName}")

                // Refetch full payload snapshots to resolve relational nested rows correctly
                val freshRefetchedSnapshot = fetchOutletOrdersSnapshot(outletId, isOwner, filterDate)
                trySend(freshRefetchedSnapshot)
            }
        }

        // 3. Fire hot subscription process hook
        realtimeChannelWS.subscribe()

        // 4. Handle clean connection teardowns
        awaitClose {
            scopeJob.cancel()
            launch {
                runCatching { realtimeChannelWS.unsubscribe() }
                runCatching { supabase.realtime.removeChannel(realtimeChannelWS) }
            }
        }
    }

    suspend fun fetchActiveOutletsList(): List<OutletDropdownUiModel> {
        return try {
            supabase.from("outlets").select {
                filter { eq("is_active", true) }
            }.decodeList<OutletListResponse>().map { OutletDropdownUiModel(id = it.id, name = it.name) }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving outlets drop down items: ${e.message}")
            emptyList()
        }
    }

    suspend fun getOutletMenu(outletId: Long): List<NewOutletMenuUiModel> {
        return try {
            supabase.from("outlet_menu_items").select(Columns.raw("*, product_catalog(*)")) {
                filter {
                    eq("outlet_id", outletId)
                    eq("is_available", true)
                    eq("is_out_of_stock", false)
                }
            }.decodeList<NewOutletMenuResponse>().map { it.toUiModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving items configuration matrices: ${e.message}")
            emptyList()
        }
    }

    suspend fun getCategories(): List<NewCategoryUiModel> {
        return try {
            supabase.from("categories").select {
                filter { eq("is_active", true) }
            }.decodeList<NewCategoryResponse>().map { NewCategoryUiModel(id = it.id, name = it.name, emoji = it.emoji) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findCustomerByPhone(phone: String): NewCustomerUiModel? {
        return try {
            supabase.from("users").select {
                filter { eq("phone", phone) }
            }.decodeSingleOrNull<NewCustomerResponse>()?.toUiModel()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Core update mutation driver. Alters target parameters and dispatches edge functions alerts to all layers.
     */
    @OptIn(InternalAPI::class)
    suspend fun updateOrderDetails(
        orderId: String, status: String, address: String, instruction: String,
        orderType: String, outletId: Long, customerPhone: String
    ) {
        try {
            val payload = buildJsonObject {
                put("status", status.lowercase().trim())
                put("address", address)
                put("instruction", instruction)
                put("order_type", orderType.lowercase().trim())
            }

            // 1. Commit Update payload fields properties changes
            supabase.from("orders").update(payload) {
                filter { eq("id", orderId) }
            }

            // 2. Broadcast updates across the corporate micro-service edge routes channels
            triggerOutletEdgeNotification(outletId, orderId, "Administrative override changes saved. Status: ${status.uppercase()}")

            // 3. Resolve customer profile token boundaries in the background to shoot FCM alerts
            launchEdgeCustomerNotificationPipeline(customerPhone, "Order Profile Updates 📦", "Your Order #${orderId.take(8)} configuration shifts to: ${status.uppercase()}")

        } catch (e: Exception) {
            Log.e(TAG, "Error executing mutations overrides: ${e.message}")
            throw e
        }
    }

    @OptIn(InternalAPI::class)
    suspend fun triggerFcmUpdateNotification(token: String, title: String, body: String) {
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
    suspend fun triggerOutletEdgeNotification(outletId: Long, orderId: String, alertMessage: String) {
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
    suspend fun placeOrder(
        outletId: Long, customer: NewCustomerUiModel?, phone: String,
        address: String, orderType: NewOrderType, instruction: String,
        items: List<NewSelectedMenuItem>, internalEmpId: Long?
    ): NewOrderUiModel {
        val orderId = UUID.randomUUID().toString()
        val total = items.sumOf { it.totalPrice }

        supabase.from("orders").insert(buildJsonObject {
            put("id", orderId)
            put("outlet_id", outletId)
            put("customer_id", customer?.id)
            put("customer_name", customer?.name ?: "Walk-in Customer")
            put("phone", phone)
            put("status", "pending")
            put("order_type", orderType.name.lowercase())
            put("address", address)
            put("instruction", instruction)
            if (internalEmpId != null) put("accepted_by", internalEmpId)
        })

        items.forEach { item ->
            supabase.from("order_items").insert(buildJsonObject {
                put("order_id", orderId)
                put("menu_item_id", item.menuItemId)
                put("qty", item.qty)
                put("price_per_item", item.price)
                put("total_price", item.totalPrice)
                put("total_discount", 0)
            })
        }

        supabase.from("payments").insert(buildJsonObject {
            put("order_id", orderId)
            put("customer_id", customer?.id)
            put("amount", total)
            put("payment_status", "created")
            put("payment_method", "cash")
        })

        // Fire real-time Edge functions pipelines
        triggerOutletEdgeNotification(outletId, orderId, "New counter POS placement receipt recorded.")

        customer?.fcmToken?.let { token ->
            triggerFcmUpdateNotification(token, "Order Received! 🍔", "Your counter checkout order has cleared successfully.")
        }

        return NewOrderUiModel(
            orderId = orderId, customerName = customer?.name ?: "Walk-in Customer",
            phone = phone, orderType = orderType.name, items = items, totalAmount = total
        )
    }
}