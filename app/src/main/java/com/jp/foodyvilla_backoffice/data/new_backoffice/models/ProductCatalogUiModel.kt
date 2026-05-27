package com.jp.foodyvilla_backoffice.data.new_backoffice.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductCatalogUiModel(
    val id: Long = 0L,
    val name: String,
    val description: String,
    val categoryId: Long?,
    val isVeg: Boolean,
    val isVegan: Boolean,
    val isBestseller: Boolean,
    val prepTime: String,
    val categoryName: String = ""
)

@Serializable
data class ProductCatalogResponse(
    val id: Long = 0L,
    val name: String,
    val description: String? = null,
    val is_veg: Boolean = true,
    val is_vegan: Boolean = false,
    val is_bestseller: Boolean = false,
    val prep_time: String? = null,
    val category_id: Long? = null,
    val categories: InnerCategoryNameJoin? = null
)

@Serializable
data class InnerCategoryNameJoin(val name: String)

fun ProductCatalogResponse.toUiModel() = ProductCatalogUiModel(
    id = id, name = name, description = description.orEmpty(),
    categoryId = category_id, isVeg = is_veg, isVegan = is_vegan,
    isBestseller = is_bestseller, prepTime = prep_time.orEmpty(),
    categoryName = categories?.name ?: "Unassigned"
)

// --- CATEGORY DATA BOUNDS ---
@Serializable
data class ProductCategoryUiModel(
    val id: Long = 0L,
    val name: String,
    val emoji: String,
    val isActive: Boolean = true
)

@Serializable
data class ProductCategoryResponse(
    val id: Long = 0L,
    val name: String,
    val emoji: String,
    val is_active: Boolean = true
)

fun ProductCategoryResponse.toUiModel() = ProductCategoryUiModel(
    id = id, name = name, emoji = emoji, isActive = is_active
)