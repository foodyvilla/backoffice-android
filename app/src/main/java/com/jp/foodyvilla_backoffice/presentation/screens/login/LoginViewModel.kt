package com.jp.foodyvilla_backoffice.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.jp.foodyvilla_backoffice.data.repo.AuthRepo
import com.jp.foodyvilla_backoffice.data.repo.LocationRepository
import com.jp.foodyvilla_backoffice.data.repo.UserRepository
import com.jp.foodyvilla_backoffice.data.repo.NewEmployeeProfileUiModel
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val session: UserSession) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authRepo: AuthRepo,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository,
    private val backOfficeAuthRepository: AuthRepository
) : ViewModel() {

    private val _credentialLoginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val credentialLoginUiState = _credentialLoginUiState.asStateFlow()

    val currentSession = backOfficeAuthRepository.currentSession

    // Expose employee profile state using the project's standard UiState wrapper
    private val _employeeProfileState = MutableStateFlow<UiState<NewEmployeeProfileUiModel>>(UiState.Idle)
    val employeeProfileState = _employeeProfileState.asStateFlow()

    init {
        updateFcmToken()

        // Observe session changes to update FCM token and fetch profile immediately upon login
        viewModelScope.launch {
            currentSession.collectLatest { session ->
                if (session != null) {
                    updateFcmToken()
                    getEmployeeProfile()
                } else {
                    _employeeProfileState.value = UiState.Idle
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            backOfficeAuthRepository.logout()
        }
    }

    fun loginEmployee(identifier: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Employee login requested for identifier=${identifier.trim()}")
            _credentialLoginUiState.value = LoginUiState.Loading
            backOfficeAuthRepository.loginEmployee(identifier = identifier, password = password)
                .onSuccess { session ->
                    Log.d(
                        TAG,
                        "Employee login success: empId=${session.empId}, role=${session.role}, name=${session.name}"
                    )
                    _credentialLoginUiState.value = LoginUiState.Success(session)
                    updateFcmToken()
                    getEmployeeProfile()
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Employee login failed: ${throwable.message}", throwable)
                    _credentialLoginUiState.value =
                        LoginUiState.Error(throwable.message ?: "Unable to sign in")
                }
        }
    }

    /**
     * Fetches the current authenticated employee's detailed workspace profile matrix
     */
    fun getEmployeeProfile() {
        viewModelScope.launch {
            _employeeProfileState.value = UiState.Loading
            try {
                val profile = userRepository.getCurrentEmployeeProfile()
                println("Employee Profile $profile")
                if (profile != null) {
                    _employeeProfileState.value = UiState.Success(profile)
                    // Match and make sure token syncs with accurate database table row identity
                    updateFcmToken()
                } else {
                    _employeeProfileState.value = UiState.Error(Exception("Employee profile record not found."))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading employee profile: ${e.message}", e)
                _employeeProfileState.value = UiState.Error(e)
            }
        }
    }

    fun logoutBackOffice() {
        viewModelScope.launch {
            Log.d(TAG, "Backoffice logout requested")
            // Clear FCM token on server before clearing local session
            try {
                val session = backOfficeAuthRepository.currentSession.value
                val empId = (session as? UserSession.EmployeeSession)?.empId
                userRepository.updateFcmToken(null, empId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear FCM token on logout", e)
            }

            backOfficeAuthRepository.logout()
            _credentialLoginUiState.value = LoginUiState.Idle
            _employeeProfileState.value = UiState.Idle
        }
    }

    fun updateFcmToken(tokenOverride: String? = null) {
        viewModelScope.launch {
            try {
                val token = tokenOverride ?: FirebaseMessaging.getInstance().token.await()
                val session = backOfficeAuthRepository.currentSession.value

                // Prioritize the actual profile ID fallback to active session identity details
                val empId = when (val profile = _employeeProfileState.value) {
                    is UiState.Success -> profile.data.id.toString()
                    else -> (session as? UserSession.EmployeeSession)?.empId
                }

                userRepository.updateFcmToken(
                    fcmToken = token,
                    empId = empId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating FCM token: ${e.message}")
            }
        }
    }

    private companion object {
        const val TAG = "BackOfficeLoginVM"
    }
}