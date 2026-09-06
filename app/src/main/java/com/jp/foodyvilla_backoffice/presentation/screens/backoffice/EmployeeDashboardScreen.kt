package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AttendanceStatus
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AttendanceUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeResponse
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.AttendanceDetailsVerificationDialog
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.LocationStateOverlayHandler
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.SwipeToConfirmButton
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceUiState
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceViewModel
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme
import kotlinx.coroutines.Job
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(viewModel: AttendanceViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSelfEmployeeContext()
    }

    val today = LocalDate.now()
    val isCurrentMonth = state.currentYearMonth.year == today.year && state.currentYearMonth.month == today.month
    val todayRecord = state.attendanceMap[today]

    val hasRealPunchIn = todayRecord != null && todayRecord.id != -1L && todayRecord.inTime != null
    val hasRealPunchOut = todayRecord != null && todayRecord.id != -1L && todayRecord.outTime != null

    val historyList = state.attendanceMap.values
        .filter { !it.date.isAfter(today) }
        .sortedByDescending { it.date }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Menu, "Menu") } },
                actions = {
                    IconButton(onClick = {


                    }) {
                        Icon(Icons.Default.Notifications, "Notifications")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount -> if (dragAmount > 40) onNavigateBack() }
        }) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).alpha(if (state.isLoading) 0.5f else 1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item { ProfileHeader(state.activeEmployee) }

                item { SummaryGrid(state) }

                if (isCurrentMonth) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Shift Action", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

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
                                    Text("● Shift completed successfully for today.", color = AppTheme.colors.success, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Attendance History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "See More",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { /* TODO */ }
                        )
                    }
                }

                items(historyList) { record ->
                    AttendanceHistoryRow(record = record, onClick = { viewModel.selectDayRecord(record) })
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }

            LocationStateOverlayHandler(state = state, viewModel = viewModel)

            state.selectedDayRecord?.let { selectedDay ->
                AttendanceDetailsVerificationDialog(record = selectedDay, onDismiss = { viewModel.selectDayRecord(null) })
            }

            if (state.isLoading) { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }

            AnimatedVisibility(visible = state.dynamicExecutionErrorMessage != null, modifier = Modifier.align(Alignment.BottomCenter)) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.padding(16.dp)) {
                    Text(state.dynamicExecutionErrorMessage.orEmpty(), modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(employee: EmployeeResponse?) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
    val greeting = when (LocalTime.now().hour) {
        in 0..11 -> "Good Morning,"
        in 12..16 -> "Good Afternoon,"
        else -> "Good Evening,"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = greeting, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = employee?.name ?: "User", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = today.format(formatter), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Office Location", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
fun SummaryGrid(state: AttendanceUiState) {
    val today = LocalDate.now()
    val todayRecord = state.attendanceMap[today]
    val absenceCount = state.attendanceMap.values.count { it.status == AttendanceStatus.ABSENT }
    val attendedCount = state.attendanceMap.values.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE || it.status == AttendanceStatus.HALF_DAY }

    val checkInTime = todayRecord?.inTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--"
    val checkOutTime = todayRecord?.outTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--"

    val successColor = AppTheme.colors.success
    val errorColor = MaterialTheme.colorScheme.error

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AttendanceSummaryCard(
                label = "Check In",
                value = checkInTime,
                subValue = if (todayRecord?.inTime != null) "Today" else "Not Present",
                icon = Icons.Default.ArrowDownward,
                iconColor = successColor,
                modifier = Modifier.weight(1f),
                valueColor = if (todayRecord?.inTime == null) errorColor else MaterialTheme.colorScheme.onSurface
            )
            AttendanceSummaryCard(
                label = "Check Out",
                value = checkOutTime,
                subValue = if (todayRecord?.outTime != null) "Today" else "Not Present",
                icon = Icons.Default.ArrowUpward,
                iconColor = errorColor,
                modifier = Modifier.weight(1f),
                valueColor = if (todayRecord?.outTime == null) errorColor else MaterialTheme.colorScheme.onSurface
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AttendanceSummaryCard(
                label = "Absence",
                value = "$absenceCount Day",
                subValue = today.month.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                icon = Icons.Default.CalendarToday,
                iconColor = errorColor,
                modifier = Modifier.weight(1f)
            )
            AttendanceSummaryCard(
                label = "Total Attended",
                value = "$attendedCount Day",
                subValue = today.month.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                icon = Icons.Default.Sync,
                iconColor = successColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AttendanceSummaryCard(
    label: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(4.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subValue, style = MaterialTheme.typography.labelSmall, color = if (subValue == "Not Present") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun AttendanceHistoryRow(record: AttendanceUiModel, onClick: () -> Unit) {
    val dayFormatter = DateTimeFormatter.ofPattern("dd", Locale.getDefault())
    val monthFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = record.date.format(dayFormatter), color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = record.date.format(monthFormatter), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text = record.inTime?.format(timeFormatter) ?: "--:--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (record.inTime == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = "Check In", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column {
                        Text(
                            text = record.outTime?.format(timeFormatter) ?: "--:--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (record.outTime == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = "Check out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
