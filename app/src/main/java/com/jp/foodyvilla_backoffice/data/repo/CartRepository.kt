package com.jp.foodyvilla_backoffice.data.repo

import com.jp.foodyvilla_backoffice.data.model.cart.CartItem
import com.jp.foodyvilla_backoffice.data.model.user.UserProfile
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CartRepository(
    private val supabase: SupabaseClient
)
{

    // 🔐 Get auth user id from token
    private suspend fun getAuthUserId(): String? {
        return supabase.auth.currentSessionOrNull()?.user?.id
    }

    // 🧩 Map auth user → users table → customer_id
    private suspend fun getCustomerId(): String? {

        val authId = getAuthUserId() ?: return null

        val user = supabase.postgrest["users"]
            .select {
                filter {
                    eq("auth_user_id", authId)
                }
            }
            .decodeSingleOrNull<UserProfile>()

        return user?.id
    }

    // ➕ Add to cart
    fun addToCart(productId: Int, qty: Int = 1): Flow<UiState<String>> = flow {
        emit(UiState.Loading)

        try {
            val customerId = getCustomerId()
                ?: throw Exception("User not found")

            val existing = supabase.postgrest["cart"]
                .select {
                    filter {
                        eq("customer_id", customerId)
                        eq("product_id", productId)
                    }
                }
                .decodeList<CartItem>()

            if (existing.isEmpty()) {

                // ➕ INSERT only if qty > 0
                if (qty > 0) {
                    supabase.postgrest["cart"].insert(
                        mapOf(
                            "customer_id" to customerId,
                            "product_id" to productId,
                            "qty" to qty
                        )
                    )
                }

            } else {

                val cartItem = existing.first()

                if (qty == 0) {
                    // ❌ DELETE
                    supabase.postgrest["cart"]
                        .delete {
                            filter {
                                eq("id", cartItem.id)
                            }
                        }

                } else {
                    // 🔄 UPDATE (set qty directly)
                    supabase.postgrest["cart"]
                        .update(
                            mapOf("qty" to qty)
                        ) {
                            filter {
                                eq("id", cartItem.id)
                            }
                        }
                }
            }

            emit(UiState.Success("Cart updated"))

        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Unknown error")))
        }
    }

    // 📥 Get cart items
    fun getCartItems(): Flow<UiState<List<CartItem>>> = flow {
        emit(UiState.Loading)

        try {
            val customerId = getCustomerId()
                ?: throw Exception("User not found")

            val items = supabase.postgrest["cart"]
                .select(
                    Columns.raw(
                        """
                        id,
                        customer_id,
                        product_id,
                                qty,
                        products (*)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("customer_id", customerId)
                    }
                }
                .decodeList<CartItem>()

            emit(UiState.Success(items))

        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Failed to load cart")))
        }
    }

    // ❌ Remove item
    fun removeFromCart(cartId: Int): Flow<UiState<String>> = flow {
        emit(UiState.Loading)

        try {
            supabase.postgrest["cart"]
                .delete {
                    filter {
                        eq("id", cartId)
                    }
                }

            emit(UiState.Success("Removed from cart"))

        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Delete failed")))
        }
    }

    // 🧹 Clear cart
    fun clearCart(): Flow<UiState<String>> = flow {
        emit(UiState.Loading)

        try {
            val customerId = getCustomerId()
                ?: throw Exception("User not found")

            supabase.postgrest["cart"]
                .delete {
                    filter {
                        eq("customer_id", customerId)
                    }
                }

            emit(UiState.Success("Cart cleared"))

        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Clear failed")))
        }
    }
}