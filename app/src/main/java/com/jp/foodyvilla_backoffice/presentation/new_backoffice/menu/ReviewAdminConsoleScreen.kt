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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminReviewType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.ReviewAdminRowCard
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.ReviewAdminViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewAdminConsoleScreen(viewModel: ReviewAdminViewModel = koinViewModel(), onMenuClick: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    var userFilterDropdownOpen by remember { mutableStateOf(false) }
    var typeFilterDropdownOpen by remember { mutableStateOf(false) }
    var ratingFilterDropdownOpen by remember { mutableStateOf(false) }

    var userFormDropdownOpen by remember { mutableStateOf(false) }
    var typeFormDropdownOpen by remember { mutableStateOf(false) }
    var ratingFormDropdownOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAllReviewsDirectory()
    }

    val filteredReviews = remember(
        state.reviewsList, state.reviewsSearchQuery, 
        state.filterCustomerId, state.filterType, state.filterRating
    ) {
        state.reviewsList.filter { rev ->
            val matchSearch = rev.customerName.contains(state.reviewsSearchQuery, ignoreCase = true) ||
                    rev.description.contains(state.reviewsSearchQuery, ignoreCase = true) ||
                    rev.title.contains(state.reviewsSearchQuery, ignoreCase = true)
            
            val matchCustomer = state.filterCustomerId == null || rev.customerId == state.filterCustomerId
            val matchType = state.filterType == null || rev.reviewType == state.filterType
            val matchRating = state.filterRating == null || rev.rating == state.filterRating
            
            matchSearch && matchCustomer && matchType && matchRating
        }
    }

    val summary = remember(filteredReviews) {
        val total = filteredReviews.size
        val avgRating = if (total > 0) filteredReviews.map { it.rating }.average() else 0.0
        val typeBreakdown = filteredReviews.groupBy { it.reviewType }.mapValues { it.value.size }
        Triple(total, avgRating, typeBreakdown)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Reviews Workspace", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.initFormWorkspace(null) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Manual Review Override Entry")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SUMMARY CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", style = MaterialTheme.typography.labelMedium)
                        Text("${summary.first}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Avg Rating", style = MaterialTheme.typography.labelMedium)
                        Text(String.format(Locale.getDefault(), "%.1f ★", summary.second), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                    }
                    summary.third.forEach { (type, count) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(type.name, style = MaterialTheme.typography.labelMedium)
                            Text("$count", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // FILTERS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1.2f)) {
                    val selectedUser = state.cachedUsers.find { it.id == state.filterCustomerId }?.name ?: "All Users"
                    OutlinedButton(onClick = { userFilterDropdownOpen = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text(selectedUser, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = userFilterDropdownOpen, onDismissRequest = { userFilterDropdownOpen = false }) {
                        DropdownMenuItem(text = { Text("All Users") }, onClick = { viewModel.onFilterCustomerSelected(null); userFilterDropdownOpen = false })
                        state.cachedUsers.forEach { user ->
                            DropdownMenuItem(text = { Text(user.name.orEmpty()) }, onClick = { viewModel.onFilterCustomerSelected(user.id); userFilterDropdownOpen = false })
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { typeFilterDropdownOpen = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text(state.filterType?.name ?: "All Types", style = MaterialTheme.typography.labelSmall)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = typeFilterDropdownOpen, onDismissRequest = { typeFilterDropdownOpen = false }) {
                        DropdownMenuItem(text = { Text("All Types") }, onClick = { viewModel.onFilterTypeSelected(null); typeFilterDropdownOpen = false })
                        AdminReviewType.entries.forEach { type ->
                            DropdownMenuItem(text = { Text(type.name) }, onClick = { viewModel.onFilterTypeSelected(type); typeFilterDropdownOpen = false })
                        }
                    }
                }

                Box(modifier = Modifier.weight(0.8f)) {
                    OutlinedButton(onClick = { ratingFilterDropdownOpen = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text(state.filterRating?.let { "$it ★" } ?: "Rating", style = MaterialTheme.typography.labelSmall)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = ratingFilterDropdownOpen, onDismissRequest = { ratingFilterDropdownOpen = false }) {
                        DropdownMenuItem(text = { Text("All Ratings") }, onClick = { viewModel.onFilterRatingSelected(null); ratingFilterDropdownOpen = false })
                        (5 downTo 1).forEach { rating ->
                            DropdownMenuItem(text = { Text("$rating ★") }, onClick = { viewModel.onFilterRatingSelected(rating); ratingFilterDropdownOpen = false })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.reviewsSearchQuery,
                onValueChange = viewModel::updateSearchFilter,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search comments or user labels...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            AnimatedVisibility(visible = state.dynamicErrorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(text = state.dynamicErrorMessage.orEmpty(), modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                }
            }

            if (state.isLoading && state.reviewsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (filteredReviews.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("No review rows matched within the current workspace.") }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredReviews, key = { it.id }) { reviewNode ->
                        ReviewAdminRowCard(record = reviewNode, onModifyClick = { viewModel.initFormWorkspace(reviewNode) }, onPurgeClick = { viewModel.dispatchPurgeAction(reviewNode.id) })
                    }
                }
            }
        }

        // =====================================================================
        // DIALOG COMPOSITIONAL OVERRIDE FORM LAYER
        // =====================================================================
        if (state.isFormWindowOpen) {
            val targetedUserName = state.cachedUsers.find { it.id == state.formCustomerId }?.name ?: "Select Customer Account"

            AlertDialog(
                onDismissRequest = viewModel::dismissFormWorkspace,
                title = { Text(text = if (state.formWorkspaceTargetId == null) "Create Manual Review Data row" else "Modify Review Fields", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { userFormDropdownOpen = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = targetedUserName)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = userFormDropdownOpen, onDismissRequest = { userFormDropdownOpen = false }) {
                                state.cachedUsers.forEach { userRow ->
                                    DropdownMenuItem(text = { Text(userRow.name.orEmpty()) }, onClick = { viewModel.onFormCustomerSelected(userRow.id); userFormDropdownOpen = false })
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { typeFormDropdownOpen = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Target Category: ${state.formReviewType.name}")
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = typeFormDropdownOpen, onDismissRequest = { typeFormDropdownOpen = false }) {
                                AdminReviewType.entries.forEach { enumType ->
                                    DropdownMenuItem(text = { Text(enumType.name) }, onClick = { viewModel.onFormTypeChanged(enumType); typeFormDropdownOpen = false })
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { ratingFormDropdownOpen = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Rating Stars: ${state.formRating} ★")
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = ratingFormDropdownOpen, onDismissRequest = { ratingFormDropdownOpen = false }) {
                                (1..5).forEach { stars ->
                                    DropdownMenuItem(text = { Text("$stars Stars") }, onClick = { viewModel.onFormRatingChanged(stars); ratingFormDropdownOpen = false })
                                }
                            }
                        }

                        OutlinedTextField(value = state.formTitle, onValueChange = viewModel::onFormTitleChanged, label = { Text("Review Summary Headline Header") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.formDescription, onValueChange = viewModel::onFormDescChanged, label = { Text("Detailed Comments Matrix CopyText") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        OutlinedTextField(value = state.formImages, onValueChange = viewModel::onFormImagesChanged, label = { Text("Image URLs (space separated)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.formOrderId, onValueChange = viewModel::onFormOrderIdChanged, label = { Text("Linked Order System UUID String (Optional)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.formMenuItemId, onValueChange = viewModel::onFormMenuItemIdChanged, label = { Text("Linked Menu Item Reference ID (Optional)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.formOutletId, onValueChange = viewModel::onFormOutletIdChanged, label = { Text("Linked Outlet Unit Reference ID (Optional)") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = { Button(onClick = viewModel::dispatchCommitCrudAction) { Text("Save Document changes") } },
                dismissButton = { TextButton(onClick = viewModel::dismissFormWorkspace) { Text("Cancel") } }
            )
        }
    }
}