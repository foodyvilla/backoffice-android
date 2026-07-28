package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewCategoryUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewCustomerUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewDetailedOrderUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOrderType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOrderUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.NewOrdersManagementRepository
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOutletMenuUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewSelectedMenuItem
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OutletDropdownUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UnifiedOrderSystemUiState(
    val orders: List<NewDetailedOrderUiModel> = emptyList(),
    val menuItems: List<NewOutletMenuUiModel> = emptyList(),
    val categories: List<NewCategoryUiModel> = emptyList(),
    val outlets: List<OutletDropdownUiModel> = emptyList(),

    // Operational Filters
    val selectedCategory: String? = null,
    val menuSearchQuery: String = "",
    val cartSelectedItems: Map<Long, NewSelectedMenuItem> = emptyMap(),
    val activeSelectedDate: LocalDate = LocalDate.now(),

    // Loading & Status
    val isLoading: Boolean = false,
    val isPlacingOrderState: Boolean = false,
    val globalErrorMessage: String? = null,

    // Permissions & Visibility
    val isOwnerUser: Boolean = false,
    val canAcceptOrders: Boolean = false,
    val canManageMenu: Boolean = false,
    val userRole: String = "employee",
    val activeSelectedOutlet: OutletDropdownUiModel? = null,
    val isOperationAllowed: Boolean = false,

    // Form Checkout States
    val customerPhone: String = "",
    val resolvedCustomer: NewCustomerUiModel? = null,
    val checkoutOrderType: NewOrderType = NewOrderType.DELIVERY,
    val checkoutInstruction: String = "",
    val checkoutAddress: String = "",
    val summaryPendingDialogOrder: NewOrderUiModel? = null,

    // Detailed Update Panel State
    val targetedEditingOrder: NewDetailedOrderUiModel? = null,
    val formEditAddress: String = "",
    val formEditInstruction: String = "",
    val formEditStatus: String = "",
    val formEditType: String = "DINE_IN",

    // Real-time Heads-up Popups
    val realTimeIncomingInterceptedOrder: NewDetailedOrderUiModel? = null
)

class UnifiedOrderControlViewModel(
    private val repository: NewOrdersManagementRepository,
    private val backOfficeAuthRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UnifiedOrderSystemUiState())
    val state = _state.asStateFlow()

    private var internalEmployeeRowId: Long? = null
    private var sessionOutletId: Long? = null
    private var userDesignationRole: String? = "employee"
    private var previousOrdersChecksum: List<NewDetailedOrderUiModel> = emptyList()
    private var realtimeStreamJob: Job? = null

    init {
        viewModelScope.launch {
            backOfficeAuthRepository.currentSession.collectLatest { session ->
                if (session == null) return@collectLatest
                
                val isOwner = session.isOwner()
                val operationalGating = isOwner || (session.outletId != 0L)
                
                userDesignationRole = session.role()
                sessionOutletId = session.outletId

                _state.update {
                    it.copy(
                        isOwnerUser = isOwner,
                        isOperationAllowed = operationalGating,
                        canAcceptOrders = session.canAcceptOrders(),
                        canManageMenu = session.canManageMenu(),
                        userRole = userDesignationRole!!
                    )
                }

                if (isOwner) {
                    val list = repository.fetchActiveOutletsList()
                    _state.update { it.copy(outlets = list, activeSelectedOutlet = null) }
                    restartRealtimeSubscriptionChannel()
                } else if (sessionOutletId != null) {
                    val branch = OutletDropdownUiModel(sessionOutletId!!, "Assigned Branch Profile")
                    _state.update { it.copy(activeSelectedOutlet = branch) }
                    restartRealtimeSubscriptionChannel()
                }
            }
        }
    }

    private fun emitTemporaryError(msg: String) {
        viewModelScope.launch {
            _state.update { it.copy(globalErrorMessage = msg, isLoading = false, isPlacingOrderState = false) }
            delay(4000)
            _state.update { s -> if (s.globalErrorMessage == msg) s.copy(globalErrorMessage = null) else s }
        }
    }

    // ==========================================
    // Realtime Stream Subscription Controller
    // ==========================================

    fun updateFilterDate(newDate: LocalDate) {
        _state.update { it.copy(activeSelectedDate = newDate) }
        restartRealtimeSubscriptionChannel()
    }

    fun selectOutletScope(outlet: OutletDropdownUiModel?) {
        if (!_state.value.isOwnerUser) return
        _state.update {
            it.copy(
                activeSelectedOutlet = outlet,
                isOperationAllowed = outlet != null,
                cartSelectedItems = emptyMap(),
                orders = emptyList()
            )
        }
        restartRealtimeSubscriptionChannel()
    }

    fun updateCustomCustomerName(name: String) {
        _state.update { current ->
            if (current.resolvedCustomer == null) {
                // Mock a customer layout data transfer object if it's a completely new customer entry
                current.copy(
                    resolvedCustomer = NewCustomerUiModel(
                        id = 0L, // Handled as serial identity bypass safely
                        name = name,
                        phone = current.customerPhone,
                        address = current.checkoutAddress,
                        lat = null, lng = null, fcmToken = null
                    )
                )
            } else {
                current
            }
        }
    }
    private fun restartRealtimeSubscriptionChannel() {
        realtimeStreamJob?.cancel()

        val activeOutletId = _state.value.activeSelectedOutlet?.id
        val isOwner = _state.value.isOwnerUser
        val currentDateFilter = _state.value.activeSelectedDate

        // Deny channel connection if user is an unlinked employee node
        if (!isOwner && activeOutletId == null) return

        realtimeStreamJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Fetch menu structural configurations asynchronously
            if (activeOutletId != null) {
                runCatching {
                    val menuList = repository.getOutletMenu(activeOutletId)
                    val categoriesList = repository.getCategories()
                    _state.update { it.copy(menuItems = menuList, categories = categoriesList) }
                }
            }

            // Bind directly to our real-time streaming channel flow
            repository.observeOutletOrdersRealTime(activeOutletId, isOwner, currentDateFilter)
                .collectLatest { freshOrdersList ->

                    // FIXED CHECKER CONDITION: Triggers dialog popup on incoming rows OR
                    // if it is the very first connection load and an unhandled task is caught.
                    val isSizeIncreased = previousOrdersChecksum.isNotEmpty() && freshOrdersList.size > previousOrdersChecksum.size
                    val isInitialFetchBoot = previousOrdersChecksum.isEmpty()

                    if (isSizeIncreased || isInitialFetchBoot) {
                        val targetedItemsGroup = if (isInitialFetchBoot) freshOrdersList else {
                            freshOrdersList.filter { fresh -> previousOrdersChecksum.none { old -> old.id == fresh.id } }
                        }

                        val targetPendingRow = targetedItemsGroup.find {
                            val s = it.status.lowercase()
                            if (userDesignationRole == "delivery_boy") {
                                // Notify Delivery Boy when order is prepared/ready for pickup
                                s == "ready" || s == "prepared" || s == "out_for_delivery"
                            } else {
                                s == "pending" || s == "placed"
                            }
                        }

                        if (targetPendingRow != null) {
                            _state.update { it.copy(realTimeIncomingInterceptedOrder = targetPendingRow) }
                        }
                    }

                    previousOrdersChecksum = freshOrdersList

                    val finalDisplayList = if (userDesignationRole == "delivery_boy") {
                        freshOrdersList.filter { 
                            val s = it.status.lowercase()
                            s == "out_for_delivery" || s == "dispatched"
                        }
                    } else {
                        freshOrdersList
                    }

                    // Turn loading to false directly inside the hot update callback flow sequence to stop infinite spins
                    _state.update { it.copy(orders = finalDisplayList, isLoading = false) }
                }
        }
    }

    // ==========================================
    // Realtime Incoming Popup Dialogue Actions
    // ==========================================

    fun dismissIncomingInterceptedDialog() {
        _state.update { it.copy(realTimeIncomingInterceptedOrder = null) }
    }

    fun acceptInterceptedOrder(orderId: String) {
        if (!_state.value.canAcceptOrders && !_state.value.isOwnerUser) {
            emitTemporaryError("Permission Denied: You cannot accept online orders.")
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(realTimeIncomingInterceptedOrder = null) }
            modifyOrderStatusCardInline(orderId, "accepted")
        }
    }

    // ==========================================
    // Shopping Basket Operations
    // ==========================================

    fun incrementCartItem(item: NewOutletMenuUiModel) {
        val currentMap = _state.value.cartSelectedItems.toMutableMap()
        val match = currentMap[item.id]
        if (match == null) {
            currentMap[item.id] = NewSelectedMenuItem(
                menuItemId = item.id, name = item.name, qty = 1,
                price = item.finalPrice, totalPrice = item.finalPrice, image = item.image
            )
        } else {
            val q = match.qty + 1
            currentMap[item.id] = match.copy(qty = q, totalPrice = q * match.price)
        }
        _state.update { it.copy(cartSelectedItems = currentMap) }
    }

    fun decrementCartItem(item: NewOutletMenuUiModel) {
        val currentMap = _state.value.cartSelectedItems.toMutableMap()
        val match = currentMap[item.id] ?: return
        if (match.qty <= 1) currentMap.remove(item.id) else {
            val q = match.qty - 1
            currentMap[item.id] = match.copy(qty = q, totalPrice = q * match.price)
        }
        _state.update { it.copy(cartSelectedItems = currentMap) }
    }

    // ==========================================
    // Invoice Creation Actions
    // ==========================================

    fun updateCustomerPhone(phone: String) {
        _state.update { it.copy(customerPhone = phone) }
        if (phone.trim().length >= 13) {
            viewModelScope.launch {
                val profile = repository.findCustomerByPhone(phone.trim())
                _state.update {
                    it.copy(resolvedCustomer = profile, checkoutAddress = profile?.address.orEmpty())
                }
            }
        }
    }

    fun updateCheckoutOrderType(t: NewOrderType) { _state.update { it.copy(checkoutOrderType = t) } }
    fun updateCheckoutAddress(v: String) { _state.update { it.copy(checkoutAddress = v) } }
    fun updateCheckoutInstruction(v: String) { _state.update { it.copy(checkoutInstruction = v) } }
    fun dismissSummaryDialog() { _state.update { it.copy(summaryPendingDialogOrder = null) } }
    fun updateSearchQuery(v: String) { _state.update { it.copy(menuSearchQuery = v) } }
    fun selectCategoryScope(v: String?) { _state.update { it.copy(selectedCategory = v) } }

    fun executeOrderPlacement() {
        val targetOutletId = _state.value.activeSelectedOutlet?.id
        if (targetOutletId == null) {
            emitTemporaryError("Error: Operations restricted until an outlet target is selected.")
            return
        }
        if (_state.value.customerPhone.isBlank()) {
            emitTemporaryError("Validation Error: Missing a valid phone target configuration.")
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isPlacingOrderState = true) }
            runCatching {
                repository.placeOrder(
                    outletId = targetOutletId, customer = _state.value.resolvedCustomer,
                    phone = _state.value.customerPhone, address = _state.value.checkoutAddress,
                    orderType = _state.value.checkoutOrderType, instruction = _state.value.checkoutInstruction,
                    items = _state.value.cartSelectedItems.values.toList(), internalEmpId = internalEmployeeRowId
                )
            }.onSuccess { summary ->
                _state.update {
                    it.copy(
                        isPlacingOrderState = false,
                        summaryPendingDialogOrder = summary,
                        cartSelectedItems = emptyMap(),
                        customerPhone = "",
                        resolvedCustomer = null
                    )
                }
            }.onFailure { err ->
                Log.e("UnifiedVM", "Placement crashed", err)
                emitTemporaryError(err.localizedMessage ?: "Failed to save placement invoice changes.")
            }
        }
    }

    // ==========================================
    // Inline Card Status Changers
    // ==========================================

    fun modifyOrderStatusCardInline(orderId: String, nextStatus: String) {
        val targetOrder = _state.value.orders.find { it.id == orderId } ?: return
        viewModelScope.launch {
            val currentSession = backOfficeAuthRepository.currentSession.value
            if (currentSession != null && !currentSession.canUpdateOrderStatus(nextStatus)) {
                emitTemporaryError("Permission Denied: Your role cannot mark order as $nextStatus.")
                return@launch
            }

            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.updateOrderDetails(
                    orderId = orderId,
                    status = nextStatus,
                    address = targetOrder.address,
                    instruction = targetOrder.instruction,
                    orderType = targetOrder.orderType,
                    outletId = targetOrder.outletId,
                    customerPhone = targetOrder.phone,
                    internalEmpId = internalEmployeeRowId
                )
            }.onFailure { err ->
                _state.update { it.copy(isLoading = false) }
                emitTemporaryError(err.localizedMessage ?: "Failed to update status changes.")
            }
        }
    }

    // ==========================================
    // Detailed Workspace Updation Logic Hooks
    // ==========================================

    fun launchDetailedOrderFormEdition(order: NewDetailedOrderUiModel) {
        _state.update {
            it.copy(
                targetedEditingOrder = order,
                formEditAddress = order.address,
                formEditInstruction = order.instruction,
                formEditStatus = order.status,
                formEditType = order.orderType
            )
        }
    }

    fun dismissDetailedEditForm() { _state.update { it.copy(targetedEditingOrder = null) } }
    fun onFormAddressChanged(v: String) { _state.update { it.copy(formEditAddress = v) } }
    fun onFormInstructionChanged(v: String) { _state.update { it.copy(formEditInstruction = v) } }
    fun onFormStatusChanged(v: String) { _state.update { it.copy(formEditStatus = v) } }
    fun onFormTypeChanged(v: String) { _state.update { it.copy(formEditType = v) } }

    fun commitDetailedFormModifications() {
        val activeFormOrder = _state.value.targetedEditingOrder ?: return
        viewModelScope.launch {
            val currentSession = backOfficeAuthRepository.currentSession.value
            val nextStatus = _state.value.formEditStatus
            if (currentSession != null && !currentSession.canUpdateOrderStatus(nextStatus)) {
                emitTemporaryError("Permission Denied: Your role cannot mark order as $nextStatus.")
                return@launch
            }

            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.updateOrderDetails(
                    orderId = activeFormOrder.id,
                    status = _state.value.formEditStatus,
                    address = _state.value.formEditAddress,
                    instruction = _state.value.formEditInstruction,
                    orderType = _state.value.formEditType,
                    outletId = activeFormOrder.outletId,
                    customerPhone = activeFormOrder.phone,
                    internalEmpId = internalEmployeeRowId
                )
            }.onSuccess {
                _state.update { it.copy(targetedEditingOrder = null, isLoading = false) }
            }.onFailure { err ->
                _state.update { it.copy(isLoading = false) }
                emitTemporaryError(err.localizedMessage ?: "Failed to write modifications to data layer.")
            }
        }
    }
}