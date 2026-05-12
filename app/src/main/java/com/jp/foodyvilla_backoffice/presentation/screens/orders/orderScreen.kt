package com.jp.foodyvilla_backoffice.presentation.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.data.model.order.OrderItem
import com.jp.foodyvilla_backoffice.data.model.order.OrderModel
import com.jp.foodyvilla_backoffice.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla_backoffice.presentation.utils.UiState

// ─── DATA MODELS (Based on your logs) ────────────────────────────────────────


// ─── MAIN SCREEN ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    viewModel: HomeViewModel
) {


    val orderState  = viewModel.orderHistoryState.collectAsStateWithLifecycle().value


    LaunchedEffect(Unit) {
        if(orderState is UiState.Error){
            viewModel.getOrderedItems()
        }
    }
    when(orderState){
        is UiState.Error -> {}

        is UiState.Success -> {
            val orders  = orderState.data
            if (orders.isEmpty()) {
                EmptyOrdersView()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(order = order)
                    }

                    item{
                        Spacer(Modifier.height(180.dp))
                    }
                }
            }
        }

        else -> {

            Box(modifier =  Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center){
                CircularProgressIndicator()
            }
        }
    }


}

// ─── REUSABLE COMPONENTS ─────────────────────────────────────────────────────

@Composable
fun OrderCard(order: OrderModel) {
    val totalAmount = order.order_items.sumOf { it.total_price }
    val totalQty = order.order_items.sumOf { it.qty }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.created_at.substringBefore("T"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                StatusBadge(status = order.status)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            // Row 2: Order Items Summary
            order.order_items.forEach { item ->
                OrderItemRow(item)
                Spacer(Modifier.height(8.dp))

            }

            Spacer(Modifier.height(12.dp))

            // Row 3: Meta Info (Type & Total)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OrderTypeIcon(type = order.order_type?:"N/A")
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = order.order_type?:"N/A",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$totalQty Items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "₹${totalAmount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {

        AsyncImage(
            item.products?.image[0],
            contentDescription = "productImage", modifier = Modifier.size(60.dp).clip(CircleShape), contentScale = ContentScale.Crop
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            Text(
                text = "${item.products?.name} × ${item.qty}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "₹${item.total_price}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }

}

@Composable
private fun StatusBadge(status: String) {
    val containerColor = when (status.uppercase()) {
        "PLACED" -> Color(0xFFE3F2FD)
        "DELIVERED" -> Color(0xFFE8F5E9)
        "CANCELLED" -> Color(0xFFFFEBEE)
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (status.uppercase()) {
        "PLACED" -> Color(0xFF1976D2)
        "DELIVERED" -> Color(0xFF2E7D32)
        "CANCELLED" -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        color = containerColor,
        shape = CircleShape
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor
        )
    }
}

@Composable
private fun OrderTypeIcon(type: String) {
    val (icon, color) = when (type) {
        "Dine-In" -> Icons.Rounded.Restaurant to MaterialTheme.colorScheme.primary
        "Pickup" -> Icons.Rounded.LocalMall to MaterialTheme.colorScheme.secondary
        else -> Icons.Rounded.DeliveryDining to MaterialTheme.colorScheme.tertiary
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = color)
    }
}

@Composable
private fun EmptyOrdersView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No orders yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}