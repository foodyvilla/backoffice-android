package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

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
import com.jp.foodyvilla_backoffice.presentation.components.OutletMenuItemRow
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.OutletManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificOutletMenuHandlingScreen(
    outletId: Long,
    outletName: String,
    viewModel: OutletManagementViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var catalogDropdownOpen by remember { mutableStateOf(false) }

    LaunchedEffect(outletId) {
        viewModel.loadMenuForSpecificOutlet(outletId)
    }

    val filteredMenu = remember(state.currentOutletMenu, state.menuSearchQuery) {
        state.currentOutletMenu.filter { 
            it.productName.contains(state.menuSearchQuery, ignoreCase = true) || 
                    it.productCategoryName.contains(state.menuSearchQuery, ignoreCase = true) 
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openMenuCreationForm, 
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(outletName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Outlet Specific Linked Digital Menu Menu Items", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.menuSearchQuery, 
                    onValueChange = viewModel::updateMenuSearch, 
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    placeholder = { Text("Search menu items by label identifier text titles...") }, 
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = { 
                        AnimatedVisibility(state.menuSearchQuery.isNotBlank()) { 
                            IconButton(onClick = { viewModel.updateMenuSearch("") }) { 
                                Icon(Icons.Default.Clear, null) 
                            } 
                        } 
                    },
                    shape = RoundedCornerShape(12.dp), 
                    singleLine = true
                )

                if (state.isLoading && state.currentOutletMenu.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { 
                        CircularProgressIndicator() 
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredMenu, key = { it.id }) { item ->
                            OutletMenuItemRow(
                                item = item, 
                                onEditClick = { viewModel.openMenuEditionForm(item) }, 
                                onDeleteClick = { viewModel.removeProductFromMenu(item.id, outletId) }
                            )
                        }
                    }
                }
            }

            // FIXED: MENU FORM WORKSPACE DIALOG VARIANCE MAPPED TO ITS PROPER STATE POINTERS
            if (state.isMenuFormOpen) {
                AlertDialog(
                    onDismissRequest = viewModel::closeMenuForm,
                    title = { 
                        Text(
                            text = if (state.targetMenuItem == null) "Link Core Catalog Item to Outlet" else "Modify Unit Price Document Rules", 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.mSelectedProduct?.name ?: "Link Central Master Product Reference Row", 
                                    onValueChange = {}, 
                                    readOnly = true, 
                                    modifier = Modifier.fillMaxWidth(), 
                                    label = { Text("Product Node") },
                                    trailingIcon = { 
                                        IconButton(onClick = { if (state.targetMenuItem == null) catalogDropdownOpen = true }) { 
                                            Icon(Icons.Default.Add, null) 
                                        } 
                                    }
                                )
                                DropdownMenu(expanded = catalogDropdownOpen, onDismissRequest = { catalogDropdownOpen = false }) {
                                    state.globalProductCatalogOptions.forEach { prod -> 
                                        DropdownMenuItem(
                                            text = { Text(prod.name) }, 
                                            onClick = { 
                                                viewModel.onMProductSelected(prod)
                                                catalogDropdownOpen = false 
                                            }
                                        ) 
                                    }
                                }
                            }
                            OutlinedTextField(value = state.mPrice, onValueChange = viewModel::onMPriceChanged, label = { Text("Outlet Price Point Rate (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = state.mDiscount, onValueChange = viewModel::onMDiscountChanged, label = { Text("Franchise Flat Discount Value Amount (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                                Text("Expose Availability inside Client UI Channels")
                                Switch(checked = state.mIsAvailable, onCheckedChange = viewModel::onMAvailableToggled) 
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                                Text("Mark Item Out of Stock / Kitchen Shortage")
                                Switch(checked = state.mIsOutOfStock, onCheckedChange = viewModel::onMStockToggled) 
                            }
                        }
                    },
                    confirmButton = { 
                        Button(onClick = { viewModel.commitOutletMenuAction(outletId) }) { 
                            Text("Save Document Entry") 
                        } 
                    },
                    dismissButton = { 
                        TextButton(onClick = viewModel::closeMenuForm) { 
                            Text("Cancel") 
                        } 
                    }
                )
            }
        }
    }
}