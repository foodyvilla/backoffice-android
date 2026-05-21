package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonObject

@Composable
internal fun EmployeeRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(), row["name"].toDisplayText("Employee"), 68)
            Column(Modifier.weight(1f)) {
                Text(row["name"].toDisplayText("No name"), fontWeight = FontWeight.Bold)
                Text(row["role"].toDisplayText("No role"), color = Muted)
                Text(row["contact"].toDisplayText("No contact"), color = Muted, fontSize = 12.sp)
                Text("Joined: ${row["joining_date"].toDisplayText().formatDate()}", color = Muted, fontSize = 11.sp)
            }
        }
    }
}
