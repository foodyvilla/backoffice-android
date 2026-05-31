package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import android.util.Log
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.EmployeeIdLookupResponse
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewCategoryResponse
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewCategoryUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewCustomerResponse
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewCustomerUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewDetailedOrderUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOrderType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOrderUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOutletMenuResponse
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOutletMenuUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewSelectedMenuItem
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OrderListResponse
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OutletDropdownUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OutletListResponse
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.toUiModel
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
        if (!isOwner && outletId == null) {
            Log.w(TAG, "Gating boundary triggered: Employee profile lacks assigned branch ID.")
            return emptyList()
        }

        return try {
            // 1. Calculate the start and end of the local day using the system's timezone context
            val localZoneId = ZoneId.systemDefault() // Resolves to 'Asia/Kolkata' or device default

            // Convert local midnight to an absolute UTC Instant snapshot
            val startUtcInstant = filterDate.atStartOfDay(localZoneId).toInstant()
            // Convert the next day's local midnight to an absolute UTC Instant snapshot
            val endUtcInstant = filterDate.plusDays(1).atStartOfDay(localZoneId).toInstant()

            // 2. Format into true, correctly converted UTC ISO-8601 strings
            val isoStartInUtcString = DateTimeFormatter.ISO_INSTANT.format(startUtcInstant)
            val isoEndInUtcString = DateTimeFormatter.ISO_INSTANT.format(endUtcInstant)

            Log.d(TAG, "Querying window: Local Date $filterDate -> UTC Bounds: [$isoStartInUtcString] to [$isoEndInUtcString]")

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
                if (outletId != null) {
                    filter { eq("outlet_id", outletId) }
                }

                // Enforce mathematically correct UTC timestamp range boundaries
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
            }.decodeList<OutletListResponse>().map {
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
        orderType: String, outletId: Long, customerPhone: String,  internalEmpId: Long?
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
            }

            // 1. Commit Update payload fields properties changes and verify
            val result = supabase.from("orders").update(payload) {
                filter {
                    eq("id", trimmedId)
                    eq("outlet_id", outletId)
                }
                select()
            }.decodeSingleOrNull<JsonObject>()

            if (result == null) {
                // Diagnostic check to see if the order exists at all or if outletId mismatched
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

            // 2. Broadcast updates across the corporate micro-service edge routes channels
            runCatching {
                triggerOutletEdgeNotification(outletId, trimmedId, "Administrative override changes saved. Status: ${dbStatus.uppercase()}")
            }.onFailure { e ->
                Log.e(TAG, "Outlet notification failed but database update was successful: ${e.message}")
            }

            // 3. Resolve customer profile token boundaries in the background to shoot FCM alerts
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
        val scope = CoroutineScope(Dispatchers.Default)
        Log.d(TAG, "Starting placeOrder sequence for Outlet ID: $outletId, Order ID: $orderId, Items Count: ${items.size}")

        return try {
            // ====================================================
            // STEP 1: INSERT INTO public.orders TABLE
            // ====================================================
            Log.d(TAG, "Step 1: Preparing to insert core order structure row metadata...")
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
            })
            Log.d(TAG, "Step 1 Success: Core order entry written cleanly into database.")

            // ====================================================
            // STEP 2: INSERT BATCH ITEMS INTO public.order_items TABLE
            // ====================================================
            Log.d(TAG, "Step 2: Preparing to batch insert item allocations into order_items table...")
            items.forEachIndexed { index, item ->
                Log.v(TAG, "Batching item index [$index] -> Menu Item ID: ${item.menuItemId}, Qty: ${item.qty}")
                supabase.from("order_items").insert(buildJsonObject {
                    put("order_id", orderId)
                    put("menu_item_id", item.menuItemId)
                    put("qty", item.qty)
                    put("price_per_item", item.price)
                    put("total_price", item.totalPrice)
                    put("total_discount", 0)
                })
            }
            Log.d(TAG, "Step 2 Success: All batch items registered successfully.")

            // ====================================================
            // STEP 3: FINANCIAL CALCULATIONS & PAISA CONVERSION
            // ====================================================
            Log.d(TAG, "Step 3: Executing itemized invoicing and tax ratio computations...")
            val packagingFee = 15.0
            val gstCalculatedTax = total * 0.05 // 5% uniform GST allocation split
            val riderLogisticsFee = if (orderType == NewOrderType.DELIVERY) 30.0 else 0.0
            val grandTotalInRupees = total + packagingFee + gstCalculatedTax + riderLogisticsFee

            // FIXED 22P02: Absolute whole numerical Paisa value to match integer BigInt specifications
            val totalAmountInPaisa = (grandTotalInRupees * 100).toLong()
            Log.v(TAG, "Calculated Subtotal: ₹$total, Grand Total: ₹$grandTotalInRupees -> Parsed BigInt: $totalAmountInPaisa Paisa")

            // ====================================================
            // STEP 4: INSERT TRANSACTION INTO public.payments TABLE
            // ====================================================
            Log.d(TAG, "Step 4: Compiling transactional record for payments table matrix...")
            supabase.from("payments").insert(buildJsonObject {
                put("order_id", orderId)
                put("customer_id", customer?.id)
                put("amount", totalAmountInPaisa) // Dispatched as safe bigint numeric payload data structures
                put("payment_status", "created")
                put("payment_method", "cash")
            })
            Log.d(TAG, "Step 4 Success: Payment transaction row finalized.")

            // ====================================================
            // STEP 5: TRIGGER MULTI-CHANNEL SERVICE NOTIFICATIONS
            // ====================================================
            Log.d(TAG, "Step 5: Invoking remote Edge Functions cloud message brokers pipelines...")

            // Channel A: Notify Kitchen Workspace Instance Terminal
            scope.launch {
                runCatching {
                    triggerOutletEdgeNotification(outletId, orderId, "New counter POS placement receipt recorded.")
                    Log.i(TAG, "Notification Channel A Success: Outlet terminal alerted.")
                }.onFailure { err ->
                    Log.e(TAG, "Notification Channel A Warning: Failed to trigger outlet function: ${err.message}")
                }
            }

            // Channel B: Notify Customer Client FCM Instance Token
            customer?.fcmToken?.let { token ->
                scope.launch {
                    runCatching {
                        triggerFcmUpdateNotification(token, "Order Received! 🍔", "Your counter checkout order has cleared successfully.")
                        Log.i(TAG, "Notification Channel B Success: Customer client push dispatch completed.")
                    }.onFailure { err ->
                        Log.e(TAG, "Notification Channel B Warning: Failed to dispatch customer FCM: ${err.message}")
                    }
                }
            }

            Log.i(TAG, "Checkout workflow finalized smoothly for Order ID: $orderId. Returning response model.")
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
            throw e // Relaunch up to the lifecycle viewModel Scope handler wrapper layers cleanly
        }
    }
}