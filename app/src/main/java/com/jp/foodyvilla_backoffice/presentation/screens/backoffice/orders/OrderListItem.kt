package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Order
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
fun OrderListItem(
    order: Order,
    onStatusChange: (String) -> Unit,
    onClick: () -> Unit
) {
    PremiumCard(onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = order.customerName ?: "Unknown Customer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Order #${order.id?.takeLast(8)}",
                        color = Muted,
                        fontSize = 13.sp
                    )
                }
                StatusPill(
                    label = order.status.normalizeOrderStatus(),
                    color = statusColor(order.status)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderType ?: "Delivery",
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = order.createdAt?.formatTimestamp() ?: "-",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
