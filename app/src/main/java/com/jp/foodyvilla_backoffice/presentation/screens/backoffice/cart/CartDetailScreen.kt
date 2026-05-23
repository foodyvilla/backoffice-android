package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Cart
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun CartDetailScreen(
    row: JsonObject?
) {
    if (row == null) {
        EmptyState("No cart row selected", "Go back and select a cart row.")
        return
    }

    val cart = remember(row) { row.toModel<Cart>() }

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
                    LargeRecordImage(cart.productImage?.firstOrNull(), cart.productName)
                    
                    Text(
                        text = "Cart Entry",
                        style = MaterialTheme.typography.titleLarge
                    )

                    DetailLine("Customer", cart.customerName ?: "-")
                    DetailLine("Phone", cart.customerPhone ?: "-")
                    DetailLine("Product", cart.productName ?: "-")
                    DetailLine("Quantity", cart.qty.toString())
                    DetailLine("Unit Price", "Rs ${cart.productPrice ?: 0.0}")
                    DetailLine("Outlet", cart.outletName ?: "-")
                    DetailLine("Created At", cart.createdAt ?: "-")
                }
            }
        }
    }
}
