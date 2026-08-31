package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.*
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeePunchReportScreen(viewModel: AttendanceViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSelfEmployeeContext()
    }

    val today = LocalDate.now()
    val isCurrentMonth = state.currentYearMonth.year == today.year && state.currentYearMonth.month == today.month
    val todayRecord = state.attendanceMap[today]

    val hasRealPunchIn = todayRecord != null && todayRecord.id != -1L && todayRecord.inTime != null
    val hasRealPunchOut = todayRecord != null && todayRecord.id != -1L && todayRecord.outTime != null

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Punch Reports Console", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Menu, "Menu") } }
            )
        }
    )
 { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount -> if (dragAmount > 40) onNavigateBack() }
        }) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp).alpha(if (state.isLoading) 0.5f else 1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isCurrentMonth) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Shift Action Control Grips", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                            if (!hasRealPunchIn) {
                                SwipeToConfirmButton(
                                    text = "Swipe to Punch In", icon = Icons.Default.PunchClock,
                                    enabled = state.activeEmployee != null && !state.isLoading,
                                    onConfirm = { viewModel.executeSelfTransactionalPunchWorkflow(isPunchOut = false) }
                                )
                            } else if (!hasRealPunchOut) {
                                SwipeToConfirmButton(
                                    text = "Swipe to Punch Out", icon = Icons.Default.LockOpen,
                                    isRightToLeft = true, containerColor = MaterialTheme.colorScheme.secondary,
                                    enabled = state.activeEmployee != null && !state.isLoading,
                                    onConfirm = { viewModel.executeSelfTransactionalPunchWorkflow(isPunchOut = true) }
                                )
                            }

                            if (hasRealPunchIn && hasRealPunchOut) {
                                Text("● Shift completed successfully for today.", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                CustomCalendarMatrixView(
                    currentMonth = state.currentYearMonth, attendanceMap = state.attendanceMap,
                    onDaySelected = viewModel::selectDayRecord,
                    onNavigateBack = viewModel::shiftCalendarMonthBackward, onNavigateForward = viewModel::shiftCalendarMonthForward,
                    onMonthYearJump = { m, y -> viewModel.updateCalendarJumpTarget(m, y) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                state.activeEmployee?.salary?.let { payScale ->
                    AttendanceMetricSummaryCard(salary = payScale, history = state.attendanceMap.values)
                }
            }

            LocationStateOverlayHandler(state = state, viewModel = viewModel)

            state.selectedDayRecord?.let { selectedDay ->
                AttendanceDetailsVerificationDialog(record = selectedDay, onDismiss = { viewModel.selectDayRecord(null) })
            }

            if (state.isLoading) { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }

            AnimatedVisibility(visible = state.dynamicExecutionErrorMessage != null, modifier = Modifier.align(Alignment.BottomCenter)) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(state.dynamicExecutionErrorMessage.orEmpty(), modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}