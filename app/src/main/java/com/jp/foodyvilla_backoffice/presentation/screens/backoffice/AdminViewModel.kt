package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import android.content.Context
import android.net.Uri
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumnType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminTable
import com.jp.foodyvilla_backoffice.data.model.backoffice.adminTables
import com.jp.foodyvilla_backoffice.data.repo.AdminRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

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
    val orderItemsByOrderId: Map<String, List<JsonObject>> = emptyMap(),
    val productsById: Map<String, JsonObject> = emptyMap(),
    val dashboardRows: Map<String, List<JsonObject>> = emptyMap(),
    val uploadingColumn: String? = null,
    val newOrder: JsonObject? = null
)

class AdminViewModel(
    private val repository: AdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var knownOrderIds: Set<String> = emptySet()
    private var hasLoadedInitialOrders = false

    init {
        selectTable(adminTables.first())
    }

    fun selectTable(table: AdminTable) {
        val current = _uiState.value
        if (current.selectedTable.name == table.name && observeJob?.isActive == true) return

        observeJob?.cancel()
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
                productsById = emptyMap()
            )
        }
        observeJob = viewModelScope.launch {
            repository.observeRows(table).collect { result ->
                result.fold(
                    onSuccess = { rows ->
                        if (table.name == "orders") {
                            detectNewOrder(rows)
                        }
                        _uiState.update {
                            it.copy(rows = rows, isLoading = false, error = null)
                        }
                        if (table.name == "orders") {
                            loadOrderItemsFor(rows)
                        }
                        loadDashboardRows()
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.loadRows(table) }
                .onSuccess { rows ->
                    _uiState.update { it.copy(rows = rows, isLoading = false) }
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
                    .groupBy { it["order_id"].toDisplayText() }
                val productsById = repository.loadProducts()
                    .associateBy { it["id"].toDisplayText() }
                itemsByOrder to productsById
            }.onSuccess { itemsByOrder ->
                _uiState.update {
                    it.copy(
                        orderItemsByOrderId = itemsByOrder.first,
                        productsById = itemsByOrder.second
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

    fun startCreate() {
        val table = _uiState.value.selectedTable
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
                    repository.createRow(state.selectedTable, state.formValues)
                } else {
                    repository.updateRow(state.selectedTable, state.editingRow, state.formValues)
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
            runCatching { repository.deleteRow(table, row) }
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

    fun clearMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun clearNewOrder() {
        _uiState.update { it.copy(newOrder = null) }
    }

    private fun loadDashboardRows() {
        viewModelScope.launch {
            val tables = _uiState.value.tables.filter { it.name in dashboardTableNames }
            val rowsByTable = tables.associate { table ->
                table.name to runCatching { repository.loadRows(table) }.getOrDefault(emptyList())
            } + ("order_items" to runCatching { repository.loadOrderItems() }.getOrDefault(emptyList()))
            _uiState.update { it.copy(dashboardRows = rowsByTable) }
        }
    }

    private fun detectNewOrder(rows: List<JsonObject>) {
        val latestIds = rows.map { it["id"].toDisplayText() }.toSet()
        if (!hasLoadedInitialOrders) {
            knownOrderIds = latestIds
            hasLoadedInitialOrders = true
            return
        }

        val newRows = rows.filter { it["id"].toDisplayText() !in knownOrderIds }
        knownOrderIds = latestIds
        newRows.maxByOrNull { it["created_at"].toDisplayText() }?.let { row ->
            _uiState.update { it.copy(newOrder = row) }
        }
    }

    private companion object {
        val dashboardTableNames = setOf("orders", "products", "users", "reviews", "offers", "banners", "employee", "attendance")
    }
}
