package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NutritionalInfo(
    val protein: String = "",
    val energy: String = "",
    val carbs: String = "",
    val fat: String = ""
)
@Serializable
data class FoodItem(
    val id: Int = 0,
    val createdAt: String = "",           // timestamptz
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discount: Int = 0,                // int4
    val image: List<String> = emptyList(), // _jsonb (array)
    val category: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val prepTime: String = "",
    val review: List<Review> = emptyList(), // _jsonb (array)
    val nutritionalInfo: NutritionalInfo = NutritionalInfo(),
    val isVeg: Boolean = true,
    val isVegan: Boolean = false,
    val isBestSeller: Boolean = false
)





@Serializable
data class Category(
    val id: Long? = null,
    val name: String = "",
    val emoji: String = "",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Review(
    val id: Int = 0,
    val userName: String = "",
    val title: String = "",
    val rating: Double = 0.0,
    val desc: String = "",
    val date: String = "",
    val foodItem: String = "",
    val img_url: List<String> = emptyList() // ✅ NEW
)

