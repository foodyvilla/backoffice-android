package com.jp.foodyvilla_backoffice.presentation.new_backoffice.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.DialogType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.EmployeeAdminRowCard
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.OperationResultDialog
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.ZoomableImagePreviewDialog
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.EmployeeAdminViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeAdminConsoleScreen(
    viewModel: EmployeeAdminViewModel = koinViewModel(),
    onNavigateToAddEmployee: () -> Unit = {},
    onNavigateToEditEmployee: (Long) -> Unit = {},
    onMenuClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var previewProfileUrl by remember { mutableStateOf<String?>(null) }
    var previewProfileTitle by remember { mutableStateOf("") }
    var showPreviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAllEmployeesDirectory()
    }

    val filteredStaff = remember(state.employeesList, state.employeeSearchQuery) {
        state.employeesList.filter { employee ->
            employee.name.contains(state.employeeSearchQuery, ignoreCase = true) ||
                    employee.contact.contains(state.employeeSearchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Roster Desk", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddEmployee) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.employeeSearchQuery,
                onValueChange = viewModel::updateSearchFilter,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Filter roster by staff name or contact fields...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (state.isLoading && state.employeesList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStaff, key = { it.id }) { staffMember ->
                        EmployeeAdminRowCard(
                            record = staffMember,
                            onModifyClick = { onNavigateToEditEmployee(staffMember.id) },
                            onPurgeClick = { viewModel.dispatchPurgeAction(staffMember.id) },
                            onProfileImageClick = {
                                if (staffMember.profileImgUrl.isNotBlank()) {
                                    previewProfileUrl = staffMember.profileImgUrl
                                    previewProfileTitle = "${staffMember.name}'s Profile Photo"
                                    showPreviewDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }

        // =====================================================================
        // ZOOMABLE PROFILE IMAGE PREVIEW DIALOG
        // =====================================================================
        if (showPreviewDialog && !previewProfileUrl.isNullOrBlank()) {
            ZoomableImagePreviewDialog(
                title = previewProfileTitle,
                imageUrl = previewProfileUrl,
                onDismiss = { showPreviewDialog = false }
            )
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

        if (state.dynamicErrorMessage != null) {
            OperationResultDialog(
                type = DialogType.ERROR,
                title = "Operation Failed",
                message = state.dynamicErrorMessage.orEmpty(),
                onDismiss = viewModel::dismissErrorMessage
            )
        }
    }
}