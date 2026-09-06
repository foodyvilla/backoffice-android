package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.AttendanceAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AttendanceAdminUiState(
    val recordsList: List<AttendanceAdminUiModel> = emptyList(),
    val cachedEmployees: List<EmployeeAdminMinimalResponse> = emptyList(),
    val logsSearchQuery: String = "",
    val isLoading: Boolean = false,
    val dynamicErrorMessage: String? = null,
    val successMessage: String? = null,

    // Filters
    val filterEmpId: Long? = null,
    val filterStatus: AdminAttendanceStatus? = null,
    val filterDate: String = "", // YYYY-MM-DD
    val filterMonth: Int? = null, // 1-12

    // Sheet Workspace Properties
    val isFormWindowOpen: Boolean = false,
    val formWorkspaceTargetId: Long? = null,
    val formEmpId: Long = 0L,
    val formStatus: AdminAttendanceStatus = AdminAttendanceStatus.PRESENT,
    val formInTime: String = "",
    val formOutTime: String = "",
    val formInLat: String = "",
    val formInLng: String = "",
    val formOutLat: String = "",
    val formOutLng: String = ""
)

class AttendanceAdminViewModel(private val repository: AttendanceAdminRepository) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceAdminUiState())
    val state = _state.asStateFlow()

    fun updateSearchFilter(q: String) { _state.update { it.copy(logsSearchQuery = q) } }
    fun onFilterEmpSelected(id: Long?) { _state.update { it.copy(filterEmpId = id) } }
    fun onFilterStatusSelected(s: AdminAttendanceStatus?) { _state.update { it.copy(filterStatus = s) } }
    fun onFilterDateChanged(d: String) { _state.update { it.copy(filterDate = d) } }
    fun onFilterMonthChanged(m: Int?) { _state.update { it.copy(filterMonth = m) } }

    fun onFormEmpSelected(id: Long) { _state.update { it.copy(formEmpId = id) } }
    fun onFormStatusChanged(s: AdminAttendanceStatus) { _state.update { it.copy(formStatus = s) } }
    fun onFormInTimeChanged(t: String) { _state.update { it.copy(formInTime = t) } }
    fun onFormOutTimeChanged(t: String) { _state.update { it.copy(formOutTime = t) } }
    fun onFormInLatChanged(v: String) { _state.update { it.copy(formInLat = v) } }
    fun onFormInLngChanged(v: String) { _state.update { it.copy(formInLng = v) } }
    fun onFormOutLatChanged(v: String) { _state.update { it.copy(formOutLat = v) } }
    fun onFormOutLngChanged(v: String) { _state.update { it.copy(formOutLng = v) } }
    
    fun dismissFormWorkspace() { _state.update { it.copy(isFormWindowOpen = false) } }
    fun dismissSuccessDialog() { _state.update { it.copy(successMessage = null) } }
    fun dismissErrorMessage() { _state.update { it.copy(dynamicErrorMessage = null) } }

    fun loadAllLogsDirectory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, dynamicErrorMessage = null) }
            runCatching {
                val staffList = repository.fetchAllEmployeesMinimal()
                val rawPunches = repository.fetchRawLogsAdmin()
                val mappedUiLogs = rawPunches.map { punch ->
                    val match = staffList.find { it.id == punch.emp_id }
                    punch.toUiModel(match?.name ?: "ID Reference: ${punch.emp_id}")
                }
                Pair(staffList, mappedUiLogs)
            }.onSuccess { (staff, logs) ->
                _state.update { it.copy(cachedEmployees = staff, recordsList = logs, isLoading = false) }
            }.onFailure { err ->
                _state.update { it.copy(dynamicErrorMessage = err.localizedMessage ?: "Unable to fetch logs directory.", isLoading = false) }
            }
        }
    }

    fun initFormWorkspace(target: AttendanceAdminUiModel?) {
        val staffCache = _state.value.cachedEmployees
        val fallbackId = staffCache.firstOrNull()?.id ?: 0L
        
        if (target == null) {
            _state.update { it.copy(
                formWorkspaceTargetId = null, formEmpId = fallbackId, formStatus = AdminAttendanceStatus.PRESENT,
                formInTime = "", formOutTime = "", formInLat = "", formInLng = "", formOutLat = "", formOutLng = "",
                isFormWindowOpen = true
            )}
        } else {
            _state.update { it.copy(
                formWorkspaceTargetId = target.id, formEmpId = target.empId, formStatus = target.status,
                formInTime = target.inTime, formOutTime = target.outTime, 
                formInLat = target.inLat, formInLng = target.inLng,
                formOutLat = target.outLat, formOutLng = target.outLng,
                isFormWindowOpen = true
            )}
        }
    }

    fun dispatchCommitCrudAction() {
        val s = _state.value
        if (s.formInTime.isBlank()) {
            _state.update { it.copy(dynamicErrorMessage = "Punch In timestamp cannot be blank.") }
            return
        }

        val payload = AttendanceAdminUiModel(
            id = s.formWorkspaceTargetId ?: 0L, empId = s.formEmpId, status = s.formStatus,
            inTime = s.formInTime, outTime = s.formOutTime,
            inLat = s.formInLat, inLng = s.formInLng, outLat = s.formOutLat, outLng = s.formOutLng
        )

        val successMsg = if (s.formWorkspaceTargetId == null) {
            "Attendance record added successfully!"
        } else {
            "Attendance record updated successfully!"
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                if (s.formWorkspaceTargetId == null) {
                    repository.createAttendanceRecord(payload)
                } else {
                    repository.updateAttendanceRecord(payload)
                }
            }.onSuccess {
                _state.update { it.copy(isFormWindowOpen = false, successMessage = successMsg, isLoading = false) }
                loadAllLogsDirectory()
            }.onFailure { err ->
                _state.update { it.copy(dynamicErrorMessage = err.localizedMessage ?: "Failed to save attendance record.", isLoading = false) }
            }
        }
    }

    fun dispatchPurgeAction(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.purgeAttendanceLogRecord(id) }
                .onSuccess {
                    _state.update { it.copy(successMessage = "Attendance log entry deleted successfully!", isLoading = false) }
                    loadAllLogsDirectory()
                }
                .onFailure { err ->
                    _state.update { it.copy(dynamicErrorMessage = err.localizedMessage ?: "Failed to delete attendance record.", isLoading = false) }
                }
        }
    }
}