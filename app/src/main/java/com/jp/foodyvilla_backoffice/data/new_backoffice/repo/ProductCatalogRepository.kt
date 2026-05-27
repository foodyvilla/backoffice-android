package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCatalogResponse
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCatalogUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCategoryResponse
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCategoryUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.toUiModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProductCatalogRepository(private val supabase: SupabaseClient) {

    // ==========================================
    // Product Catalog Table CRUD Actions
    // ==========================================
    suspend fun getAllCatalogProducts(): List<ProductCatalogUiModel> {
        return supabase.from("product_catalog")
            .select(Columns.raw("*, categories(name)")) {
                order("name", order = Order.ASCENDING)
            }
            .decodeList<ProductCatalogResponse>()
            .map { it.toUiModel() }
    }

    suspend fun createCatalogProduct(product: ProductCatalogUiModel) {
        supabase.from("product_catalog").insert(buildJsonObject {
            put("name", product.name.trim())
            put("description", product.description.trim())
            put("is_veg", product.isVeg)
            put("is_vegan", product.isVegan)
            put("is_bestseller", product.isBestseller)
            put("prep_time", product.prepTime.trim())
            if (product.categoryId != null) put("category_id", product.categoryId)
        })
    }

    suspend fun updateCatalogProduct(product: ProductCatalogUiModel) {
        supabase.from("product_catalog").update(buildJsonObject {
            put("name", product.name.trim())
            put("description", product.description.trim())
            put("is_veg", product.isVeg)
            put("is_vegan", product.isVegan)
            put("is_bestseller", product.isBestseller)
            put("prep_time", product.prepTime.trim())
            put("category_id", product.categoryId)
        }) {
            filter { eq("id", product.id) }
        }
    }

    suspend fun deleteCatalogProduct(productId: Long) {
        supabase.from("product_catalog").delete {
            filter { eq("id", productId) }
        }
    }

    // ==========================================
    // Categories Table CRUD Actions
    // ==========================================
    suspend fun getAllCategories(): List<ProductCategoryUiModel> {
        return supabase.from("categories")
            .select { order("name", order = Order.ASCENDING) }
            .decodeList<ProductCategoryResponse>()
            .map { it.toUiModel() }
    }

    suspend fun createCategory(category: ProductCategoryUiModel) {
        supabase.from("categories").insert(buildJsonObject {
            put("name", category.name.trim())
            put("emoji", category.emoji.trim())
            put("is_active", category.isActive)
        })
    }

    suspend fun updateCategory(category: ProductCategoryUiModel) {
        supabase.from("categories").update(buildJsonObject {
            put("name", category.name.trim())
            put("emoji", category.emoji.trim())
            put("is_active", category.isActive)
        }) {
            filter { eq("id", category.id) }
        }
    }

    suspend fun deleteCategory(categoryId: Long) {
        supabase.from("categories").delete {
            filter { eq("id", categoryId) }
        }
    }
}