package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminAttendanceStatus
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AttendanceAdminUiModel
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AttendanceAdminRowCard(
    record: AttendanceAdminUiModel,
    onModifyClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Employee Name + ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.employeeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID #${record.empId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AdminStatusBadge(status = record.status)
            }

            // Punch In & Punch Out Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Punch In Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PUNCH IN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatPunchTime(record.inTime),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val inDate = formatPunchDate(record.inTime)
                    if (inDate.isNotBlank()) {
                        Text(
                            text = inDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Punch Out Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PUNCH OUT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (record.outTime.isBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Active",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Text(
                            text = formatPunchTime(record.outTime),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val outDate = formatPunchDate(record.outTime)
                        if (outDate.isNotBlank()) {
                            Text(
                                text = outDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Worked Duration (if available)
            val duration = calculateWorkedDuration(record.inTime, record.outTime)
            if (duration != null) {
                Text(
                    text = "Worked $duration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Footer Row: Location & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (record.inLat.isNotBlank() || record.outLat.isNotBlank()) "Location recorded" else "No location data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Log") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onModifyClick()
                            }
                        )
                        if (onDeleteClick != null) {
                            DropdownMenuItem(
                                text = { Text("Delete Log", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatusBadge(status: AdminAttendanceStatus) {
    val extendedColors = AppTheme.colors
    val (contentColor, containerColor, label, icon) = when (status) {
        AdminAttendanceStatus.PRESENT -> Quadruple(extendedColors.success, extendedColors.successContainer, "PRESENT", Icons.Default.CheckCircle)
        AdminAttendanceStatus.LATE -> Quadruple(extendedColors.warning, extendedColors.warningContainer, "LATE", Icons.Default.Schedule)
        AdminAttendanceStatus.HALF_DAY -> Quadruple(Color(0xFFE65100), Color(0xFFFFE0B2), "HALF DAY", Icons.Default.Timer)
        AdminAttendanceStatus.ABSENT -> Quadruple(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer, "ABSENT", Icons.Default.Block)
        AdminAttendanceStatus.PENDING -> Quadruple(extendedColors.info, extendedColors.infoContainer, "PENDING", Icons.Default.Pending)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(12.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

fun formatPunchTime(dateTimeStr: String): String {
    if (dateTimeStr.isBlank()) return "--:--"
    return try {
        val cleaned = dateTimeStr.replace("T", " ").trim()
        val timePart = cleaned.split(" ").getOrNull(1)?.split(".")?.get(0) ?: ""
        if (timePart.isBlank()) return "--:--"
        val components = timePart.split(":")
        val hour = components.getOrNull(0)?.toIntOrNull() ?: return dateTimeStr
        val minute = components.getOrNull(1)?.toIntOrNull() ?: 0
        val localTime = LocalTime.of(hour, minute)
        localTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
    } catch (e: Exception) {
        dateTimeStr
    }
}

fun formatPunchDate(dateTimeStr: String): String {
    if (dateTimeStr.isBlank()) return ""
    return try {
        val cleaned = dateTimeStr.replace("T", " ").trim()
        val datePart = cleaned.split(" ").getOrNull(0) ?: return ""
        val localDate = LocalDate.parse(datePart)
        localDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
    } catch (e: Exception) {
        ""
    }
}

fun calculateWorkedDuration(inTimeStr: String, outTimeStr: String): String? {
    if (inTimeStr.isBlank() || outTimeStr.isBlank()) return null
    return try {
        val inClean = inTimeStr.replace("T", " ").trim().split(".").get(0)
        val outClean = outTimeStr.replace("T", " ").trim().split(".").get(0)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val inDateTime = LocalDateTime.parse(inClean, formatter)
        val outDateTime = LocalDateTime.parse(outClean, formatter)
        val duration = java.time.Duration.between(inDateTime, outDateTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        if (hours < 0 || minutes < 0) return null
        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    } catch (e: Exception) {
        null
    }
}
