package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import com.jp.foodyvilla_backoffice.data.repo.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class DashboardUiState(
    val dashboardData: DashboardData = DashboardData(),
    val dashboardRows: Map<String, List<JsonObject>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val orders = repository.loadOrders()
                val products = repository.loadProductCatalog()
                val users = repository.loadUsers()
                val orderItems = repository.loadOrderItems()

                val data = DashboardData(
                    orders = orders.mapNotNull { runCatching { it.toModel<Order>() }.getOrNull() },
                    orderItems = orderItems.mapNotNull { runCatching { it.toModel<OrderItem>() }.getOrNull() },
                    products = products.mapNotNull { runCatching { it.toModel<ProductCatalog>() }.getOrNull() },
                    users = users.mapNotNull { runCatching { it.toModel<User>() }.getOrNull() }
                )
                val rowsByTable = mapOf(
                    "orders" to orders,
                    "product_catalog" to products,
                    "users" to users,
                    "order_items" to orderItems,
                    "employee" to repository.loadEmployees(),
                    "outlets" to repository.loadOutlets(),
                    "attendance" to repository.loadAttendance()
                )
                data to rowsByTable
            }.onSuccess { (data, rows) ->
                _uiState.update { it.copy(
                    dashboardData = data,
                    dashboardRows = rows,
                    isLoading = false
                ) }
            }.onFailure { t ->
                _uiState.update { it.copy(error = t.message, isLoading = false) }
            }
        }
    }
}
