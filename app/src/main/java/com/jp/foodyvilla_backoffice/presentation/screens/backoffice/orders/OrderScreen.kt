package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders

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
import com.jp.foodyvilla_backoffice.data.model.backoffice.Order
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OrderScreen(
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onOrderDateChange: (String?) -> Unit,
    onOrderStatusFilterChange: (String?) -> Unit,
    onCreate: () -> Unit,
    onOrderStatusChange: (JsonObject, String) -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val orders = remember(state.rows, state.searchQuery, state.orderDateFilter, state.orderStatusFilter) {
        var filtered = state.rows.map { it to it.toModel<Order>() }
        
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter { (row, model) ->
                model.customerName?.contains(state.searchQuery, ignoreCase = true) == true ||
                        model.phone?.contains(state.searchQuery) == true ||
                        model.id?.contains(state.searchQuery, ignoreCase = true) == true
            }
        }

        state.orderDateFilter?.let { date ->
            filtered = filtered.filter { (_, model) ->
                model.createdAt?.startsWith(date) == true
            }
        }

        state.orderStatusFilter?.let { status ->
            filtered = filtered.filter { (_, model) ->
                model.status.normalizeOrderStatus().lowercase() == status.lowercase()
            }
        }

        filtered
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SearchAndFilterBar(
                query = state.searchQuery,
                onQueryChange = onSearch,
                placeholder = "Search orders"
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePickerField(
                    label = "Date",
                    value = state.orderDateFilter,
                    onValueChange = onOrderDateChange,
                    modifier = Modifier.weight(1f)
                )
                OrderStatusFilterDropdown(
                    current = state.orderStatusFilter,
                    onSelect = onOrderStatusFilterChange,
                    modifier = Modifier.weight(1f)
                )
            }
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
                    Icon(AdminRoute.Orders.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${orders.size} Orders",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Kitchen and delivery queue",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onCreate,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("New")
                    }
                }
            }
        }

        if (state.isLoading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary) }
        } else if (orders.isEmpty()) {
            item {
                EmptyState(
                    title = "No orders found",
                    message = "No orders matched your filters.",
                    actionLabel = "Create Order",
                    onAction = onCreate
                )
            }
        } else {
            items(orders) { (row, order) ->
                OrderListItem(
                    order = order,
                    onStatusChange = { onOrderStatusChange(row, it) },
                    onClick = { onOpenDetails(row) }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
