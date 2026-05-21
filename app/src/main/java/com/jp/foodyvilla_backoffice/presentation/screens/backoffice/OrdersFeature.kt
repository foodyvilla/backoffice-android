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
import kotlinx.serialization.json.JsonObject

@Composable
internal fun OrderRecordCard(
    row: JsonObject,
    onStatusChange: ((String) -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PremiumCard(onClick = onClick) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = RoyalBlue)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(row["customer_name"].toDisplayText("No customer name"), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Order #${row["id"].toDisplayText().take(8)}", color = Muted, fontSize = 11.sp)
                    }
                    StatusPill(row["status"].toDisplayText().normalizeOrderStatus(), statusColor(row["status"].toDisplayText()))
                }
                Text(row["created_at"].toDisplayText().formatTimestamp(), color = Muted, fontSize = 12.sp)
            }
        }
        if (onStatusChange != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OrderStatusDropdown(
                    current = row["status"].toDisplayText().normalizeOrderStatus(),
                    onStatusChange = onStatusChange,
                    modifier = Modifier.weight(1f)
                )
                CallCustomerButton(row["phone"].toDisplayText(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun OrderItemRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(), row["product_name"].toDisplayText("Order item"), 76)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(row["product_name"].toDisplayText("Product"), fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row["order_label"].toDisplayText("Order #${row["order_id"].toDisplayText().take(8)}"), color = Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill("Qty ${row["qty"].toDisplayText("1")}", RoyalBlue)
                    StatusPill("Rs ${row["total_price"].toDisplayText("0")}", Success)
                    Spacer(Modifier.weight(1f))
                    Text(row["created_at"].toDisplayText().formatTimestamp(), color = Muted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
internal fun OrderDetailsSection(
    orderItems: List<JsonObject>,
    productsById: Map<String, JsonObject>
) {
    PremiumCard {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Order items", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (orderItems.isEmpty()) {
                Text("No linked order_items rows found.", color = Muted)
            } else {
                orderItems.forEach { item ->
                    val product = productsById[item["menu_item_id"].toDisplayText()]
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RecordImage(product?.firstImageUrl(), product?.get("name").toDisplayText("Product"), 52)
                        Column(Modifier.weight(1f)) {
                            Text(product?.get("name").toDisplayText("Product"), fontWeight = FontWeight.Bold)
                            Text("Qty ${item["qty"].toDisplayText("1")}", color = Muted, fontSize = 12.sp)
                        }
                        Text("Rs ${item["total_price"].toDisplayText("0")}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusDropdown(current: String, onStatusChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("pending", "accepted", "preparing", "ready", "completed", "rejected")
    Surface(
        modifier = modifier.height(50.dp).clickable { expanded = true },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(current, modifier = Modifier.weight(1f), color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Change status",
                tint = Muted,
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
