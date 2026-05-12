package com.jp.foodyvilla_backoffice.data.repo

import android.content.Context
import android.net.Uri
import com.jp.foodyvilla_backoffice.data.utils.compressImage
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumn
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumnType
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminTable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
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
    private val supabase: SupabaseClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun loadRows(table: AdminTable): List<JsonObject> {
        return supabase.from(table.name)
            .select {
                order(table.orderBy, Order.DESCENDING)
            }
            .decodeList<JsonObject>()
    }

    suspend fun loadOrderItems(): List<JsonObject> {
        return supabase.from("order_items")
            .select {
                order("created_at", Order.ASCENDING)
            }
            .decodeList<JsonObject>()
    }

    suspend fun loadProducts(): List<JsonObject> {
        return supabase.from("products")
            .select {
                order("name", Order.ASCENDING)
            }
            .decodeList<JsonObject>()
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

        val deleteJob = channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
            this.table = table.name
        }.onEach { pushRows() }.launchIn(this)

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
            deleteJob.cancel()
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

        supabase.from("orders").update(
            buildJsonObject { put("status", status) }
        ) {
            filter { eq("id", id) }
        }

        notifyOrderStatusChanged(order, status)
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
            if (raw.isBlank() && !column.required && !includeEmptyRequired) return@forEach
            put(column.name, column.toJsonElement(raw))
        }
    }

    suspend fun notifySavedRecord(table: AdminTable, row: JsonObject?, values: Map<String, String>) {
        when (table.name) {
            "offers" -> sendFcmToTopic(
                topic = "offers",
                title = values["title"].orEmpty().ifBlank { "New offer" },
                body = values["desc"].orEmpty().ifBlank { "A new FoodyVilla offer is available." },
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

private fun AdminTable.storageBucketCandidates(): List<String> {
    val preferred = when (name) {
        "reviews" -> listOf("review", "reviews")
        "products" -> listOf("products", "product")
        "banners" -> listOf("banners", "banner")
        "offers" -> listOf("offers", "offer")
        "employee" -> listOf("employee", "employees")
        "users" -> listOf("users", "profile")
        else -> listOf(name)
    }
    return (preferred + listOf("images", "uploads", "review")).distinct()
}

private fun String.extractUrl(): String? {
    val trimmed = trim().trim('"')
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    return Regex("""https?://[^\s,\]"}]+""").find(trimmed)?.value?.trimEnd('.', ')')
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
