package com.jp.foodyvilla_backoffice.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.jp.foodyvilla_backoffice.data.model.user.UserProfile
import com.jp.foodyvilla_backoffice.data.repo.AuthRepo
import com.jp.foodyvilla_backoffice.data.repo.LocationRepository
import com.jp.foodyvilla_backoffice.data.repo.UserRepository
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
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
) :
    ViewModel() {



    init {
        viewModelScope.launch {

            val user = userRepository.getCurrentUserProfile()
            println("ViewModel user $user")
        }
    }

    private val _loginUiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginUiState = _loginUiState.asStateFlow()

    private val _credentialLoginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val credentialLoginUiState = _credentialLoginUiState.asStateFlow()
    val currentSession = backOfficeAuthRepository.currentSession


    private val _getOtpState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val getOtpState = _getOtpState.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()
    private val _otp = MutableStateFlow("")
    val otp = _phoneNumber.asStateFlow()

    fun updatePhone(newValue: String) {
        _phoneNumber.value = newValue
    }

    fun updateOtp(newValue: String) {
        _otp.value = newValue
    }

    private val _isLoggedIn = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        isLoggedIn()
        viewModelScope.launch {
            userRepository.getCurrentUserProfile().collectLatest {
                println("User $it")
            }
        }
        getUserProfile()
        updateFcmToken()

        // Observe session changes to update FCM token immediately when logged in
        viewModelScope.launch {
            currentSession.collectLatest { session ->
                if (session != null) {
                    updateFcmToken()
                }
            }
        }
    }


    private fun isLoggedIn() {
        viewModelScope.launch {

         authRepo.isLoggedIn().collectLatest {
             _isLoggedIn.value = it
         }
            println("Login status : ${authRepo.isLoggedIn()}    ${_isLoggedIn.value}")
        }
    }

    fun login() {
        viewModelScope.launch {
            authRepo.loginWithOtp("+91${phoneNumber.value}").collectLatest {

                println("Login res $it")
                _getOtpState.value = it

            }

        }
    }


    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState = _logoutState.asStateFlow()
    fun logout() {
        viewModelScope.launch {
            authRepo.logout().collect { state ->
                _logoutState.value = state
            }
        }
    }

    fun login(otp: String? = null) {
        viewModelScope.launch {
            authRepo.loginWithOtp("+91${phoneNumber.value}", otp).collectLatest {

                println("Login res $it")
                _loginUiState.value = it
                if (it is UiState.Success) {
                    _isLoggedIn.value = UiState.Success(true)
                    updateFcmToken()
                }
            }

        }
    }

    fun loginOutlet(username: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Outlet login requested for username=${username.trim()}")
            _credentialLoginUiState.value = LoginUiState.Loading
            backOfficeAuthRepository.loginOutlet(username = username, password = password)
                .onSuccess { session ->
                    Log.d(TAG, "Outlet login success: outletId=${session.outletId}, username=${session.username}, role=${session.role}")
                    _credentialLoginUiState.value = LoginUiState.Success(session)
                    updateFcmToken()
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Outlet login failed: ${throwable.message}", throwable)
                    _credentialLoginUiState.value =
                        LoginUiState.Error(throwable.message ?: "Unable to sign in")
                }
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
                        "Employee login success: empId=${session.empId}, outletId=${session.outletId}, designationId=${session.designationId}, role=${session.role}, name=${session.name}, contact=${session.contact}, permissions=${session.permissions}"
                    )
                    _credentialLoginUiState.value = LoginUiState.Success(session)
                    updateFcmToken()
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Employee login failed: ${throwable.message}", throwable)
                    _credentialLoginUiState.value =
                        LoginUiState.Error(throwable.message ?: "Unable to sign in")
                }
        }
    }

    fun logoutBackOffice() {
        viewModelScope.launch {
            Log.d(TAG, "Backoffice logout requested")
            backOfficeAuthRepository.logout()
            _credentialLoginUiState.value = LoginUiState.Idle
        }
    }

    fun updateFcmToken() {
        viewModelScope.launch {
            try {

                val token = FirebaseMessaging.getInstance().token.await()
                userRepository.updateFcmToken(token)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _userState = MutableStateFlow<UiState<UserProfile>>(UiState.Idle)
    val user = _userState.asStateFlow()
    fun getUserProfile() {
        viewModelScope.launch {
            try {
                userRepository.getCurrentUserProfile().collectLatest {


                   if(_userState.value is UiState.Success){
                       if(it is UiState.Success){
                           _userState.value = it
                       }
                   }else{
                       _userState.value = it

                   }

                }
            } catch (e: Exception) {

            }
        }
    }

    fun signInWithSupabase(idToken: String) {
        viewModelScope.launch {
            authRepo.signInWithSupabase(idToken).collectLatest {
                println("Login Res $it")
            }
        }
    }


    private val _locationState =
        MutableStateFlow<UiState<Pair<Double, Double>>>(UiState.Idle)

    val locationState = _locationState.asStateFlow()

    fun hasLocationPermission(): Boolean {
        return locationRepository.hasLocationPermission()
    }

    fun isGpsEnabled(): Boolean {
        return locationRepository.isGpsEnabled()
    }

    fun fetchCurrentLocation() {

        viewModelScope.launch {

            _locationState.value = UiState.Loading

            val result = locationRepository.fetchLocation()

            println("Location fetch $result")
            result.onSuccess { location ->

                _locationState.value = UiState.Success(location)
                val addressResult =
                    locationRepository.getAddressFromLocation(
                        latitude = location.first,
                        longitude = location.second
                    )

                println("Location address fetch $addressResult")


                val address = addressResult.getOrNull() ?: ""
                _userState.update { state ->

                    when (state) {

                        is UiState.Success -> {

                            UiState.Success(
                                state.data.copy(
                                  address = address ?: "",
                                    lat = location.first,
                                    long = location.second

                                )
                            )
                        }

                        else -> state
                    }

                }
            }

            result.onFailure { exception ->

                _locationState.value =
                    UiState.Error(
                        Exception(exception)
                    )
            }
        }
    }

private val _updateState = MutableStateFlow<UiState<String>>(UiState.Idle)
val updateState = _updateState.asStateFlow()

    fun updateProfile(userProfile: UserProfile) {

        viewModelScope.launch {

            _updateState.value = UiState.Loading

            _updateState.value =
                userRepository.updateUserProfile(userProfile)
            getUserProfile()
            delay(1500)
            resetUpdateState()
        }
    }

    fun resetUpdateState(){
        _updateState.value = UiState.Idle
    }

    private companion object {
        const val TAG = "BackOfficeLoginVM"
    }
}


