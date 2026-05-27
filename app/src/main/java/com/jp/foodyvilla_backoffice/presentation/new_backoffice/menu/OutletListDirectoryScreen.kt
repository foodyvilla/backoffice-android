package com.jp.foodyvilla_backoffice.presentation.new_backoffice.outlets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.presentation.components.OutletRowCard
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.OutletManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletListDirectoryScreen(
    viewModel: OutletManagementViewModel,
    onOutletNavigateLambda: (outletId: Long, name: String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val filteredOutlets = remember(state.outletsList, state.outletSearchQuery) {
        state.outletsList.filter {
            it.name.contains(state.outletSearchQuery, ignoreCase = true) ||
                    it.city.contains(state.outletSearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openOutletCreationForm,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Franchise Outlets Registry", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.outletSearchQuery,
                    onValueChange = viewModel::updateOutletSearch,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    placeholder = { Text("Search branches by name or city location...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        AnimatedVisibility(state.outletSearchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateOutletSearch("") }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (state.isLoading && state.outletsList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredOutlets, key = { it.id }) { outlet ->
                            OutletRowCard(
                                outlet = outlet,
                                onEditClick = { viewModel.openOutletEditionForm(outlet) },
                                onRowNavigateClick = { onOutletNavigateLambda(outlet.id, outlet.name) }
                            )
                        }
                    }
                }
            }

            // FIXED: TARGET DIALOG VARIANCE MAPPED TO THE CORRECT UI STATE PROPERTIES
            if (state.isOutletFormOpen) {
                AlertDialog(
                    onDismissRequest = viewModel::closeOutletForm,
                    title = {
                        Text(
                            text = if (state.targetOutlet == null) "Open New Branch Unit" else "Update Branch Parameters Layout",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = state.oName, onValueChange = viewModel::onONameChanged, label = { Text("Outlet Brand Unit Label Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = state.oCity, onValueChange = viewModel::onOCityChanged, label = { Text("City Node") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = state.oPhone, onValueChange = viewModel::onOPhoneChanged, label = { Text("Operational Contact Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = state.oEmail, onValueChange = viewModel::onOEmailChanged, label = { Text("Corporate Billing Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = state.oRadius, onValueChange = viewModel::onORadiusChanged, label = { Text("Logistics Geofencing Radius (KM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = state.oAddress, onValueChange = viewModel::onOAddressChanged, label = { Text("Physical Mailing Address Location Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Set Branch Active Open Operational Status")
                                Switch(checked = state.oIsActive, onCheckedChange = viewModel::onOActiveToggled)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = viewModel::commitOutletAction) {
                            Text("Save Layout Settings Document")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::closeOutletForm) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}