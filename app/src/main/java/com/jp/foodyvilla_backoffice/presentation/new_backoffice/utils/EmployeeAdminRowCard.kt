package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeAdminUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeRole

@Composable
fun EmployeeAdminRowCard(
    record: EmployeeAdminUiModel,
    onModifyClick: () -> Unit,
    onPurgeClick: () -> Unit
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
                    Text(text = record.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Branch Unit: ${record.outletName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                RoleIndicatorBadge(role = record.role)
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "CONTACT INFO", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = record.contact, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "MONTHLY BASE PAY", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = if(record.salary.isNotBlank()) "₹${record.salary}" else "Unset", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val statusText = if (record.isActive) "ACTIVE STAFF" else "INACTIVE / DEACTIVATED"
                    val statusColor = if (record.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                    Text(text = statusText, color = statusColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                Row {
                    IconButton(onClick = onModifyClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Staff Configuration", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onPurgeClick) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Purge Roster Row Log", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleIndicatorBadge(role: EmployeeRole) {
    val containerColor = when (role) {
        EmployeeRole.OWNER -> Color(0xFFE3F2FD)
        EmployeeRole.HEAD -> Color(0xFFFFE0B2)
        EmployeeRole.CHEF -> Color(0xFFE1BEE7)
        EmployeeRole.EMPLOYEE -> Color(0xFFF5F5F5)
    }
    val contentColor = when (role) {
        EmployeeRole.OWNER -> Color(0xFF0D47A1)
        EmployeeRole.HEAD -> Color(0xFFE65100)
        EmployeeRole.CHEF -> Color(0xFF4A148C)
        EmployeeRole.EMPLOYEE -> Color(0xFF212121)
    }

    Surface(color = containerColor, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = role.name, color = contentColor,
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}