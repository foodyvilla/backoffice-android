package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AnalyticsUiState
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.NewOrdersManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class AnalyticsViewModel(private val repository: NewOrdersManagementRepository) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsUiState())
    val state = _state.asStateFlow()

    init {
        refreshAnalytics()
    }

    fun refreshAnalytics() {
        val current = _state.value
        loadAnalytics(current.startDate, current.endDate)
    }

    fun loadAnalytics(start: LocalDate, end: LocalDate) {
        _state.update { it.copy(isLoading = true, error = null, startDate = start, endDate = end) }
        viewModelScope.launch {
            val response = repository.getAnalyticsSummary(start, end)
            if (response.success && response.data != null) {
                _state.update { it.copy(summary = response.data, isLoading = false) }
            } else {
                _state.update { it.copy(error = response.error ?: "Failed to fetch analytics", isLoading = false) }
            }
        }
    }
}
