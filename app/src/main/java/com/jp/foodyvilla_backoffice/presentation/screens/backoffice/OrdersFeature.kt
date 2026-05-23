package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
internal fun OrderRecordCard(
    order: Order,
    onStatusChange: ((String) -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PremiumCard(onClick = onClick) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(order.customerName ?: "No customer name", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Order #${order.id?.take(8) ?: "N/A"}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                    StatusPill(order.status.normalizeOrderStatus(), statusColor(order.status))
                }
                Text(order.createdAt?.formatTimestamp() ?: "-", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        if (onStatusChange != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OrderStatusDropdown(
                    current = order.status.normalizeOrderStatus(),
                    onStatusChange = onStatusChange,
                    modifier = Modifier.weight(1f)
                )
                CallCustomerButton(order.phone ?: "-", Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun OrderItemRecordCard(item: OrderItem, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(item.image?.firstOrNull(), item.productName ?: "Order item", 76)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.productName ?: "Untitled Product", fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.orderLabel ?: "Order #${item.orderId?.take(8)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill("Qty ${item.qty}", MaterialTheme.colorScheme.primary)
                    StatusPill("Rs ${item.totalPrice}", MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.weight(1f))
                    Text(item.createdAt?.formatTimestamp() ?: "-", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
internal fun OrderDetailsSection(
    orderItems: List<OrderItem>,
    productsById: Map<String, JsonObject>
) {
    val totalAmount = orderItems.sumOf { it.totalPrice }

    PremiumCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Order Breakdown",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (orderItems.isEmpty()) {
                Text(
                    text = "No items found for this order.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                orderItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RecordImage(
                            url = item.image?.firstOrNull(),
                            label = item.productName ?: "Product",
                            size = 56
                        )
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName ?: "Unknown Product",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.qty} x Rs ${item.pricePerItem}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                        
                        Text(
                            text = "Rs ${item.totalPrice}",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Amount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rs $totalAmount",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderStatusDropdown(current: String, onStatusChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("placed", "accepted", "preparing", "ready", "picked", "delivered", "rejected", "cancelled")
    Surface(
        modifier = modifier.height(50.dp).clickable { expanded = true },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(current, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Change status",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                statuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = {
                            expanded = false
                            if (status != current) onStatusChange(status)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallCustomerButton(phone: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            val cleanPhone = phone.takeIf { it != "-" }.orEmpty()
            if (cleanPhone.isNotBlank()) {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")))
            }
        },
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Phone, contentDescription = null)
        Text("Call", modifier = Modifier.padding(start = 8.dp))
    }
}
