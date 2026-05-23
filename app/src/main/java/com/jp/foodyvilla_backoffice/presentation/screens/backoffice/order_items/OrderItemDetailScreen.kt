package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.order_items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.model.backoffice.OrderItem
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OrderItemDetailScreen(
    row: JsonObject?
) {
    if (row == null) {
        EmptyState("No item selected", "Go back and select an item.")
        return
    }

    val item = remember(row) { row.toModel<OrderItem>() }

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
                    LargeRecordImage(item.outletMenuItem?.image?.firstOrNull(), item.productName)
                    
                    Text(
                        text = "Order Item Details",
                        style = MaterialTheme.typography.titleLarge
                    )

                    DetailLine("Product", item.productName ?: "-")
                    DetailLine("Quantity", item.qty.toString())
                    DetailLine("Price Per Item", "Rs ${item.pricePerItem}")
                    DetailLine("Total Price", "Rs ${item.totalPrice}")
                    DetailLine("Discount", "Rs ${item.totalDiscount ?: 0.0}")
                    DetailLine("Order ID", item.orderId ?: "-")
                    DetailLine("Menu Item ID", item.menuItemId.toString())
                    DetailLine("Created At", item.createdAt ?: "-")
                }
            }
        }
    }
}
