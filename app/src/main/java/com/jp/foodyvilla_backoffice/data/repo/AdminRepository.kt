package com.jp.foodyvilla_backoffice.data.repo

import android.content.Context
import android.net.Uri
import com.jp.foodyvilla_backoffice.data.utils.compressImage
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.OutletRole
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.asNumber
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toDisplayText
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

class AdminRepository(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) {
    val authSession: kotlinx.coroutines.flow.StateFlow<UserSession?> = authRepository.currentSession

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun loadRows(table: AdminTable): List<JsonObject> {
        return loadTableRows(table)
    }

    private suspend fun loadTableRows(table: AdminTable, dateFilter: String? = null): List<JsonObject> {
        val query = supabase.from(table.name)
            .select(table.selectColumns()) {
                order(table.orderBy, Order.DESCENDING)
                if (table.name == "orders" && dateFilter != null) {
                    filter {
                        and {
                            gte("created_at", "${dateFilter}T00:00:00")
                            lte("created_at", "${dateFilter}T23:59:59")
                        }
                    }
                }
            }
        val rows = query.decodeList<JsonObject>()
        return scopeRows(table.name, rows).map { row -> row.withDisplayJoins(table.name) }
    }

    suspend fun loadOutlets() = loadTableRows(adminTables.first { it.name == "outlets" })
    suspend fun loadOrders(dateFilter: String? = null) = loadTableRows(adminTables.first { it.name == "orders" }, dateFilter)
    suspend fun loadProductCatalog() = loadTableRows(adminTables.first { it.name == "product_catalog" })
    suspend fun loadUsers() = loadTableRows(adminTables.first { it.name == "users" })
    suspend fun loadCart() = loadTableRows(adminTables.first { it.name == "cart" })
    suspend fun loadBanners() = loadTableRows(adminTables.first { it.name == "banners" })
    suspend fun loadOffers() = loadTableRows(adminTables.first { it.name == "offers" })
    suspend fun loadReviews() = loadTableRows(adminTables.first { it.name == "reviews" })
    suspend fun loadEmployees() = loadTableRows(adminTables.first { it.name == "employee" })
    suspend fun loadAttendance() = loadTableRows(adminTables.first { it.name == "attendance" })
    suspend fun loadPayments() = loadTableRows(adminTables.first { it.name == "payments" })
    suspend fun loadOutletMenuItems() = loadTableRows(adminTables.first { it.name == "outlet_menu_items" })
    suspend fun loadAuthOtp() = loadTableRows(adminTables.first { it.name == "auth_otp" })

    suspend fun loadOrderItems(): List<JsonObject> {
        val table = adminTables.first { it.name == "order_items" }
        val rows = supabase.from("order_items")
            .select(table.selectColumns()) {
                order("created_at", Order.ASCENDING)
            }
            .decodeList<JsonObject>()
        val scopedOrderIds = loadRowsBypassScope("orders")
            .let { scopeRows("orders", it) }
            .map { it["id"].toDisplayText() }
            .toSet()
        return rows.filter { it["order_id"].toDisplayText() in scopedOrderIds }
            .map { it.withDisplayJoins("order_items") }
    }

    suspend fun loadProducts(): List<JsonObject> {
        return supabase.from("product_catalog")
            .select {
                order("name", Order.ASCENDING)
            }
            .decodeList<JsonObject>()
    }

    suspend fun loadCustomerOrders(customerId: String): List<JsonObject> {
        val rows = supabase.from("orders")
            .select(adminTables.first { it.name == "orders" }.selectColumns()) {
                filter { eq("customer_id", customerId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<JsonObject>()
        return rows.map { it.withDisplayJoins("orders") }
    }

    suspend fun loadCustomerCart(customerId: String): List<JsonObject> {
        val rows = supabase.from("cart")
            .select(adminTables.first { it.name == "cart" }.selectColumns()) {
                filter { eq("customer_id", customerId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<JsonObject>()
        return rows.map { it.withDisplayJoins("cart") }
    }

    suspend fun loadLookupRows(tableName: String): List<JsonObject> {
        val table = adminTables.firstOrNull { it.name == tableName }
        val rows = if (table != null) {
            supabase.from(table.name)
                .select(table.selectColumns()) {
                    order(table.name.defaultOrderBy(), Order.ASCENDING)
                }
                .decodeList<JsonObject>()
        } else {
            loadRowsBypassScope(tableName, tableName.defaultOrderBy())
        }.map { it.withDisplayJoins(tableName) }
        return scopeRows(tableName, rows)
    }

    private suspend fun loadRowsBypassScope(tableName: String, orderBy: String = "created_at"): List<JsonObject> {
        return supabase.from(tableName)
            .select {
                order(orderBy, Order.DESCENDING)
            }
            .decodeList<JsonObject>()
    }

    private suspend fun scopeRows(tableName: String, rows: List<JsonObject>): List<JsonObject> {
        val session = authRepository.currentSession.value ?: return emptyList()
        if (session.isOwner()) return rows

        return when (tableName) {
            "orders",
            "order_items",
            "outlet_menu_items",
            "cart",
            "banners",
            "offers",
            "outlets",
            "wa_campaigns",
            "wa_config",
            "wa_conversations",
            "wa_messages",
            "wa_templates",
            "wa_webhook_logs",
            "employee",
            "reviews" -> rows.filter { it["outlet_id"].toDisplayText() == session.outletId.toString() }

            "attendance" -> scopeAttendance(rows, session)
            "users" -> scopeUsers(rows, session)
            "product_catalog" -> rows
            "payments" -> scopePayments(rows, session)
            else -> rows
        }
    }

    private suspend fun scopeAttendance(rows: List<JsonObject>, session: UserSession): List<JsonObject> {
        val role = session.roleOrNull()
        if (role == OutletRole.OWNER) return rows
        if (role == OutletRole.HEAD || role == OutletRole.MANAGER) {
            val outletEmployeeIds = loadRowsBypassScope("employee")
                .filter { it["outlet_id"].toDisplayText() == session.outletId.toString() }
                .map { it["id"].toDisplayText() }
                .toSet()
            return rows.filter { it["emp_id"].toDisplayText() in outletEmployeeIds }
        }

        return when (session) {
            is UserSession.EmployeeSession -> rows.filter { it["emp_id"].toDisplayText() == session.empId.toString() }
            is UserSession.OutletSession -> emptyList()
        }
    }

    private suspend fun scopeUsers(rows: List<JsonObject>, session: UserSession): List<JsonObject> {
        val customerIdsForOutlet = loadRowsBypassScope("orders")
            .let { scopeRows("orders", it) }
            .mapNotNull { it["customer_id"].toDisplayText().takeIf { value -> value != "-" } }
            .toSet()
        return rows.filter { it["id"].toDisplayText() in customerIdsForOutlet }
    }

    private suspend fun scopePayments(rows: List<JsonObject>, session: UserSession): List<JsonObject> {
        val scopedOrderIds = loadRowsBypassScope("orders")
            .let { scopeRows("orders", it) }
            .map { it["id"].toDisplayText() }
            .toSet()
        return rows.filter { it["order_id"].toDisplayText() in scopedOrderIds }
    }

    suspend fun uploadImage(context: Context, table: AdminTable, column: AdminColumn, uri: Uri): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val path = "${table.name}/${column.name}/$fileName"
        val bytes = compressImage(context, uri)
        var lastError: Throwable? = null

        table.storageBucketCandidates().forEach { bucket ->
            runCatching {
                supabase.storage.from(bucket).upload(
                    path = path,
                    data = bytes
                )

                return supabase.storage.from(bucket).publicUrl(path)
            }.onFailure { throwable ->
                lastError = throwable
            }
        }

        throw lastError ?: IllegalStateException("Image upload failed")
    }

    fun observeRows(table: AdminTable): Flow<Result<List<JsonObject>>> {
        return observeTableRows(table)
    }

    private fun observeTableRows(table: AdminTable, dateFilter: String? = null): Flow<Result<List<JsonObject>>> = callbackFlow {
        var currentData = emptyList<JsonObject>()

        suspend fun pushRows() {
            try {
                val newRows = loadTableRows(table, dateFilter)
                currentData = newRows
                trySend(Result.success(newRows))
            } catch (e: Exception) {
                trySend(Result.failure(e))
            }
        }

        val channel = supabase.realtime.channel("backoffice-${table.name}-${UUID.randomUUID()}")
        val insertJob = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            this.table = table.name
        }.onEach { pushRows() }.launchIn(this)

        val updateJob = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            this.table = table.name
        }.onEach { pushRows() }.launchIn(this)

        val deleteJob = if (table.supportsDeleteRealtime()) {
            channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
                this.table = table.name
            }.onEach { pushRows() }.launchIn(this)
        } else {
            null
        }

        val orderItemsChannel = if (table.name == "orders") {
            supabase.realtime.channel("backoffice-order-items-${UUID.randomUUID()}")
        } else {
            null
        }
        val orderItemJobs = orderItemsChannel?.let { orderChannel ->
            listOf(
                orderChannel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    this.table = "order_items"
                }.onEach { pushRows() }.launchIn(this),
                orderChannel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                    this.table = "order_items"
                }.onEach { pushRows() }.launchIn(this),
                orderChannel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
                    this.table = "order_items"
                }.onEach { pushRows() }.launchIn(this)
            )
        }.orEmpty()

        channel.subscribe()
        orderItemsChannel?.subscribe()
        pushRows()

        awaitClose {
            insertJob.cancel()
            updateJob.cancel()
            deleteJob?.cancel()
            orderItemJobs.forEach { it.cancel() }
            launch {
                runCatching { channel.unsubscribe() }
                runCatching { supabase.realtime.removeChannel(channel) }
                if (orderItemsChannel != null) {
                    runCatching { orderItemsChannel.unsubscribe() }
                    runCatching { supabase.realtime.removeChannel(orderItemsChannel) }
                }
            }
        }
    }

    suspend fun updateOrderStatus(order: JsonObject, status: String) {
        val session = authRepository.currentSession.value ?: throw IllegalStateException("Not logged in")
        if (!session.isOwner()) {
            val rowOutletId = order["outlet_id"].toDisplayText()
            if (rowOutletId != "-" && rowOutletId != session.outletId.toString()) {
                throw IllegalStateException("You cannot update orders from another outlet")
            }
        }

        val id = order["id"]?.asFilterValue(AdminColumnType.Uuid)
            ?: throw IllegalArgumentException("Missing order id")
        val dbStatus = status.toOrderDbStatus()

        supabase.from("orders").update(
            buildJsonObject { put("status", dbStatus) }
        ) {
            filter { eq("id", id) }
        }

        notifyOrderStatusChanged(order, dbStatus)
    }

    suspend fun punchIn() {
        val session = authRepository.currentSession.value as? UserSession.EmployeeSession
            ?: throw IllegalStateException("Employee session required for attendance punch")
        
        val outlet = supabase.from("outlets")
            .select { filter { eq("id", session.outletId) } }
            .decodeSingleOrNull<Outlet>() ?: throw IllegalStateException("Outlet not found")

        val location = locationRepository.fetchLocation().getOrThrow()
        
        val distance = calculateDistance(location.first, location.second, outlet.lat, outlet.lng)
        if (distance > (outlet.radiusKm * 1000)) {
            throw IllegalStateException("You are %.1f meters away from the outlet. Please punch in within %d meters.".format(distance, (outlet.radiusKm * 1000).toInt()))
        }

        supabase.from("attendance").insert(
            buildJsonObject {
                put("emp_id", session.empId)
                put("status", "present")
                put("in_time", java.time.LocalDateTime.now().toString())
                put("in_lat", location.first)
                put("in_lng", location.second)
            }
        )
    }

    suspend fun punchOut() {
        val session = authRepository.currentSession.value as? UserSession.EmployeeSession
            ?: throw IllegalStateException("Employee session required for attendance punch")
        
        val outlet = supabase.from("outlets")
            .select { filter { eq("id", session.outletId) } }
            .decodeSingleOrNull<Outlet>() ?: throw IllegalStateException("Outlet not found")

        val location = locationRepository.fetchLocation().getOrThrow()
        
        val distance = calculateDistance(location.first, location.second, outlet.lat, outlet.lng)
        if (distance > (outlet.radiusKm * 1000)) {
            throw IllegalStateException("You are %.1f meters away from the outlet. Please punch out within %d meters.".format(distance, (outlet.radiusKm * 1000).toInt()))
        }

        val openAttendance = supabase.from("attendance")
            .select {
                filter { eq("emp_id", session.empId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<JsonObject>()
            .firstOrNull { it["out_time"].toDisplayText() == "-" }
            ?: throw IllegalStateException("No open punch-in found")

        val id = openAttendance["id"]?.asFilterValue(AdminColumnType.LongNumber)
            ?: throw IllegalStateException("Missing attendance id")

        supabase.from("attendance").update(
            buildJsonObject {
                put("status", "completed")
                put("out_time", java.time.LocalDateTime.now().toString())
                put("out_lat", location.first)
                put("out_lng", location.second)
            }
        ) {
            filter { eq("id", id) }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // Earth's radius in meters
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return r * c
    }

    private fun checkPermission(table: AdminTable, action: String) {
        val session = authRepository.currentSession.value ?: throw IllegalStateException("Not logged in")
        if (session.isOwner()) return

        val role = when (session) {
            is UserSession.OutletSession -> session.role
            is UserSession.EmployeeSession -> session.role
        } ?: throw IllegalStateException("Role not assigned")

        val canWrite = when (role) {
            OutletRole.OWNER -> true
            OutletRole.HEAD, OutletRole.MANAGER -> {
                // Heads have CRUD for own outlet tables except categories and employee
                table.name != "categories" && table.name != "employee"
            }
            OutletRole.CHEF, OutletRole.EMPLOYEE, OutletRole.KITCHEN, OutletRole.WAITER, OutletRole.CASHIER -> {
                // Employees only have CRU for orders and order items
                if (action == "delete") false
                else table.name == "orders" || table.name == "order_items" || table.name == "attendance"
            }
        }

        if (!canWrite && action != "read") {
            throw IllegalStateException("You do not have permission to $action in ${table.title}")
        }
    }

    suspend fun createRow(table: AdminTable, values: Map<String, String>) {
        checkPermission(table, "create")
        insertRow(table, values)
    }

    private suspend fun insertRow(table: AdminTable, values: Map<String, String>) {
        val payload = buildPayload(table, values, includeEmptyRequired = false, isUpdate = false)
        supabase.from(table.name).insert(payload)
    }

    suspend fun createOutlet(values: Map<String, String>) {
        val session = authRepository.currentSession.value ?: throw IllegalStateException("Not logged in")
        if (!session.isOwner()) throw IllegalStateException("Only owners can create outlets")
        insertRow(adminTables.first { it.name == "outlets" }, values)
    }

    suspend fun createOrder(values: Map<String, String>, items: List<Pair<JsonObject, Int>> = emptyList()) {
        val session = authRepository.currentSession.value ?: throw IllegalStateException("Not logged in")
        val phone = values["phone"] ?: throw IllegalArgumentException("Phone number is required")
        val customerName = values["customer_name"] ?: "Unknown Customer"
        val orderType = values["order_type"] ?: "pickup"
        val address = values["address"] ?: ""
        val outletId = values["outlet_id"]?.toLongOrNull() ?: session.outletId
        val status = (values["status"] ?: "placed").toOrderDbStatus()

        // 1. Find or create user
        var user = supabase.from("users")
            .select { filter { eq("phone", phone) } }
            .decodeSingleOrNull<JsonObject>()

        if (user == null) {
            supabase.from("users").insert(
                buildJsonObject {
                    put("name", customerName)
                    put("phone", phone)
                    put("address", address)
                }
            )
            user = supabase.from("users")
                .select { filter { eq("phone", phone) } }
                .decodeSingle<JsonObject>()
        }

        val userId = user["id"]?.toDisplayText()?.toLongOrNull() ?: throw IllegalStateException("Failed to get user ID")

        // 2. Create Order
        val tempTransactionId = "CASH_${UUID.randomUUID().toString().take(8)}"
        val employeeId = (session as? UserSession.EmployeeSession)?.empId?.toLongOrNull()

        supabase.from("orders").insert(
            buildJsonObject {
                put("outlet_id", outletId)
                put("customer_id", userId)
                put("customer_name", customerName)
                put("phone", phone)
                put("status", status)
                put("order_type", orderType)
                put("address", address)
                put("transaction_id", tempTransactionId)
                put("accepted_by", employeeId)
            }
        )

        val order = supabase.from("orders")
            .select { filter { eq("transaction_id", tempTransactionId) } }
            .decodeSingle<JsonObject>()

        val orderId = order["id"]?.toDisplayText() ?: throw IllegalStateException("Failed to get order ID")

        var totalOrderPrice = 0.0

        // 3. Create Order Items
        items.forEach { (menuItem, qty) ->
            val itemId = menuItem["id"]?.toDisplayText()?.toLongOrNull() ?: return@forEach
            val price = menuItem["price"]?.asNumber() ?: 0.0
            val discount = menuItem["discount"]?.asNumber() ?: 0.0
            val handling = menuItem["handling_charges"]?.asNumber() ?: 0.0
            
            val netPrice = (price - discount).coerceAtLeast(0.0)
            val lineTotal = (netPrice + handling) * qty
            totalOrderPrice += lineTotal

            supabase.from("order_items").insert(
                buildJsonObject {
                    put("order_id", orderId)
                    put("menu_item_id", itemId)
                    put("qty", qty)
                    put("price_per_item", netPrice)
                    put("total_price", lineTotal)
                }
            )
        }

        // Add delivery charges if applicable
        val deliveryCharges = if (orderType == "delivery") {
            items.maxOfOrNull { (it, _) -> it["delivery_charges"]?.asNumber() ?: 0.0 } ?: 0.0
        } else 0.0
        totalOrderPrice += deliveryCharges

        // 4. Create Payment
        supabase.from("payments").insert(
            buildJsonObject {
                put("order_id", orderId)
                put("customer_id", userId)
                put("amount", totalOrderPrice) // Stored as Rupees
                put("payment_status", "captured")
                put("payment_method", "cash")
                put("currency", "INR")
            }
        )
    }

    suspend fun createProduct(values: Map<String, String>) = createRow(adminTables.first { it.name == "product_catalog" }, values)
    suspend fun createUser(values: Map<String, String>) = createRow(adminTables.first { it.name == "users" }, values)
    suspend fun createCart(values: Map<String, String>) = createRow(adminTables.first { it.name == "cart" }, values)
    suspend fun createBanner(values: Map<String, String>) = createRow(adminTables.first { it.name == "banners" }, values)
    suspend fun createOffer(values: Map<String, String>) = createRow(adminTables.first { it.name == "offers" }, values)
    suspend fun createReview(values: Map<String, String>) = createRow(adminTables.first { it.name == "reviews" }, values)
    
    suspend fun createEmployee(values: Map<String, String>) {
        val session = authRepository.currentSession.value ?: throw IllegalStateException("Not logged in")
        if (!session.isOwner()) throw IllegalStateException("Only owners can create employees")
        
        insertRow(adminTables.first { it.name == "employee" }, values)
    }

    suspend fun createAttendance(values: Map<String, String>) = createRow(adminTables.first { it.name == "attendance" }, values)
    suspend fun createOrderItem(values: Map<String, String>) = createRow(adminTables.first { it.name == "order_items" }, values)
    suspend fun createOutletMenuItem(values: Map<String, String>) = createRow(adminTables.first { it.name == "outlet_menu_items" }, values)
    suspend fun createPayment(values: Map<String, String>) = createRow(adminTables.first { it.name == "payments" }, values)

    suspend fun updateRow(table: AdminTable, row: JsonObject, values: Map<String, String>) {
        checkPermission(table, "update")
        performUpdate(table, row, values)
    }

    private suspend fun performUpdate(table: AdminTable, row: JsonObject, values: Map<String, String>) {
        val id = row[table.primaryKey]?.asFilterValue(table.primaryKeyType)
            ?: throw IllegalArgumentException("Missing ${table.primaryKey}")

        val convertedValues = values.toMutableMap()
        if (table.name == "orders" && values.containsKey("status")) {
            convertedValues["status"] = values["status"]!!.toOrderDbStatus()
        }

        val payload = buildPayload(table, convertedValues, includeEmptyRequired = true, isUpdate = true)
        supabase.from(table.name).update(payload) {
            filter { eq(table.primaryKey, id) }
        }
    }

    suspend fun updateOutlet(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "outlets" }, row, values)
    suspend fun updateOrder(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "orders" }, row, values)
    suspend fun updateProduct(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "product_catalog" }, row, values)
    suspend fun updateUser(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "users" }, row, values)
    suspend fun updateCart(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "cart" }, row, values)
    suspend fun updateBanner(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "banners" }, row, values)
    suspend fun updateOffer(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "offers" }, row, values)
    suspend fun updateReview(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "reviews" }, row, values)
    
    suspend fun updateEmployee(row: JsonObject, values: Map<String, String>) {
        val session = authRepository.currentSession.value ?: throw IllegalStateException("Not logged in")
        if (!session.isOwner()) throw IllegalStateException("Only owners can update employees")
        
        performUpdate(adminTables.first { it.name == "employee" }, row, values)
    }

    suspend fun updateAttendance(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "attendance" }, row, values)
    suspend fun updateOrderItem(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "order_items" }, row, values)
    suspend fun updateOutletMenuItem(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "outlet_menu_items" }, row, values)
    suspend fun updatePayment(row: JsonObject, values: Map<String, String>) = updateRow(adminTables.first { it.name == "payments" }, row, values)

    suspend fun deleteRow(table: AdminTable, row: JsonObject) {
        checkPermission(table, "delete")
        performDelete(table, row)
    }

    private suspend fun performDelete(table: AdminTable, row: JsonObject) {
        val id = row[table.primaryKey]?.asFilterValue(table.primaryKeyType)
            ?: throw IllegalArgumentException("Missing ${table.primaryKey}")
        
        // Scope check for delete
        val session = authRepository.currentSession.value ?: throw IllegalStateException("Not logged in")
        if (!session.isOwner()) {
            val rowOutletId = row["outlet_id"].toDisplayText()
            if (rowOutletId != "-" && rowOutletId != session.outletId.toString()) {
                throw IllegalStateException("You cannot delete records from another outlet")
            }
        }

        supabase.from(table.name).delete {
            filter { eq(table.primaryKey, id) }
        }
    }

    suspend fun deleteOutlet(row: JsonObject) {
        val session = authRepository.currentSession.value ?: throw IllegalStateException("Not logged in")
        if (!session.isOwner()) throw IllegalStateException("Only owners can delete outlets")
        performDelete(adminTables.first { it.name == "outlets" }, row)
    }

    suspend fun deleteOrder(row: JsonObject) = deleteRow(adminTables.first { it.name == "orders" }, row)
    suspend fun deleteProduct(row: JsonObject) = deleteRow(adminTables.first { it.name == "product_catalog" }, row)
    suspend fun deleteUser(row: JsonObject) = deleteRow(adminTables.first { it.name == "users" }, row)
    suspend fun deleteCart(row: JsonObject) = deleteRow(adminTables.first { it.name == "cart" }, row)
    suspend fun deleteBanner(row: JsonObject) = deleteRow(adminTables.first { it.name == "banners" }, row)
    suspend fun deleteOffer(row: JsonObject) = deleteRow(adminTables.first { it.name == "offers" }, row)
    suspend fun deleteReview(row: JsonObject) = deleteRow(adminTables.first { it.name == "reviews" }, row)
    
    suspend fun deleteEmployee(row: JsonObject) {
        val session = authRepository.currentSession.value ?: throw IllegalStateException("Not logged in")
        if (!session.isOwner()) throw IllegalStateException("Only owners can delete employees")
        
        performDelete(adminTables.first { it.name == "employee" }, row)
    }

    suspend fun deleteAttendance(row: JsonObject) = deleteRow(adminTables.first { it.name == "attendance" }, row)
    suspend fun deleteOrderItem(row: JsonObject) = deleteRow(adminTables.first { it.name == "order_items" }, row)
    suspend fun deleteOutletMenuItem(row: JsonObject) = deleteRow(adminTables.first { it.name == "outlet_menu_items" }, row)
    suspend fun deletePayment(row: JsonObject) = deleteRow(adminTables.first { it.name == "payments" }, row)

    fun observeOutlets() = observeTableRows(adminTables.first { it.name == "outlets" })
    fun observeOrders(dateFilter: String? = null) = observeTableRows(adminTables.first { it.name == "orders" }, dateFilter)
    fun observeProductCatalog() = observeTableRows(adminTables.first { it.name == "product_catalog" })
    fun observeUsers() = observeTableRows(adminTables.first { it.name == "users" })
    fun observeCart() = observeTableRows(adminTables.first { it.name == "cart" })
    fun observeBanners() = observeTableRows(adminTables.first { it.name == "banners" })
    fun observeOffers() = observeTableRows(adminTables.first { it.name == "offers" })
    fun observeReviews() = observeTableRows(adminTables.first { it.name == "reviews" })
    fun observeEmployees() = observeTableRows(adminTables.first { it.name == "employee" })
    fun observeAttendance() = observeTableRows(adminTables.first { it.name == "attendance" })
    fun observePayments() = observeTableRows(adminTables.first { it.name == "payments" })
    fun observeOutletMenuItems() = observeTableRows(adminTables.first { it.name == "outlet_menu_items" })

    @OptIn(InternalAPI::class)
    suspend fun sendFcmToTopic(topic: String, title: String, body: String, imageUrl: String? = null) {
        supabase.functions.invoke("send_fcm_to_topic") {
            this.body = buildJsonObject {
                put("topic", topic)
                put("title", title)
                put("body", body)
                imageUrl?.let { put("imageUrl", it) }
            }.toString()
        }
    }

    @OptIn(InternalAPI::class)
    suspend fun sendFcmToToken(token: String, title: String, body: String, imageUrl: String? = null) {
        supabase.functions.invoke("send_fcm_to_token") {
            this.body = buildJsonObject {
                put("token", token)
                put("title", title)
                put("body", body)
                imageUrl?.let { put("imageUrl", it) }
            }.toString()
        }
    }

    fun toEditableValues(table: AdminTable, row: JsonObject?): Map<String, String> {
        return table.editableColumns.associate { column ->
            column.name to row?.get(column.name).toEditorText(column.type)
        }
    }

    private fun buildPayload(
        table: AdminTable,
        values: Map<String, String>,
        includeEmptyRequired: Boolean,
        isUpdate: Boolean
    ): JsonObject = buildJsonObject {
        val session = authRepository.currentSession.value
        val isOwner = session?.isOwner() == true

        table.columns.forEach { column ->
            if (column.name == "id" || column.name == "created_at" || column.name == "updated_at") return@forEach

            // Force outlet_id from session for non-owners
            if (column.name == "outlet_id") {
                if (isUpdate) return@forEach // Prevent changing outlet of existing record
                
                val value = if (isOwner) {
                    // Owners can specify outlet_id (from dropdown in UI if we enable it)
                    values[column.name].takeIf { !it.isNullOrBlank() } ?: session.outletId.toString()
                } else {
                    // Others are forced to their own outlet
                    session?.outletId?.toString()
                }
                if (value != null) put(column.name, column.toJsonElement(value))
                return@forEach
            }

            // Force emp_id for attendance
            if (column.name == "emp_id" && table.name == "attendance") {
                val empId = (session as? UserSession.EmployeeSession)?.empId
                if (empId != null) put(column.name, column.toJsonElement(empId))
                return@forEach
            }

            // Banner Display Order logic: automatically set if not provided or hidden
            if (column.name == "display_order" && table.name == "banners" && !isUpdate) {
                // If it's a new banner, we can default to 0 or something unique.
                // The user said "automatically fetched by the user id", but it's a long number.
                // We'll set a default of 0 and let them re-order later if needed, 
                // or use a timestamp for sequence.
                val autoOrder = values[column.name]?.toLongOrNull() ?: 0L
                put(column.name, column.toJsonElement(autoOrder.toString()))
                return@forEach
            }

            // Protect other ID fields if they are not editable
            if (!column.editable && column.name.endsWith("_id")) {
                // If it's a reference and not editable, it might be auto-filled from context
                // (e.g. order_id when creating order_items)
                val contextValue = values[column.name]
                if (!contextValue.isNullOrBlank()) {
                    put(column.name, column.toJsonElement(contextValue))
                }
                return@forEach
            }

            if (column.editable) {
                // Skip virtual columns for standard DB operations
                if (table.name == "orders" && (column.name == "menu_item_id" || column.name == "qty")) return@forEach

                val raw = values[column.name].orEmpty().trim()
                    .ifBlank { defaultValueFor(table.name, column.name).orEmpty() }
                if (raw.isBlank() && !column.required && !includeEmptyRequired) return@forEach
                put(column.name, column.toJsonElement(raw))
            }
        }
    }

    private fun defaultValueFor(tableName: String, columnName: String): String? {
        if (columnName != "outlet_id") return null
        val session = authRepository.currentSession.value ?: return null
        if (session.isOwner()) return null
        return when (tableName) {
            "orders", "outlet_menu_items", "banners", "offers", "employee", "reviews" -> session.outletId.toString()
            else -> null
        }
    }

    suspend fun notifySavedRecord(table: AdminTable, row: JsonObject?, values: Map<String, String>) {
        when (table.name) {
            "offers" -> sendFcmToTopic(
                topic = "offers",
                title = values["title"].orEmpty().ifBlank { "New offer" },
                body = values["description"].orEmpty().ifBlank { "A new FoodyVilla offer is available." },
                imageUrl = values["img_url"].orEmpty().extractUrl()
            )
            "banners" -> sendFcmToTopic(
                topic = "banners",
                title = values["title"].orEmpty().ifBlank { "FoodyVilla update" },
                body = "A new store banner is live.",
                imageUrl = values["img_url"].orEmpty().extractUrl()
            )
            "orders" -> row?.let {
                notifyOrderStatusChanged(it, values["status"].orEmpty().ifBlank { it["status"].toDisplayText() })
            }
        }
    }

    private suspend fun notifyOrderStatusChanged(order: JsonObject, status: String) {
        val message = "Your order is now ${status.replace("_", " ").lowercase()}."
        val customerToken = order["customer_id"]?.asFilterValue(AdminColumnType.LongNumber)?.let { customerId ->
            supabase.from("users")
                .select {
                    filter { eq("id", customerId) }
                }
                .decodeSingleOrNull<JsonObject>()
                ?.get("fcm_token")
                .toDisplayText()
                .takeIf { it != "-" }
        }

        if (!customerToken.isNullOrBlank()) {
            sendFcmToToken(
                token = customerToken,
                title = "Order update",
                body = message
            )
        }

        sendFcmToTopic(
            topic = "order_updates",
            title = "Order status changed",
            body = "Order #${order["id"].toDisplayText().take(8)} moved to $status"
        )
    }

    @OptIn(InternalAPI::class)
    suspend fun sendNotificationToCart(title: String, body: String) {
        supabase.functions.invoke("send_notification_to_cart") {
            this.body = buildJsonObject {
                put("title", title)
                put("body", body)
            }.toString()
        }
    }

    private fun AdminColumn.toJsonElement(raw: String): JsonElement {
        if (raw.isBlank()) return JsonNull
        return when (type) {
            AdminColumnType.LongNumber -> JsonPrimitive(raw.toLongOrNull())
            AdminColumnType.DecimalNumber -> JsonPrimitive(raw.toDoubleOrNull())
            AdminColumnType.Boolean -> JsonPrimitive(raw.equals("true", ignoreCase = true) || raw == "1" || raw.equals("yes", ignoreCase = true))
            AdminColumnType.TextArray -> parseArray(raw)
            AdminColumnType.Json -> runCatching { json.parseToJsonElement(raw) }.getOrElse { JsonPrimitive(raw) }
            AdminColumnType.Text,
            AdminColumnType.Timestamp,
            AdminColumnType.Date,
            AdminColumnType.Uuid -> JsonPrimitive(raw)
        }
    }

    private fun parseArray(raw: String): JsonArray {
        return if (raw.startsWith("[")) {
            runCatching { json.parseToJsonElement(raw) as JsonArray }
                .getOrDefault(JsonArray(emptyList()))
        } else {
            buildJsonArray {
                raw.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .forEach { add(JsonPrimitive(it)) }
            }
        }
    }
}

private fun AdminTable.selectColumns(): Columns {
    return when (name) {
        "outlet_menu_items" -> Columns.raw("*, product_catalog(*)")
        "order_items" -> Columns.raw("*, orders(customer_name, phone, status, outlet_id), outlet_menu_items(id, image, price, product_catalog(name, category, description))")
        "attendance" -> Columns.raw("*, employee(name, role, contact, outlet_id)")
        "orders" -> Columns.raw("*, outlets(name, city), users(name, phone), payments(amount)")
        "payments" -> Columns.raw("*, orders(customer_name, phone, status, outlet_id), users(name, phone)")
        "reviews" -> Columns.raw("*, users(name, phone), outlet_menu_items(id, price, product_catalog(name, category)), outlets(name, city)")
        "cart" -> Columns.raw("*, users(name, phone), outlet_menu_items(id, price, product_catalog(name, category)), outlets(name, city)")
        else -> Columns.ALL
    }
}

private fun String.defaultOrderBy(): String {
    return when (this) {
        "product_catalog", "outlets", "employee", "users" -> "id"
        else -> "created_at"
    }
}

private fun JsonObject.withDisplayJoins(tableName: String): JsonObject {
    val additions = buildMap<String, JsonElement> {
        when (tableName) {
            "outlet_menu_items" -> {
                val product = this@withDisplayJoins["product_catalog"] as? JsonObject
                product?.get("name")?.let { put("product_name", it) }
                product?.get("category")?.let { put("product_category", it) }
                product?.get("description")?.let { put("product_description", it) }
                product?.get("is_veg")?.let { put("product_is_veg", it) }
                product?.get("prep_time")?.let { put("product_prep_time", it) }
            }
            "order_items" -> {
                val order = this@withDisplayJoins["orders"] as? JsonObject
                val menuItem = this@withDisplayJoins["outlet_menu_items"] as? JsonObject
                val product = menuItem?.get("product_catalog") as? JsonObject
                val orderCustomer = order?.get("customer_name").toDisplayText()
                val orderPhone = order?.get("phone").toDisplayText()
                put("order_label", JsonPrimitive(listOf(orderCustomer, orderPhone).filter { it != "-" }.joinToString(" - ").ifBlank { this@withDisplayJoins["order_id"].toDisplayText() }))
                product?.get("name")?.let { put("product_name", it) }
                product?.get("category")?.let { put("product_category", it) }
                menuItem?.get("image")?.let { put("image", it) }
            }
            "attendance" -> {
                val employee = this@withDisplayJoins["employee"] as? JsonObject
                employee?.get("name")?.let { put("employee_name", it) }
                employee?.get("role")?.let { put("employee_role", it) }
                employee?.get("contact")?.let { put("employee_contact", it) }
            }
            "orders" -> {
                val payments = this@withDisplayJoins["payments"] as? JsonArray
                val firstPayment = payments?.firstOrNull() as? JsonObject
                firstPayment?.get("amount")?.let { put("grand_total", it) }
            }
            "payments" -> {
                val order = this@withDisplayJoins["orders"] as? JsonObject
                order?.get("customer_name")?.let { put("order_customer", it) }
                order?.get("status")?.let { put("order_status", it) }
            }
            "cart" -> {
                val user = this@withDisplayJoins["users"] as? JsonObject
                val outlet = this@withDisplayJoins["outlets"] as? JsonObject
                val menuItem = this@withDisplayJoins["outlet_menu_items"] as? JsonObject
                val product = menuItem?.get("product_catalog") as? JsonObject
                user?.get("name")?.let { put("customer_name", it) }
                user?.get("phone")?.let { put("customer_phone", it) }
                user?.get("fcm_token")?.let { put("customer_fcm", it) }
                outlet?.get("name")?.let { put("outlet_name", it) }
                product?.get("name")?.let { put("product_name", it) }
                menuItem?.get("price")?.let { put("product_price", it) }
                menuItem?.get("image")?.let { put("product_image", it) }
            }
        }
    }
    return if (additions.isEmpty()) this else JsonObject(this + additions)
}

private fun AdminTable.storageBucketCandidates(): List<String> {
    val preferred = when (name) {
        "reviews" -> listOf("review", "reviews")
        "product_catalog" -> listOf("products", "product")
        "banners" -> listOf("banners", "banner")
        "offers" -> listOf("offers", "offer")
        "employee" -> listOf("employee", "employees")
        "users" -> listOf("users", "profile")
        else -> listOf(name)
    }
    return (preferred + listOf("images", "uploads", "review")).distinct()
}

private fun AdminTable.supportsDeleteRealtime(): Boolean {
    // Some tables might not have DELETE events enabled in Supabase Realtime config
    // or we may want to avoid tracking deletions for these tables in realtime.
    return when (name) {
        "attendance", "outlets", "payments", "users", "employee" -> false
        else -> true
    }
}

private fun UserSession.isOwner(): Boolean {
    return roleOrNull() == OutletRole.OWNER
}

private fun UserSession.roleOrNull(): OutletRole? {
    return when (this) {
        is UserSession.OutletSession -> role
        is UserSession.EmployeeSession -> role
    }
}

private fun String.extractUrl(): String? {
    val trimmed = trim().trim('"')
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    return Regex("""https?://[^\s,\]"}]+""").find(trimmed)?.value?.trimEnd('.', ')')
}

private fun String.toOrderDbStatus(): String = when (val s = trim().lowercase().replace(" ", "_")) {
    "placed" -> "pending"
    "delivered" -> "completed"
    else -> s
}

fun JsonElement?.toDisplayText(): String {
    return when (this) {
        null, JsonNull -> "-"
        is JsonPrimitive -> contentOrNull ?: toString()
        is JsonArray -> joinToString(", ") { it.toDisplayText() }
        is JsonObject -> entries.take(3).joinToString(", ") { "${it.key}: ${it.value.toDisplayText()}" }
    }
}

private fun JsonElement?.toEditorText(type: AdminColumnType): String {
    if (this == null || this is JsonNull) return ""
    return when (type) {
        AdminColumnType.TextArray -> when (this) {
            is JsonArray -> joinToString(", ") { it.jsonPrimitive.contentOrNull ?: it.toString() }
            else -> toString()
        }
        AdminColumnType.Json -> toString()
        else -> toDisplayText().takeIf { it != "-" }.orEmpty()
    }
}

private fun JsonElement.asFilterValue(type: AdminColumnType): Any {
    val value = jsonPrimitive.contentOrNull ?: toString()
    return when (type) {
        AdminColumnType.LongNumber -> value.toLongOrNull() ?: value
        AdminColumnType.DecimalNumber -> value.toDoubleOrNull() ?: value
        AdminColumnType.Boolean -> value.toBooleanStrictOrNull() ?: value
        else -> value
    }
}
