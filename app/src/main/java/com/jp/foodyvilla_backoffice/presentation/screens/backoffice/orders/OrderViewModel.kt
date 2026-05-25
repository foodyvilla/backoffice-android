package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.Order
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeOrderRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toDisplayText
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val rawRows: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val dateFilter: String? = null,
    val statusFilter: String? = null,
    val allOrderItems: List<com.jp.foodyvilla_backoffice.data.model.backoffice.OrderItem> = emptyList(),
    val orderItemsByOrderId: Map<String, List<com.jp.foodyvilla_backoffice.data.model.backoffice.OrderItem>> = emptyMap(),
    val productsById: Map<String, com.jp.foodyvilla_backoffice.data.model.backoffice.ProductCatalog> = emptyMap()
)

class OrderViewModel(
    private val repository: BackOfficeOrderRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun loadOrders() {
        if (observeJob?.isActive == true) return
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeOrders().collect { result ->
                result.fold(
                    onSuccess = { rows ->
                        _uiState.update { state ->
                            state.copy(
                                rawRows = rows,
                                orders = rows.mapNotNull { runCatching { it.toModel<Order>() }.getOrNull() },
                                isLoading = false
                            )
                        }
                        loadOrderItemsFor(rows)
                    },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(error = throwable.message, isLoading = false) }
                    }
                )
            }
        }
    }

    fun loadAllOrderItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.getAllOrderItems()
            }.onSuccess { rows ->
                _uiState.update { state ->
                    state.copy(
                        rawRows = rows,
                        allOrderItems = rows.mapNotNull { runCatching { it.toModel<com.jp.foodyvilla_backoffice.data.model.backoffice.OrderItem>() }.getOrNull() },
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message, isLoading = false) }
            }
        }
    }

    private fun loadOrderItemsFor(orders: List<JsonObject>) {
        viewModelScope.launch {
            val orderIds = orders.map { it["id"].toDisplayText() }.toSet()
            if (orderIds.isEmpty()) return@launch
            
            runCatching {
                val itemsByOrder = repository.getOrderItems(orderIds)
                    .map { it.toModel<com.jp.foodyvilla_backoffice.data.model.backoffice.OrderItem>() }
                    .groupBy { it.orderId ?: "" }
                
                val productsById = repository.getProducts()
                    .mapNotNull { runCatching { it.toModel<com.jp.foodyvilla_backoffice.data.model.backoffice.ProductCatalog>() }.getOrNull() }
                    .associateBy { it.id.toString() }
                
                itemsByOrder to productsById
            }.onSuccess { (itemsByOrder, productsById) ->
                _uiState.update {
                    it.copy(
                        orderItemsByOrderId = it.orderItemsByOrderId + itemsByOrder,
                        productsById = it.productsById + productsById
                    )
                }
            }
        }
    }

    fun updateStatus(orderId: String, status: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateOrderStatus(orderId, status)
            }.onSuccess { updatedRow ->
                _uiState.update { state ->
                    val updatedOrders = state.orders.map { 
                        if (it.id == orderId) updatedRow.toModel<Order>() else it 
                    }
                    val updatedRows = state.rawRows.map {
                        if (it["id"].toDisplayText() == orderId) updatedRow else it
                    }
                    state.copy(orders = updatedOrders, rawRows = updatedRows)
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message) }
            }
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.deleteOrder(orderId)
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        orders = state.orders.filter { it.id != orderId },
                        rawRows = state.rawRows.filter { it["id"].toDisplayText() != orderId },
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message, isLoading = false) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setDateFilter(date: String?) {
        _uiState.update { it.copy(dateFilter = date) }
    }

    fun setStatusFilter(status: String?) {
        _uiState.update { it.copy(statusFilter = status) }
    }
}
