package com.jp.foodyvilla_backoffice.presentation.new_backoffice.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.AttendanceDetailsVerificationDialog
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.AttendanceMetricSummaryCard
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.CustomCalendarMatrixView
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerAttendanceDashboardScreen(
    outletsListContext: List<OutletUiModel>,
    viewModel: AttendanceViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var outletDropdownOpen by remember { mutableStateOf(false) }
    var employeeDropdownOpen by remember { mutableStateOf(false) }

    val currentSelectedOutlet = outletsListContext.find { it.id == state.selectedOutletId }
    val currentSelectedEmployee = state.outletEmployeesList.find { it.id == state.activeEmployee?.id }

    LaunchedEffect(outletsListContext) { viewModel.loadManagementOutlets(outletsListContext) }

    Scaffold(topBar = { TopAppBar(title = { Text("Franchise Staff Logs Central", fontWeight = FontWeight.Bold) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { outletDropdownOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(currentSelectedOutlet?.name ?: "Select Outlet", maxLines = 1)
                        Icon(Icons.Default.ArrowDropUp, null)
                    }
                    DropdownMenu(expanded = outletDropdownOpen, onDismissRequest = { outletDropdownOpen = false }) {
                        outletsListContext.forEach { branch ->
                            DropdownMenuItem(text = { Text(branch.name) }, onClick = { viewModel.onOutletSelected(branch.id); outletDropdownOpen = false })
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { employeeDropdownOpen = true }, modifier = Modifier.fillMaxWidth(), enabled = state.outletEmployeesList.isNotEmpty()) {
                        Text(currentSelectedEmployee?.name ?: "Select Staff", maxLines = 1)
                        Icon(Icons.Default.ArrowDropUp, null)
                    }
                    DropdownMenu(expanded = employeeDropdownOpen, onDismissRequest = { employeeDropdownOpen = false }) {
                        state.outletEmployeesList.forEach { staff ->
                            DropdownMenuItem(text = { Text(staff.name) }, onClick = { viewModel.loadEmployeeContext(staff.id); employeeDropdownOpen = false })
                        }
                    }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.activeEmployee == null) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Select a branch location and choose an employee profile context to view logs.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CustomCalendarMatrixView(
                        currentMonth = state.currentYearMonth, attendanceMap = state.attendanceMap,
                        onDaySelected = viewModel::selectDayRecord,
                        onNavigateBack = viewModel::shiftCalendarMonthBackward, onNavigateForward = viewModel::shiftCalendarMonthForward,
                        onMonthYearJump = { m, y -> viewModel.updateCalendarJumpTarget(m, y) }
                    )
                    state.activeEmployee?.salary?.let { payScale -> AttendanceMetricSummaryCard(salary = payScale, history = state.attendanceMap.values) }
                }
            }
        }
        state.selectedDayRecord?.let { record -> AttendanceDetailsVerificationDialog(record = record, onDismiss = { viewModel.selectDayRecord(null) }) }
    }
}