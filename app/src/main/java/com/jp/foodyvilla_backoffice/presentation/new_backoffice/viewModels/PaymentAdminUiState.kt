package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.PaymentAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class PaymentAdminUiState(
    val transactionsList: List<PaymentAdminUiModel> = emptyList(),
    val cachedUsers: List<UserResponse> = emptyList(),
    val currentSelectedMonth: YearMonth = YearMonth.now(),
    val paymentSearchQuery: String = "",
    val isLoading: Boolean = false,
    val dynamicErrorMessage: String? = null,

    // Sheet Workspace Input Bindings
    val isFormWindowOpen: Boolean = false,
    val formWorkspaceTargetId: Long? = null,
    val formOrderId: String = "",
    val formCustomerId: Long = 0L,
    val formRazorpayOrderId: String = "",
    val formRazorpayPaymentId: String = "",
    val formAmountInput: String = "",
    val formStatus: AdminPaymentStatus = AdminPaymentStatus.CREATED,
    val formMethod: AdminPaymentMethod = AdminPaymentMethod.UNKNOWN
)

class PaymentAdminViewModel(private val repository: PaymentAdminRepository) : ViewModel() {

    private val _state = MutableStateFlow(PaymentAdminUiState())
    val state = _state.asStateFlow()

    fun updateSearchFilter(q: String) { _state.update { it.copy(paymentSearchQuery = q) } }
    fun onFormOrderIdChanged(v: String) { _state.update { it.copy(formOrderId = v) } }
    fun onFormCustomerSelected(id: Long) { _state.update { it.copy(formCustomerId = id) } }
    fun onFormRpOrderChanged(v: String) { _state.update { it.copy(formRazorpayOrderId = v) } }
    fun onFormRpPaymentChanged(v: String) { _state.update { it.copy(formRazorpayPaymentId = v) } }
    fun onFormAmountChanged(v: String) { _state.update { it.copy(formAmountInput = v) } }
    fun onFormStatusChanged(s: AdminPaymentStatus) { _state.update { it.copy(formStatus = s) } }
    fun onFormMethodChanged(m: AdminPaymentMethod) { _state.update { it.copy(formMethod = m) } }
    
    fun dismissFormWorkspace() { _state.update { it.copy(isFormWindowOpen = false) } }

    fun changeFilterMonth(month: Int, year: Int) {
        val newTargetMonth = YearMonth.of(year, month)
        _state.update { it.copy(currentSelectedMonth = newTargetMonth) }
        loadAllPaymentsDirectory()
    }

    fun loadAllPaymentsDirectory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, dynamicErrorMessage = null) }
            runCatching {
                val coreUsers = repository.fetchAllUsersMinimal()
                val targetMonth = _state.value.currentSelectedMonth
                val rawPayments = repository.fetchRawPaymentsAdmin(targetMonth)
                
                val uiTransactions = rawPayments.map { raw ->
                    val userMatch = coreUsers.find { it.id == raw.customer_id }
                    raw.toUiModel(
                        customerName = userMatch?.name ?: "User ID: ${raw.customer_id ?: "N/A"}",
                        contact = userMatch?.phone ?: "N/A"
                    )
                }
                Pair(coreUsers, uiTransactions)
            }.onSuccess { (users, logs) ->
                _state.update { it.copy(cachedUsers = users, transactionsList = logs, isLoading = false) }
            }.onFailure { err ->
                _state.update { it.copy(dynamicErrorMessage = err.localizedMessage, isLoading = false) }
            }
        }
    }

    fun initFormWorkspace(target: PaymentAdminUiModel?) {
        val userCache = _state.value.cachedUsers
        val fallbackId = userCache.firstOrNull()?.id ?: 0L

        if (target == null) {
            _state.update { it.copy(
                formWorkspaceTargetId = null, formOrderId = "", formCustomerId = fallbackId,
                formRazorpayOrderId = "", formRazorpayPaymentId = "", formAmountInput = "",
                formStatus = AdminPaymentStatus.CREATED, formMethod = AdminPaymentMethod.UNKNOWN,
                isFormWindowOpen = true
            )}
        } else {
            _state.update { it.copy(
                formWorkspaceTargetId = target.id, formOrderId = target.orderId, formCustomerId = target.customerId,
                formRazorpayOrderId = target.razorpayOrderId, formRazorpayPaymentId = target.razorpayPaymentId,
                formAmountInput = target.amountDisplay.toString(), formStatus = target.status, formMethod = target.method,
                isFormWindowOpen = true
            )}
        }
    }

    fun dispatchCommitCrudAction() {
        val s = _state.value
        val parsedAmount = s.formAmountInput.toDoubleOrNull()
        if (s.formOrderId.isBlank() || parsedAmount == null || parsedAmount <= 0.0) {
            _state.update { it.copy(dynamicErrorMessage = "Please supply a valid system Order ID reference string and a positive numeric amount value.") }
            return
        }

        val payload = PaymentAdminUiModel(
            id = s.formWorkspaceTargetId ?: 0L, orderId = s.formOrderId, customerId = s.formCustomerId,
            razorpayOrderId = s.formRazorpayOrderId, razorpayPaymentId = s.formRazorpayPaymentId,
            amountDisplay = parsedAmount, status = s.formStatus, method = s.formMethod
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                if (s.formWorkspaceTargetId == null) repository.createPaymentRecord(payload)
                else repository.updatePaymentRecord(payload)
            }.onSuccess {
                _state.update { it.copy(isFormWindowOpen = false) }
                loadAllPaymentsDirectory()
            }.onFailure { err ->
                _state.update { it.copy(dynamicErrorMessage = err.localizedMessage, isLoading = false) }
            }
        }
    }

    fun dispatchPurgeAction(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.purgePaymentLogRecord(id) }
                .onSuccess { loadAllPaymentsDirectory() }
                .onFailure { err -> _state.update { it.copy(dynamicErrorMessage = err.localizedMessage, isLoading = false) } }
        }
    }
}