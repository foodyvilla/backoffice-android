package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    viewModel: OutletManagementViewModel,
    onNavigateToMenuFormAdd: (outletId: Long) -> Unit,       // Lambda navigation callback path matching Route Add
    onNavigateToMenuFormEdit: (outletId: Long, menuId: Long) -> Unit,  // Lambda navigation callback path matching Route Edit(id)
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var currentOutletId by remember { mutableLongStateOf(outletId) }
    var currentOutletName by remember { mutableStateOf(outletName) }
    var outletDropdownExpanded by remember { mutableStateOf(false) }

    // Sync menu items list matrix records from target database table on startup
    LaunchedEffect(currentOutletId) {
        viewModel.loadMenuForSpecificOutlet(currentOutletId)
    }

    // Load outlets list for owners to allow switching
    LaunchedEffect(state.isOwner) {
        if (state.isOwner && state.outletsList.isEmpty()) {
            viewModel.loadAllOutletsData()
        }
    }

    // Default to first outlet for owners if ID is 0
    LaunchedEffect(state.outletsList) {
        if (state.isOwner && currentOutletId == 0L && state.outletsList.isNotEmpty()) {
            val first = state.outletsList.first()
            currentOutletId = first.id
            currentOutletName = first.name
        }
    }

    // Dynamic, reactive local list filtration matching user query string parameters
    val filteredMenu = remember(state.currentOutletMenu, state.menuSearchQuery) {
        state.currentOutletMenu.filter { item ->
            item.productName.contains(state.menuSearchQuery, ignoreCase = true) ||
                    item.productCategoryName.contains(state.menuSearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentOutletName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.canManageMenu) {
                FloatingActionButton(
                    onClick = { onNavigateToMenuFormAdd(currentOutletId) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Link New Product Variant")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {

                // 1. DYNAMIC DROP-DOWN SELECTOR FOR OWNER PROFILES
                if (state.isOwner) {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = outletDropdownExpanded,
                            onExpandedChange = { outletDropdownExpanded = !outletDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = state.outletsList.find { it.id == currentOutletId }?.name ?: currentOutletName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Managing Menu For Outlet") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = outletDropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = outletDropdownExpanded,
                                onDismissRequest = { outletDropdownExpanded = false }
                            ) {
                                state.outletsList.forEach { outlet ->
                                    DropdownMenuItem(
                                        text = { Text(outlet.name) },
                                        onClick = {
                                            currentOutletId = outlet.id
                                            currentOutletName = outlet.name
                                            outletDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Outlet Specific Linked Digital Menu Items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // SEARCH TEXT FIELD WORKSPACE
                OutlinedTextField(
                    value = state.menuSearchQuery,
                    onValueChange = viewModel::updateMenuSearch,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    placeholder = { Text("Search menu items by label title or section...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        AnimatedVisibility(visible = state.menuSearchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateMenuSearch("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Input")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (state.errorText != null) {
                    Text(
                        text = state.errorText.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                // DATA VISIBILITY RENDERING CHANNELS
                if (state.isLoading && state.currentOutletMenu.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredMenu.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.currentOutletMenu.isEmpty()) "No menu items mapped onto this branch database records index node."
                            else "No products match your custom search keyword context.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredMenu, key = { it.id }) { item ->
                            OutletMenuItemRow(
                                item = item,
                                onEditClick = { onNavigateToMenuFormEdit(currentOutletId, item.id) }, // Safely pipes out ids to trigger edit routes
                                onDeleteClick = { viewModel.removeProductFromMenu(item.id, currentOutletId) },
                                canManage = state.canManageMenu
                            )
                        }
                    }
                }
            }
        }
    }
}