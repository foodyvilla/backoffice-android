package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import com.jp.foodyvilla_backoffice.data.new_backoffice.models.CategoryDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OrderDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OrderItemInsertDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OrderItemWithMenuDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletMenuItemWithProductDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.RestaurantTableDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder

class TableManagementRepository(
    private val supabase: SupabaseClient
) {

    // ---------- Tables ----------

    suspend fun getTablesForOutlet(outletId: Long): List<RestaurantTableDto> {
        return supabase.from("restaurant_tables")
            .select {
                filter { eq("outlet_id", outletId) }
                order("table_number", SupabaseOrder.ASCENDING)
            }
            .decodeList<RestaurantTableDto>()
    }

    suspend fun setTableStatus(tableId: Long, status: String) {
        supabase.from("restaurant_tables").update(
            { set("status", status) }
        ) {
            filter { eq("id", tableId) }
        }
    }

    // ---------- Categories + Menu ----------

    suspend fun getCategories(): List<CategoryDto> {
        return supabase.from("categories")
            .select { filter { eq("is_active", true) } }
            .decodeList<CategoryDto>()
    }

    suspend fun getMenuForOutlet(outletId: Long): List<OutletMenuItemWithProductDto> {
        return supabase.from("outlet_menu_items")
            .select(Columns.raw("*, product_catalog(id, name, category_id)")) {
                filter {
                    eq("outlet_id", outletId)
                    eq("is_available", true)
                }
            }
            .decodeList<OutletMenuItemWithProductDto>()
    }

    // ---------- Orders ----------

    // finds an already-open dine-in order for a table, if the table is occupied
    suspend fun getActiveOrderForTable(tableId: Long): OrderDto? {
        return supabase.from("orders")
            .select {
                filter {
                    eq("table_id", tableId)
                    eq("order_type", "dine_in")
                    neq("status", "completed")
                    neq("status", "cancelled")
                }
                order("created_at", SupabaseOrder.DESCENDING)
                limit(1)
            }
            .decodeList<OrderDto>()
            .firstOrNull()
    }

    suspend fun getActiveOrdersForOutlet(outletId: Long): List<OrderDto> {
        return supabase.from("orders")
            .select {
                filter {
                    eq("outlet_id", outletId)
                    eq("order_type", "dine_in")
                    neq("status", "completed")
                    neq("status", "cancelled")
                }
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<OrderDto>()
    }

    suspend fun createDineInOrder(outletId: Long, tableId: Long, customerName: String?): OrderDto {
        val newOrder = OrderDto(
            outlet_id = outletId,
            table_id = tableId,
            customer_name = customerName,
            status = "pending",
            order_type = "dine_in"
        )
        return supabase.from("orders")
            .insert(newOrder) { select() }
            .decodeSingle<OrderDto>()
    }

    suspend fun setOrderStatus(orderId: String, status: String) {
        supabase.from("orders").update(
            { set("status", status) }
        ) {
            filter { eq("id", orderId) }
        }
    }

    // ---------- Order items (the running bill) ----------

    suspend fun getOrderItemsWithMenu(orderId: String): List<OrderItemWithMenuDto> {
        return supabase.from("order_items")
            .select(Columns.raw("*, outlet_menu_items(id, product_catalog(name))")) {
                filter { eq("order_id", orderId) }
            }
            .decodeList<OrderItemWithMenuDto>()
    }

    suspend fun insertOrderItem(
        orderId: String,
        menuItemId: Long,
        qty: Long,
        pricePerItem: Double,
        totalDiscount: Float = 0f
    ) {
        val totalPrice = (pricePerItem * qty) - totalDiscount
        supabase.from("order_items").insert(
            OrderItemInsertDto(
                order_id = orderId,
                menu_item_id = menuItemId,
                qty = qty,
                price_per_item = pricePerItem,
                total_price = totalPrice,
                total_discount = totalDiscount
            )
        )
    }

    suspend fun updateOrderItemQty(orderItemId: Long, newQty: Long, pricePerItem: Double, totalDiscount: Float = 0f) {
        val totalPrice = (pricePerItem * newQty) - totalDiscount
        supabase.from("order_items").update(
            {
                set("qty", newQty)
                set("total_price", totalPrice)
            }
        ) {
            filter { eq("id", orderItemId) }
        }
    }

    suspend fun deleteOrderItem(orderItemId: Long) {
        supabase.from("order_items").delete {
            filter { eq("id", orderItemId) }
        }
    }

    suspend fun markOrderItemsKotPrinted(orderId: String) {
        supabase.from("order_items").update(
            { set("kot_printed", true) }
        ) {
            filter {
                eq("order_id", orderId)
                eq("kot_printed", false)
            }
        }
    }

    suspend fun getOrdersByDate(outletId: Long, date: java.time.LocalDate): List<OrderDto> {
        val start = date.atStartOfDay().toString()
        val end = date.plusDays(1).atStartOfDay().toString()
        return supabase.from("orders")
            .select {
                filter {
                    eq("outlet_id", outletId)
                    gte("created_at", start)
                    lt("created_at", end)
                }
                order("created_at", SupabaseOrder.DESCENDING)
            }
            .decodeList<OrderDto>()
    }
}