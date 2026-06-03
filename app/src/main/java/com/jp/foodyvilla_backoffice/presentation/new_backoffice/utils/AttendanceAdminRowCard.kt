package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminAttendanceStatus
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AttendanceAdminUiModel

@Composable
fun AttendanceAdminRowCard(
    record: AttendanceAdminUiModel,
    onModifyClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = record.employeeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Employee ID Ref: ${record.empId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                AdminStatusBadge(status = record.status)
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "PUNCH IN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = record.inTime.ifBlank { "N/A" }, style = MaterialTheme.typography.bodySmall)
                    if (record.inLat.isNotBlank()) {
                        Text(text = "GPS: [${record.inLat}, ${record.inLng}]", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "PUNCH OUT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(text = record.outTime.ifBlank { "Active Shift..." }, style = MaterialTheme.typography.bodySmall)
                    if (record.outLat.isNotBlank()) {
                        Text(text = "GPS: [${record.outLat}, ${record.outLng}]", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onModifyClick) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Log Entry", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun AdminStatusBadge(status: AdminAttendanceStatus) {
    val colorPair = when (status) {
        AdminAttendanceStatus.PRESENT -> Pair(Color(0xFF4CAF50), Color(0xFFE8F5E9))
        AdminAttendanceStatus.LATE -> Pair(Color(0xFFFF9800), Color(0xFFFFF3E0))
        AdminAttendanceStatus.HALF_DAY -> Pair(Color(0xFF03A9F4), Color(0xE1E1F5FE))
        AdminAttendanceStatus.ABSENT -> Pair(Color(0xFFE53935), Color(0xFFFFEBEE))
        AdminAttendanceStatus.PENDING -> Pair(Color.Gray, Color.LightGray.copy(alpha = 0.2f))
    }
    
    Surface(
        color = colorPair.second,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status.name,
            color = colorPair.first,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
