package com.jp.foodyvilla_backoffice.presentation.components

import androidx.compose.foundation.border
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
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletMenuItemUiModel

@Composable
fun OutletMenuItemRow(
    item: OutletMenuItemUiModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    canManage: Boolean = true
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val indicatorColor = if (item.isProductVeg) Color(0xFF4CAF50) else Color(0xFFE53935)
                    Box(modifier = Modifier.size(12.dp).border(1.5.dp, indicatorColor, RoundedCornerShape(2.dp)).padding(2.dp)) {
                        Surface(modifier = Modifier.fillMaxSize(), color = indicatorColor) {}
                    }
                    Text(item.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("Branch Price: ₹${item.price} (Discount: ₹${item.discount})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text("Catalog Category Context: ${item.productCategoryName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Text(text = if (item.isOutOfStock) "❌ OUT OF STOCK" else if (!item.isAvailable) "⚠️ HIDDEN" else "✅ ACTIVE LIVE ON TERMINALS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            if (canManage) {
                Row {
                    IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}
