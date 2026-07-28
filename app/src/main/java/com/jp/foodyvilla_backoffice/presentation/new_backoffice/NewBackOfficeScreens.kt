package com.jp.foodyvilla_backoffice.presentation.new_backoffice
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.UnifiedOrderControlViewModel
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive

import java.time.Instant

import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.ScreenDestinations
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme

// ==========================================
// ViewModels Implementations
// ==========================================


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrdersListScreen(
    viewModel: UnifiedOrderControlViewModel,
    navController: NavController,
    onMenuClick: () -> Unit,
    onNavigateToCreateOrderMenuSelection: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val availableFulfillmentStatuses = remember(state.userRole) {
        if (state.userRole == "delivery_boy") {
            listOf("completed", "cancelled")
        } else {
            listOf("pending", "accepted", "preparing", "completed", "cancelled")
        }
    }

    var dropdownScopeExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // ====================================================
    // REAL-TIME INCOMING HEADS-UP INTERCEPTOR OVERLAY
    // ====================================================
//    state.realTimeIncomingInterceptedOrder?.let { incomingOrder ->
//        AlertDialog(
//            onDismissRequest = viewModel::dismissIncomingInterceptedDialog,
//            icon = {
//                Icon(
//                    imageVector = Icons.Default.NotificationsActive,
//                    contentDescription = null,
//                    modifier = Modifier.size(32.dp),
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            },
//            title = { Text("Incoming Pending Order Received", fontWeight = FontWeight.Bold) },
//            text = {
//                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                    Text("Customer Name: ${incomingOrder.customerName}", fontWeight = FontWeight.SemiBold)
//                    Text("Total Amount: ₹${incomingOrder.totalAmount} • Type: ${incomingOrder.orderType}")
//                }
//            },
//            confirmButton = {
//                Button(onClick = { viewModel.acceptInterceptedOrder(incomingOrder.id) }) {
//                    Text("Accept Order")
//                }
//            },
//            dismissButton = {
//                Row {
//                    TextButton(
//                        onClick = {
//                            viewModel.dismissIncomingInterceptedDialog()
//                            navController.navigate(ScreenDestinations.OrderDetails(incomingOrder.id))
//                        }
//                    ) { Text("View Details") }
//                    TextButton(onClick = viewModel::dismissIncomingInterceptedDialog) {
//                        Text("Dismiss", color = MaterialTheme.colorScheme.error)
//                    }
//                }
//            }
//        )
//    }

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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
//            Text(
//                text = "Orders Dashboard Management",
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold
//            )
//            Spacer(modifier = Modifier.height(16.dp))

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
                if (state.userRole != "delivery_boy") {
                    Button(
                        onClick = onNavigateToCreateOrderMenuSelection,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text("Place New Counter Sale Placement Order")
                    }
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
                                        val extendedColors = AppTheme.colors
                                        val statusColor = when (order.status.lowercase()) {
                                            "pending", "accepted" -> extendedColors.info
                                            "preparing" -> extendedColors.warning
                                            "completed" -> extendedColors.success
                                            "cancelled" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.outline
                                        }
                                        val statusContainer = when (order.status.lowercase()) {
                                            "pending", "accepted" -> extendedColors.infoContainer
                                            "preparing" -> extendedColors.warningContainer
                                            "completed" -> extendedColors.successContainer
                                            "cancelled" -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                        
                                        Box {
                                            AssistChip(
                                                onClick = { inlineMenuShow = true },
                                                label = { Text(order.status.uppercase()) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    labelColor = statusColor,
                                                    containerColor = statusContainer
                                                ),
                                                border = AssistChipDefaults.assistChipBorder(
                                                    borderColor = statusColor.copy(alpha = 0.5f),
                                                    enabled = true
                                                )
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
