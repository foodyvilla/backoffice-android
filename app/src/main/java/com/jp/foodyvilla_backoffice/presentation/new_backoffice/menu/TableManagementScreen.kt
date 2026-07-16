package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.BillLineUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.CartLineUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.MenuItemUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.TableUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.TableManagementViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.currentBillLines
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.currentCartLines
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.currentGrandTotal
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.selectedTable
import org.koin.androidx.compose.koinViewModel

@Composable
fun TableManagementScreen(
    outletId: Long = 1,
    viewModel: TableManagementViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(outletId) {
        viewModel.loadOutlet(outletId)
    }

    state.lastInvoice?.let { (tableNumber, total) ->
        AlertDialog(
            onDismissRequest = viewModel::dismissInvoiceConfirmation,
            confirmButton = {
                TextButton(onClick = viewModel::dismissInvoiceConfirmation) { Text("Done") }
            },
            title = { Text("Bill settled — Table $tableNumber") },
            text = { Text("Invoice sent to printer. Total collected: ₹${"%.2f".format(total)}") }
        )
    }

    Scaffold(
        bottomBar = {
            if (state.selectedTableId != null) {
                BottomActionBar(
                    hasUnprintedKot = state.currentBillLines.any { !it.kotPrinted },
                    hasAnyItems = state.currentBillLines.isNotEmpty() || state.currentCartLines.isNotEmpty(),
                    onPrintKot = { viewModel.printKot(context) },
                    onMarkDone = viewModel::markOrderServed,
                    onPrintInvoice = { viewModel.printInvoiceAndSettle(context) }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ---- Table selection strip ----
            Text(
                text = "Tables",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.tables, key = { it.id }) { table ->
                    TableChip(
                        table = table,
                        isSelected = table.id == state.selectedTableId,
                        onClick = { viewModel.selectTable(table) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (state.isLoading && state.selectedTableId == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@Column
            }

            if (state.selectedTableId == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a table to start taking an order", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                return@Column
            }

            state.errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            }

            // ---- Category strip ----
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories, key = { it.id }) { category ->
                    FilterChip(
                        selected = category.id == state.selectedCategoryId,
                        onClick = { viewModel.selectCategory(category.id) },
                        label = { Text("${category.emoji} ${category.name}") }
                    )
                }
            }

            // ---- Product list + running bill, split so both stay reachable ----
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val filteredMenu = state.menuItems.filter {
                    it.categoryId == state.selectedCategoryId && it.isAvailable
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMenu, key = { it.menuItemId }) { item ->
                        MenuItemRow(item = item, onAdd = { viewModel.addToCart(item) })
                    }
                }

                VerticalDivider()

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Text("Order summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    items(state.currentBillLines, key = { "saved-${it.orderItemId}" }) { line ->
                        BillLineRow(
                            name = line.name,
                            price = line.pricePerItem,
                            qty = line.qty,
                            sent = line.kotPrinted,
                            onQtyChange = { newQty -> viewModel.updateSavedLineQty(line, newQty) }
                        )
                    }
                    items(state.currentCartLines, key = { "cart-${it.menuItemId}" }) { line ->
                        BillLineRow(
                            name = line.name,
                            price = line.price,
                            qty = line.qty,
                            sent = false,
                            isPending = true,
                            onQtyChange = { newQty -> viewModel.updateCartQty(line.menuItemId, newQty) }
                        )
                    }
                    if (state.currentCartLines.isNotEmpty()) {
                        item {
                            Button(
                                onClick = viewModel::saveCartToOrder,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) { Text("Save to order") }
                        }
                    }
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontWeight = FontWeight.Bold)
                            Text("₹${"%.2f".format(state.currentGrandTotal)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableChip(table: TableUiModel, isSelected: Boolean, onClick: () -> Unit) {
    val statusColor = when (table.status) {
        "occupied" -> Color(0xFFE64A19)
        "reserved" -> Color(0xFFFFA000)
        else -> Color(0xFF43A047)
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, statusColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(statusColor))
            Spacer(Modifier.width(6.dp))
            Text(table.tableNumber, fontWeight = FontWeight.Bold)
        }
        Text("${table.capacity} seats", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MenuItemRow(item: MenuItemUiModel, onAdd: () -> Unit) {
    ListItem(
        headlineContent = { Text(item.name) },
        supportingContent = { Text("₹${"%.2f".format(item.price)}") },
        trailingContent = {
            IconButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "Add ${item.name}") }
        }
    )
}

@Composable
private fun BillLineRow(
    name: String,
    price: Double,
    qty: Long,
    sent: Boolean,
    isPending: Boolean = false,
    onQtyChange: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (isPending) "not saved yet" else if (sent) "sent to kitchen" else "saved",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { onQtyChange(qty - 1) }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease")
        }
        Text("$qty", modifier = Modifier.padding(horizontal = 4.dp))
        IconButton(onClick = { onQtyChange(qty + 1) }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Increase")
        }
        Text("₹${"%.2f".format(price * qty)}", modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun BottomActionBar(
    hasUnprintedKot: Boolean,
    hasAnyItems: Boolean,
    onPrintKot: () -> Unit,
    onMarkDone: () -> Unit,
    onPrintInvoice: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPrintKot,
                enabled = hasUnprintedKot,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Print KOT")
            }
            OutlinedButton(
                onClick = onMarkDone,
                enabled = hasAnyItems,
                modifier = Modifier.weight(1f)
            ) { Text("Mark as Done") }
            Button(
                onClick = onPrintInvoice,
                enabled = hasAnyItems,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Print Invoice")
            }
        }
    }
}