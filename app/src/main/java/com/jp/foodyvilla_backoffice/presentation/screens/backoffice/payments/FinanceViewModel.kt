package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.Payment
import com.jp.foodyvilla_backoffice.data.model.backoffice.Cart
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeFinanceRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class FinanceUiState(
    val payments: List<Payment> = emptyList(),
    val cartItems: List<Cart> = emptyList(),
    val rawRows: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class FinanceViewModel(private val repository: BackOfficeFinanceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState = _uiState.asStateFlow()

    fun loadPayments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getPayments() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(
                        rawRows = rows,
                        payments = rows.mapNotNull { runCatching { it.toModel<Payment>() }.getOrNull() },
                        isLoading = false
                    ) }
                }
                .onFailure { t -> _uiState.update { it.copy(error = t.message, isLoading = false) } }
        }
    }

    fun loadCartItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getCartItems() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(
                        rawRows = rows,
                        cartItems = rows.mapNotNull { runCatching { it.toModel<Cart>() }.getOrNull() },
                        isLoading = false
                    ) }
                }
                .onFailure { t -> _uiState.update { it.copy(error = t.message, isLoading = false) } }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
