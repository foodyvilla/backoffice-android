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
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OutletDropdownUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TableManagementUiState(
    val outletId: Long = 0,
    val tables: List<TableUiModel> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val menuItems: List<MenuItemUiModel> = emptyList(),
    val outlets: List<OutletDropdownUiModel> = emptyList(),

    val selectedTableId: Long? = null,
    val selectedCategoryId: Long? = null,

    // per-table state so switching tables never loses another table's work
    val ordersByTable: Map<Long, String> = emptyMap(),       // tableId -> orderId
    val billLinesByTable: Map<Long, List<BillLineUiModel>> = emptyMap(),
    val cartsByTable: Map<Long, List<CartLineUiModel>> = emptyMap(),

    val isOwner: Boolean = false,
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
    private val printerBridge: ThermalPrinterBridge,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TableManagementUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentSession.collect { session ->
                _state.update { it.copy(isOwner = session?.isOwner() ?: false) }
                if (session?.isOwner() == true) {
                    runCatching { repository.getOutlets() }.onSuccess { list ->
                        _state.update { it.copy(outlets = list) }
                    }
                }
            }
        }
    }

    fun loadOutlet(outletId: Long, force: Boolean = false) {
        if (!force && _state.value.outletId == outletId && _state.value.tables.isNotEmpty()) return

        _state.update { it.copy(outletId = outletId, isLoading = true, errorText = null) }
        viewModelScope.launch {
            runCatching {
                var targetOutletId = outletId
                
                if (targetOutletId == 0L && _state.value.isOwner) {
                    val outlets = if (_state.value.outlets.isEmpty()) repository.getOutlets() else _state.value.outlets
                    if (outlets.isNotEmpty()) {
                        targetOutletId = outlets.first().id
                        _state.update { it.copy(outlets = outlets, outletId = targetOutletId) }
                    }
                }

                if (targetOutletId == 0L) {
                    throw IllegalStateException("Please select an outlet.")
                }

                // 1. Fetch tables, categories, and menu concurrently
                val tablesDeferred = async { 
                    repository.getTablesForOutlet(targetOutletId).map {
                        TableUiModel(it.id, it.table_number, it.capacity, it.status)
                    }
                }
                val categoriesDeferred = async { repository.getCategories() }
                val menuDeferred = async { 
                    repository.getMenuForOutlet(targetOutletId).map { m ->
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
                }
                
                val tables = tablesDeferred.await()
                val categories = categoriesDeferred.await()
                val menu = menuDeferred.await()

                // 2. Fetch all active orders for the outlet
                val activeOrders = repository.getActiveOrdersForOutlet(targetOutletId)
                val orderIds = activeOrders.map { it.id }
                
                // 3. Fetch all order items for these active orders in one go (Bulk loading)
                val allOrderItems = repository.getAllOrderItemsForOrders(orderIds)
                
                val ordersMap = mutableMapOf<Long, String>()
                val linesMap = mutableMapOf<Long, List<BillLineUiModel>>()

                activeOrders.forEach { order ->
                    val tableId = order.table_id ?: return@forEach
                    ordersMap[tableId] = order.id
                    
                    val items = allOrderItems.filter { it.order_id == order.id }.map { i ->
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
                    linesMap[tableId] = items
                }

                // 4. Override table status based on whether it has an active order on server
                val updatedTables = tables.map { t ->
                    if (ordersMap.containsKey(t.id)) t.copy(status = "occupied")
                    else t.copy(status = "available")
                }

                Triple(updatedTables, categories, menu) to (ordersMap to linesMap)
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
                        selectedTableId = if (tables.any { t -> t.id == it.selectedTableId }) it.selectedTableId else null,
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

    fun selectTable(table: TableUiModel) {
        _state.update { it.copy(selectedTableId = table.id, errorText = null) }
    }

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

    fun saveCartToOrder() {
        val tableId = _state.value.selectedTableId ?: return
        val cart = _state.value.currentCartLines
        if (cart.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                var orderId = _state.value.currentOrderId
                
                // Safety: Check if table has an active order on server that we missed locally
                if (orderId == null) {
                    val serverOrder = repository.getActiveOrderForTable(tableId)
                    if (serverOrder != null) {
                        orderId = serverOrder.id
                    }
                }

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

    fun updateSavedLineQty(line: BillLineUiModel, newQty: Long) {
        val tableId = _state.value.selectedTableId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
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
                    it.copy(billLinesByTable = it.billLinesByTable + (tableId to updated), isLoading = false)
                }
            }.onFailure { err -> _state.update { it.copy(errorText = err.localizedMessage, isLoading = false) } }
        }
    }

    fun printKot(context: android.content.Context) {
        val table = _state.value.selectedTable ?: return
        val orderId = _state.value.currentOrderId ?: return
        val unprinted = _state.value.currentBillLines.filterNot { it.kotPrinted }
        if (unprinted.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                printerBridge.printKot(context, table.tableNumber, unprinted)
                repository.markOrderItemsKotPrinted(orderId)
            }.onSuccess {
                _state.update {
                    val updated = it.currentBillLines.map { l -> l.copy(kotPrinted = true) }
                    it.copy(
                        billLinesByTable = it.billLinesByTable + (table.id to updated),
                        errorText = null,
                        isLoading = false
                    )
                }
            }.onFailure { err -> _state.update { it.copy(errorText = "Printing failed: ${err.localizedMessage}", isLoading = false) } }
        }
    }

    fun completeOrderSession() {
        val table = _state.value.selectedTable ?: return
        val orderId = _state.value.currentOrderId ?: return
        val outletId = _state.value.outletId

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.setOrderStatus(orderId, "completed")
                repository.setTableStatus(table.id, "available")
            }.onSuccess {
                _state.update { 
                    it.copy(
                        selectedTableId = null,
                        ordersByTable = it.ordersByTable - table.id,
                        billLinesByTable = it.billLinesByTable - table.id,
                        cartsByTable = it.cartsByTable - table.id,
                        isLoading = false
                    )
                }
                loadOutlet(outletId, force = true)
            }.onFailure { err ->
                _state.update { it.copy(isLoading = false, errorText = err.localizedMessage) }
            }
        }
    }

    fun printInvoiceAndSettle(context: android.content.Context) {
        val table = _state.value.selectedTable ?: return
        val orderId = _state.value.currentOrderId ?: return
        val grandTotal = _state.value.currentGrandTotal
        val outletId = _state.value.outletId

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                printerBridge.printInvoice(context, table.tableNumber, _state.value.currentBillLines, grandTotal)
                repository.setOrderStatus(orderId, "completed")
                repository.setTableStatus(table.id, "available")
            }.onSuccess {
                _state.update {
                    it.copy(
                        selectedTableId = null,
                        ordersByTable = it.ordersByTable - table.id,
                        billLinesByTable = it.billLinesByTable - table.id,
                        cartsByTable = it.cartsByTable - table.id,
                        lastInvoice = table.tableNumber to grandTotal,
                        isLoading = false
                    )
                }
                loadOutlet(outletId, force = true)
            }.onFailure { err ->
                _state.update { it.copy(isLoading = false, errorText = "Settlement failed: ${err.localizedMessage}") }
            }
        }
    }

    fun dismissInvoiceConfirmation() {
        _state.update { it.copy(lastInvoice = null) }
    }
}
