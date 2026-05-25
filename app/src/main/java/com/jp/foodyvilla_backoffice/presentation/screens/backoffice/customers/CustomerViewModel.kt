package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.User
import com.jp.foodyvilla_backoffice.data.model.backoffice.Order
import com.jp.foodyvilla_backoffice.data.model.backoffice.Cart
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeCustomerRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class CustomerUiState(
    val customers: List<User> = emptyList(),
    val rawRows: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val customerOrders: List<Order> = emptyList(),
    val customerCart: List<Cart> = emptyList()
)

class CustomerViewModel(private val repository: BackOfficeCustomerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState = _uiState.asStateFlow()

    fun loadCustomers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getCustomers() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(
                        rawRows = rows,
                        customers = rows.mapNotNull { runCatching { it.toModel<User>() }.getOrNull() },
                        isLoading = false
                    ) }
                }
                .onFailure { t -> _uiState.update { it.copy(error = t.message, isLoading = false) } }
        }
    }

    fun loadCustomerDetails(customerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, customerOrders = emptyList(), customerCart = emptyList()) }
            val orders = runCatching { repository.getCustomerOrders(customerId) }
                .getOrDefault(emptyList())
                .mapNotNull { runCatching { it.toModel<Order>() }.getOrNull() }
            
            val cart = runCatching { repository.getCustomerCart(customerId) }
                .getOrDefault(emptyList())
                .mapNotNull { runCatching { it.toModel<Cart>() }.getOrNull() }
            
            _uiState.update { it.copy(isLoading = false, customerOrders = orders, customerCart = cart) }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
