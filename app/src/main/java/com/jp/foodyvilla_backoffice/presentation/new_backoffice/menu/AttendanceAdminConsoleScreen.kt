package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminAttendanceStatus
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.AttendanceAdminRowCard
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.DialogType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.OperationResultDialog
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.formatPunchDate
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceAdminViewModel
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme
import java.time.Instant
import java.time.LocalDate
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
    var dateOptionDropdownOpen by remember { mutableStateOf(false) }
    
    var staffFormDropdownOpen by remember { mutableStateOf(false) }
    var statusFormDropdownOpen by remember { mutableStateOf(false) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showFormDatePicker by remember { mutableStateOf<((Long?) -> Unit)?>(null) }
    var showFormTimePicker by remember { mutableStateOf<((Int, Int) -> Unit)?>(null) }

    // Date Picker for Filter
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onFilterDateChanged(date.toString())
                    }
                    showDatePickerDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Date & Time Pickers for Form Workspace
    if (showFormDatePicker != null) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFormDatePicker = null },
            confirmButton = {
                TextButton(onClick = {
                    showFormDatePicker?.invoke(datePickerState.selectedDateMillis)
                    showFormDatePicker = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFormDatePicker = null }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showFormTimePicker != null) {
        val calendar = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showFormTimePicker = null },
            confirmButton = {
                TextButton(onClick = {
                    showFormTimePicker?.invoke(timePickerState.hour, timePickerState.minute)
                    showFormTimePicker = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFormTimePicker = null }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    fun launchFullDateTimePicker(onDateTimeSelected: (String) -> Unit) {
        showFormDatePicker = { dateMillis ->
            if (dateMillis != null) {
                showFormTimePicker = { hour, minute ->
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
            val matchSearch = state.logsSearchQuery.isBlank() ||
                    log.employeeName.contains(state.logsSearchQuery, ignoreCase = true) ||
                    log.empId.toString().contains(state.logsSearchQuery)
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

    val hasActiveFilters = state.filterEmpId != null || state.filterStatus != null ||
            state.filterDate.isNotBlank() || state.filterMonth != null || state.logsSearchQuery.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Punch Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { dateOptionDropdownOpen = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
                    }
                    IconButton(onClick = { /* Export action */ }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Punch", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { viewModel.initFormWorkspace(null) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. ATTENDANCE SUMMARY ROW (70-85dp high)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryStatDotItem(label = "Present", count = summary["Present"] ?: 0, color = AppTheme.colors.success)
                    SummaryStatDotItem(label = "Late", count = summary["Late"] ?: 0, color = AppTheme.colors.warning)
                    SummaryStatDotItem(label = "Half Day", count = summary["HalfDay"] ?: 0, color = Color(0xFFE65100))
                    SummaryStatDotItem(label = "Absent", count = summary["Absent"] ?: 0, color = MaterialTheme.colorScheme.error)
                }
            }

            // 2. SEARCH BAR (~52dp high)
            OutlinedTextField(
                value = state.logsSearchQuery,
                onValueChange = viewModel::updateSearchFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = { Text("Search employees...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
                trailingIcon = {
                    if (state.logsSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchFilter("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                },
                shape = RoundedCornerShape(26.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // 3. COMPACT FILTER CHIPS ROW (Horizontally Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Filter Chip
                Box {
                    val dateLabel = if (state.filterDate.isNotBlank()) {
                        formatPunchDate(state.filterDate)
                    } else "06 Sep 2026"
                    
                    FilterChip(
                        selected = state.filterDate.isNotBlank(),
                        onClick = { dateOptionDropdownOpen = true },
                        label = { Text("📅 $dateLabel") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        shape = RoundedCornerShape(20.dp)
                    )

                    DropdownMenu(
                        expanded = dateOptionDropdownOpen,
                        onDismissRequest = { dateOptionDropdownOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Today") },
                            onClick = {
                                viewModel.onFilterDateChanged(LocalDate.now().toString())
                                dateOptionDropdownOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Yesterday") },
                            onClick = {
                                viewModel.onFilterDateChanged(LocalDate.now().minusDays(1).toString())
                                dateOptionDropdownOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Select Date...") },
                            onClick = {
                                dateOptionDropdownOpen = false
                                showDatePickerDialog = true
                            }
                        )
                        if (state.filterDate.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text("Clear Date Filter", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    viewModel.onFilterDateChanged("")
                                    dateOptionDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Employee Filter Chip
                Box {
                    val selectedName = state.cachedEmployees.find { it.id == state.filterEmpId }?.name ?: "Employee"
                    FilterChip(
                        selected = state.filterEmpId != null,
                        onClick = { staffFilterDropdownOpen = true },
                        label = { Text(selectedName) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        shape = RoundedCornerShape(20.dp)
                    )
                    DropdownMenu(
                        expanded = staffFilterDropdownOpen,
                        onDismissRequest = { staffFilterDropdownOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Employees") },
                            onClick = {
                                viewModel.onFilterEmpSelected(null)
                                staffFilterDropdownOpen = false
                            }
                        )
                        state.cachedEmployees.forEach { person ->
                            DropdownMenuItem(
                                text = { Text(person.name) },
                                onClick = {
                                    viewModel.onFilterEmpSelected(person.id)
                                    staffFilterDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Status Filter Chip
                Box {
                    val statusLabel = state.filterStatus?.name ?: "Status"
                    FilterChip(
                        selected = state.filterStatus != null,
                        onClick = { statusFilterDropdownOpen = true },
                        label = { Text(statusLabel) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        shape = RoundedCornerShape(20.dp)
                    )
                    DropdownMenu(
                        expanded = statusFilterDropdownOpen,
                        onDismissRequest = { statusFilterDropdownOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Status") },
                            onClick = {
                                viewModel.onFilterStatusSelected(null)
                                statusFilterDropdownOpen = false
                            }
                        )
                        AdminAttendanceStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name) },
                                onClick = {
                                    viewModel.onFilterStatusSelected(status)
                                    statusFilterDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Month Filter Chip
                Box {
                    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    val monthLabel = state.filterMonth?.let { months[it - 1] } ?: "Month"
                    FilterChip(
                        selected = state.filterMonth != null,
                        onClick = { monthFilterDropdownOpen = true },
                        label = { Text(monthLabel) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        shape = RoundedCornerShape(20.dp)
                    )
                    DropdownMenu(
                        expanded = monthFilterDropdownOpen,
                        onDismissRequest = { monthFilterDropdownOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Months") },
                            onClick = {
                                viewModel.onFilterMonthChanged(null)
                                monthFilterDropdownOpen = false
                            }
                        )
                        months.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    viewModel.onFilterMonthChanged(index + 1)
                                    monthFilterDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Clear All Filters Option
                if (hasActiveFilters) {
                    TextButton(
                        onClick = {
                            viewModel.onFilterEmpSelected(null)
                            viewModel.onFilterStatusSelected(null)
                            viewModel.onFilterDateChanged("")
                            viewModel.onFilterMonthChanged(null)
                            viewModel.updateSearchFilter("")
                        }
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // 4. EMPLOYEE COUNT & SORT HEADER ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredLogs.size} employees",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* Sort option */ }
                ) {
                    Text(
                        text = "Sort",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. EMPLOYEE ATTENDANCE CARDS LIST
            if (state.isLoading && state.recordsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No attendance records found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { logNode ->
                        AttendanceAdminRowCard(
                            record = logNode,
                            onModifyClick = { viewModel.initFormWorkspace(logNode) },
                            onDeleteClick = { viewModel.dispatchPurgeAction(logNode.id) }
                        )
                    }
                }
            }
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

        // =====================================================================
        // MANUAL OVERRIDE / ENTRY MODAL DIALOG
        // =====================================================================
        if (state.isFormWindowOpen) {
            val selectedStaffName = state.cachedEmployees.find { it.id == state.formEmpId }?.name ?: "Select Employee Profile"

            AlertDialog(
                onDismissRequest = viewModel::dismissFormWorkspace,
                title = {
                    Text(
                        text = if (state.formWorkspaceTargetId == null) "Add Attendance Record" else "Update Attendance Log",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Employee Selection Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { if (state.formWorkspaceTargetId == null) staffFormDropdownOpen = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = selectedStaffName, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(
                                expanded = staffFormDropdownOpen,
                                onDismissRequest = { staffFormDropdownOpen = false }
                            ) {
                                state.cachedEmployees.forEach { person ->
                                    DropdownMenuItem(
                                        text = { Text(person.name) },
                                        onClick = {
                                            viewModel.onFormEmpSelected(person.id)
                                            staffFormDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status Selection Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { statusFormDropdownOpen = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Status: ${state.formStatus.name}", modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(
                                expanded = statusFormDropdownOpen,
                                onDismissRequest = { statusFormDropdownOpen = false }
                            ) {
                                AdminAttendanceStatus.entries.forEach { enumStatus ->
                                    DropdownMenuItem(
                                        text = { Text(enumStatus.name) },
                                        onClick = {
                                            viewModel.onFormStatusChanged(enumStatus)
                                            statusFormDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        // Punch In Field with Date/Time Picker Trigger
                        OutlinedTextField(
                            value = state.formInTime,
                            onValueChange = viewModel::onFormInTimeChanged,
                            label = { Text("Punch In Time") },
                            placeholder = { Text("YYYY-MM-DD HH:MM:SS") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { launchFullDateTimePicker { viewModel.onFormInTimeChanged(it) } }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Pick Date Time")
                                }
                            }
                        )

                        // Punch Out Field with Date/Time Picker Trigger
                        OutlinedTextField(
                            value = state.formOutTime,
                            onValueChange = viewModel::onFormOutTimeChanged,
                            label = { Text("Punch Out Time") },
                            placeholder = { Text("YYYY-MM-DD HH:MM:SS") },
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
                        Text("Save Record")
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

@Composable
private fun SummaryStatDotItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
