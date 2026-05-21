package com.jp.foodyvilla_backoffice.data.repo

import com.jp.foodyvilla_backoffice.data.model.FoodItem
import com.jp.foodyvilla_backoffice.data.model.NutritionalInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ProductRepo(private val client : SupabaseClient) {

    fun getProducts(): Flow<List<FoodItem>> = flow {
        try {
            val res = client
                .from("outlet_menu_items")
                .select(Columns.raw("*, product_catalog(*)"))
                .decodeList<JsonObject>()
                .mapNotNull { it.toFoodItem() }

            emit(res)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun getProductById(id: Int): Flow<FoodItem?> = flow {
        try {
            val res = client
                .from("outlet_menu_items")
                .select(Columns.raw("*, product_catalog(*)")) {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<JsonObject>()
                ?.toFoodItem()

            emit(res)

        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }
}

private fun JsonObject.toFoodItem(): FoodItem? {
    val product = this["product_catalog"] as? JsonObject ?: JsonObject(emptyMap())
    val id = longText("id").toIntOrNull() ?: product.longText("id").toIntOrNull() ?: return null
    val imageUrls = this["image"].asUrlList()

    return FoodItem(
        id = id,
        createdAt = text("created_at"),
        name = product.text("name", text("name")),
        description = product.text("description", text("description")),
        price = number("price"),
        discount = number("discount").toInt(),
        image = imageUrls,
        category = product.text("category"),
        rating = number("rating"),
        reviewsCount = longText("reviews_count").toIntOrNull() ?: 0,
        prepTime = product.text("prep_time"),
        nutritionalInfo = product["nutritional_info"].toNutritionalInfo(),
        isVeg = product.boolean("is_veg", true),
        isVegan = product.boolean("is_vegan", false),
        isBestSeller = product.boolean("is_bestseller", false)
    )
}

private fun JsonObject.text(name: String, fallback: String = ""): String {
    return this[name]?.asPlainText()?.takeIf { it.isNotBlank() && it != "-" } ?: fallback
}

private fun JsonObject.longText(name: String): String = text(name)

private fun JsonObject.number(name: String): Double {
    return this[name]?.jsonPrimitive?.doubleOrNull ?: text(name).toDoubleOrNull() ?: 0.0
}

private fun JsonObject.boolean(name: String, fallback: Boolean): Boolean {
    return this[name]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: fallback
}

private fun JsonElement?.toNutritionalInfo(): NutritionalInfo {
    val obj = this as? JsonObject ?: return NutritionalInfo()
    return NutritionalInfo(
        protein = obj.text("protein"),
        energy = obj.text("energy"),
        carbs = obj.text("carbs"),
        fat = obj.text("fat")
    )
}

private fun JsonElement?.asUrlList(): List<String> {
    return when (this) {
        null, JsonNull -> emptyList()
        is JsonArray -> mapNotNull { it.extractUrl() }
        is JsonObject -> values.mapNotNull { it.extractUrl() }
        is JsonPrimitive -> contentOrNull
            ?.split(",")
            ?.mapNotNull { it.extractUrl() }
            .orEmpty()
    }
}

private fun JsonElement?.extractUrl(): String? = asPlainText().extractUrl()

private fun JsonElement?.asPlainText(): String {
    return when (this) {
        null, JsonNull -> ""
        is JsonPrimitive -> contentOrNull ?: toString()
        is JsonArray -> joinToString(",") { it.asPlainText() }
        is JsonObject -> values.joinToString(",") { it.asPlainText() }
    }
}

private fun String.extractUrl(): String? {
    val cleaned = trim()
        .trim('"', '[', ']')
        .replace("\\/", "/")
        .replace("\\u0026", "&")
        .replace("\\\"", "\"")
    if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) return cleaned
    return Regex("""https?://[^\s,\]"}]+""").find(cleaned)?.value?.trimEnd('.', ')')
}
