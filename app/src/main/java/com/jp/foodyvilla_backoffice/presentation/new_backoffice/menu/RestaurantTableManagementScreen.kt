package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.RestaurantTableDto
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.RestaurantTableManagementViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantTableManagementScreen(
    viewModel: RestaurantTableManagementViewModel = koinViewModel(),
    onMenuClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorText) {
        state.errorText?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tables Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadTables(state.outletId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddForm,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Table")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            if (state.isOwner) {
                var dropdownExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.outlets.find { it.id == state.outletId }?.name ?: "Select Outlet",
                            onValueChange = {}, readOnly = true, label = { Text("Managing Outlet") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                            state.outlets.forEach { outletItem ->
                                DropdownMenuItem(
                                    text = { Text(outletItem.name) },
                                    onClick = { 
                                        viewModel.loadTables(outletItem.id)
                                        dropdownExpanded = false 
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.tables.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tables configured for this outlet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.tables, key = { it.id }) { table ->
                        TableAdminCard(
                            table = table,
                            onEdit = { viewModel.openEditForm(table) },
                            onDelete = { viewModel.deleteTable(table.id) }
                        )
                    }
                }
            }
        }

        if (state.isFormOpen) {
            TableEditDialog(
                title = if (state.editingTableId == null) "Add New Table" else "Edit Table",
                tableNumber = state.formTableNumber,
                capacity = state.formCapacity,
                onTableNumberChange = viewModel::updateFormTableNumber,
                onCapacityChange = viewModel::updateFormCapacity,
                onDismiss = viewModel::closeForm,
                onConfirm = viewModel::saveTable
            )
        }
    }
}

@Composable
private fun TableAdminCard(
    table: RestaurantTableDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Table ${table.table_number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.People, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Capacity: ${table.capacity} Persons",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Status: ${table.status.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (table.status == "available") Color(0xFF4CAF50) else Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun TableEditDialog(
    title: String,
    tableNumber: String,
    capacity: String,
    onTableNumberChange: (String) -> Unit,
    onCapacityChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = tableNumber,
                    onValueChange = onTableNumberChange,
                    label = { Text("Table Number/Name") },
                    placeholder = { Text("e.g. 01, VIP-A") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = onCapacityChange,
                    label = { Text("Seating Capacity") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = tableNumber.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Table")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
