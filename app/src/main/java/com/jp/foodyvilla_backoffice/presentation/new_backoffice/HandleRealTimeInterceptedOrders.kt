package com.jp.foodyvilla_backoffice.presentation.new_backoffice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewDetailedOrderUiModel

// ==========================================
// Dashboard Interception Overlay Component 
// ==========================================

@Composable
fun HandleRealTimeInterceptedOrders(
    viewModel: UnifiedOrderControlViewModel,
    onViewOrderDetailsAction: (NewDetailedOrderUiModel) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.realTimeIncomingInterceptedOrder?.let { incomingOrder ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissIncomingInterceptedDialog() },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Incoming Pending Order Received", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Customer: ${incomingOrder.customerName}", fontWeight = FontWeight.SemiBold)
                    Text("Type: ${incomingOrder.orderType} • Amount: ₹${incomingOrder.totalAmount}")
                    Text("Destination: ${incomingOrder.address.ifBlank { "Counter / Dine-In" }}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.acceptInterceptedOrder(incomingOrder.id) }) { Text("Accept Order") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { 
                        viewModel.dismissIncomingInterceptedDialog()
                        onViewOrderDetailsAction(incomingOrder)
                    }) { Text("View Order Details") }
                    TextButton(onClick = { viewModel.dismissIncomingInterceptedDialog() }) { Text("Dismiss", color = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }
}

// ==========================================
// NEW DETAILED ORDER PROFILE SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderDetailsScreen(
    orderId: String,
    viewModel: UnifiedOrderControlViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val availableStatuses = listOf("pending", "accepted", "preparing", "completed", "cancelled")

    // Find our current targeted order from the reactive data store context matching screen parameters
    val order = remember(state.orders, orderId) { state.orders.find { it.id == orderId } }

    // Synchronize localized form editor states on initial composition entry
    LaunchedEffect(order) {
        order?.let { viewModel.launchDetailedOrderFormEdition(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Management Details Workspace", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = {
//                    viewModel.dismissDetailedEditForm();
                    onNavigateBack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { innerPadding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Error: Order reference matrix missing. Return back to dashboard.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(text = "Order Spec Matrix: #${order.id.take(12)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Date Registered: ${order.createdAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Customer Overview Matrix Summary Panel Block
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Customer Record Info Data Profile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Identity Name: ${order.customerName}", fontWeight = FontWeight.Medium)
                            Text("Primary Contact Line: ${order.phone}")
                        }
                    }
                }

                // INTERACTIVE FORM INPUT EDITORS SECTION
                item {
                    Text("Modify Order Properties Specifications", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        
                        // 1. Interactive Dropdown Fulfillment Status Changer
                        var statusDropdownShow by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.formEditStatus.uppercase(), onValueChange = {}, label = { Text("Order Fulfillment Status Matrix") },
                                modifier = Modifier.fillMaxWidth(), readOnly = true,
                                trailingIcon = { IconButton(onClick = { statusDropdownShow = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                            )
                            DropdownMenu(expanded = statusDropdownShow, onDismissRequest = { statusDropdownShow = false }) {
                                availableStatuses.forEach { itemStatus ->
                                    DropdownMenuItem(text = { Text(itemStatus.uppercase()) }, onClick = {
                                        viewModel.onFormStatusChanged(itemStatus)
                                        statusDropdownShow = false
                                    })
                                }
                            }
                        }

                        // 2. Modifiable Logistic Delivery Destination Address
                        OutlinedTextField(
                            value = state.formEditAddress, onValueChange = viewModel::onFormAddressChanged,
                            label = { Text("Delivery Destination Shipping Address Location") }, modifier = Modifier.fillMaxWidth()
                        )

                        // 3. Modifiable Production Kitchen Instructions
                        OutlinedTextField(
                            value = state.formEditInstruction, onValueChange = viewModel::onFormInstructionChanged,
                            label = { Text("Preparation Notes Kitchen Instructions Guide") }, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Itemized Receipt Breakdowns Row Sliders
                item {
                    Text("Itemized Bill Receipt Invoices", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                items(order.items) { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${item.name} x ${item.qty}", modifier = Modifier.weight(1f))
                        Text(text = "₹${item.totalPrice}", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Complete Accounting Matrix Summaries Block
                item {
                    HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                    val computedPkgSum = 15.0
                    val taxCgstSum = order.totalAmount * 0.025
                    val taxSgstSum = order.totalAmount * 0.025
                    val logisticSum = if (order.orderType == "DELIVERY") 30.0 else 0.0
                    val comprehensiveGrandInvoiceSum = order.totalAmount + computedPkgSum + taxCgstSum + taxSgstSum + logisticSum

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Items Base Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${order.totalAmount}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Packaging Materials Surcharge", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹$computedPkgSum")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST Central Tax (2.5%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%.2f", taxCgstSum)}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST State Tax (2.5%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%.2f", taxSgstSum)}")
                        }
                        if (logisticSum > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Logistics handling Rider Fee", color = MaterialTheme.colorScheme.primary)
                                Text("₹$logisticSum", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Grand Total Due", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("₹${String.format("%.2f", comprehensiveGrandInvoiceSum)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.commitDetailedFormModifications(); onNavigateBack() },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isLoading
                    ) {
                        Text("Save & Overwrite Document modifications Parameters", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}