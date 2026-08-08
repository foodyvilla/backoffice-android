package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AttendanceUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeResponse
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletResponse
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.AttendanceRepository
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class EmployeeProfileUiState(
    val employee: EmployeeResponse? = null,
    val outlet: OutletResponse? = null,
    val attendanceHistory: List<AttendanceUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class EmployeeProfileViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EmployeeProfileUiState())
    val state: StateFlow<EmployeeProfileUiState> = _state.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val session = authRepository.currentSession.value
            if (session is UserSession.EmployeeSession) {
                val empId = session.empId.toLongOrNull()
                if (empId != null) {
                    runCatching {
                        val employee = attendanceRepository.fetchEmployeeProfile(empId)
                        val outlet = employee.outlet_id?.let { attendanceRepository.fetchOutletSpecs(it) }
                        val history = attendanceRepository.fetchAttendanceHistory(empId, YearMonth.now())
                        
                        _state.update { it.copy(
                            employee = employee,
                            outlet = outlet,
                            attendanceHistory = history,
                            isLoading = false
                        ) }
                    }.onFailure { e ->
                        _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Invalid Employee ID") }
                }
            } else if (session is UserSession.OutletSession) {
                // For outlet session, we might only have outlet info
                runCatching {
                    val outlet = attendanceRepository.fetchOutletSpecs(session.outletId)
                    _state.update { it.copy(outlet = outlet, isLoading = false) }
                }.onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "No active session") }
            }
        }
    }
}
