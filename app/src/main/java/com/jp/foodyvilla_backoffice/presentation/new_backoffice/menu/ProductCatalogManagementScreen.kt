package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCatalogUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.ProductCatalogViewModel


import androidx.compose.foundation.border

import androidx.compose.material.icons.filled.Timer

import androidx.compose.ui.text.style.TextOverflow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogManagementScreen(viewModel: ProductCatalogViewModel, onMenuClick: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dropdownExpanded by remember { mutableStateOf(false) }

    // DYNAMIC IN-MEMORY FILTER FOR PRODUCTS
    val filteredCatalogProducts = remember(state.productsList, state.productSearchQuery) {
        if (state.productSearchQuery.isBlank()) {
            state.productsList
        } else {
            state.productsList.filter { product ->
                product.name.contains(state.productSearchQuery, ignoreCase = true) ||
                        product.categoryName.contains(state.productSearchQuery, ignoreCase = true) ||
                        product.description.contains(state.productSearchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Catalog", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openProductCreationForm,
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, contentDescription = "Create Item Entry") }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
//                Text(
//                    text = "Master Menu Product Catalog",
//                    style = MaterialTheme.typography.headlineMedium,
//                    fontWeight = FontWeight.Bold
//                )
                Spacer(modifier = Modifier.height(12.dp))

                // SEARCH BAR MAPPED TO THE CORRECT PRODUCT SEARCH STATE
                OutlinedTextField(
                    value = state.productSearchQuery,
                    onValueChange = viewModel::updateProductSearch,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    placeholder = { Text("Search by name, description or category...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        AnimatedVisibility(visible = state.productSearchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateProductSearch("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search Input Field")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (state.isLoading && state.productsList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredCatalogProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.productsList.isEmpty()) "No products tracked inside database."
                            else "No records match your search phrase criteria.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCatalogProducts, key = { it.id }) { product ->
                            CatalogProductRow(
                                product = product,
                                onEditClick = { viewModel.openProductEditionForm(product) },
                                onDeleteClick = { viewModel.deleteProductRow(product.id) }
                            )
                        }
                    }
                }
            }

            // ====================================================
            // FIXED PRODUCT FORM WORKSPACE DIALOG MATCHING STATE
            // ====================================================
            if (state.isProductFormOpen) {
                AlertDialog(
                    onDismissRequest = viewModel::closeProductForm,
                    title = {
                        Text(
                            text = if (state.targetProduct == null) "Add Catalog Entry" else "Update Core Product Specifications",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.pName,
                                onValueChange = viewModel::onPNameChanged,
                                label = { Text("Product Display Label") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // Category Selection Dropdown mapped to categoriesList
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.pSelectedCategory?.name ?: "Tap to Link Category Row",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category Table Binding Reference") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { dropdownExpanded = true }) {
                                            Icon(Icons.Default.Add, null)
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    state.categoriesList.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text("${cat.emoji} ${cat.name}") },
                                            onClick = {
                                                viewModel.onPCatSelected(cat)
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = state.pPrepTime,
                                onValueChange = viewModel::onPPrepChanged,
                                label = { Text("Preparation Duration Time (e.g., 15 mins)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = state.pDescription,
                                onValueChange = viewModel::onPDescChanged,
                                label = { Text("Nutritional Details Summary") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Veg Food Standard Indicator")
                                Switch(checked = state.pIsVeg, onCheckedChange = viewModel::onPVegToggled)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Is Vegan Recipe")
                                Switch(checked = state.pIsVegan, onCheckedChange = viewModel::onPVeganToggled)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tag as Franchise Bestseller")
                                Switch(checked = state.pIsBestseller, onCheckedChange = viewModel::onPBestToggled)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = viewModel::commitProductFormAction, shape = RoundedCornerShape(8.dp)) {
                            Text("Save Document Changes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::closeProductForm) { Text("Cancel") }
                    }
                )
            }
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogProductRow(
    product: ProductCatalogUiModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top // Aligned top for cleaner layout structure with descriptions
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ROW 1: Veg Status Badge Indicator + Title Line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Standard explicit Food-Tech Veg/NonVeg Square Frame Badge
                    val badgeColor = if (product.isVeg) Color(0xFF4CAF50) else Color(0xFFE53935)
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .border(1.5.dp, badgeColor, RoundedCornerShape(2.dp))
                            .padding(2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = badgeColor,
                            shape = RoundedCornerShape(1.dp)
                        ) {}
                    }

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (product.isBestseller) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🔥 BESTSELLER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // ROW 2: Structured Category & Prep Time Badges Block
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(product.categoryName) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null,
                        modifier = Modifier.height(24.dp)
                    )

                    if (product.prepTime.isNotBlank()) {
                        SuggestionChip(
                            onClick = {},
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            label = { Text(product.prepTime) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null,
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                // ROW 3: Description Paragraph Frame
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // RIGHT PANEL ACTION CONTROLS STRIP
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                FilledIconButton(
                    onClick = onEditClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Item Specification Details",
                        modifier = Modifier.size(18.dp)
                    )
                }

                FilledIconButton(
                    onClick = { showDeleteConfirmation = true },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Product Entry File",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // INTERACTION PROTECTION SECURITY OVERLAY DIALOG GATEWAY
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Catalog Deletion", fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely sure you want to remove '${product.name}' permanently from your central catalog index schema data records?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}