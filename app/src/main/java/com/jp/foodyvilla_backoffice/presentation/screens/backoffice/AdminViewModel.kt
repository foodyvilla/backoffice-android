package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumnType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import com.jp.foodyvilla_backoffice.data.repo.AdminRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.text.SimpleDateFormat
import java.util.*
import org.koin.androidx.compose.koinViewModel

data class AdminUiState(
    val tables: List<AdminTable> = adminTables,
    val selectedTable: AdminTable = adminTables.first(),
    val rows: List<JsonObject> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val editingRow: JsonObject? = null,
    val formValues: Map<String, String> = emptyMap(),
    val searchQuery: String = "",
    val orderItemsByOrderId: Map<String, List<OrderItem>> = emptyMap(),
    val productsById: Map<String, ProductCatalog> = emptyMap(),
    val customerOrders: List<Order> = emptyList(),
    val customerCart: List<Cart> = emptyList(),
    val lookupRows: Map<String, List<JsonObject>> = emptyMap(),
    val uploadingColumn: String? = null,
    val pendingOrders: List<Order> = emptyList(),
    val pendingOrderRows: List<JsonObject> = emptyList(),
    val dashboardData: DashboardData = DashboardData(emptyList(), emptyList(), emptyList(), emptyList()),
    val dashboardRows: Map<String, List<JsonObject>> = emptyMap(),
    
    // Filters
    val orderDateFilter: String? = null, // yyyy-MM-dd
    val orderStatusFilter: String? = null,
    val attendanceDateFilter: String? = null,
    val attendanceOutletFilter: String? = null,
    val attendanceSearchQuery: String = "",
    val punchResult: Pair<String, String>? = null, // status to message
    val currentOutlet: Outlet? = null,
    val draftOrderItems: List<Pair<JsonObject, Int>> = emptyList(), // MenuItem to Qty

    // Typed data lists
    val orders: List<Order> = emptyList(),
    val orderItems: List<OrderItem> = emptyList(),
    val products: List<ProductCatalog> = emptyList(),
    val customers: List<User> = emptyList(),
    val employees: List<Employee> = emptyList(),
    val attendance: List<Attendance> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val categories: List<Category> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val outletMenuItems: List<OutletMenuItem> = emptyList()
)

class AdminViewModel(
    private val repository: AdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var globalOrdersJob: Job? = null

    init {
        // selectTable(adminTables.first()) // Removed to avoid loading data at once
        startGlobalOrderObservation()
    }

    private fun startGlobalOrderObservation() {
        globalOrdersJob?.cancel()
        globalOrdersJob = viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.observeOrders(today).collect { result ->
                result.onSuccess { allOrders ->
                    val pendingRows = allOrders.filter { row ->
                        val status = row["status"].toDisplayText().lowercase()
                        val acceptedBy = row["accepted_by"]
                        (status == "pending" || status == "placed") && (acceptedBy == null || acceptedBy is kotlinx.serialization.json.JsonNull)
                    }
                    val pending = pendingRows.mapNotNull { runCatching { it.toModel<Order>() }.getOrNull() }
                    
                    _uiState.update { it.copy(pendingOrders = pending, pendingOrderRows = pendingRows) }
                    
                    if (pending.isNotEmpty()) {
                        loadOrderItemsFor(allOrders)
                    }
                }
                result.onFailure { throwable ->
                    Log.e("AdminViewModel", "Global order observation failed: ${throwable.message}")
                    delay(5000) // Retry after delay
                    startGlobalOrderObservation()
                }
            }
        }
    }

    fun selectTable(table: AdminTable) {
        val current = _uiState.value
        if (current.selectedTable.name == table.name && observeJob?.isActive == true) return

        observeJob?.cancel()
        
        // Default to today's date for orders if not already filtered
        val initialOrderDate = if (table.name == "orders") {
            current.orderDateFilter ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        } else null

        _uiState.update {
            it.copy(
                selectedTable = table,
                rows = emptyList(),
                isLoading = true,
                error = null,
                successMessage = null,
                editingRow = null,
                formValues = repository.toEditableValues(table, null),
                searchQuery = "",
                orderItemsByOrderId = emptyMap(),
                productsById = emptyMap(),
                orderDateFilter = initialOrderDate
            )
        }
        loadLookupsFor(table)
        observeJob = viewModelScope.launch {
            val flow = when (table.name) {
                "outlets" -> repository.observeOutlets()
                "orders" -> repository.observeOrders(_uiState.value.orderDateFilter)
                "product_catalog" -> repository.observeProductCatalog()
                "users" -> repository.observeUsers()
                "cart" -> repository.observeCart()
                "banners" -> repository.observeBanners()
                "offers" -> repository.observeOffers()
                "reviews" -> repository.observeReviews()
                "employee" -> repository.observeEmployees()
                "attendance" -> repository.observeAttendance()
                "payments" -> repository.observePayments()
                "outlet_menu_items" -> repository.observeOutletMenuItems()
                else -> repository.observeRows(table)
            }
            flow.collect { result ->
                result.fold(
                    onSuccess = { rows ->
                        _uiState.update {
                            it.copy(
                                rows = rows,
                                isLoading = false,
                                error = null,
                                orders = if (table.name == "orders") rows.mapNotNull { r -> runCatching { r.toModel<Order>() }.getOrNull() } else it.orders,
                                orderItems = if (table.name == "order_items") rows.mapNotNull { r -> runCatching { r.toModel<OrderItem>() }.getOrNull() } else it.orderItems,
                                products = if (table.name == "product_catalog") rows.mapNotNull { r -> runCatching { r.toModel<ProductCatalog>() }.getOrNull() } else it.products,
                                customers = if (table.name == "users") rows.mapNotNull { r -> runCatching { r.toModel<User>() }.getOrNull() } else it.customers,
                                employees = if (table.name == "employee") rows.mapNotNull { r -> runCatching { r.toModel<Employee>() }.getOrNull() } else it.employees,
                                attendance = if (table.name == "attendance") rows.mapNotNull { r -> runCatching { r.toModel<Attendance>() }.getOrNull() } else it.attendance,
                                offers = if (table.name == "offers") rows.mapNotNull { r -> runCatching { r.toModel<Offer>() }.getOrNull() } else it.offers,
                                banners = if (table.name == "banners") rows.mapNotNull { r -> runCatching { r.toModel<Banner>() }.getOrNull() } else it.banners,
                                categories = if (table.name == "categories") rows.mapNotNull { r -> runCatching { r.toModel<Category>() }.getOrNull() } else it.categories,
                                payments = if (table.name == "payments") rows.mapNotNull { r -> runCatching { r.toModel<Payment>() }.getOrNull() } else it.payments,
                                outletMenuItems = if (table.name == "outlet_menu_items") rows.mapNotNull { r -> runCatching { r.toModel<OutletMenuItem>() }.getOrNull() } else it.outletMenuItems
                            )
                        }
                        if (table.name == "orders") {
                            loadOrderItemsFor(rows)
                        }
                        // loadDashboardRows() // Removed as it is now in DashboardViewModel
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = throwable.message ?: "Could not load ${table.title}"
                            )
                        }
                    }
                )
            }
        }
    }

    fun refresh() {
        val table = _uiState.value.selectedTable
        val dateFilter = _uiState.value.orderDateFilter
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                when (table.name) {
                    "outlets" -> repository.loadOutlets()
                    "orders" -> repository.loadOrders(dateFilter)
                    "product_catalog" -> repository.loadProductCatalog()
                    "users" -> repository.loadUsers()
                    "cart" -> repository.loadCart()
                    "banners" -> repository.loadBanners()
                    "offers" -> repository.loadOffers()
                    "reviews" -> repository.loadReviews()
                    "employee" -> repository.loadEmployees()
                    "attendance" -> repository.loadAttendance()
                    "payments" -> repository.loadPayments()
                    "outlet_menu_items" -> repository.loadOutletMenuItems()
                    "auth_otp" -> repository.loadAuthOtp()
                    else -> repository.loadRows(table)
                }
            }
                .onSuccess { rows ->
                    _uiState.update {
                        it.copy(
                            rows = rows,
                            isLoading = false,
                            orders = if (table.name == "orders") rows.mapNotNull { r -> runCatching { r.toModel<Order>() }.getOrNull() } else it.orders,
                            orderItems = if (table.name == "order_items") rows.mapNotNull { r -> runCatching { r.toModel<OrderItem>() }.getOrNull() } else it.orderItems,
                            products = if (table.name == "product_catalog") rows.mapNotNull { r -> runCatching { r.toModel<ProductCatalog>() }.getOrNull() } else it.products,
                            customers = if (table.name == "users") rows.mapNotNull { r -> runCatching { r.toModel<User>() }.getOrNull() } else it.customers,
                            employees = if (table.name == "employee") rows.mapNotNull { r -> runCatching { r.toModel<Employee>() }.getOrNull() } else it.employees,
                            attendance = if (table.name == "attendance") rows.mapNotNull { r -> runCatching { r.toModel<Attendance>() }.getOrNull() } else it.attendance,
                            offers = if (table.name == "offers") rows.mapNotNull { r -> runCatching { r.toModel<Offer>() }.getOrNull() } else it.offers,
                            banners = if (table.name == "banners") rows.mapNotNull { r -> runCatching { r.toModel<Banner>() }.getOrNull() } else it.banners,
                            categories = if (table.name == "categories") rows.mapNotNull { r -> runCatching { r.toModel<Category>() }.getOrNull() } else it.categories,
                            payments = if (table.name == "payments") rows.mapNotNull { r -> runCatching { r.toModel<Payment>() }.getOrNull() } else it.payments,
                            outletMenuItems = if (table.name == "outlet_menu_items") rows.mapNotNull { r -> runCatching { r.toModel<OutletMenuItem>() }.getOrNull() } else it.outletMenuItems
                        )
                    }
                    if (table.name == "orders") {
                        loadOrderItemsFor(rows)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Refresh failed")
                    }
                }
        }
    }

    private fun loadOrderItemsFor(orders: List<JsonObject>) {
        viewModelScope.launch {
            val orderIds = orders.map { it["id"].toDisplayText() }.toSet()
            runCatching {
                val itemsByOrder = repository.loadOrderItems()
                    .filter { it["order_id"].toDisplayText() in orderIds }
                    .map { it.toModel<OrderItem>() }
                    .groupBy { it.orderId ?: "" }
                val productsById = repository.loadProducts()
                    .mapNotNull { runCatching { it.toModel<ProductCatalog>() }.getOrNull() }
                    .associateBy { it.id.toString() }
                itemsByOrder to productsById
            }.onSuccess { (itemsByOrder, productsById) ->
                _uiState.update {
                    it.copy(
                        orderItemsByOrderId = it.orderItemsByOrderId + itemsByOrder,
                        productsById = it.productsById + productsById
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(error = throwable.message ?: "Could not load order items")
                }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateOrderDateFilter(date: String?) {
        _uiState.update { it.copy(orderDateFilter = date) }
        // Re-observe with new date filter
        if (_uiState.value.selectedTable.name == "orders") {
            selectTable(_uiState.value.selectedTable)
        }
    }

    fun updateOrderStatusFilter(status: String?) {
        _uiState.update { it.copy(orderStatusFilter = status) }
    }

    fun updateAttendanceDateFilter(date: String?) {
        _uiState.update { it.copy(attendanceDateFilter = date) }
    }

    fun updateAttendanceOutletFilter(outletId: String?) {
        _uiState.update { it.copy(attendanceOutletFilter = outletId) }
    }

    fun updateAttendanceSearch(query: String) {
        _uiState.update { it.copy(attendanceSearchQuery = query) }
    }

    fun loadCustomerDetails(customerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, customerOrders = emptyList(), customerCart = emptyList()) }
            val orders = runCatching { repository.loadCustomerOrders(customerId) }.getOrDefault(emptyList()).map { it.toModel<Order>() }
            val cart = runCatching { repository.loadCustomerCart(customerId) }.getOrDefault(emptyList()).map { it.toModel<Cart>() }
            _uiState.update { it.copy(isLoading = false, customerOrders = orders, customerCart = cart) }
        }
    }

    fun startCreate() {
        val table = _uiState.value.selectedTable
        val session = repository.authSession.value
        
        var initialValues = repository.toEditableValues(table, null)
        if (table.name == "orders" && session != null) {
            initialValues = initialValues + ("outlet_id" to session.outletId.toString())
        }

        _uiState.update {
            it.copy(
                editingRow = null,
                formValues = initialValues,
                error = null,
                successMessage = null,
                draftOrderItems = emptyList()
            )
        }
    }

    fun addDraftOrderItem(menuItem: JsonObject, qty: Int) {
        _uiState.update { 
            it.copy(draftOrderItems = it.draftOrderItems + (menuItem to qty))
        }
    }

    fun removeDraftOrderItem(index: Int) {
        _uiState.update {
            val newList = it.draftOrderItems.toMutableList()
            if (index in newList.indices) newList.removeAt(index)
            it.copy(draftOrderItems = newList)
        }
    }

    fun startCreateFor(tableName: String) {
        val table = adminTables.firstOrNull { it.name == tableName } ?: return
        selectTable(table)
        _uiState.update {
            it.copy(
                editingRow = null,
                formValues = repository.toEditableValues(table, null),
                error = null,
                successMessage = null
            )
        }
    }

    fun startEdit(row: JsonObject) {
        val table = _uiState.value.selectedTable
        _uiState.update {
            it.copy(
                editingRow = row,
                formValues = repository.toEditableValues(table, row),
                error = null,
                successMessage = null
            )
        }
    }

    fun updateFormValue(column: String, value: String) {
        _uiState.update {
            it.copy(formValues = it.formValues + (column to value))
        }
    }

    fun uploadImage(context: Context, uri: Uri, columnName: String) {
        val state = _uiState.value
        val column = state.selectedTable.columns.firstOrNull { it.name == columnName } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(uploadingColumn = columnName, error = null, successMessage = null) }
            runCatching {
                repository.uploadImage(context, state.selectedTable, column, uri)
            }.onSuccess { url ->
                val current = _uiState.value.formValues[columnName].orEmpty()
                val nextValue = when (column.type) {
                    AdminColumnType.TextArray -> listOf(current, url).filter { it.isNotBlank() }.joinToString(", ")
                    AdminColumnType.Json -> {
                        val values = current
                            .removePrefix("[")
                            .removeSuffix("]")
                            .split(",")
                            .map { it.trim().trim('"') }
                            .filter { it.isNotBlank() }
                            .plus(url)
                        values.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
                    }
                    else -> url
                }
                _uiState.update {
                    it.copy(
                        formValues = it.formValues + (columnName to nextValue),
                        uploadingColumn = null,
                        successMessage = "Image uploaded"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        uploadingColumn = null,
                        error = throwable.message ?: "Image upload failed"
                    )
                }
            }
        }
    }

    fun save() {
        val state = _uiState.value
        val missing = state.selectedTable.editableColumns
            .firstOrNull { it.required && state.formValues[it.name].isNullOrBlank() }
        if (missing != null) {
            _uiState.update { it.copy(error = "${missing.label} is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            runCatching {
                if (state.editingRow == null) {
                    when (state.selectedTable.name) {
                        "outlets" -> repository.createOutlet(state.formValues)
                        "orders" -> repository.createOrder(state.formValues, state.draftOrderItems)
                        "product_catalog" -> repository.createProduct(state.formValues)
                        "users" -> repository.createUser(state.formValues)
                        "cart" -> repository.createCart(state.formValues)
                        "banners" -> repository.createBanner(state.formValues)
                        "offers" -> repository.createOffer(state.formValues)
                        "reviews" -> repository.createReview(state.formValues)
                        "employee" -> repository.createEmployee(state.formValues)
                        "attendance" -> repository.createAttendance(state.formValues)
                        "order_items" -> repository.createOrderItem(state.formValues)
                        "outlet_menu_items" -> repository.createOutletMenuItem(state.formValues)
                        "payments" -> repository.createPayment(state.formValues)
                        else -> repository.createRow(state.selectedTable, state.formValues)
                    }
                } else {
                    when (state.selectedTable.name) {
                        "outlets" -> repository.updateOutlet(state.editingRow, state.formValues)
                        "orders" -> repository.updateOrder(state.editingRow, state.formValues)
                        "product_catalog" -> repository.updateProduct(state.editingRow, state.formValues)
                        "users" -> repository.updateUser(state.editingRow, state.formValues)
                        "cart" -> repository.updateCart(state.editingRow, state.formValues)
                        "banners" -> repository.updateBanner(state.editingRow, state.formValues)
                        "offers" -> repository.updateOffer(state.editingRow, state.formValues)
                        "reviews" -> repository.updateReview(state.editingRow, state.formValues)
                        "employee" -> repository.updateEmployee(state.editingRow, state.formValues)
                        "attendance" -> repository.updateAttendance(state.editingRow, state.formValues)
                        "order_items" -> repository.updateOrderItem(state.editingRow, state.formValues)
                        "outlet_menu_items" -> repository.updateOutletMenuItem(state.editingRow, state.formValues)
                        "payments" -> repository.updatePayment(state.editingRow, state.formValues)
                        else -> repository.updateRow(state.selectedTable, state.editingRow, state.formValues)
                    }
                }
            }.onSuccess {
                runCatching {
                    repository.notifySavedRecord(state.selectedTable, state.editingRow, state.formValues)
                }
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "${state.selectedTable.title} saved",
                        formValues = repository.toEditableValues(state.selectedTable, null),
                        editingRow = null
                    )
                }
                refresh()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: "Save failed"
                    )
                }
            }
        }
    }

    fun delete(row: JsonObject) {
        val table = _uiState.value.selectedTable
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            runCatching {
                when (table.name) {
                    "outlets" -> repository.deleteOutlet(row)
                    "orders" -> repository.deleteOrder(row)
                    "product_catalog" -> repository.deleteProduct(row)
                    "users" -> repository.deleteUser(row)
                    "cart" -> repository.deleteCart(row)
                    "banners" -> repository.deleteBanner(row)
                    "offers" -> repository.deleteOffer(row)
                    "reviews" -> repository.deleteReview(row)
                    "employee" -> repository.deleteEmployee(row)
                    "attendance" -> repository.deleteAttendance(row)
                    "order_items" -> repository.deleteOrderItem(row)
                    "outlet_menu_items" -> repository.deleteOutletMenuItem(row)
                    "payments" -> repository.deletePayment(row)
                    else -> repository.deleteRow(table, row)
                }
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            successMessage = "${table.title} row deleted",
                            editingRow = null
                        )
                    }
                    refresh()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isSaving = false, error = throwable.message ?: "Delete failed")
                    }
                }
        }
    }

    fun updateOrderStatus(row: JsonObject, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            runCatching {
                repository.updateOrderStatus(row, status)
            }.onSuccess {
                _uiState.update {
                    it.copy(isSaving = false, successMessage = "Order moved to $status")
                }
                refresh()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaving = false, error = throwable.message ?: "Could not update order status")
                }
            }
        }
    }

    fun punchIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            runCatching { repository.punchIn() }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, successMessage = "Punch in recorded") }
                    refresh()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isSaving = false, error = throwable.message ?: "Punch in failed")
                    }
                }
        }
    }

    fun punchOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            runCatching { repository.punchOut() }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, successMessage = "Punch out recorded") }
                    refresh()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isSaving = false, error = throwable.message ?: "Punch out failed")
                    }
                }
        }
    }

    fun sendFcmToToken(token: String, title: String, body: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            runCatching { repository.sendFcmToToken(token, title, body) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, successMessage = "Notification sent") }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isSaving = false, error = throwable.message ?: "Failed to send notification")
                    }
                }
        }
    }

    fun sendOfferToCart(title: String, body: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            runCatching { repository.sendNotificationToCart(title, body) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, successMessage = "Offers sent to cart customers") }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isSaving = false, error = throwable.message ?: "Failed to send offers")
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    private fun loadLookupsFor(table: AdminTable) {
        val referenceTables = table.editableColumns
            .mapNotNull { it.reference?.table }
            .distinct()
        if (referenceTables.isEmpty()) return

        viewModelScope.launch {
            val lookupRows = referenceTables.associateWith { tableName ->
                runCatching { repository.loadLookupRows(tableName) }.getOrDefault(emptyList())
            }
            _uiState.update { it.copy(lookupRows = it.lookupRows + lookupRows) }
        }
    }

    private companion object {
        val dashboardTableNames = setOf("orders", "product_catalog", "outlet_menu_items", "users", "reviews", "offers", "banners", "employee", "attendance", "outlets", "payments", "categories")
    }
}
