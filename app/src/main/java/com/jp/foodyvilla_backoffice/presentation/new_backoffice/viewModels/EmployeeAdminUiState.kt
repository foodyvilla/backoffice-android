package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.EmployeeAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmployeeAdminUiState(
    val employeesList: List<EmployeeAdminUiModel> = emptyList(),
    val cachedOutlets: List<OutletResponse> = emptyList(),
    val employeeSearchQuery: String = "",
    val isLoading: Boolean = false,
    val dynamicErrorMessage: String? = null,
    val successMessage: String? = null,

    // Mutable Form Fields
    val isFormWindowOpen: Boolean = false,
    val formWorkspaceTargetId: Long? = null,
    val formOutletId: Long? = null,
    val formName: String = "",
    val formAddress: String = "",
    val formContact: String = "",
    val formAadharNo: String = "",
    val formEmergencyContact: String = "",
    val formSalary: String = "",
    val formProfileImgUrl: String = "",
    val formJoiningDate: String = "",
    val formPasswordText: String = "",
    val formRole: EmployeeRole = EmployeeRole.EMPLOYEE,
    val formIsActive: Boolean = true,
    val formPunchInTime: String = "",
    val formPunchOutTime: String = ""
)

class EmployeeAdminViewModel(private val repository: EmployeeAdminRepository) : ViewModel() {

    private val _state = MutableStateFlow(EmployeeAdminUiState())
    val state = _state.asStateFlow()

    fun updateSearchFilter(q: String) { _state.update { it.copy(employeeSearchQuery = q) } }
    fun onFormNameChanged(v: String) { _state.update { it.copy(formName = v) } }
    fun onFormAddressChanged(v: String) { _state.update { it.copy(formAddress = v) } }
    fun onFormContactChanged(v: String) { _state.update { it.copy(formContact = v) } }
    fun onFormAadharChanged(v: String) { _state.update { it.copy(formAadharNo = v) } }
    fun onFormEmergencyChanged(v: String) { _state.update { it.copy(formEmergencyContact = v) } }
    fun onFormSalaryChanged(v: String) { _state.update { it.copy(formSalary = v) } }
    fun onFormJoiningDateChanged(v: String) { _state.update { it.copy(formJoiningDate = v) } }
    fun onFormPasswordChanged(v: String) { _state.update { it.copy(formPasswordText = v) } }
    fun onFormRoleChanged(r: EmployeeRole) { _state.update { it.copy(formRole = r) } }
    fun onFormActiveToggled(v: Boolean) { _state.update { it.copy(formIsActive = v) } }
    fun onFormOutletSelected(id: Long?) { _state.update { it.copy(formOutletId = id) } }
    fun onFormPunchInTimeChanged(v: String) { _state.update { it.copy(formPunchInTime = v) } }
    fun onFormPunchOutTimeChanged(v: String) { _state.update { it.copy(formPunchOutTime = v) } }

    fun dismissFormWorkspace() { _state.update { it.copy(isFormWindowOpen = false) } }
    fun dismissSuccessDialog() { _state.update { it.copy(successMessage = null) } }
    fun dismissErrorMessage() { _state.update { it.copy(dynamicErrorMessage = null) } }

    suspend fun uploadImageBytes(bytes: ByteArray, prefix: String): String {
        return repository.uploadImageBytes(bytes, prefix)
    }

    suspend fun createEmployeeDirect(model: EmployeeAdminUiModel, imgUrl: String?) {
        repository.createEmployee(model, imgUrl)
    }

    suspend fun updateEmployeeDirect(model: EmployeeAdminUiModel, imgUrl: String?) {
        repository.updateEmployeeRecord(model, imgUrl)
    }

    fun loadAllEmployeesDirectory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, dynamicErrorMessage = null) }
            runCatching {
                val outlets = repository.fetchAllOutletsMinimal()
                val employees = repository.fetchRawEmployeesAdmin()
                val uiModels = employees.map { emp ->
                    val matchedOutlet = outlets.find { it.id == emp.outlet_id }
                    emp.toUiModel(matchedOutlet?.name ?: "Unassigned Outlet")
                }
                Pair(outlets, uiModels)
            }.onSuccess { (outlets, roster) ->
                _state.update { it.copy(cachedOutlets = outlets, employeesList = roster, isLoading = false) }
            }.onFailure { err ->
                _state.update { it.copy(dynamicErrorMessage = err.localizedMessage ?: "Unable to fetch employee roster.", isLoading = false) }
            }
        }
    }

    fun initFormWorkspace(target: EmployeeAdminUiModel?) {
        if (target == null) {
            _state.update { it.copy(
                formWorkspaceTargetId = null, formOutletId = null, formName = "", formAddress = "",
                formContact = "", formAadharNo = "", formEmergencyContact = "", formSalary = "",
                formProfileImgUrl = "", formJoiningDate = "", formPasswordText = "",
                formRole = EmployeeRole.EMPLOYEE, formIsActive = true,
                formPunchInTime = "", formPunchOutTime = "",
                isFormWindowOpen = true
            )}
        } else {
            _state.update { it.copy(
                formWorkspaceTargetId = target.id, formOutletId = target.outletId, formName = target.name,
                formAddress = target.address, formContact = target.contact, formAadharNo = target.aadharNo,
                formEmergencyContact = target.emergencyContact, formSalary = target.salary,
                formProfileImgUrl = target.profileImgUrl, formJoiningDate = target.joiningDate,
                formPasswordText = target.passwordText, formRole = target.role, formIsActive = target.isActive,
                formPunchInTime = target.punchInTime, formPunchOutTime = target.punchOutTime,
                isFormWindowOpen = true
            )}
        }
    }

    fun dispatchCommitCrudAction(localImageUri: Uri?) {
        val s = _state.value
        if (s.formName.isBlank() || s.formContact.isBlank()) {
            _state.update { it.copy(dynamicErrorMessage = "Name and primary contact parameters are required.") }
            return
        }

        val successMsg = if (s.formWorkspaceTargetId == null) {
            "Employee profile created successfully!"
        } else {
            "Employee profile updated successfully!"
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, dynamicErrorMessage = null) }
            runCatching {
                val structuralRemoteUrl = localImageUri?.let { repository.uploadProfileImage(it) }

                val payload = EmployeeAdminUiModel(
                    id = s.formWorkspaceTargetId ?: 0L, outletId = s.formOutletId, name = s.formName,
                    address = s.formAddress, contact = s.formContact, aadharNo = s.formAadharNo,
                    emergencyContact = s.formEmergencyContact, salary = s.formSalary,
                    profileImgUrl = s.formProfileImgUrl, joiningDate = s.formJoiningDate,
                    passwordText = s.formPasswordText, role = s.formRole, isActive = s.formIsActive,
                    punchInTime = s.formPunchInTime, punchOutTime = s.formPunchOutTime
                )

                if (s.formWorkspaceTargetId == null) {
                    repository.createEmployee(payload, structuralRemoteUrl)
                } else {
                    repository.updateEmployeeRecord(payload, structuralRemoteUrl)
                }
            }.onSuccess {
                _state.update { it.copy(isFormWindowOpen = false, successMessage = successMsg, isLoading = false) }
                loadAllEmployeesDirectory()
            }.onFailure { err ->
                _state.update { it.copy(dynamicErrorMessage = err.localizedMessage ?: "Failed to save employee profile.", isLoading = false) }
            }
        }
    }

    fun dispatchPurgeAction(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.deleteEmployeeRecord(id) }
                .onSuccess {
                    _state.update { it.copy(successMessage = "Employee record deleted successfully!", isLoading = false) }
                    loadAllEmployeesDirectory()
                }
                .onFailure { err ->
                    _state.update { it.copy(dynamicErrorMessage = err.localizedMessage ?: "Failed to delete employee record.", isLoading = false) }
                }
        }
    }
}