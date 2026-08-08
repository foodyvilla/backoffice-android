package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.RestaurantTableDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.TableManagementRepository
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OutletDropdownUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TableManagementAdminUiState(
    val outletId: Long = 0,
    val tables: List<RestaurantTableDto> = emptyList(),
    val outlets: List<OutletDropdownUiModel> = emptyList(),
    val isOwner: Boolean = false,
    val isLoading: Boolean = false,
    val errorText: String? = null,
    
    // Form state
    val isFormOpen: Boolean = false,
    val editingTableId: Long? = null,
    val formTableNumber: String = "",
    val formCapacity: String = "4"
)

class RestaurantTableManagementViewModel(
    private val repository: TableManagementRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TableManagementAdminUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentSession.collect { session ->
                _state.update { it.copy(isOwner = session?.isOwner() ?: false) }
                if (session?.isOwner() == true) {
                    runCatching { repository.getOutlets() }.onSuccess { list ->
                        _state.update { it.copy(outlets = list) }
                        if (list.isNotEmpty()) loadTables(list.first().id)
                    }
                } else {
                    session?.outletId?.let { loadTables(it) }
                }
            }
        }
    }

    fun loadTables(outletId: Long) {
        _state.update { it.copy(outletId = outletId, isLoading = true) }
        viewModelScope.launch {
            runCatching {
                repository.getTablesForOutlet(outletId)
            }.onSuccess { list ->
                _state.update { it.copy(tables = list, isLoading = false) }
            }.onFailure { err ->
                _state.update { it.copy(errorText = err.localizedMessage, isLoading = false) }
            }
        }
    }

    fun openAddForm() {
        _state.update { it.copy(
            isFormOpen = true, 
            editingTableId = null, 
            formTableNumber = "", 
            formCapacity = "4"
        ) }
    }

    fun openEditForm(table: RestaurantTableDto) {
        _state.update { it.copy(
            isFormOpen = true,
            editingTableId = table.id,
            formTableNumber = table.table_number,
            formCapacity = table.capacity.toString()
        ) }
    }

    fun updateFormTableNumber(v: String) { _state.update { it.copy(formTableNumber = v) } }
    fun updateFormCapacity(v: String) { _state.update { it.copy(formCapacity = v) } }
    fun closeForm() { _state.update { it.copy(isFormOpen = false) } }

    fun saveTable() {
        val st = _state.value
        if (st.formTableNumber.isBlank()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val cap = st.formCapacity.toIntOrNull() ?: 4
                if (st.editingTableId == null) {
                    repository.createTable(st.outletId, st.formTableNumber, cap)
                } else {
                    repository.updateTable(st.editingTableId, st.formTableNumber, cap)
                }
            }.onSuccess {
                closeForm()
                loadTables(st.outletId)
            }.onFailure { err ->
                _state.update { it.copy(errorText = err.localizedMessage, isLoading = false) }
            }
        }
    }

    fun deleteTable(tableId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.deleteTable(tableId)
            }.onSuccess {
                loadTables(_state.value.outletId)
            }.onFailure { err ->
                _state.update { it.copy(errorText = err.localizedMessage, isLoading = false) }
            }
        }
    }

    fun clearError() { _state.update { it.copy(errorText = null) } }
}
