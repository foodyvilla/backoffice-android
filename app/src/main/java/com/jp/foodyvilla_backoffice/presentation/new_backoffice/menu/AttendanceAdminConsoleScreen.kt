package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminAttendanceStatus
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.AttendanceAdminRowCard
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceAdminViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceAdminConsoleScreen(viewModel: AttendanceAdminViewModel, onMenuClick: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var staffFilterDropdownOpen by remember { mutableStateOf(false) }
    var statusFilterDropdownOpen by remember { mutableStateOf(false) }
    var monthFilterDropdownOpen by remember { mutableStateOf(false) }
    
    var staffFormDropdownOpen by remember { mutableStateOf(false) }
    var statusFormDropdownOpen by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf<((Long?) -> Unit)?>(null) }
    var showTimePicker by remember { mutableStateOf<((Int, Int) -> Unit)?>(null) }

    if (showDatePicker != null) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = null },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker?.invoke(datePickerState.selectedDateMillis)
                    showDatePicker = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = null }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker != null) {
        val calendar = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = null },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker?.invoke(timePickerState.hour, timePickerState.minute)
                    showTimePicker = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = null }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    fun launchFullDateTimePicker(onDateTimeSelected: (String) -> Unit) {
        showDatePicker = { dateMillis ->
            if (dateMillis != null) {
                showTimePicker = { hour, minute ->
                    val date = Instant.ofEpochMilli(dateMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val dateTime = LocalDateTime.of(date, LocalTime.of(hour, minute))
                    onDateTimeSelected(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllLogsDirectory()
    }

    val filteredLogs = remember(
        state.recordsList, state.logsSearchQuery, state.filterEmpId,
        state.filterStatus, state.filterDate, state.filterMonth
    ) {
        state.recordsList.filter { log ->
            val matchSearch = log.employeeName.contains(state.logsSearchQuery, ignoreCase = true) ||
                    log.empId.toString() == state.logsSearchQuery
            val matchEmp = state.filterEmpId == null || log.empId == state.filterEmpId
            val matchStatus = state.filterStatus == null || log.status == state.filterStatus
            val matchDate = state.filterDate.isBlank() || log.inTime.startsWith(state.filterDate)
            val matchMonth = state.filterMonth == null || (log.inTime.isNotBlank() && log.inTime.split("-").getOrNull(1)?.toIntOrNull() == state.filterMonth)
            
            matchSearch && matchEmp && matchStatus && matchDate && matchMonth
        }
    }

    val summary = remember(filteredLogs) {
        val present = filteredLogs.count { it.status == AdminAttendanceStatus.PRESENT }
        val late = filteredLogs.count { it.status == AdminAttendanceStatus.LATE }
        val halfDay = filteredLogs.count { it.status == AdminAttendanceStatus.HALF_DAY }
        val absent = filteredLogs.count { it.status == AdminAttendanceStatus.ABSENT }
        mapOf("Present" to present, "Late" to late, "HalfDay" to halfDay, "Absent" to absent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Admin Workspace", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.initFormWorkspace(null) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Manual Override Log Insert")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SUMMARY STATISTICS SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    summary.forEach { (label, count) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(text = count.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // FILTERS ROW
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Employee Filter
                Box(modifier = Modifier.weight(1f)) {
                    val selectedName = state.cachedEmployees.find { it.id == state.filterEmpId }?.name ?: "All Employees"
                    OutlinedButton(onClick = { staffFilterDropdownOpen = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text(text = selectedName, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = staffFilterDropdownOpen, onDismissRequest = { staffFilterDropdownOpen = false }) {
                        DropdownMenuItem(text = { Text("All Employees") }, onClick = {
                            viewModel.onFilterEmpSelected(null)
                            staffFilterDropdownOpen = false
                        })
                        state.cachedEmployees.forEach { person ->
                            DropdownMenuItem(text = { Text(person.name) }, onClick = {
                                viewModel.onFilterEmpSelected(person.id)
                                staffFilterDropdownOpen = false
                            })
                        }
                    }
                }

                // Status Filter
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { statusFilterDropdownOpen = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text(text = state.filterStatus?.name ?: "All Status", style = MaterialTheme.typography.labelSmall)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = statusFilterDropdownOpen, onDismissRequest = { statusFilterDropdownOpen = false }) {
                        DropdownMenuItem(text = { Text("All Status") }, onClick = {
                            viewModel.onFilterStatusSelected(null)
                            statusFilterDropdownOpen = false
                        })
                        AdminAttendanceStatus.entries.forEach { status ->
                            DropdownMenuItem(text = { Text(status.name) }, onClick = {
                                viewModel.onFilterStatusSelected(status)
                                statusFilterDropdownOpen = false
                            })
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Month Filter
                Box(modifier = Modifier.weight(1f)) {
                    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    OutlinedButton(onClick = { monthFilterDropdownOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = state.filterMonth?.let { months[it - 1] } ?: "All Months", style = MaterialTheme.typography.labelSmall)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = monthFilterDropdownOpen, onDismissRequest = { monthFilterDropdownOpen = false }) {
                        DropdownMenuItem(text = { Text("All Months") }, onClick = {
                            viewModel.onFilterMonthChanged(null)
                            monthFilterDropdownOpen = false
                        })
                        months.forEachIndexed { index, name ->
                            DropdownMenuItem(text = { Text(name) }, onClick = {
                                viewModel.onFilterMonthChanged(index + 1)
                                monthFilterDropdownOpen = false
                            })
                        }
                    }
                }

                // Date Filter (YYYY-MM-DD)
                OutlinedTextField(
                    value = state.filterDate,
                    onValueChange = viewModel::onFilterDateChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Date (YYYY-MM-DD)", style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelSmall
                )
            }

            // SEARCH QUERY BAR NODE
            OutlinedTextField(
                value = state.logsSearchQuery,
                onValueChange = viewModel::updateSearchFilter,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or ID...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // RUNTIME SYSTEM EXCEPTION WARNING TIERS BOUNDARY
            AnimatedVisibility(visible = state.dynamicErrorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = state.dynamicErrorMessage.orEmpty(),
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // LIST LAYER SYSTEM INTERFACE PIPELINE RENDER
            if (state.isLoading && state.recordsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No records found for the selected filters.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { logNode ->
                        AttendanceAdminRowCard(
                            record = logNode,
                            onModifyClick = { viewModel.initFormWorkspace(logNode) }
                        )
                    }
                }
            }
        }

        // =====================================================================
        // MODAL ENTRY DIALOG CRU WORKSPACE
        // =====================================================================
        if (state.isFormWindowOpen) {
            val selectedStaffName = state.cachedEmployees.find { it.id == state.formEmpId }?.name ?: "Select Target Employee Node"
            
            AlertDialog(
                onDismissRequest = viewModel::dismissFormWorkspace,
                title = {
                    Text(
                        text = if (state.formWorkspaceTargetId == null) "Insert Manual Override Log" else "Update Database Punch row",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Relational dropdown mapping target profiles
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { if (state.formWorkspaceTargetId == null) staffFormDropdownOpen = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = selectedStaffName)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = staffFormDropdownOpen, onDismissRequest = { staffFormDropdownOpen = false }) {
                                state.cachedEmployees.forEach { person ->
                                    DropdownMenuItem(text = { Text(person.name) }, onClick = {
                                        viewModel.onFormEmpSelected(person.id)
                                        staffFormDropdownOpen = false
                                    })
                                }
                            }
                        }

                        // Status metric selection controller dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { statusFormDropdownOpen = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Status: ${state.formStatus.name}")
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = statusFormDropdownOpen, onDismissRequest = { statusFormDropdownOpen = false }) {
                                AdminAttendanceStatus.entries.forEach { enumStatus ->
                                    DropdownMenuItem(text = { Text(enumStatus.name) }, onClick = {
                                        viewModel.onFormStatusChanged(enumStatus)
                                        statusFormDropdownOpen = false
                                    })
                                }
                            }
                        }

                        // Temporal parameters input boxes
                        OutlinedTextField(
                            value = state.formInTime,
                            onValueChange = viewModel::onFormInTimeChanged,
                            label = { Text("Punch In (YYYY-MM-DD HH:MM:SS)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { launchFullDateTimePicker { viewModel.onFormInTimeChanged(it) } }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Pick Date Time")
                                }
                            }
                        )
                        OutlinedTextField(
                            value = state.formOutTime,
                            onValueChange = viewModel::onFormOutTimeChanged,
                            label = { Text("Punch Out (YYYY-MM-DD HH:MM:SS)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { launchFullDateTimePicker { viewModel.onFormOutTimeChanged(it) } }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Pick Date Time")
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = viewModel::dispatchCommitCrudAction) {
                        Text("Commit Row Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissFormWorkspace) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}