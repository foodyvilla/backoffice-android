package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AttendanceUiModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AttendanceStatus

@Composable
fun AttendanceMetricSummaryCard(salary: Long, history: Collection<AttendanceUiModel>) {
    val validHistory = history.filter { it.id != -1L || it.status == AttendanceStatus.ABSENT }
    val countPresent = validHistory.count { it.status == AttendanceStatus.PRESENT }
    val countLate = validHistory.count { it.status == AttendanceStatus.LATE }
    val countHalfDay = validHistory.count { it.status == AttendanceStatus.HALF_DAY }
    val countAbsent = validHistory.count { it.status == AttendanceStatus.ABSENT }

    val aggregatePresent = countPresent + countLate + countHalfDay

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Contract Payroll & Metric Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Base Pay Salary Structure:", fontWeight = FontWeight.Medium)
                Text(
                    "₹$salary / Month",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricIndicatorChip(
                    "Present: $aggregatePresent",
                    Color(0xFF4CAF50),
                    Modifier.weight(1f)
                )
                MetricIndicatorChip("Late: $countLate", Color(0xFFFF9800), Modifier.weight(1f))
                MetricIndicatorChip(
                    "HalfDay: $countHalfDay",
                    Color(0xFF03A9F4),
                    Modifier.weight(1f)
                )
                MetricIndicatorChip("Absent: $countAbsent", Color(0xFFE53935), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricIndicatorChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CustomCalendarMatrixView(
    currentMonth: YearMonth,
    attendanceMap: Map<LocalDate, AttendanceUiModel>,
    onDaySelected: (AttendanceUiModel?) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit,
    onMonthYearJump: (month: Int, year: Int) -> Unit
) {
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }
    val currentYear = LocalDate.now().year
    val validYearsRange = remember { (currentYear - 2)..currentYear }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ChevronLeft, "Prev Month") }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { monthMenuExpanded = true }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = monthMenuExpanded,
                        onDismissRequest = { monthMenuExpanded = false }) {
                        Month.values().forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        m.getDisplayName(
                                            TextStyle.FULL,
                                            Locale.getDefault()
                                        )
                                    )
                                },
                                onClick = {
                                    onMonthYearJump(
                                        m.value,
                                        currentMonth.year
                                    ); monthMenuExpanded = false
                                })
                        }
                    }
                }
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { yearMenuExpanded = true }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            currentMonth.year.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = yearMenuExpanded,
                        onDismissRequest = { yearMenuExpanded = false }) {
                        validYearsRange.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString()) },
                                onClick = {
                                    onMonthYearJump(
                                        currentMonth.monthValue,
                                        y
                                    ); yearMenuExpanded = false
                                })
                        }
                    }
                }
            }

            IconButton(
                onClick = onNavigateForward,
                enabled = currentMonth.isBefore(YearMonth.now())
            ) { Icon(Icons.Filled.ChevronRight, "Next Month") }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        val firstDayOffset = currentMonth.atDay(1).dayOfWeek.value - 1
        val daysInMonth = currentMonth.lengthOfMonth()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            userScrollEnabled = false,
//            modifier = Modifier.height(240.dp)
        ) {
            items(firstDayOffset) { Spacer(modifier = Modifier.fillMaxSize()) }
            items(daysInMonth) { index ->
                val day = index + 1
                val targetDate = currentMonth.atDay(day)
                val dayRecord = attendanceMap[targetDate]

                val backgroundColor = when (dayRecord?.status) {
                    AttendanceStatus.PRESENT -> Color(0xFF4CAF50)
                    AttendanceStatus.LATE -> Color(0xFFFF9800)
                    AttendanceStatus.HALF_DAY -> Color(0xFF03A9F4)
                    AttendanceStatus.ABSENT -> Color(0xFFE53935)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val contentColor =
                    if (dayRecord?.status != null && dayRecord.status != AttendanceStatus.PENDING) Color.White else MaterialTheme.colorScheme.onSurface

                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(
                            if (targetDate.isAfter(LocalDate.now())) MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            ) else backgroundColor
                        )
                        .clickable(enabled = targetDate.isBefore(LocalDate.now().plusDays(1))) {
                            onDaySelected(
                                dayRecord ?: AttendanceUiModel(
                                    -1,
                                    targetDate,
                                    -1,
                                    AttendanceStatus.PENDING,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) { Text("$day", fontWeight = FontWeight.Bold, color = contentColor) }
            }
        }
    }
}

@Composable
fun SwipeToConfirmButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    isRightToLeft: Boolean = false,
    enabled: Boolean = true,
    onConfirm: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val buttonWidth = maxWidth
        val handleSize = 56.dp
        val maxOffsetPx = with(density) { (buttonWidth - handleSize).toPx() }
        val dragAmountPx = remember { Animatable(0f) }
        val progress =
            if (maxOffsetPx > 0f) (dragAmountPx.value / maxOffsetPx).coerceIn(0f, 1f) else 0f
        val dynamicPaddingDp: Dp = with(density) { dragAmountPx.value.toDp() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(handleSize)
                .clip(RoundedCornerShape(handleSize / 2))
                .background(
                    if (enabled) containerColor.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonWidth * progress)
                    .align(if (isRightToLeft) Alignment.CenterEnd else Alignment.CenterStart)
                    .background(containerColor.copy(alpha = 0.3f))
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isRightToLeft) Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    null,
                    tint = containerColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(20.dp)
                        .alpha((1f - progress))
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (enabled) containerColor else Color.Gray,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .alpha((1f - progress * 1.5f).coerceIn(0f, 1f))
                )
                if (!isRightToLeft) Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = containerColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(20.dp)
                        .alpha((1f - progress))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (isRightToLeft) 0.dp else dynamicPaddingDp,
                        end = if (isRightToLeft) dynamicPaddingDp else 0.dp
                    ),
                contentAlignment = if (isRightToLeft) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(handleSize)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (enabled) containerColor else Color.Gray)
                        .draggable(
                            state = rememberDraggableState { delta ->
                                if (enabled) {
                                    scope.launch {
                                        val adjusted =
                                            if (isRightToLeft) -delta else delta; dragAmountPx.snapTo(
                                        (dragAmountPx.value + adjusted).coerceIn(
                                            0f,
                                            maxOffsetPx
                                        )
                                    )
                                    }
                                }
                            },
                            orientation = Orientation.Horizontal,
                            onDragStopped = {
                                if (enabled) {
                                    if (progress > 0.85f) onConfirm(); scope.launch {
                                        dragAmountPx.animateTo(
                                            0f
                                        )
                                    }
                                }
                            }
                        ), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceDetailsVerificationDialog(record: AttendanceUiModel, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Verification Record Node Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Divider()
                Text(
                    "Target Identity Date: ${record.date}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Calculated Status: ${record.status.name}",
                    fontWeight = FontWeight.Bold,
                    color = when (record.status) {
                        AttendanceStatus.PRESENT -> Color(0xFF4CAF50)
                        AttendanceStatus.LATE -> Color(0xFFFF9800)
                        AttendanceStatus.HALF_DAY -> Color(0xFF03A9F4)
                        else -> Color(0xFFE53935)
                    }
                )
                Text(
                    "Clock In Time: ${record.inTime?.toLocalTime() ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Clock Out Time: ${record.outTime?.toLocalTime() ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "In Geo-Bounds: [${record.inLat ?: "N/A"}, ${record.inLng ?: "N/A"}]",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "Out Geo-Bounds: [${record.outLat ?: "N/A"}, ${record.outLng ?: "N/A"}]",
                    style = MaterialTheme.typography.labelSmall
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Acknowledge") }
            }
        }
    }
}