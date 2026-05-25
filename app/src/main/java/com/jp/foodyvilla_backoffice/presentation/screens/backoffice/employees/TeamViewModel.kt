package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.Employee
import com.jp.foodyvilla_backoffice.data.model.backoffice.Attendance
import com.jp.foodyvilla_backoffice.data.repo.backoffice.TeamBackOfficeRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class TeamUiState(
    val employees: List<Employee> = emptyList(),
    val attendance: List<Attendance> = emptyList(),
    val rawRows: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val attendanceSearchQuery: String = "",
    val attendanceDateFilter: String? = null,
    val attendanceOutletFilter: String? = null
)

class TeamViewModel(private val repository: TeamBackOfficeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState = _uiState.asStateFlow()

    fun loadEmployees() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getEmployees() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(
                        employees = rows.mapNotNull { runCatching { it.toModel<Employee>() }.getOrNull() },
                        rawRows = rows,
                        isLoading = false
                    ) }
                }
                .onFailure { t -> _uiState.update { it.copy(error = t.message, isLoading = false) } }
        }
    }

    fun loadAttendance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getAttendance() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(
                        attendance = rows.mapNotNull { runCatching { it.toModel<Attendance>() }.getOrNull() },
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

    fun setAttendanceSearch(query: String) {
        _uiState.update { it.copy(attendanceSearchQuery = query) }
    }

    fun setAttendanceDateFilter(date: String?) {
        _uiState.update { it.copy(attendanceDateFilter = date) }
    }

    fun setAttendanceOutletFilter(outletId: String?) {
        _uiState.update { it.copy(attendanceOutletFilter = outletId) }
    }
}
