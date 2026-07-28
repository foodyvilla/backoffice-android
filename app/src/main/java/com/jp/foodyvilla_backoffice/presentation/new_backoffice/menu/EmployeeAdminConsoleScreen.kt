package com.jp.foodyvilla_backoffice.presentation.new_backoffice.attendance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeRole
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.EmployeeAdminRowCard
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.EmployeeAdminViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeAdminConsoleScreen(viewModel: EmployeeAdminViewModel = koinViewModel(), onMenuClick: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var outletSelectionMenuOpen by remember { mutableStateOf(false) }
    var roleSelectionMenuOpen by remember { mutableStateOf(false) }

    var localImageUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        localImageUri = uri
    }

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
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { localImageUri = null; viewModel.initFormWorkspace(null) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Insert Row")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.employeeSearchQuery, onValueChange = viewModel::updateSearchFilter,
                modifier = Modifier.fillMaxWidth(), placeholder = { Text("Filter roster by staff name or contact fields...") },
                leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(12.dp), singleLine = true
            )

            AnimatedVisibility(visible = state.dynamicErrorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(text = state.dynamicErrorMessage.orEmpty(), modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                }
            }

            if (state.isLoading && state.employeesList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredStaff, key = { it.id }) { staffMember ->
                        EmployeeAdminRowCard(
                            record = staffMember,
                            onModifyClick = { localImageUri = null; viewModel.initFormWorkspace(staffMember) },
                            onPurgeClick = { viewModel.dispatchPurgeAction(staffMember.id) }
                        )
                    }
                }
            }
        }

        // =====================================================================
        // UNIFIED EDIT & INSERT COMPOSITIONAL DIALOG
        // =====================================================================
        if (state.isFormWindowOpen) {
            val selectedOutletName = state.cachedOutlets.find { it.id == state.formOutletId }?.name ?: "Assign Outlet Unit"

            AlertDialog(
                onDismissRequest = viewModel::dismissFormWorkspace,
                title = { Text(text = if (state.formWorkspaceTargetId == null) "Create Roster Profile Row" else "Update Profile Fields", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // --- MULTIMEDIA IMAGE CAPTURE BLOCK CONFIGURATION ---
                        Box(
                            modifier = Modifier.size(90.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (localImageUri != null) {
                                AsyncImage(model = localImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else if (state.formProfileImgUrl.isNotBlank()) {
                                AsyncImage(model = state.formProfileImgUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.outline)
                                    Text("Photo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }

                        // --- TEXT FIELD EDITABLE SPECS INPUT MATRIX ---
                        OutlinedTextField(value = state.formName, onValueChange = viewModel::onFormNameChanged, label = { Text("Full Identity Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = state.formContact, onValueChange = viewModel::onFormContactChanged, label = { Text("Primary Contact Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                        OutlinedTextField(value = state.formPasswordText, onValueChange = viewModel::onFormPasswordChanged, label = { Text("Employee Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = state.formAadharNo, onValueChange = viewModel::onFormAadharChanged, label = { Text("Aadhaar Verification Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { outletSelectionMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = selectedOutletName)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = outletSelectionMenuOpen, onDismissRequest = { outletSelectionMenuOpen = false }) {
                                state.cachedOutlets.forEach { outlet ->
                                    DropdownMenuItem(text = { Text(outlet.name) }, onClick = { viewModel.onFormOutletSelected(outlet.id); outletSelectionMenuOpen = false })
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { roleSelectionMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Operational Role: ${state.formRole.name}")
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = roleSelectionMenuOpen, onDismissRequest = { roleSelectionMenuOpen = false }) {
                                EmployeeRole.values().forEach { enumRole ->
                                    DropdownMenuItem(text = { Text(enumRole.name) }, onClick = { viewModel.onFormRoleChanged(enumRole); roleSelectionMenuOpen = false })
                                }
                            }
                        }

                        OutlinedTextField(value = state.formSalary, onValueChange = viewModel::onFormSalaryChanged, label = { Text("Monthly Compensation Base (INR)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                        OutlinedTextField(value = state.formAddress, onValueChange = viewModel::onFormAddressChanged, label = { Text("Residential Mailing Address") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.formEmergencyContact, onValueChange = viewModel::onFormEmergencyChanged, label = { Text("Emergency Alt Contact") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                        OutlinedTextField(value = state.formJoiningDate, onValueChange = viewModel::onFormJoiningDateChanged, label = { Text("Onboarding Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Roster Active Contract Status")
                            Switch(checked = state.formIsActive, onCheckedChange = viewModel::onFormActiveToggled)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.dispatchCommitCrudAction(localImageUri) }, enabled = !state.isLoading) {
                        Text(if (state.isLoading) "Processing..." else "Commit Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissFormWorkspace, enabled = !state.isLoading) { Text("Cancel") }
                }
            )
        }
    }
}