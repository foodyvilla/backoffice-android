package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.outlets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeOutletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class OutletUiState(
    val rawRows: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class OutletViewModel(private val repository: BackOfficeOutletRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(OutletUiState())
    val uiState = _uiState.asStateFlow()

    fun loadOutlets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getOutlets() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(
                        rawRows = rows,
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
