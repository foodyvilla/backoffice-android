package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminTable
import kotlinx.serialization.json.JsonObject

@Composable
internal fun MediaRecordCard(table: AdminTable, row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(table), row["title"].toDisplayText(table.title), 86)
            Column(Modifier.weight(1f)) {
                Text(row["title"].toDisplayText(table.title), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(row["description"].toDisplayText(row["created_at"].toDisplayText()), color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
