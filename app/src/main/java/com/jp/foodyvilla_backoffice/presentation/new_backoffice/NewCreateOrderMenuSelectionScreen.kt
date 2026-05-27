package com.jp.foodyvilla_backoffice.presentation.new_backoffice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.UnifiedOrderControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCreateOrderMenuSelectionScreen(
    viewModel: UnifiedOrderControlViewModel,
    onNavigateToCheckout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var outletDropdownExpanded by remember { mutableStateOf(false) }

    // Real-time filtering computation block for the menu grid layout matrix
    val processedFilteredMenu = remember(state.menuItems, state.menuSearchQuery, state.selectedCategory) {
        state.menuItems.filter { item ->
            val matchesCategory = state.selectedCategory == null || 
                    item.category.equals(state.selectedCategory, ignoreCase = true)
            val matchesSearch = item.name.contains(state.menuSearchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        bottomBar = {
            // Only show the checkout bar if an outlet is active and items are selected
            if (state.isOperationAllowed && state.cartSelectedItems.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val grandTotalCartSum = state.cartSelectedItems.values.sumOf { it.totalPrice }
                        val absoluteItemsCount = state.cartSelectedItems.values.sumOf { it.qty }

                        Column {
                            Text(
                                text = "$absoluteItemsCount ${if (absoluteItemsCount == 1) "Item" else "Items"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹$grandTotalCartSum",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = onNavigateToCheckout,
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Review Basket", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Counter POS Menu Selector",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 1. DYNAMIC DROP-DOWN SELECTOR FOR OWNER PROFILES
            if (state.isOwnerUser) {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = outletDropdownExpanded,
                        onExpandedChange = { outletDropdownExpanded = !outletDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.activeSelectedOutlet?.name ?: "Select Outlet for Sales Terminal...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Active Sales Franchise Outlet") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = outletDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = outletDropdownExpanded,
                            onDismissRequest = { outletDropdownExpanded = false }
                        ) {
                            state.outlets.forEach { outlet ->
                                DropdownMenuItem(
                                    text = { Text(outlet.name) },
                                    onClick = {
                                        viewModel.selectOutletScope(outlet)
                                        outletDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Temporary Error Diagnostics Notification Banner Box
            if (state.globalErrorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = state.globalErrorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 2. ROLE SECURITY ENFORCEMENT & ENTRY GATING LOGIC
            if (!state.isOperationAllowed) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = if (state.isOwnerUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (state.isOwnerUser) "Please pick an active restaurant branch layout from the dropdown above to display food items."
                                else "Terminal Access Locked: Your account has not been assigned to a valid outlet ID workspace configuration. Operations disabled.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth(),
                               textAlign =  TextAlign.Center,



                            )
                        }
                    }
                }
            } else {
                // 3. MENU SELECTION UI ENGINE CONTAINER (Rendered only if active Selected Outlet exists)
                OutlinedTextField(
                    value = state.menuSearchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search product name, category...") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal Interactive Chip Category Row Slider
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategory == null,
                            onClick = { viewModel.selectCategoryScope(null) },
                            label = { Text("All Categories") }
                        )
                    }
                    items(state.categories) { category ->
                        FilterChip(
                            selected = state.selectedCategory == category.name,
                            onClick = { viewModel.selectCategoryScope(category.name) },
                            label = { Text("${category.emoji} ${category.name}") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (processedFilteredMenu.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No matching catalog item inventories found.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Two-Column Grid displaying dynamic catalog components
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        items(processedFilteredMenu, key = { it.id }) { item ->
                            val activeCartItemSpec = state.cartSelectedItems[item.id]

                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    if (!item.image.isNullOrBlank()) {
                                        AsyncImage(
                                            model = item.image,
                                            contentDescription = item.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            minLines = 2,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "₹${item.finalPrice}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Quantities Stepper Switch
                                        if (activeCartItemSpec == null) {
                                            Button(
                                                onClick = { viewModel.incrementCartItem(item) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Add to Cart")
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(onClick = { viewModel.decrementCartItem(item) }) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Decrease Quantity")
                                                }
                                                Text(
                                                    text = activeCartItemSpec.qty.toString(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                IconButton(onClick = { viewModel.incrementCartItem(item) }) {
                                                    Icon(Icons.Default.Add, contentDescription = "Increase Quantity")
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