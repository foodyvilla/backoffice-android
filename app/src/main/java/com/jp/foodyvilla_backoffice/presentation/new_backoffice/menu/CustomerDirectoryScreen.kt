package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.CustomerUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.DialogType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.OperationResultDialog
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.CustomerManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDirectoryScreen(
    viewModel: CustomerManagementViewModel,
    onMenuClick: () -> Unit,
    onNavigateToDetails: (customerId: Long, phone :String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var crudFormOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadAllCustomers() }

    val filteredList = remember(state.customersList, state.customerSearchQuery) {
        state.customersList.filter { it.name.contains(state.customerSearchQuery, ignoreCase = true) || it.phone.contains(state.customerSearchQuery) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Base Directory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.setupForm(null); crudFormOpen = true }) {
                Icon(Icons.Default.Add, "Add Customer")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = state.customerSearchQuery, onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                placeholder = { Text("Search by customer name or phone numbers...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp), singleLine = true
            )

            if (state.isLoading && state.customersList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredList, key = { it.id }) { customer ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetails(customer.id, customer.phone) },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(customer.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(customer.address, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                                IconButton(onClick = { viewModel.setupForm(customer); crudFormOpen = true }) {
                                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { viewModel.removeCustomerRecord(customer.id) }) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // OPERATION SUCCESS & ERROR DIALOGS
        // =====================================================================
        if (state.successMessage != null) {
            OperationResultDialog(
                type = DialogType.SUCCESS,
                title = "Success",
                message = state.successMessage.orEmpty(),
                onDismiss = viewModel::dismissSuccessDialog
            )
        }

        if (state.errorText != null) {
            OperationResultDialog(
                type = DialogType.ERROR,
                title = "Operation Failed",
                message = state.errorText.orEmpty(),
                onDismiss = viewModel::dismissErrorMessage
            )
        }

        // Inline Form Sheet Dialog Context
        if (crudFormOpen) {
            AlertDialog(
                onDismissRequest = { crudFormOpen = false },
                title = { Text(if (state.targetCustomerId == null) "Create Profile Row" else "Modify Profile Row", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = state.cName, onValueChange = viewModel::onNameChanged, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.cPhone, onValueChange = viewModel::onPhoneChanged, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.cEmail, onValueChange = viewModel::onEmailChanged, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.cAddress, onValueChange = viewModel::onAddressChanged, label = { Text("Mailing Address") }, modifier = Modifier.fillMaxWidth())
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Profile KYC Verified")
                            Switch(checked = state.cIsVerified, onCheckedChange = viewModel::onVerifiedToggled)
                        }
                    }
                },
                confirmButton = { Button(onClick = { viewModel.commitCustomerCrudAction { crudFormOpen = false } }) { Text("Save") } },
                dismissButton = { TextButton(onClick = { crudFormOpen = false }) { Text("Cancel") } }
            )
        }
    }
}