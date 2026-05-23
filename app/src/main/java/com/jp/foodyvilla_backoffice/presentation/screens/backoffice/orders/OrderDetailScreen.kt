package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders

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
import com.jp.foodyvilla_backoffice.data.model.backoffice.Order
import com.jp.foodyvilla_backoffice.data.model.backoffice.OrderItem
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OrderDetailScreen(
    row: JsonObject?,
    orderItems: List<OrderItem>,
    productsById: Map<String, JsonObject>,
    onEdit: () -> Unit
) {
    if (row == null) {
        EmptyState("No order selected", "Go back and select an order.")
        return
    }

    val order = remember(row) { row.toModel<Order>() }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order Details",
                            style = MaterialTheme.typography.titleLarge
                        )
                        StatusPill(
                            label = order.status.normalizeOrderStatus(),
                            color = statusColor(order.status)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onEdit,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Text("Edit Order", modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    DetailLine("Customer", order.customerName ?: "-")
                    DetailLine("Phone", order.phone ?: "-")
                    DetailLine("Type", order.orderType ?: "Delivery")
                    DetailLine("Address", order.address ?: "-")
                    DetailLine("Instructions", order.instruction ?: "-")
                    DetailLine("Transaction ID", order.transactionId ?: "-")
                    DetailLine("Created At", order.createdAt?.formatTimestamp() ?: "-")
                }
            }
        }

        item {
            OrderDetailsSection(orderItems, productsById)
        }
    }
}
