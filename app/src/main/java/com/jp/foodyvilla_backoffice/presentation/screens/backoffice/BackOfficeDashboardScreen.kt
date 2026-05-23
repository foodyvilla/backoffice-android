package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import kotlinx.serialization.json.JsonObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Composable
internal fun DashboardScreen(
    state: AdminUiState,
    onOpenRoute: (AdminRoute) -> Unit
) {
    val orders = remember(state.dashboardRows) { 
        state.dashboardRows["orders"].orEmpty().map { it.toModel<Order>() } 
    }
    val orderItems = remember(state.dashboardRows) { 
        state.dashboardRows["order_items"].orEmpty().map { it.toModel<OrderItem>() } 
    }
    val products = state.dashboardRows["product_catalog"].orEmpty()
    val users = state.dashboardRows["users"].orEmpty()
    val productsById = remember(products) { products.associateBy { it["id"].toDisplayText() } }
    var selectedRange by remember { mutableStateOf("Today") }
    var selectedProductId by remember { mutableStateOf<String?>(null) }
    
    val filteredOrders = remember(orders, selectedRange) {
        orders.filter { it.matchesDateRange(selectedRange) }
    }
    val filteredOrderIds = remember(filteredOrders) { filteredOrders.map { it.id }.toSet() }
    val filteredItems = remember(orderItems, filteredOrderIds, selectedProductId) {
        orderItems.filter { item ->
            item.orderId in filteredOrderIds &&
                (selectedProductId == null || item.menuItemId.toString() == selectedProductId)
        }
    }
    
    val pending = filteredOrders.count { it.status.normalizeOrderStatus() in listOf("Pending", "Accepted", "Preparing", "Ready") }
    val cancelled = filteredOrders.count { it.status.normalizeOrderStatus() == "Rejected" }
    val delivered = filteredOrders.count { it.status.normalizeOrderStatus() == "Completed" }
    val revenue = filteredItems.sumOf { it.totalPrice }
    
    val topProductIds = filteredItems
        .groupBy { it.menuItemId.toString() }
        .entries
        .sortedByDescending { (_, rows) -> rows.sumOf { it.qty } }
        .map { it.key }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SalesFilters(
                selectedRange = selectedRange,
                onRangeChange = { selectedRange = it },
                products = products,
                selectedProductId = selectedProductId,
                onProductChange = { selectedProductId = it }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Total Orders", filteredOrders.size.toString(), Icons.AutoMirrored.Filled.ReceiptLong, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                MetricCard("Revenue", "Rs %.0f".format(revenue), Icons.Default.Payments, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Pending", pending.toString(), Icons.Default.PendingActions, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                MetricCard("Rejected", cancelled.toString(), Icons.Default.Cancel, MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
        }
        item {
            PremiumCard {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Orders status", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    StatusPill("Completed: $delivered", MaterialTheme.colorScheme.primary)
                    StatusPill("Active: $pending", MaterialTheme.colorScheme.secondary)
                    StatusPill("Rejected: $cancelled", MaterialTheme.colorScheme.error)
                }
            }
        }
        item {
            PremiumCard {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sales graph", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (filteredItems.isEmpty()) {
                        Text("No order item sales found for this filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        DashboardSparkline(values = filteredItems.map { it.totalPrice.toFloat() })
                    }
                }
            }
        }
        item {
            SectionTitle("Top selling products")
            if (topProductIds.isEmpty()) {
                EmptyState("No sales", "Sold products from order_items will appear here.")
            }
        }
        items(topProductIds.take(5)) { productId ->
            productsById[productId]?.let { row ->
                ProductRecordCard(product = row.toModel<ProductCatalog>(), onClick = { onOpenRoute(AdminRoute.Products) })
            }
        }
        item {
            SectionTitle("Recent orders")
            if (filteredOrders.isEmpty()) {
                EmptyState("No orders", "Orders from Supabase will appear here.")
            }
        }
        items(filteredOrders.take(5)) { order ->
            OrderRecordCard(order = order, onClick = { onOpenRoute(AdminRoute.Orders) })
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Products", products.size.toString(), AdminRoute.Products.icon, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                MetricCard("Customers", users.size.toString(), AdminRoute.Customers.icon, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun SalesFilters(
    selectedRange: String,
    onRangeChange: (String) -> Unit,
    products: List<JsonObject>,
    selectedProductId: String?,
    onProductChange: (String?) -> Unit
) {
    PremiumCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Sales report filters", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Today", "Week", "Month", "Year", "All")) { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onRangeChange(range) },
                        label = { Text(range) }
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedProductId == null,
                        onClick = { onProductChange(null) },
                        label = { Text("All products") }
                    )
                }
                items(products) { product ->
                    val id = product["id"].toDisplayText()
                    FilterChip(
                        selected = selectedProductId == id,
                        onClick = { onProductChange(id) },
                        label = { Text(product["name"].toDisplayText("Product")) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
}

@Composable
private fun DashboardSparkline(values: List<Float>) {
    val max = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    Canvas(Modifier.fillMaxWidth().height(130.dp)) {
        val normalized = values.takeLast(12).map { it / max }
        val step = size.width / (normalized.size - 1).coerceAtLeast(1)
        val path = Path()
        normalized.forEachIndexed { index, value ->
            val x = index * step
            val y = size.height - (value * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        repeat(4) {
            val y = size.height * (it + 1) / 5
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        drawPath(path, lineColor, style = Stroke(width = 4.dp.toPx()))
    }
}

private fun Order.matchesDateRange(range: String): Boolean {
    if (range == "All") return true
    val createdAtDate = this.createdAt?.toLocalDateOrNull() ?: return false
    val today = LocalDate.now()
    return when (range) {
        "Today" -> createdAtDate == today
        "Week" -> !createdAtDate.isBefore(today.minusDays(6))
        "Month" -> createdAtDate.year == today.year && createdAtDate.month == today.month
        "Year" -> createdAtDate.year == today.year
        else -> true
    }
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return runCatching { OffsetDateTime.parse(this).toLocalDate() }
        .getOrElse {
            runCatching { LocalDateTime.parse(this.substringBefore("+")).toLocalDate() }
                .getOrNull()
        }
}
