package com.jp.foodyvilla_backoffice.presentation.screens.task_category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.TaskCategory
import com.jp.foodyvilla_backoffice.data.repo.TaskCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskCategoryUiState(
    val categories: List<TaskCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TaskCategoryViewModel(
    private val repository: TaskCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskCategoryUiState())
    val uiState: StateFlow<TaskCategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                repository.getCategories()
            }.onSuccess { categories ->
                _uiState.update { it.copy(categories = categories, isLoading = false) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false, error = throwable.message) }
            }
        }
    }

    fun addCategory(name: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.insertCategory(TaskCategory(name = name, description = description))
            }.onSuccess {
                loadCategories()
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false, error = throwable.message) }
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.deleteCategory(id)
            }.onSuccess {
                loadCategories()
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false, error = throwable.message) }
            }
        }
    }
}
