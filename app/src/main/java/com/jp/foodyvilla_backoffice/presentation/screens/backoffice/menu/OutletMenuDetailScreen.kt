package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.model.backoffice.OutletMenuItem
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OutletMenuDetailScreen(
    row: JsonObject?,
    onEdit: () -> Unit
) {
    if (row == null) {
        EmptyState("No menu item selected", "Go back and select an item.")
        return
    }

    val item = remember(row) { row.toModel<OutletMenuItem>() }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PremiumCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LargeRecordImage(item.image?.firstOrNull(), item.productName)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onEdit,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Text("Edit Menu Item", modifier = Modifier.padding(start = 8.dp))
                        }
                        StatusPill(if (item.isAvailable) "Available" else "Hidden", if (item.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    DetailLine("Product", item.productName ?: "-")
                    DetailLine("Category", item.productCategory ?: "-")
                    DetailLine("Price", "Rs ${item.price}")
                    DetailLine("Discount", "Rs ${item.discount}")
                    DetailLine("Out of Stock", if (item.isOutOfStock) "Yes" else "No")
                    DetailLine("Outlet ID", item.outletId.toString())
                    DetailLine("Created At", item.createdAt ?: "-")
                }
            }
        }
        
        item {
            MenuDetailsSection(item)
        }
    }
}
