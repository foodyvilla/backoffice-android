package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.BillLineUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.CartLineUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.CategoryDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.MenuItemUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.TableUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ThermalPrinterBridge
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.TableManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TableManagementUiState(
    val outletId: Long = 0,
    val tables: List<TableUiModel> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val menuItems: List<MenuItemUiModel> = emptyList(),

    val selectedTableId: Long? = null,
    val selectedCategoryId: Long? = null,

    // per-table state so switching tables never loses another table's work
    val ordersByTable: Map<Long, String> = emptyMap(),       // tableId -> orderId
    val billLinesByTable: Map<Long, List<BillLineUiModel>> = emptyMap(),
    val cartsByTable: Map<Long, List<CartLineUiModel>> = emptyMap(),

    val isLoading: Boolean = false,
    val errorText: String? = null,
    val lastInvoice: Pair<String, Double>? = null // tableNumber to grandTotal, for the "bill printed" confirmation
)

val TableManagementUiState.selectedTable: TableUiModel?
    get() = tables.find { it.id == selectedTableId }

val TableManagementUiState.currentOrderId: String?
    get() = ordersByTable[selectedTableId]

val TableManagementUiState.currentBillLines: List<BillLineUiModel>
    get() = billLinesByTable[selectedTableId].orEmpty()

val TableManagementUiState.currentCartLines: List<CartLineUiModel>
    get() = cartsByTable[selectedTableId].orEmpty()

val TableManagementUiState.currentGrandTotal: Double
    get() {
        val savedTotal = currentBillLines.sumOf { it.totalPrice }
        val cartTotal = currentCartLines.sumOf { it.price * it.qty }
        return savedTotal + cartTotal
    }

class TableManagementViewModel(
    private val repository: TableManagementRepository,
    private val printerBridge: ThermalPrinterBridge
) : ViewModel() {

    private val _state = MutableStateFlow(TableManagementUiState())
    val state = _state.asStateFlow()

    fun loadOutlet(outletId: Long) {
        _state.update { it.copy(outletId = outletId, isLoading = true, errorText = null) }
        viewModelScope.launch {
            runCatching {
                val tables = repository.getTablesForOutlet(outletId).map {
                    TableUiModel(it.id, it.table_number, it.capacity, it.status)
                }
                val categories = repository.getCategories()
                val menu = repository.getMenuForOutlet(outletId).map { m ->
                    MenuItemUiModel(
                        menuItemId = m.id,
                        productId = m.product_id,
                        name = m.product_catalog?.name ?: "Item #${m.product_id}",
                        categoryId = m.product_catalog?.category_id,
                        price = m.price,
                        discount = m.discount,
                        isAvailable = m.is_available && !m.is_out_of_stock
                    )
                }

                // Restore active orders and bill lines for all occupied tables
                val orders = mutableMapOf<Long, String>()
                val lines = mutableMapOf<Long, List<BillLineUiModel>>()

                tables.filter { it.status == "occupied" }.forEach { table ->
                    repository.getActiveOrderForTable(table.id)?.let { order ->
                        orders[table.id] = order.id
                        val items = repository.getOrderItemsWithMenu(order.id).map { i ->
                            BillLineUiModel(
                                orderItemId = i.id,
                                menuItemId = i.menu_item_id,
                                name = i.outlet_menu_items?.product_catalog?.name ?: "Item #${i.menu_item_id}",
                                qty = i.qty,
                                pricePerItem = i.price_per_item,
                                totalPrice = i.total_price,
                                kotPrinted = i.kot_printed
                            )
                        }
                        lines[table.id] = items
                    }
                }

                Triple(tables, categories, menu) to (orders to lines)
            }.onSuccess { (data, session) ->
                val (tables, categories, menu) = data
                val (orders, lines) = session
                _state.update {
                    it.copy(
                        tables = tables,
                        categories = categories,
                        menuItems = menu,
                        ordersByTable = orders,
                        billLinesByTable = lines,
                        selectedCategoryId = it.selectedCategoryId ?: categories.firstOrNull()?.id,
                        isLoading = false
                    )
                }
            }.onFailure { err ->
                _state.update { it.copy(errorText = err.localizedMessage, isLoading = false) }
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    // Step 1: waiter marks a table for the arriving customer (or reopens one already occupied)
    fun selectTable(table: TableUiModel) {
        _state.update { it.copy(selectedTableId = table.id, errorText = null) }
    }

    // Step 2/3: waiter taps products from the category-filtered list; builds a local cart first
    fun addToCart(item: MenuItemUiModel) {
        val tableId = _state.value.selectedTableId ?: return
        _state.update {
            val cart = it.cartsByTable[tableId].orEmpty()
            val existing = cart.find { l -> l.menuItemId == item.menuItemId }
            val updatedCart = if (existing != null) {
                cart.map { l -> if (l.menuItemId == item.menuItemId) l.copy(qty = l.qty + 1) else l }
            } else {
                cart + CartLineUiModel(item.menuItemId, item.name, item.price, qty = 1)
            }
            it.copy(cartsByTable = it.cartsByTable + (tableId to updatedCart))
        }
    }

    fun updateCartQty(menuItemId: Long, newQty: Long) {
        val tableId = _state.value.selectedTableId ?: return
        _state.update {
            val cart = it.cartsByTable[tableId].orEmpty()
            val updatedCart = if (newQty <= 0) {
                cart.filterNot { l -> l.menuItemId == menuItemId }
            } else {
                cart.map { l -> if (l.menuItemId == menuItemId) l.copy(qty = newQty) else l }
            }
            it.copy(cartsByTable = it.cartsByTable + (tableId to updatedCart))
        }
    }

    // Step 4: push the cart into the same order as new order_items (or bump qty of a matching saved line)
    fun saveCartToOrder() {
        val tableId = _state.value.selectedTableId ?: return
        val cart = _state.value.currentCartLines
        if (cart.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                var orderId = _state.value.currentOrderId
                if (orderId == null) {
                    val newOrder = repository.createDineInOrder(
                        outletId = _state.value.outletId,
                        tableId = tableId,
                        customerName = null
                    )
                    repository.setTableStatus(tableId, "occupied")
                    orderId = newOrder.id
                }

                val existingLines = _state.value.billLinesByTable[tableId].orEmpty()
                for (line in cart) {
                    val existing = existingLines.find { it.menuItemId == line.menuItemId && !it.kotPrinted }
                    if (existing != null) {
                        repository.updateOrderItemQty(existing.orderItemId, existing.qty + line.qty, line.price)
                    } else {
                        repository.insertOrderItem(orderId, line.menuItemId, line.qty, line.price)
                    }
                }
                
                val refreshedLines = repository.getOrderItemsWithMenu(orderId).map { i ->
                    BillLineUiModel(
                        orderItemId = i.id,
                        menuItemId = i.menu_item_id,
                        name = i.outlet_menu_items?.product_catalog?.name ?: "Item #${i.menu_item_id}",
                        qty = i.qty,
                        pricePerItem = i.price_per_item,
                        totalPrice = i.total_price,
                        kotPrinted = i.kot_printed
                    )
                }
                orderId to refreshedLines
            }.onSuccess { (orderId, refreshedLines) ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        ordersByTable = it.ordersByTable + (tableId to orderId),
                        billLinesByTable = it.billLinesByTable + (tableId to refreshedLines),
                        cartsByTable = it.cartsByTable + (tableId to emptyList()),
                        tables = it.tables.map { t -> if (t.id == tableId) t.copy(status = "occupied") else t }
                    )
                }
            }.onFailure { err ->
                _state.update { it.copy(isLoading = false, errorText = err.localizedMessage) }
            }
        }
    }

    // adjust / remove an item that's already saved on the order (increase, decrease, or delete)
    fun updateSavedLineQty(line: BillLineUiModel, newQty: Long) {
        val tableId = _state.value.selectedTableId ?: return
        viewModelScope.launch {
            runCatching {
                if (newQty <= 0) repository.deleteOrderItem(line.orderItemId)
                else repository.updateOrderItemQty(line.orderItemId, newQty, line.pricePerItem)
            }.onSuccess {
                _state.update {
                    val updated = if (newQty <= 0) {
                        it.currentBillLines.filterNot { l -> l.orderItemId == line.orderItemId }
                    } else {
                        it.currentBillLines.map { l ->
                            if (l.orderItemId == line.orderItemId) l.copy(qty = newQty, totalPrice = l.pricePerItem * newQty) else l
                        }
                    }
                    it.copy(billLinesByTable = it.billLinesByTable + (tableId to updated))
                }
            }.onFailure { err -> _state.update { it.copy(errorText = err.localizedMessage) } }
        }
    }

    // "Print KOT" — sends only the not-yet-printed lines to the kitchen printer
    fun printKot(context: android.content.Context) {
        val table = _state.value.selectedTable ?: return
        val orderId = _state.value.currentOrderId ?: return
        val unprinted = _state.value.currentBillLines.filterNot { it.kotPrinted }
        if (unprinted.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                printerBridge.printKot(context, table.tableNumber, unprinted)
                repository.markOrderItemsKotPrinted(orderId)
            }.onSuccess {
                _state.update {
                    val updated = it.currentBillLines.map { l -> l.copy(kotPrinted = true) }
                    it.copy(
                        billLinesByTable = it.billLinesByTable + (table.id to updated),
                        errorText = null
                    )
                }
            }.onFailure { err -> _state.update { it.copy(errorText = "Printing failed: ${err.localizedMessage}") } }
        }
    }

    // "Mark as Done" — food has been served, doesn't touch payment or free the table
    fun markOrderServed() {
        val orderId = _state.value.currentOrderId ?: return
        viewModelScope.launch {
            runCatching { repository.setOrderStatus(orderId, "served") }
                .onSuccess {
                    _state.update { it.copy(errorText = null) }
                }
                .onFailure { err -> _state.update { it.copy(errorText = err.localizedMessage) } }
        }
    }

    // "Print Invoice" — final settle: prints the bill, marks the order paid, frees the table
    fun printInvoiceAndSettle(context: android.content.Context) {
        val table = _state.value.selectedTable ?: return
        val orderId = _state.value.currentOrderId ?: return
        val grandTotal = _state.value.currentGrandTotal

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                printerBridge.printInvoice(context, table.tableNumber, _state.value.currentBillLines, grandTotal)
                repository.setOrderStatus(orderId, "paid")
                repository.setTableStatus(table.id, "available")
            }.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        selectedTableId = null,
                        ordersByTable = it.ordersByTable - table.id,
                        billLinesByTable = it.billLinesByTable - table.id,
                        cartsByTable = it.cartsByTable - table.id,
                        tables = it.tables.map { t -> if (t.id == table.id) t.copy(status = "available") else t },
                        lastInvoice = table.tableNumber to grandTotal,
                        errorText = null
                    )
                }
            }.onFailure { err ->
                _state.update { it.copy(isLoading = false, errorText = "Settlement failed: ${err.localizedMessage}") }
            }
        }
    }

    fun dismissInvoiceConfirmation() {
        _state.update { it.copy(lastInvoice = null) }
    }
}