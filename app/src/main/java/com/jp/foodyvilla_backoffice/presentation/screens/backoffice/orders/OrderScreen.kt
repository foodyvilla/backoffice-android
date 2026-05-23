package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Order
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OrderScreen(
    session: UserSession?,
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onOrderDateChange: (String?) -> Unit,
    onOrderStatusFilterChange: (String?) -> Unit,
    onCreate: () -> Unit,
    onOrderStatusChange: (JsonObject, String) -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val orders = remember(state.orders, state.searchQuery, state.orderDateFilter, state.orderStatusFilter) {
        var filtered = state.orders

        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter { model ->
                model.customerName?.contains(state.searchQuery, ignoreCase = true) == true ||
                        model.phone?.contains(state.searchQuery) == true ||
                        model.id?.contains(state.searchQuery, ignoreCase = true) == true
            }
        }

        state.orderDateFilter?.let { date ->
            filtered = filtered.filter { model ->
                model.createdAt?.startsWith(date) == true
            }
        }

        state.orderStatusFilter?.let { status ->
            filtered = filtered.filter { model ->
                model.status.normalizeOrderStatus().lowercase() == status.lowercase()
            }
        }

        filtered
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Dashboard Header Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Orders",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Kitchen & delivery stream queue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (session?.canCreate("orders") == true) {
                    Button(
                        onClick = onCreate,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text("New Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Persistent KPI Counter Metric Bar
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = AdminRoute.Orders.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Showing ${orders.size} matching records",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Compact Context Filtering Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SearchAndFilterBar(
                    query = state.searchQuery,
                    onQueryChange = onSearch,
                    placeholder = "Search by ID, name, or phone number..."
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small Date Filter with Icon
                    DatePickerField(
                        label = "Date",
                        value = state.orderDateFilter,
                        onValueChange = onOrderDateChange,
                        modifier = Modifier.weight(0.45f)
                    )

                    OrderStatusFilterDropdown(
                        current = state.orderStatusFilter,
                        onSelect = onOrderStatusFilterChange,
                        modifier = Modifier.weight(0.55f)
                    )
                }
            }
        }

        // 4. Conditional Content Stream
        if (state.isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (orders.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxHeight(0.6f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No active entries found",
                        message = "We couldn't find any orders matching your combination of filters.",
                        actionLabel = "Clear Filters or Create Order",
                        onAction = onCreate
                    )
                }
            }
        } else {
            items(orders, key = { order -> order.id ?: order.hashCode() }) { order ->
                OrderListItem(
                    session = session,
                    order = order,
                    onStatusChange = { status -> 
                        state.rows.firstOrNull { it["id"].toDisplayText() == order.id }?.let { row ->
                            onOrderStatusChange(row, status)
                        }
                    },
                    onClick = { 
                        state.rows.firstOrNull { it["id"].toDisplayText() == order.id }?.let { row ->
                            onOpenDetails(row)
                        }
                    }
                )
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}