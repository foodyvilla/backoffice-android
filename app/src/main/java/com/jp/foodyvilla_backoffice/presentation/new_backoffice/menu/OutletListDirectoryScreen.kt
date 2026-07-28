package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
    onNavigateToOutletFormAdd: () -> Unit,             // Lambda navigation callback matching Route Add
    onNavigateToOutletFormEdit: (outletId: Long) -> Unit,   // Lambda navigation callback matching Route Edit(id)
    onOutletNavigateLambda: (outletId: Long, name: String) -> Unit,
    onMenuClick: () -> Unit
) {

    // Refresh and sync active list rows from the PostgREST server table when screen mounts
    LaunchedEffect(Unit) {
        viewModel.loadAllOutletsData()
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    // Real-time local database filtration engine operating inside state updates
    val filteredOutlets = remember(state.outletsList, state.outletSearchQuery) {
        state.outletsList.filter { outlet ->
            outlet.name.contains(state.outletSearchQuery, ignoreCase = true) ||
                    outlet.city.contains(state.outletSearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outlets Directory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isOwner) {
                FloatingActionButton(
                    onClick = onNavigateToOutletFormAdd, // Fires off clean destination changes
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Open New Franchise Branch")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Franchise Outlets Registry", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Filter the list if not owner? 
                // User said "access of all outlet will be under the owner only"
                val displayList = remember(filteredOutlets, state.isOwner) {
                    if (state.isOwner) filteredOutlets else emptyList() // Or maybe filter by assigned outlet?
                }

                // DIRECTORY FILTRATION INPUT FIELD
                OutlinedTextField(
                    value = state.outletSearchQuery,
                    onValueChange = viewModel::updateOutletSearch,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    placeholder = { Text("Search branches by name or city location...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        AnimatedVisibility(state.outletSearchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateOutletSearch("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Inputs")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // VISIBILITY STATE EMISSION RENDER PATHS
                if (state.isLoading && state.outletsList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (displayList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.outletsList.isEmpty()) "No branch listings indexed in table."
                            else "Access Restricted or No records match your criteria.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayList, key = { it.id }) { outlet ->
                            OutletRowCard(
                                outlet = outlet,
                                onEditClick = { onNavigateToOutletFormEdit(outlet.id) }, // Routes out cleanly to the dynamic edit screen workspace
                                onRowNavigateClick = { onOutletNavigateLambda(outlet.id, outlet.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}