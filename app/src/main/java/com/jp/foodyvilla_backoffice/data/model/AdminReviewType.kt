package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Serializable
enum class AdminReviewType {
    ORDER, PRODUCT, OUTLET
}

@Serializable
data class ReviewAdminResponse(
    val id: Long? = null,
    val created_at: String? = null,
    val customer_id: Long? = null,
    val review_type: String? = null,
    val order_id: String? = null, // UUID string format mapping
    val menu_item_id: Long? = null,
    val outlet_id: Long? = null,
    val rating: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val img_url: JsonElement? = null // Decoded as JsonElement to handle jsonb flexibility (string, array, etc.)
)

data class ReviewAdminUiModel(
    val id: Long = 0L,
    val customerId: Long,
    val customerName: String = "Anonymous Customer",
    val reviewType: AdminReviewType = AdminReviewType.PRODUCT,
    val orderId: String = "",
    val menuItemId: String = "",
    val outletId: String = "",
    val rating: Int = 5,
    val title: String = "",
    val description: String = "",
    val images: List<String> = emptyList()
)

fun ReviewAdminResponse.toUiModel(customerName: String) = ReviewAdminUiModel(
    id = id ?: 0L,
    customerId = customer_id ?: 0L,
    customerName = customerName,
    reviewType = when (review_type?.lowercase()) {
        "order" -> AdminReviewType.ORDER
        "outlet" -> AdminReviewType.OUTLET
        else -> AdminReviewType.PRODUCT
    },
    orderId = order_id.orEmpty(),
    menuItemId = menu_item_id?.toString().orEmpty(),
    outletId = outlet_id?.toString().orEmpty(),
    rating = rating?.toInt()?.coerceIn(1, 5) ?: 5,
    title = title.orEmpty(),
    description = description.orEmpty(),
    images = try {
        img_url?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
    } catch (e: Exception) {
        try {
            img_url?.jsonPrimitive?.content?.let { listOf(it) } ?: emptyList()
        } catch (e2: Exception) {
            emptyList()
        }
    }
)