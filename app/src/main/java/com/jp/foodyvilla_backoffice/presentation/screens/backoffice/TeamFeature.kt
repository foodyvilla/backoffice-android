package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
internal fun EmployeeRecordCard(row: JsonObject, onClick: () -> Unit) {
    val employee = remember(row) { row.toModel<Employee>() }
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(employee.profileImg, employee.name, 68)
            Column(Modifier.weight(1f)) {
                Text(employee.name, fontWeight = FontWeight.Bold)
                Text(employee.role ?: "No role", color = Muted)
                Text(employee.contact ?: "No contact", color = Muted, fontSize = 12.sp)
                Text("Joined: ${employee.joiningDate?.formatDate() ?: "-"}", color = Muted, fontSize = 11.sp)
            }
        }
    }
}
