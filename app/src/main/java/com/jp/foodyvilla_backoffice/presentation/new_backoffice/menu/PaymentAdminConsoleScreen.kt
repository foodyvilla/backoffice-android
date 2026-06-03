package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminPaymentMethod
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminPaymentStatus
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.PaymentAdminRowCard
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.PaymentAdminViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentAdminConsoleScreen(viewModel: PaymentAdminViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }
    var formUserMenuExpanded by remember { mutableStateOf(false) }
    var formStatusMenuExpanded by remember { mutableStateOf(false) }
    var formMethodMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAllPaymentsDirectory()
    }

    val filteredPayments = remember(state.transactionsList, state.paymentSearchQuery) {
        state.transactionsList.filter { row ->
            row.customerName.contains(state.paymentSearchQuery, ignoreCase = true) ||
                    row.orderId.contains(state.paymentSearchQuery, ignoreCase = true) ||
                    row.razorpayPaymentId.contains(state.paymentSearchQuery, ignoreCase = true)
        }
    }

    val runningTotalRevenue = remember(filteredPayments) {
        filteredPayments.filter { it.status == AdminPaymentStatus.CAPTURED }.sumOf { it.amountDisplay }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Payments Accounting Desk", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.initFormWorkspace(null) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Insert Manual Log Payload")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            // --- TOP SEGMENT ROW: TIME BLOCK DROPDOWNS PICKER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Reporting Period:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                
                Box {
                    Button(onClick = { monthMenuExpanded = true }) {
                        Text(state.currentSelectedMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = monthMenuExpanded, onDismissRequest = { monthMenuExpanded = false }) {
                        Month.values().forEach { m ->
                            DropdownMenuItem(text = { Text(m.getDisplayName(TextStyle.FULL, Locale.getDefault())) }, onClick = {
                                viewModel.changeFilterMonth(m.value, state.currentSelectedMonth.year)
                                monthMenuExpanded = false
                            })
                        }
                    }
                }

                Box {
                    Button(onClick = { yearMenuExpanded = true }) {
                        Text(state.currentSelectedMonth.year.toString())
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = yearMenuExpanded, onDismissRequest = { yearMenuExpanded = false }) {
                        ((YearMonth.now().year - 2)..YearMonth.now().year).forEach { y ->
                            DropdownMenuItem(text = { Text(y.toString()) }, onClick = {
                                viewModel.changeFilterMonth(state.currentSelectedMonth.monthValue, y)
                                yearMenuExpanded = false
                            })
                        }
                    }
                }
            }

            // FINANCIAL REVENUE METRIC SHEET OVERVIEW CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Aggregate Volume Capture Summary", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = "₹${String.format("%.2f", runningTotalRevenue)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text(text = "Calculated from ${filteredPayments.size} mapped transactional operations records.", style = MaterialTheme.typography.bodySmall)
                }
            }

            OutlinedTextField(
                value = state.paymentSearchQuery, onValueChange = viewModel::updateSearchFilter,
                modifier = Modifier.fillMaxWidth(), placeholder = { Text("Filter via Customer reference, Order ID, or Gateway reference...") },
                leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(12.dp), singleLine = true
            )

            AnimatedVisibility(visible = state.dynamicErrorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(text = state.dynamicErrorMessage.orEmpty(), modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                }
            }

            if (state.isLoading && state.transactionsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredPayments, key = { it.id }) { txn ->
                        PaymentAdminRowCard(transaction = txn, onModifyClick = { viewModel.initFormWorkspace(txn) }, onPurgeClick = { viewModel.dispatchPurgeAction(txn.id) })
                    }
                }
            }
        }

        // =====================================================================
        // BACKOFFICE DATA FORM ACCOUNTING DESK SHEET MODAL
        // =====================================================================
        if (state.isFormWindowOpen) {
            val currentSelectedName = state.cachedUsers.find { it.id == state.formCustomerId }?.name ?: "Select Target Customer Node"

            AlertDialog(
                onDismissRequest = viewModel::dismissFormWorkspace,
                title = { Text(text = if (state.formWorkspaceTargetId == null) "Log Manual Transaction Record" else "Modify Transaction Matrix Record", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(value = state.formOrderId, onValueChange = viewModel::onFormOrderIdChanged, label = { Text("FoodyVilla System Order ID (UUID format required)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { formUserMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = currentSelectedName)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = formUserMenuExpanded, onDismissRequest = { formUserMenuExpanded = false }) {
                                state.cachedUsers.forEach { usr ->
                                    DropdownMenuItem(text = { Text(usr.name.orEmpty()) }, onClick = { viewModel.onFormCustomerSelected(usr.id); formUserMenuExpanded = false })
                                }
                            }
                        }

                        OutlinedTextField(value = state.formAmountInput, onValueChange = viewModel::onFormAmountChanged, label = { Text("Transaction Value Total (INR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { formStatusMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = state.formStatus.name)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                                DropdownMenu(expanded = formStatusMenuExpanded, onDismissRequest = { formStatusMenuExpanded = false }) {
                                    AdminPaymentStatus.values().forEach { st ->
                                        DropdownMenuItem(text = { Text(st.name) }, onClick = { viewModel.onFormStatusChanged(st); formStatusMenuExpanded = false })
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { formMethodMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = state.formMethod.name)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                                DropdownMenu(expanded = formMethodMenuExpanded, onDismissRequest = { formMethodMenuExpanded = false }) {
                                    AdminPaymentMethod.values().forEach { mt ->
                                        DropdownMenuItem(text = { Text(mt.name) }, onClick = { viewModel.onFormMethodChanged(mt); formMethodMenuExpanded = false })
                                    }
                                }
                            }
                        }

                        OutlinedTextField(value = state.formRazorpayOrderId, onValueChange = viewModel::onFormRpOrderChanged, label = { Text("Razorpay Order ID Link (Optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = state.formRazorpayPaymentId, onValueChange = viewModel::onFormRpPaymentChanged, label = { Text("Razorpay Payment ID Link (Optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                },
                confirmButton = { Button(onClick = viewModel::dispatchCommitCrudAction) { Text("Commit Entry Changes") } },
                dismissButton = { TextButton(onClick = viewModel::dismissFormWorkspace) { Text("Cancel") } }
            )
        }
    }
}