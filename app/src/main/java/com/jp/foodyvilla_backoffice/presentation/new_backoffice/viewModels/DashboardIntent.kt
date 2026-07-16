package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import AnalyticsRepositoryImpl
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.DashboardFilter
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.DashboardUiState
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.DateRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DashboardIntent {
    data class ApplyFilter(val filter: DashboardFilter, val range: DateRange? = null) : DashboardIntent
    object TriggerSync : DashboardIntent
}

class DashboardViewModel(private val repository: AnalyticsRepositoryImpl) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        dispatch(DashboardIntent.ApplyFilter(DashboardFilter.LAST_7_DAYS))
    }

    fun dispatch(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.ApplyFilter -> pipelineFetch(intent.filter, intent.range)
            is DashboardIntent.TriggerSync -> pipelineFetch(_uiState.value.selectedFilter, _uiState.value.customDateRange)
        }
    }

    private fun pipelineFetch(filter: DashboardFilter, range: DateRange?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
//            val dynamicState = repository.fetchDashboardMetrics(filter, range)
//            _uiState.value = dynamicState
        }
    }
}