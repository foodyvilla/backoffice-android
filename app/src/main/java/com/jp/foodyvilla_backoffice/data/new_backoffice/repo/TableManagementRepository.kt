package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import com.jp.foodyvilla_backoffice.data.new_backoffice.models.CategoryDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OrderDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OrderItemInsertDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OrderItemWithMenuDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletMenuItemWithProductDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.RestaurantTableDto
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

class TableManagementRepository(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
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

    suspend fun getOutlets(): List<com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OutletDropdownUiModel> {
        return supabase.from("outlets").select {
            filter { eq("is_active", true) }
        }.decodeList<com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OutletListResponse>().map {
            com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OutletDropdownUiModel(
                id = it.id,
                name = it.name
            )
        }
    }

    suspend fun setTableStatus(tableId: Long, status: String) {
        supabase.from("restaurant_tables").update(
            { set("status", status) }
        ) {
            filter { eq("id", tableId) }
        }
    }

    suspend fun createTable(outletId: Long, tableNumber: String, capacity: Int) {
        val newTable = buildJsonObject {
            put("outlet_id", outletId)
            put("table_number", tableNumber)
            put("capacity", capacity)
            put("status", "available")
        }
        supabase.from("restaurant_tables").insert(newTable)
    }

    suspend fun updateTable(tableId: Long, tableNumber: String, capacity: Int) {
        val updateData = buildJsonObject {
            put("table_number", tableNumber)
            put("capacity", capacity)
        }
        supabase.from("restaurant_tables").update(updateData) {
            filter { eq("id", tableId) }
        }
    }

    suspend fun deleteTable(tableId: Long) {
        supabase.from("restaurant_tables").delete {
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
        val session = authRepository.currentSession.value
        val employeeId = (session as? UserSession.EmployeeSession)?.empId?.toLongOrNull()
        val tempTransactionId = "DINE_${UUID.randomUUID().toString().take(8)}"

        // Use buildJsonObject to avoid sending "id" (DB will generate it)
        val newOrder = buildJsonObject {
            put("outlet_id", outletId)
            put("table_id", tableId)
            put("customer_name", customerName ?: "Dine-in Table $tableId")
            put("status", "pending")
            put("order_type", "dine_in")
            put("transaction_id", tempTransactionId)
            if (employeeId != null) put("accepted_by", employeeId)
            put("address", "Table $tableId")
        }

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

    suspend fun getAllOrderItemsForOrders(orderIds: List<String>): List<OrderItemWithMenuDto> {
        if (orderIds.isEmpty()) return emptyList()
        return supabase.from("order_items")
            .select(Columns.raw("*, outlet_menu_items(id, product_catalog(name))")) {
                filter { isIn("order_id", orderIds) }
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

    suspend fun insertOrderItems(items: List<OrderItemInsertDto>) {
        if (items.isEmpty()) return
        supabase.from("order_items").insert(items)
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