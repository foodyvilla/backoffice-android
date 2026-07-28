package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminAttendanceStatus
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AttendanceAdminUiModel
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme

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
                        Text(text = "GPS: [${record.inLat}, ${record.inLng}]", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "PUNCH OUT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(text = record.outTime.ifBlank { "Active Shift..." }, style = MaterialTheme.typography.bodySmall)
                    if (record.outLat.isNotBlank()) {
                        Text(text = "GPS: [${record.outLat}, ${record.outLng}]", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
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
    val extendedColors = AppTheme.colors
    val (contentColor, containerColor, icon) = when (status) {
        AdminAttendanceStatus.PRESENT -> Triple(extendedColors.success, extendedColors.successContainer, Icons.Default.CheckCircle)
        AdminAttendanceStatus.LATE -> Triple(extendedColors.warning, extendedColors.warningContainer, Icons.Default.Schedule)
        AdminAttendanceStatus.HALF_DAY -> Triple(extendedColors.warning, extendedColors.warningContainer, Icons.Default.Timer)
        AdminAttendanceStatus.ABSENT -> Triple(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer, Icons.Default.Block)
        AdminAttendanceStatus.PENDING -> Triple(extendedColors.info, extendedColors.infoContainer, Icons.Default.Pending)
    }
    
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Text(
                text = status.name,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
