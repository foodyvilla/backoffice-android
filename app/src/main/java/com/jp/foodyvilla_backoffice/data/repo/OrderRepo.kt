package com.jp.foodyvilla_backoffice.data.repo

import com.jp.foodyvilla_backoffice.data.model.cart.CartItem
import com.jp.foodyvilla_backoffice.data.model.order.*
import com.jp.foodyvilla_backoffice.data.model.user.UserProfile
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.query.Columns

class OrderRepo(
    private val supabase: SupabaseClient
) {

    // 🔐 Get customer_id from token
    private suspend fun getCustomerId(): String? {
        val authId = supabase.auth.currentUserOrNull()?.id ?: return null

        val user = supabase.postgrest["users"]
            .select {
                filter { eq("auth_user_id", authId) }
            }
            .decodeSingleOrNull<UserProfile>()

        return user?.id
    }

    // 🚀 PLACE ORDER
    fun placeOrder(
        cartItems: List<CartItem>,
        address: String,
        phone: String,
        customerName: String,
        instruction: String? = null,
        lat: Double? = null,
        long: Double? = null,
        orderType: String? = null,
        transactionId: String? = null
    ): Flow<UiState<String>> = flow {

        emit(UiState.Loading)

        try {
            val customerId = getCustomerId()
                ?: throw Exception("User not found")

            val order = supabase.postgrest["orders"]
                .insert(
                    OrderInsert(
                        customer_id = customerId,
                        status = "PLACED",
                        address = address,
                        phone = phone,
                        customer_name = customerName,
                        order_type = orderType,
                        instruction = instruction,
                        transaction_id = transactionId,
                        delivery_lat = lat,
                        delivery_long = long
                    )
                ) { select() }
                .decodeSingle<OrderModel>()

            val orderItems = cartItems.map {
                OrderItemInsert(
                    order_id = order.id,
                    productid = it.product_id,
                    qty = it.qty ?: 1,
                    price_per_item = it.products?.price ?: 0.0,
                    total_price = (it.products?.price ?: 0.0) * (it.qty ?: 1),
                    total_discount = 0.0
                )
            }

            supabase.postgrest["order_items"].insert(orderItems)

            supabase.postgrest["cart"]
                .delete { filter { eq("customer_id", customerId) } }

            emit(UiState.Success(order.id))

        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Order failed")))
        }
    }

    // ===========================
    // 🔥 REALTIME ORDERS (FIXED)
    // ===========================
    fun observeOrders(): Flow<UiState<List<OrderModel>>> = callbackFlow {

        try {
            val authId = supabase.auth.currentUserOrNull()?.id
            if (authId == null) {
                trySend(UiState.Error(Exception("User not logged in")))
                close()
                return@callbackFlow
            }

            val user = supabase.postgrest["users"]
                .select { filter { eq("auth_user_id", authId) } }
                .decodeSingleOrNull<UserProfile>()

            val customerId = user?.id
            if (customerId == null) {
                trySend(UiState.Error(Exception("Customer record not found")))
                close()
                return@callbackFlow
            }

            val channel = supabase.realtime.channel("orders-channel")

            // ✅ Attach listener BEFORE subscribe
            val job = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "orders"
            }.onEach {
                try {
                    val orders = supabase.postgrest["orders"]
                        .select {
                            filter { eq("customer_id", customerId) }
                            order("created_at", Order.DESCENDING)
                        }
                        .decodeList<OrderModel>()

                    trySend(UiState.Success(orders))
                } catch (e: Exception) {
                    trySend(UiState.Error(e))
                }
            }.launchIn(this)

            channel.subscribe()

            // ✅ Initial load
            val initial = supabase.postgrest["orders"]
                .select(
                    Columns.raw(
                        "*, order_items(*, products(name, image))"
                    )
                ) {
                    filter { eq("customer_id", customerId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<OrderModel>()

            trySend(UiState.Success(initial))

            awaitClose {
                job.cancel()
                launch {
                    channel.unsubscribe()
                    supabase.realtime.removeChannel(channel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            trySend(UiState.Error(e))
            close(e)
        }
    }

    // ===================================
    // 🔥 REALTIME ORDER ITEMS (FIXED)
    // ===================================
    fun observeOrderItems(orderId: String): Flow<List<OrderItem>> = callbackFlow {

        val channel = supabase.realtime.channel("order-items-$orderId")

        val job = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "order_items"
        }.onEach {
            val items = supabase.postgrest["order_items"]
                .select { filter { eq("order_id", orderId) } }
                .decodeList<OrderItem>()

            trySend(items)
        }.launchIn(this)

        channel.subscribe()

        // ✅ Initial load
        val initial = supabase.postgrest["order_items"]
            .select { filter { eq("order_id", orderId) } }
            .decodeList<OrderItem>()

        trySend(initial)

        awaitClose {
            job.cancel()
            launch {
                channel.unsubscribe()
                supabase.realtime.removeChannel(channel)
            }
        }
    }
}