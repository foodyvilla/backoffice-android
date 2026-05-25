package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jp.foodyvilla_backoffice.data.model.backoffice.Order
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
fun OrderListItem(
    session: UserSession? = null,
    order: Order,
    onStatusChange: (String) -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val statuses = listOf("pending", "accepted", "preparing", "ready", "completed", "rejected", "cancelled")
    val canEdit = session?.canEdit("orders") == true

    PremiumCard(onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Identifiers & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = order.customerName ?: "Anonymous Customer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "ID: #${order.id?.takeLast(8)?.uppercase() ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    val grandTotal = order.grandTotal
                    if (grandTotal != null) {
                        Text(
                            text = "Total: Rs $grandTotal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusGreen,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Box {
                    StatusPill(
                        label = order.status.normalizeOrderStatus(),
                        color = statusColor(order.status),
                        modifier = if (canEdit) Modifier.clickable { showMenu = true } else Modifier
                    )

                    if (canEdit) {
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            statuses.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        showMenu = false
                                        onStatusChange(status)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)

            // Bottom Info Row: Meta Specifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Order Fulfillment Logistics Type Tag
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = (order.orderType ?: "Delivery").uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Text(
                    text = order.createdAt?.formatTimestamp() ?: "Just now",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}