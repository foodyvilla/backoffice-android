package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
internal fun AttendanceCalendarScreen(
    state: AdminUiState,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDateRecord by remember { mutableStateOf<JsonObject?>(null) }
    val scrollState = rememberScrollState()

    val recordsForMonth = remember(state.rows, currentMonth) {
        state.rows.filter {
            runCatching {
                val date = LocalDate.parse(it["created_at"].toDisplayText().substringBefore("T"))
                YearMonth.from(date) == currentMonth
            }.getOrDefault(false)
        }
    }

    val stats = remember(recordsForMonth) {
        AttendanceStats(
            present = recordsForMonth.count { it["status"].toDisplayText() == "present" || it["status"].toDisplayText() == "completed" },
            absent = 0, // Placeholder
            late = 0,    // Placeholder
            halfDay = 0, // Placeholder
            leave = 0,   // Placeholder
            holiday = 0  // Placeholder
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Punch Action
        PunchCard(state, onPunchIn, onPunchOut)

        // Summary Card
        AttendanceSummaryCard(stats)

        // Calendar Card
        PremiumCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CalendarHeader(
                    currentMonth = currentMonth,
                    onMonthChange = { currentMonth = it }
                )

                AttendanceGrid(
                    month = currentMonth,
                    records = recordsForMonth,
                    onDateClick = { selectedDateRecord = it }
                )
                
                AttendanceLegend()
            }
        }

        // Financials Card
        MonthlyFinancialsCard()

        Spacer(Modifier.height(80.dp))
    }

    selectedDateRecord?.let { record ->
        AttendanceDetailDialog(
            record = record,
            onDismiss = { selectedDateRecord = null }
        )
    }
}

data class AttendanceStats(
    val present: Int,
    val absent: Int,
    val late: Int,
    val halfDay: Int,
    val leave: Int,
    val holiday: Int
)

@Composable
private fun AttendanceSummaryCard(stats: AttendanceStats) {
    PremiumCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryItem("${stats.present}", "Present", MaterialTheme.colorScheme.primary)
            SummaryItem("${stats.absent}", "Absent", MaterialTheme.colorScheme.error)
            SummaryItem("${stats.late}", "Late", MaterialTheme.colorScheme.tertiary)
            SummaryItem("${stats.halfDay}", "Half", MaterialTheme.colorScheme.secondary)
            SummaryItem("${stats.leave}", "Leave", MaterialTheme.colorScheme.primary)
            SummaryItem("${stats.holiday}", "Holiday", MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun SummaryItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PunchCard(state: AdminUiState, onPunchIn: () -> Unit, onPunchOut: () -> Unit) {
    val today = LocalDate.now()
    val todayRecord = state.rows.firstOrNull { 
        it["created_at"].toDisplayText().startsWith(today.toString())
    }
    
    val isPunchedIn = todayRecord != null && todayRecord["out_time"].toDisplayText() == "-"
    val isCompleted = todayRecord != null && todayRecord["out_time"].toDisplayText() != "-"

    PremiumCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Fingerprint, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("Shift Control", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPunchIn,
                    enabled = todayRecord == null && !state.isSaving,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Punch In")
                }
                
                Button(
                    onClick = onPunchOut,
                    enabled = isPunchedIn && !state.isSaving,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Punch Out")
                }
            }
            
            if (isCompleted) {
                Text("Today's shift completed successfully.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun CalendarHeader(currentMonth: YearMonth, onMonthChange: (YearMonth) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AttendanceGrid(month: YearMonth, records: List<JsonObject>, onDateClick: (JsonObject?) -> Unit) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7 
    
    val days = (1..daysInMonth).toList()
    val placeholders = (0 until firstDayOfWeek).map { -1 }
    val allCells = placeholders + days

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }
        
        val gridHeight = if (allCells.size > 35) 340.dp else 280.dp
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(gridHeight),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allCells) { day ->
                if (day == -1) {
                    Spacer(Modifier.aspectRatio(1f))
                } else {
                    val date = month.atDay(day)
                    val record = records.firstOrNull { it["created_at"].toDisplayText().startsWith(date.toString()) }
                    
                    val color = when {
                        record == null -> Color.Transparent
                        record["out_time"].toDisplayText() == "-" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    }
                    val textColor = when {
                        record == null -> if (date == LocalDate.now()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        record["out_time"].toDisplayText() == "-" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                    
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color)
                            .clickable { onDateClick(record) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.toString(),
                                fontWeight = if (record != null || date == LocalDate.now()) FontWeight.ExtraBold else FontWeight.Medium,
                                color = textColor,
                                fontSize = 15.sp
                            )
                            if (record != null) {
                                Box(Modifier.size(4.dp).clip(CircleShape).background(textColor))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LegendItem("Present", MaterialTheme.colorScheme.primary)
        LegendItem("Absent", MaterialTheme.colorScheme.error)
        LegendItem("Late", MaterialTheme.colorScheme.tertiary)
        LegendItem("Half day", MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MonthlyFinancialsCard() {
    PremiumCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Monthly Financials", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = {}) {
                    Text("Show More", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FinancialItem(Modifier.weight(1f), "Total Earning", "₹ 0", Icons.Default.Payments, MaterialTheme.colorScheme.primary)
                FinancialItem(Modifier.weight(1f), "Fixed Salary", "₹ 0", Icons.Default.CurrencyRupee, MaterialTheme.colorScheme.primary)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FinancialMiniItem(Modifier.weight(1f), "Incentive", "₹ 0", Icons.Default.TrendingUp, MaterialTheme.colorScheme.primary)
                FinancialMiniItem(Modifier.weight(1f), "Bonus", "₹ 0", Icons.Default.CardGiftcard, MaterialTheme.colorScheme.tertiary)
                FinancialMiniItem(Modifier.weight(1f), "Penalty", "₹ 0", Icons.Default.ReportProblem, MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun FinancialItem(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F4FF)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(Modifier.size(36.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Column {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun FinancialMiniItem(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(Modifier.size(32.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun AttendanceDetailDialog(record: JsonObject, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.WbSunny, null, tint = MaterialTheme.colorScheme.tertiary)
                }
                Spacer(Modifier.height(8.dp))
                Text(record["created_at"].toDisplayText().substringBefore("T"), fontWeight = FontWeight.ExtraBold)
                Text(LocalDate.parse(record["created_at"].toDisplayText().substringBefore("T")).month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + LocalDate.parse(record["created_at"].toDisplayText().substringBefore("T")).year, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusPill(
                    label = record["status"].toDisplayText().uppercase(),
                    color = if (record["out_time"].toDisplayText() == "-") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                
                DetailBox("Punch In", record["in_time"].toDisplayText().substringAfter("T").take(5), Icons.Default.Login, MaterialTheme.colorScheme.primary)
                DetailBox("Punch Out", record["out_time"].toDisplayText().let { if (it == "-") "Active" else it.substringAfter("T").take(5) }, Icons.Default.Logout, MaterialTheme.colorScheme.tertiary)
                
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniInfoBox(Modifier.weight(1f), "Total Work", "0h 00m", Icons.Default.Schedule, MaterialTheme.colorScheme.tertiary)
                    MiniInfoBox(Modifier.weight(1f), "Total Break", "0h 00m", Icons.Default.Coffee, MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Behaviour")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("Dismiss")
                    }
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                        Text("See More")
                    }
                }
            }
        }
    )
}

@Composable
private fun DetailBox(label: String, value: String, icon: ImageVector, color: Color) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFFF5F7FF)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(Modifier.size(32.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun MiniInfoBox(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Surface(modifier, shape = RoundedCornerShape(12.dp), color = Color(0xFFF5F7FF)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(Modifier.size(28.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                }
            }
            Column {
                Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
