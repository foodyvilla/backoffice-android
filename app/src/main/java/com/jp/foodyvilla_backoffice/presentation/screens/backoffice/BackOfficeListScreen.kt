package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminTable
import kotlinx.serialization.json.JsonObject

@Composable
internal fun BackOfficeListScreen(
    route: AdminRoute,
    state: AdminUiState,
    onMenu: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: (String) -> Unit,
    onCreate: () -> Unit,
    onOrderStatusChange: (JsonObject, String) -> Unit,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val rows = remember(state.rows, state.searchQuery) {
        if (state.searchQuery.isBlank()) state.rows else state.rows.filter {
            it.toString().contains(state.searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumTopBar(
                title = route.title,
                subtitle = route.subtitle,
                icon = route.icon,
                onMenu = onMenu,
                onRefresh = onRefresh
            )
        }
        item {
            SearchAndFilterBar(
                query = state.searchQuery,
                onQueryChange = onSearch,
                placeholder = "Search ${route.title.lowercase()}"
            )
        }
        item {
            PremiumCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(route.icon, contentDescription = null, tint = RoyalBlue)
                        Column(Modifier.weight(1f)) {
                            Text("${rows.size} records", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                            Text(state.selectedTable.description, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        if (route.canCreate) {
                            Button(
                                onClick = onCreate,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Text("New")
                            }
                        }
                    }
                    if (route == AdminRoute.Attendance) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onPunchIn,
                                enabled = !state.isSaving,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Success)
                            ) {
                                Text("Punch In")
                            }
                            OutlinedButton(
                                onClick = onPunchOut,
                                enabled = !state.isSaving,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                            ) {
                                Text("Punch Out")
                            }
                        }
                    }
                }
            }
        }
        if (state.isLoading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue) }
        } else if (rows.isEmpty()) {
            item {
                EmptyState(
                    title = "No ${route.title.lowercase()} found",
                    message = "No Supabase rows matched this view.",
                    actionLabel = if (route.canCreate) state.selectedTable.createLabel else null,
                    onAction = if (route.canCreate) onCreate else null
                )
            }
        } else {
            items(rows, key = { it[state.selectedTable.primaryKey].toDisplayText() }) { row ->
                when (route) {
                    AdminRoute.Products -> ProductRecordCard(row, onClick = { onOpenDetails(row) })
                    AdminRoute.OrderItems -> OrderItemRecordCard(row, onClick = { onOpenDetails(row) })
                    AdminRoute.Orders -> OrderRecordCard(
                        row = row,
                        onStatusChange = { status -> onOrderStatusChange(row, status) },
                        onClick = { onOpenDetails(row) }
                    )
                    AdminRoute.Customers -> CustomerRecordCard(row, onClick = { onOpenDetails(row) })
                    AdminRoute.Reviews -> ReviewRecordCard(row, onClick = { onOpenDetails(row) })
                    AdminRoute.Offers, AdminRoute.Banners -> MediaRecordCard(state.selectedTable, row, onClick = { onOpenDetails(row) })
                    AdminRoute.Employees -> EmployeeRecordCard(row, onClick = { onOpenDetails(row) })
                    AdminRoute.OutletMenu -> OutletMenuRecordCard(row, onClick = { onOpenDetails(row) })
                    else -> GenericRecordCard(state.selectedTable, row, onClick = { onOpenDetails(row) })
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
internal fun BackOfficeDetailScreen(
    table: AdminTable,
    row: JsonObject?,
    orderItems: List<JsonObject>,
    productsById: Map<String, JsonObject>,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    if (row == null) {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { PremiumTopBar("Details", "No row selected", AdminRoute.Details.icon, onBack = onBack) }
            item { EmptyState("No row selected", "Go back and select a Supabase row.") }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            PremiumTopBar(table.title, "Details", AdminRoute.Details.icon, onBack = onBack)
        }
        item {
            PremiumCard {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    row.firstImageUrl(table)?.let {
                        LargeRecordImage(it, table.title)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onEdit,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Text("Edit", modifier = Modifier.padding(start = 8.dp))
                        }
                        if (table.name == "orders") {
                            StatusPill(row["status"].toDisplayText().normalizeOrderStatus(), statusColor(row["status"].toDisplayText()))
                        }
                    }
                    table.columns.forEach { column ->
                        DetailLine(column.label, row[column.name].toCompactText(column))
                    }
                }
            }
        }
        if (table.name == "orders") {
            item {
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
                                        Text(product?.get("name").toDisplayText("Product"), fontWeight = FontWeight.SemiBold)
                                        Text("Qty ${item["qty"].toDisplayText("1")}", color = Muted, fontSize = 12.sp)
                                    }
                                    Text("Rs ${item["total_price"].toDisplayText("0")}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (table.name == "outlet_menu_items") {
            item {
                PremiumCard {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Product catalog details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        DetailLine("Product", row["product_name"].toDisplayText("Product"))
                        DetailLine("Category", row["product_category"].toDisplayText())
                        DetailLine("Description", row["product_description"].toDisplayText())
                        DetailLine("Veg", row["product_is_veg"].toDisplayText())
                        DetailLine("Prep time", row["product_prep_time"].toDisplayText())
                        DetailLine("Menu price", "Rs ${row["price"].toDisplayText("0")}")
                        DetailLine("Discount", row["discount"].toDisplayText("0"))
                        DetailLine("Available", row["is_available"].toDisplayText())
                        DetailLine("Out of stock", row["is_out_of_stock"].toDisplayText())
                    }
                }
            }
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
internal fun ProductRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(), row["name"].toDisplayText("Product"), 84)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(row["name"].toDisplayText("Untitled product"), fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row["category"].toDisplayText("No category"), color = Muted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("Rs ${row["price"].toDisplayText("0")}", RoyalBlue)
                    if (row["is_bestseller"].toDisplayText().equals("true", true)) StatusPill("Bestseller", Success)
                }
            }
        }
    }
}

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
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = RoyalBlue)
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
private fun OrderStatusDropdown(current: String, onStatusChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("pending", "accepted", "preparing", "ready", "completed", "rejected")
    androidx.compose.material3.Surface(
        modifier = modifier.height(50.dp).clickable { expanded = true },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = androidx.compose.ui.graphics.Color.White,
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Phone, contentDescription = null)
        Text("Call", modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
internal fun CustomerRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(), row["name"].toDisplayText("Customer"), 64)
            Column(Modifier.weight(1f)) {
                Text(row["name"].toDisplayText("No name"), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(row["phone"].toDisplayText("No phone"), color = Muted)
                Text(row["email"].toDisplayText("No email"), color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun ReviewRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RecordImage(row.firstImageUrl(), "Review", 68)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(row["title"].toDisplayText("Review"), fontWeight = FontWeight.Bold)
                Text(row["description"].toDisplayText("No description"), color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                StatusPill("${row["rating"].toDisplayText("0")} stars", Warning)
            }
        }
    }
}

@Composable
internal fun MediaRecordCard(table: AdminTable, row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(table), row["title"].toDisplayText(table.title), 86)
            Column(Modifier.weight(1f)) {
                Text(row["title"].toDisplayText(table.title), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(row["description"].toDisplayText(row["created_at"].toDisplayText()), color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun EmployeeRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(), row["name"].toDisplayText("Employee"), 68)
            Column(Modifier.weight(1f)) {
                Text(row["name"].toDisplayText("No name"), fontWeight = FontWeight.Bold)
                Text(row["role"].toDisplayText("No role"), color = Muted)
                Text(row["contact"].toDisplayText("No contact"), color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
internal fun OutletMenuRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(), row["product_name"].toDisplayText("Menu item"), 84)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(row["product_name"].toDisplayText("Product"), fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row["product_category"].toDisplayText("No category"), color = Muted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("Rs ${row["price"].toDisplayText("0")}", RoyalBlue)
                    StatusPill(if (row["is_available"].toDisplayText().equals("true", true)) "Available" else "Hidden", if (row["is_available"].toDisplayText().equals("true", true)) Success else Muted)
                    if (row["is_out_of_stock"].toDisplayText().equals("true", true)) StatusPill("Out of stock", Danger)
                }
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("Qty ${row["qty"].toDisplayText("1")}", RoyalBlue)
                    StatusPill("Rs ${row["total_price"].toDisplayText("0")}", Success)
                }
            }
        }
    }
}

@Composable
internal fun GenericRecordCard(table: AdminTable, row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            row.firstImageUrl(table)?.let { image ->
                RecordImage(image, table.title, 62)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(table.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                table.displayColumns.take(4).forEach { column ->
                    DetailLine(column.replace("_", " "), row[column].toDisplayText())
                }
            }
        }
    }
}

@Composable
internal fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 13.sp, modifier = Modifier.weight(.42f))
        Text(value, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(.58f), maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
