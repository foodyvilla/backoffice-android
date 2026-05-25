package com.jp.foodyvilla_backoffice.presentation.new_backoffice
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive

import java.time.Instant

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.ScreenDestinations
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewCategoryUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewCustomerUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewDetailedOrderUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOrderType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOrderUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOrdersManagementRepository
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOutletMenuUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewSelectedMenuItem
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.OutletDropdownUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// ViewModels Implementations
// ==========================================


import kotlinx.coroutines.Job

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
                val empSession = session as? UserSession.EmployeeSession
                userDesignationRole = empSession?.role()?.lowercase()?.trim() ?: "employee"
                sessionOutletId = empSession?.outletId



                val isOwner = userDesignationRole == "owner"
                val operationalGating = isOwner || (sessionOutletId != null)

                _state.update {
                    it.copy(isOwnerUser = isOwner, isOperationAllowed = operationalGating)
                }

                if (isOwner) {
                    val list = repository.fetchActiveOutletsList()
                    _state.update { it.copy(outlets = list, activeSelectedOutlet = null) }
                    // Owner starts with a clean slate until an outlet is chosen from the dropdown menu
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

    private fun restartRealtimeSubscriptionChannel() {
        realtimeStreamJob?.cancel()

        val activeOutletId = _state.value.activeSelectedOutlet?.id
        val isOwner = _state.value.isOwnerUser
        val currentDateFilter = _state.value.activeSelectedDate

        // If employee is completely unassigned, do not start socket subscription pipelines
        if (!isOwner && activeOutletId == null) return

        realtimeStreamJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Fetch structural menus only if an outlet selection is verified
            if (activeOutletId != null) {
                runCatching {
                    val menuList = repository.getOutletMenu(activeOutletId)
                    val categoriesList = repository.getCategories()
                    _state.update { it.copy(menuItems = menuList, categories = categoriesList) }
                }
            }

            // Bind directly onto the hot real-time WebSocket Flow pipeline
            repository.observeOutletOrdersRealTime(activeOutletId, isOwner, currentDateFilter)
                .collectLatest { freshOrdersList ->

                    // Heads-up Dialog triggers inside client views for fresh rows with pending status definitions
                    if (previousOrdersChecksum.isNotEmpty() && freshOrdersList.size > previousOrdersChecksum.size) {
                        val incomingEntries = freshOrdersList.filter { fresh ->
                            previousOrdersChecksum.none { old -> old.id == fresh.id }
                        }
                        val targetPendingRow = incomingEntries.find { it.status.lowercase() == "pending" }
                        if (targetPendingRow != null) {
                            _state.update { it.copy(realTimeIncomingInterceptedOrder = targetPendingRow) }
                        }
                    }
                    previousOrdersChecksum = freshOrdersList

                    _state.update { it.copy(orders = freshOrdersList, isLoading = false) }
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
        if (phone.trim().length >= 13) { // Fires when complete "+91XXXXXXXXXX" structure finishes
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
            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.updateOrderDetails(
                    orderId = orderId,
                    status = nextStatus,
                    address = targetOrder.address,
                    instruction = targetOrder.instruction,
                    orderType = targetOrder.orderType,
                    outletId = targetOrder.outletId,
                    customerPhone = targetOrder.phone
                )
            }.onFailure { err ->
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
            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.updateOrderDetails(
                    orderId = activeFormOrder.id,
                    status = _state.value.formEditStatus,
                    address = _state.value.formEditAddress,
                    instruction = _state.value.formEditInstruction,
                    orderType = _state.value.formEditType,
                    outletId = activeFormOrder.outletId,
                    customerPhone = activeFormOrder.phone
                )
            }.onSuccess {
                _state.update { it.copy(targetedEditingOrder = null) }
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Failed to write modifications to data layer.")
            }
        }
    }
}

// ==========================================
// Composable Presentation Interfaces
// ==========================================




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrdersListScreen(
    viewModel: UnifiedOrderControlViewModel,
    navController: NavController,
    onNavigateToCreateOrderMenuSelection: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val availableFulfillmentStatuses = listOf("pending", "accepted", "preparing", "completed", "cancelled")

    var dropdownScopeExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // ====================================================
    // REAL-TIME INCOMING HEADS-UP INTERCEPTOR OVERLAY
    // ====================================================
    state.realTimeIncomingInterceptedOrder?.let { incomingOrder ->
        AlertDialog(
            onDismissRequest = viewModel::dismissIncomingInterceptedDialog,
            icon = {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Incoming Pending Order Received", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Customer Name: ${incomingOrder.customerName}", fontWeight = FontWeight.SemiBold)
                    Text("Total Amount: ₹${incomingOrder.totalAmount} • Type: ${incomingOrder.orderType}")
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.acceptInterceptedOrder(incomingOrder.id) }) {
                    Text("Accept Order")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.dismissIncomingInterceptedDialog()
                            navController.navigate(ScreenDestinations.OrderDetails(incomingOrder.id))
                        }
                    ) { Text("View Details") }
                    TextButton(onClick = viewModel::dismissIncomingInterceptedDialog) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }

    // ====================================================
    // NATIVE SYSTEM MATERIAL3 DATE PICKER DIALOG
    // ====================================================
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.activeSelectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { timestampMillis ->
                        val parsedLocalDate = Instant.ofEpochMilli(timestampMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.updateFilterDate(parsedLocalDate)
                    }
                    showDatePickerDialog = false
                }) { Text("Apply Date") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ====================================================
    // CORE SCREEN MAIN INTERFACE
    // ====================================================
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Orders Dashboard Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 1. DYNAMIC OUTLET CONTROL FILTERS DROP-DOWN FOR OWNER USERS
            if (state.isOwnerUser) {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownScopeExpanded,
                        onExpandedChange = { dropdownScopeExpanded = !dropdownScopeExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.activeSelectedOutlet?.name ?: "All Outlets (Global Master View)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Outlet Control Filter Scope") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownScopeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownScopeExpanded,
                            onDismissRequest = { dropdownScopeExpanded = false }
                        ) {
                            state.outlets.forEach { outlet ->
                                DropdownMenuItem(
                                    text = { Text(outlet.name) },
                                    onClick = {
                                        viewModel.selectOutletScope(outlet)
                                        dropdownScopeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. INTERACTIVE DATE SELECTION BAR BUTTON FIELD
            OutlinedTextField(
                value = state.activeSelectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                onValueChange = {},
                readOnly = true,
                label = { Text("Active Operational Day Logs") },
                trailingIcon = {
                    IconButton(onClick = { showDatePickerDialog = true }) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Launch Calendar Select Picker")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            if (state.globalErrorMessage != null) {
                Text(
                    text = state.globalErrorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 3. SECURITY GATE SECURITY CHECKS ENFORCEMENTS
            if (!state.isOperationAllowed) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Security Restriction: No active branch workspace profile linked to this account session. Functions locked.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = onNavigateToCreateOrderMenuSelection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text("Place New Counter Sale Placement Order")
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.orders.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded orders found for the selected parameters.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // 4. ORDERS STREAM EVENT ROW VIEWS
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.orders, key = { it.id }) { order ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Type-safe matching execution using arguments definition targets
                                        navController.navigate(ScreenDestinations.OrderDetails(id = order.id))
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Customer Profile: ${order.customerName}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "Location Address: ${order.address.ifBlank { "Counter / Dine-In" }}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Fulfillment Type Logistics: ${order.orderType} • Total: ₹${order.totalAmount}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Inline Dropdown status update controller chip
                                        var inlineMenuShow by remember { mutableStateOf(false) }
                                        Box {
                                            FilterChip(
                                                selected = true,
                                                onClick = { inlineMenuShow = true },
                                                label = { Text(order.status.uppercase()) }
                                            )
                                            DropdownMenu(
                                                expanded = inlineMenuShow,
                                                onDismissRequest = { inlineMenuShow = false }
                                            ) {
                                                availableFulfillmentStatuses.forEach { specText ->
                                                    DropdownMenuItem(
                                                        text = { Text(specText.uppercase()) },
                                                        onClick = {
                                                            viewModel.modifyOrderStatusCardInline(order.id, specText)
                                                            inlineMenuShow = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
