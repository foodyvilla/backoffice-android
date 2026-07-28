package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.CustomerManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerManagementUiState(
    val customersList: List<CustomerUiModel> = emptyList(),
    val customerSearchQuery: String = "",
    val isLoading: Boolean = false,
    val errorText: String? = null,

    // Form tracking states
    val targetCustomerId: Long? = null,
    val cName: String = "",
    val cPhone: String = "",
    val cEmail: String = "",
    val cAddress: String = "",
    val cIsVerified: Boolean = false,

    // Deep Analytics Metrics
    val activeDetails: CustomerDetailedAnalytics = CustomerDetailedAnalytics()
)

class CustomerManagementViewModel(private val repository: CustomerManagementRepository) : ViewModel() {

    private val _state = MutableStateFlow(CustomerManagementUiState())
    val state = _state.asStateFlow()

    fun updateSearchQuery(q: String) { _state.update { it.copy(customerSearchQuery = q) } }
    fun onNameChanged(v: String) { _state.update { it.copy(cName = v) } }
    fun onPhoneChanged(v: String) { _state.update { it.copy(cPhone = v) } }
    fun onEmailChanged(v: String) { _state.update { it.copy(cEmail = v) } }
    fun onAddressChanged(v: String) { _state.update { it.copy(cAddress = v) } }
    fun onVerifiedToggled(v: Boolean) { _state.update { it.copy(cIsVerified = v) } }

    fun loadAllCustomers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.fetchAllCustomers() }
                .onSuccess { list -> _state.update { it.copy(customersList = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(errorText = e.localizedMessage, isLoading = false) } }
        }
    }

    fun setupForm(customer: CustomerUiModel?) {
        if (customer == null) {
            _state.update { it.copy(targetCustomerId = null, cName = "", cPhone = "", cEmail = "", cAddress = "", cIsVerified = false) }
        } else {
            _state.update { it.copy(targetCustomerId = customer.id, cName = customer.name, cPhone = customer.phone, cEmail = customer.email, cAddress = customer.address, cIsVerified = customer.isVerified) }
        }
    }

    fun commitCustomerCrudAction(onSuccess: () -> Unit) {
        val s = _state.value
        val payload = CustomerUiModel(id = s.targetCustomerId ?: 0L, name = s.cName, phone = s.cPhone, email = s.cEmail, address = s.cAddress, isVerified = s.cIsVerified)
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                if (s.targetCustomerId == null) repository.insertCustomer(payload) else repository.updateCustomer(payload)
            }.onSuccess { loadAllCustomers(); onSuccess() }
             .onFailure { e -> _state.update { it.copy(errorText = e.localizedMessage, isLoading = false) } }
        }
    }

    fun removeCustomerRecord(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.deleteCustomer(id) }.onSuccess { loadAllCustomers() }
                .onFailure { e -> _state.update { it.copy(errorText = e.localizedMessage, isLoading = false) } }
        }
    }

    // --- DEPTH ANALYTICS SYNC LOOPS ---
    fun loadCustomerCompleteProfileDetails(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.fetchDetailedAnalytics(id) }
                .onSuccess { metrics -> _state.update { it.copy(activeDetails = metrics, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(errorText = e.localizedMessage, isLoading = false) } }
        }
    }

    // --- LIVE COMMUNICATION CHANNEL EDGES INTERFACES ---
    fun sendFcmPushNotification(token: String, title: String, desc: String, url: String, onDone: () -> Unit) {
        println("Token $token")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                runCatching { repository.invokeFcmNotificationEdgeFunction(token, title, desc, url) }
                    .onSuccess { 
                        _state.update { it.copy(isLoading = false) }
                        onDone() 
                    }
                    .onFailure { e -> _state.update { it.copy(errorText = e.localizedMessage, isLoading = false) } }
            } catch (e : Exception) {
                _state.update { it.copy(errorText = e.localizedMessage, isLoading = false) }
                print(e.toString())
            }
        }
    }

    fun sendWhatsAppTemplateBroadcast(context: Context, phone: String, msg: String, imgUri: Uri?, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val uploadedUrl = imgUri?.let { repository.uploadWhatsAppImage(context, it) }
                repository.invokeWhatsAppTemplateEdgeFunction(phone, msg, uploadedUrl)
            }.onSuccess {
                _state.update { it.copy(isLoading = false) }
                onDone()
            }.onFailure { e ->
                _state.update { it.copy(errorText = e.localizedMessage, isLoading = false) }
            }
        }
    }
}