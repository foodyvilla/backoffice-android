package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OrderDetailScreen(
    session: UserSession?,
    row: JsonObject?,
    orderItems: List<OrderItem>,
    productsById: Map<String, ProductCatalog>,
    onEdit: () -> Unit
) {
    if (row == null) {
        EmptyState("No order selected", "Go back and select an order.")
        return
    }

    val order = remember(row) { runCatching { row.toModel<Order>() }.getOrNull() }

    if (order == null) {
        EmptyState("Invalid Data", "Could not parse order details.")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Structured Header Card Info block
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Invoice Tracking",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Order #${order.id?.takeLast(12)?.uppercase() ?: "UNKNOWN"}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                    }

                    StatusPill(
                        label = order.status.normalizeOrderStatus(),
                        color = statusColor(order.status)
                    )
                }

                if (session?.canEdit("orders") == true) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Modify Specifications", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 2. Metadata Information Block
        item {
            PremiumCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Customer & Logistics Meta Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailLineRow("Customer Identity", order.customerName ?: "Not Specified")
                        DetailLineRow("Contact Channel", order.phone ?: "No phone connection")
                        DetailLineRow("Dispatch Type", order.orderType ?: "Standard Delivery")
                        DetailLineRow("Geographic Address", order.address ?: "In-store Pickup / No Address Provided")
                        DetailLineRow("Preparation Notes", order.instruction ?: "None provided by customer")
                        
                        val grandTotal = order.grandTotal
                        if (grandTotal != null) {
                            DetailLineRow("Financial Settlement", "Rs ${grandTotal / 100.0}")
                        } else {
                            DetailLineRow("Transaction Token", order.transactionId ?: "Unpaid / COD")
                        }
                        DetailLineRow("Log Timestamp", order.createdAt?.formatTimestamp() ?: "Processing")
                    }
                }
            }
        }

        // 3. Item breakdown module
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    OrderDetailsSection(orderItems, productsById)
                }
            }
        }
    }
}

@Composable
private fun DetailLineRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}