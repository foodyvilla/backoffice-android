package com.jp.foodyvilla_backoffice.data.repo

import android.content.Context
import android.net.Uri
import com.jp.foodyvilla_backoffice.data.utils.compressImage
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumn
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumnType
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminTable
import com.jp.foodyvilla_backoffice.data.model.backoffice.adminTables
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.OutletRole
import com.jp.foodyvilla_backoffice.domain.security.UserSession
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
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun loadRows(table: AdminTable): List<JsonObject> {
        val rows = supabase.from(table.name)
            .select(table.selectColumns()) {
                order(table.orderBy, Order.DESCENDING)
            }
            .decodeList<JsonObject>()
        return scopeRows(table.name, rows).map { row -> row.withDisplayJoins(table.name) }
    }

    suspend fun loadOrderItems(): List<JsonObject> {
        val rows = supabase.from("order_items")
            .select {
                order("created_at", Order.ASCENDING)
            }
            .decodeList<JsonObject>()
        val scopedOrderIds = loadRowsBypassScope("orders")
            .let { scopeRows("orders", it) }
            .map { it["id"].toDisplayText() }
            .toSet()
        return rows.filter { it["order_id"].toDisplayText() in scopedOrderIds }
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
                filter { eq("customer_id", customerId.toLongOrNull() ?: 0L) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<JsonObject>()
        return rows.map { it.withDisplayJoins("orders") }
    }

    suspend fun loadCustomerCart(customerId: String): List<JsonObject> {
        val rows = supabase.from("cart")
            .select(adminTables.first { it.name == "cart" }.selectColumns()) {
                filter { eq("customer_id", customerId.toLongOrNull() ?: 0L) }
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

    fun observeRows(table: AdminTable): Flow<Result<List<JsonObject>>> = callbackFlow {
        suspend fun pushRows() {
            trySend(runCatching { loadRows(table) })
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
        val location = locationRepository.fetchLocation().getOrThrow()

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
        val location = locationRepository.fetchLocation().getOrThrow()
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

    suspend fun createRow(table: AdminTable, values: Map<String, String>) {
        val payload = buildPayload(table, values, includeEmptyRequired = false)
        supabase.from(table.name).insert(payload)
    }

    suspend fun updateRow(table: AdminTable, row: JsonObject, values: Map<String, String>) {
        val id = row[table.primaryKey]?.asFilterValue(table.primaryKeyType)
            ?: throw IllegalArgumentException("Missing ${table.primaryKey}")
        val payload = buildPayload(table, values, includeEmptyRequired = true)
        supabase.from(table.name).update(payload) {
            filter { eq(table.primaryKey, id) }
        }
    }

    suspend fun deleteRow(table: AdminTable, row: JsonObject) {
        val id = row[table.primaryKey]?.asFilterValue(table.primaryKeyType)
            ?: throw IllegalArgumentException("Missing ${table.primaryKey}")
        supabase.from(table.name).delete {
            filter { eq(table.primaryKey, id) }
        }
    }

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
        includeEmptyRequired: Boolean
    ): JsonObject = buildJsonObject {
        table.editableColumns.forEach { column ->
            val raw = values[column.name].orEmpty().trim()
                .ifBlank { defaultValueFor(table.name, column.name).orEmpty() }
            if (raw.isBlank() && !column.required && !includeEmptyRequired) return@forEach
            put(column.name, column.toJsonElement(raw))
        }
    }

    private fun defaultValueFor(tableName: String, columnName: String): String? {
        if (columnName != "outlet_id") return null
        val session = authRepository.currentSession.value ?: return null
        if (session.roleOrNull() == OutletRole.OWNER) return null
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
        "orders" -> Columns.raw("*, outlets(name, city), users(name, phone)")
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

private fun String.toOrderDbStatus(): String = when (trim().lowercase().replace(" ", "_")) {
    "placed" -> "pending"
    "cancelled" -> "rejected"
    "delivered" -> "completed"
    else -> trim().lowercase().replace(" ", "_")
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
