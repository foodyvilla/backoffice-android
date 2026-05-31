package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.AttendanceRepository
import com.jp.foodyvilla_backoffice.data.repo.LocationRepository
import com.jp.foodyvilla_backoffice.data.repo.LocationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

data class AttendanceUiState(
    val activeEmployee: EmployeeResponse? = null,
    val selectedOutletId: Long? = null,
    val globalOutletsList: List<OutletUiModel> = emptyList(),
    val outletEmployeesList: List<EmployeeResponse> = emptyList(),
    val attendanceMap: Map<LocalDate, AttendanceUiModel> = emptyMap(),
    val currentYearMonth: YearMonth = YearMonth.now(),
    val selectedDayRecord: AttendanceUiModel? = null,
    val isLoading: Boolean = false,
    val dynamicExecutionErrorMessage: String? = null,
    val locationUiState: LocationUiState = LocationUiState()
)

class AttendanceViewModel(
    private val repository: AttendanceRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceUiState())
    val state = _state.asStateFlow()

    fun dismissPermissionRationale() = _state.update { it.copy(locationUiState = it.locationUiState.copy(showPermissionRationaleDialog = false)) }
    fun dismissGpsDialog() = _state.update { it.copy(locationUiState = it.locationUiState.copy(showGpsDisabledDialog = false)) }
    fun selectDayRecord(record: AttendanceUiModel?) = _state.update { it.copy(selectedDayRecord = record) }

    fun shiftCalendarMonthBackward() {
        val newMonth = _state.value.currentYearMonth.minusMonths(1)
        _state.update { it.copy(currentYearMonth = newMonth) }
        _state.value.activeEmployee?.id?.let { loadAttendanceOnly(it, newMonth) }
    }

    fun shiftCalendarMonthForward() {
        val current = _state.value.currentYearMonth
        if (current.isBefore(YearMonth.now())) {
            val newMonth = current.plusMonths(1)
            _state.update { it.copy(currentYearMonth = newMonth) }
            _state.value.activeEmployee?.id?.let { loadAttendanceOnly(it, newMonth) }
        }
    }

    fun updateCalendarJumpTarget(month: Int, year: Int) {
        val targetMonth = YearMonth.of(year, month)
        _state.update { it.copy(currentYearMonth = targetMonth) }
        _state.value.activeEmployee?.id?.let { loadAttendanceOnly(it, targetMonth) }
    }

    fun loadAttendanceOnly(empId: Long, month: YearMonth) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.fetchAttendanceHistory(empId, month) }
                .onSuccess { coreHistory ->
                    val fullHistoryMap = evaluateHistoricalAbsences(coreHistory, month, empId)
                    _state.update { it.copy(attendanceMap = fullHistoryMap, isLoading = false) }
                }
                .onFailure { err -> _state.update { it.copy(dynamicExecutionErrorMessage = err.localizedMessage, isLoading = false) } }
        }
    }

    fun loadEmployeeContext(empId: Long) {
        viewModelScope.launch {
            val currentMonth = _state.value.currentYearMonth
            _state.update { it.copy(isLoading = true, dynamicExecutionErrorMessage = null) }
            runCatching {
                val profile = repository.fetchEmployeeProfile(empId)
                val coreHistory = repository.fetchAttendanceHistory(empId, currentMonth)
                Pair(profile, coreHistory)
            }.onSuccess { (profile, coreHistory) ->
                val fullHistoryMap = evaluateHistoricalAbsences(coreHistory, currentMonth, empId)
                _state.update { it.copy(activeEmployee = profile, attendanceMap = fullHistoryMap, isLoading = false) }
            }.onFailure { err -> _state.update { it.copy(dynamicExecutionErrorMessage = err.localizedMessage, isLoading = false) } }
        }
    }

    fun loadSelfEmployeeContext() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, dynamicExecutionErrorMessage = null) }
            runCatching {
                val authUser = repository.getCurrentSessionUser() ?: throw Exception("No active session found.")
                val employee = repository.fetchEmployeeProfileByAuthId(authUser.id) ?: throw Exception("Profile not found as employee row.")
                employee.id
            }.onSuccess { empId ->
                loadEmployeeContext(empId)
            }.onFailure { err ->
                _state.update { it.copy(dynamicExecutionErrorMessage = err.localizedMessage, isLoading = false) }
            }
        }
    }

    fun executeSelfTransactionalPunchWorkflow(isPunchOut: Boolean) {
        val activeEmpId = _state.value.activeEmployee?.id
        if (activeEmpId == null) {
            _state.update { it.copy(dynamicExecutionErrorMessage = "Missing employee profile identity context.") }
            return
        }
        executeTransactionalPunchWorkflow(empId = activeEmpId, isPunchOut = isPunchOut)
    }

    fun executeTransactionalPunchWorkflow(empId: Long, isPunchOut: Boolean) {
        if (!locationRepository.hasLocationPermission()) {
            _state.update { it.copy(locationUiState = it.locationUiState.copy(showPermissionRationaleDialog = true)) }
            return
        }
        if (!locationRepository.isGpsEnabled()) {
            _state.update { it.copy(locationUiState = it.locationUiState.copy(showGpsDisabledDialog = true)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(locationUiState = it.locationUiState.copy(isFetchingLocation = true), dynamicExecutionErrorMessage = null) }
            locationRepository.fetchLocation()
                .onSuccess { fixedCoordinates ->
                    _state.update { it.copy(locationUiState = it.locationUiState.copy(isFetchingLocation = false, latitude = fixedCoordinates.first, longitude = fixedCoordinates.second)) }

                    if (!isPunchOut) {
                        repository.recordPunchIn(empId, fixedCoordinates.first, fixedCoordinates.second, LocalDateTime.now())
                            .onSuccess { loadEmployeeContext(empId) }
                            .onFailure { e -> _state.update { it.copy(dynamicExecutionErrorMessage = e.localizedMessage) } }
                    } else {
                        val todaysRecord = _state.value.attendanceMap[LocalDate.now()]
                        if (todaysRecord != null && todaysRecord.id != -1L) {
                            repository.recordPunchOut(todaysRecord.id, fixedCoordinates.first, fixedCoordinates.second, LocalDateTime.now())
                                .onSuccess { loadEmployeeContext(empId) }
                                .onFailure { e -> _state.update { it.copy(dynamicExecutionErrorMessage = e.localizedMessage) } }
                        } else {
                            _state.update { it.copy(dynamicExecutionErrorMessage = "No active valid clock-in session found to sign out.") }
                        }
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(locationUiState = it.locationUiState.copy(isFetchingLocation = false, locationErrorMessage = err.localizedMessage)) }
                }
        }
    }

    private fun evaluateHistoricalAbsences(punches: List<AttendanceUiModel>, month: YearMonth, empId: Long): Map<LocalDate, AttendanceUiModel> {
        val map = punches.associateBy { it.date }.toMutableMap()
        val sequenceStartBound = month.atDay(1)
        val today = LocalDate.now()
        val executionCeilingBound = if (month == YearMonth.now()) today else month.atEndOfMonth()

        var structuralIndexIterator = sequenceStartBound
        while (!structuralIndexIterator.isAfter(executionCeilingBound)) {
            if (!map.containsKey(structuralIndexIterator)) {
                val status = if (structuralIndexIterator.isBefore(today)) AttendanceStatus.ABSENT else AttendanceStatus.PENDING
                map[structuralIndexIterator] = AttendanceUiModel(
                    id = -1L, date = structuralIndexIterator, empId = empId, status = status,
                    inTime = null, outTime = null, inLat = null, inLng = null, outLat = null, outLng = null
                )
            }
            structuralIndexIterator = structuralIndexIterator.plusDays(1)
        }
        return map
    }

    fun loadManagementOutlets(outlets: List<OutletUiModel>) { _state.update { it.copy(globalOutletsList = outlets) } }
    fun onOutletSelected(outletId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(selectedOutletId = outletId, isLoading = true, attendanceMap = emptyMap(), activeEmployee = null) }
            runCatching { repository.fetchEmployeesByOutlet(outletId) }.onSuccess { staff -> _state.update { it.copy(outletEmployeesList = staff, isLoading = false) } }
        }
    }
}