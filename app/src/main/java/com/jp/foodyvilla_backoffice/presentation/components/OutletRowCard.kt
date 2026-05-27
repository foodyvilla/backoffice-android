package com.jp.foodyvilla_backoffice.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletUiModel

@Composable
fun OutletRowCard(
    outlet: OutletUiModel,
    onEditClick: () -> Unit,
    onRowNavigateClick: () -> Unit
) {
    ElevatedCard(
        onClick = onRowNavigateClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Storefront, null, modifier = Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(outlet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${outlet.city} • Range: ${outlet.radiusKm} km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (outlet.isActive) "● Branch Operational" else "○ Inactive", style = MaterialTheme.typography.labelSmall, color = if (outlet.isActive) Color(0xFF4CAF50) else Color(0xFFE53935))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.secondary) }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}