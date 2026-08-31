package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
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
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCategoryUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.ProductCatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCategoryManagementScreen(
    viewModel: ProductCatalogViewModel,
    onMenuClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var targetDeleteCandidate by remember { mutableStateOf<ProductCategoryUiModel?>(null) }

    // Reactive filter logic operating on memory arrays locally
    val filteredCategories = remember(state.categoriesList, state.categorySearchQuery) {
        if (state.categorySearchQuery.isBlank()) state.categoriesList else {
            state.categoriesList.filter { it.name.contains(state.categorySearchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openCategoryCreationForm,
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, contentDescription = "Add New Category") }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
//                Text(
//                    text = "Menu Category Parameters Layout",
//                    style = MaterialTheme.typography.headlineMedium,
//                    fontWeight = FontWeight.Bold
//                )
                Spacer(modifier = Modifier.height(16.dp))

                // SEARCH BAR WORKSPACE
                OutlinedTextField(
                    value = state.categorySearchQuery,
                    onValueChange = viewModel::updateCategorySearch,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    placeholder = { Text("Search categories by name...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        AnimatedVisibility(visible = state.categorySearchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateCategorySearch("") }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (state.errorMessage != null) {
                    Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp), fontWeight = FontWeight.Bold)
                }

                if (state.isLoading && state.categoriesList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (filteredCategories.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No match configurations exist for your filter query.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCategories, key = { it.id }) { category ->
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = category.emoji.ifBlank { "🍽️" },
                                                style = MaterialTheme.typography.headlineMedium,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                        Column {
                                            Text(category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (category.isActive) "● Available to Customers" else "○ Hidden from Applications",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (category.isActive) Color(0xFF4CAF50) else Color(0xFFE53935),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { viewModel.openCategoryEditionForm(category) }) {
                                            Icon(Icons.Default.Edit, "Edit Row Parameters", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { targetDeleteCandidate = category }) {
                                            Icon(Icons.Default.Delete, "Purge Row from Table", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ====================================================
            // FIXED: PROPER STATE MAPPINGS ASSIGNED INSIDE INPUT FIELDS
            // ====================================================
            if (state.isCategoryFormOpen) {
                AlertDialog(
                    onDismissRequest = viewModel::closeCategoryForm,
                    title = { Text(if (state.targetCategory == null) "Create Menu Category" else "Modify Category Configurations", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.cName,
                                onValueChange = viewModel::onCNameChanged,
                                label = { Text("Category Section Title Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.cEmoji,
                                onValueChange = viewModel::onCEmojiChanged,
                                label = { Text("Display Icon Emoji Asset") },
                                placeholder = { Text("e.g. 🍛, 🥤") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Active Visibility Stream", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("When disabled, this whole category hides from customer apps.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = state.cIsActive,
                                    onCheckedChange = viewModel::onCActiveToggled
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = viewModel::commitCategoryFormAction, shape = RoundedCornerShape(8.dp)) {
                            Text("Save Category Changes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::closeCategoryForm) { Text("Cancel") }
                    }
                )
            }

            // ====================================================
            // ANTI-ACCIDENTAL TAP DELETION CONFIRMATION GATEWAY
            // ====================================================
            targetDeleteCandidate?.let { candidate ->
                AlertDialog(
                    onDismissRequest = { targetDeleteCandidate = null },
                    title = { Text("Confirm Category Purging", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you completely sure you want to remove '${candidate.emoji} ${candidate.name}' permanently? This operation will break items still relying on this database relation node mapping.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteCategoryRow(candidate.id)
                                targetDeleteCandidate = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Confirm and Delete Row") }
                    },
                    dismissButton = {
                        TextButton(onClick = { targetDeleteCandidate = null }) { Text("Bypass Operation") }
                    }
                )
            }
        }
    }
}