package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.order_items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OrderItemScreen(
    session: UserSession?,
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val items = remember(state.orderItems, state.searchQuery) {
        state.orderItems.filter {
            it.productName?.contains(state.searchQuery, ignoreCase = true) == true ||
                    it.orderId?.contains(state.searchQuery, ignoreCase = true) == true ||
                    state.searchQuery.isBlank()
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SearchAndFilterBar(
                query = state.searchQuery,
                onQueryChange = onSearch,
                placeholder = "Search order items"
            )
        }

        item {
            PremiumCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(AdminRoute.OrderItems.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${items.size} Items",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Line items and pricing",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary) }
        } else if (items.isEmpty()) {
            item {
                EmptyState(
                    title = "No items found",
                    message = "Order line items appear here."
                )
            }
        } else {
            items(items, key = { it.id ?: it.hashCode() }) { item ->
                OrderItemListItem(
                    item = item,
                    onClick = { 
                        // Find the original JsonObject for details
                        state.rows.firstOrNull { it["id"].toDisplayText() == item.id.toString() }?.let {
                            onOpenDetails(it)
                        }
                    }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
