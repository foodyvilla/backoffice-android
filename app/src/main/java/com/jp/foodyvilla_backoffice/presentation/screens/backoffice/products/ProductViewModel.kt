package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.ProductCatalog
import com.jp.foodyvilla_backoffice.data.model.backoffice.Category
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeProductRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class ProductUiState(
    val products: List<ProductCatalog> = emptyList(),
    val categories: List<Category> = emptyList(),
    val outletMenu: List<com.jp.foodyvilla_backoffice.data.model.backoffice.OutletMenuItem> = emptyList(),
    val rawRows: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class ProductViewModel(
    private val repository: BackOfficeProductRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState = _uiState.asStateFlow()

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.getProducts()
            }.onSuccess { rows ->
                _uiState.update { state ->
                    state.copy(
                        rawRows = rows,
                        products = rows.mapNotNull { runCatching { it.toModel<ProductCatalog>() }.getOrNull() },
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message, isLoading = false) }
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.getCategories()
            }.onSuccess { rows ->
                _uiState.update { state ->
                    state.copy(
                        rawRows = rows,
                        categories = rows.mapNotNull { runCatching { it.toModel<Category>() }.getOrNull() },
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message, isLoading = false) }
            }
        }
    }

    fun loadOutletMenu() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.getOutletMenu()
            }.onSuccess { rows ->
                _uiState.update { state ->
                    state.copy(
                        rawRows = rows,
                        outletMenu = rows.mapNotNull { runCatching { it.toModel<com.jp.foodyvilla_backoffice.data.model.backoffice.OutletMenuItem>() }.getOrNull() },
                        isLoading = false,
                        error = null
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
}
