package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumn
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
internal fun OrderForm(
    session: UserSession?,
    state: AdminUiState,
    onBack: () -> Unit,
    onFormChange: (String, String) -> Unit,
    onAddDraftItem: (JsonObject, Int) -> Unit,
    onRemoveDraftItem: (Int) -> Unit,
    onSave: () -> Unit
) {
    val isEdit = state.editingRow != null
    var selectedMenuItemId by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }

    val orderType = state.formValues["order_type"] ?: "pickup"
    val selectedOutletId = state.formValues["outlet_id"] ?: session?.outletId?.toString() ?: ""

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Order Core Info
            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(if (isEdit) "Update Order" else "Order Configuration", style = MaterialTheme.typography.titleMedium)
                        
                        // Status (Editable in both modes)
                        val statuses = listOf("placed", "accepted", "preparing", "ready", "completed", "rejected", "cancelled")
                        var statusExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { statusExpanded = true },
                                modifier = Modifier.fillMaxWidth().height(58.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text("Status: ${(state.formValues["status"] ?: "placed").uppercase()}")
                            }
                            DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                                statuses.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            statusExpanded = false
                                            onFormChange("status", status)
                                        }
                                    )
                                }
                            }
                        }

                        if (!isEdit) {
                            // Outlet Dropdown (Required for new orders)
                            val outletColumn = state.selectedTable.columns.firstOrNull { it.name == "outlet_id" }
                            if (outletColumn != null) {
                                ReferenceDropdownField(
                                    column = outletColumn,
                                    value = selectedOutletId,
                                    options = state.lookupRows["outlets"].orEmpty(),
                                    onChange = { onFormChange("outlet_id", it) }
                                )
                            }

                            // Order Type Dropdown
                            val orderTypes = listOf("delivery", "pickup", "dine_in")
                            var typeExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { typeExpanded = true },
                                    modifier = Modifier.fillMaxWidth().height(58.dp),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Text("Type: ${orderType.replaceFirstChar { it.uppercase() }}")
                                }
                                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                    orderTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                            onClick = {
                                                typeExpanded = false
                                                onFormChange("order_type", type)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Customer Information
            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Customer Information", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = state.formValues["customer_name"] ?: "",
                            onValueChange = { onFormChange("customer_name", it) },
                            label = { Text("Customer Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            enabled = !isEdit
                        )
                        OutlinedTextField(
                            value = state.formValues["phone"] ?: "",
                            onValueChange = { onFormChange("phone", it) },
                            label = { Text("Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            enabled = !isEdit
                        )
                        
                        if (orderType == "delivery") {
                            OutlinedTextField(
                                value = state.formValues["address"] ?: "",
                                onValueChange = { onFormChange("address", it) },
                                label = { Text("Delivery Address") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(18.dp)
                            )
                        }
                    }
                }
            }

            // 3. Add Items (Only for new orders)
            if (!isEdit && selectedOutletId.isNotBlank()) {
                item {
                    PremiumCard {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Add Items", style = MaterialTheme.typography.titleMedium)

                            // Filter menu items by selected outlet
                            val menuItems = state.lookupRows["outlet_menu_items"].orEmpty()
                                .filter { it["outlet_id"].toDisplayText() == selectedOutletId }
                            
                            val itemColumn = state.selectedTable.columns.firstOrNull { it.name == "menu_item_id" }
                            if (itemColumn != null) {
                                ReferenceDropdownField(
                                    column = itemColumn,
                                    value = selectedMenuItemId,
                                    options = menuItems,
                                    onChange = { selectedMenuItemId = it }
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = itemQty,
                                    onValueChange = { itemQty = it },
                                    label = { Text("Qty") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(18.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                                Button(
                                    onClick = {
                                        val menuItem = menuItems.firstOrNull { it["id"].toDisplayText() == selectedMenuItemId }
                                        if (menuItem != null) {
                                            onAddDraftItem(menuItem, itemQty.toIntOrNull() ?: 1)
                                            selectedMenuItemId = ""
                                            itemQty = "1"
                                        }
                                    },
                                    enabled = selectedMenuItemId.isNotBlank(),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Icon(Icons.Default.Add, null)
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }

            // 4. Draft Items & Invoice
            if (!isEdit && state.draftOrderItems.isNotEmpty()) {
                item {
                    Text("Order Items", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                itemsIndexed(state.draftOrderItems) { index, (menuItem, qty) ->
                    val name = menuItem["product_name"].toDisplayText()
                    val price = menuItem["price"].asNumber()
                    val discount = menuItem["discount"].asNumber()
                    val net = (price - discount).coerceAtLeast(0.0)

                    PremiumCard {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold)
                                Text("$qty x Rs $net", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                            Text("Rs ${net * qty}", fontWeight = FontWeight.ExtraBold, color = StatusBlue)
                            IconButton(onClick = { onRemoveDraftItem(index) }) {
                                Icon(Icons.Default.Delete, null, tint = StatusRed)
                            }
                        }
                    }
                }

                item {
                    val total = state.draftOrderItems.sumOf { (item, qty) -> 
                        val p = item["price"].asNumber()
                        val d = item["discount"].asNumber()
                        (p - d).coerceAtLeast(0.0) * qty
                    }

                    PremiumCard {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Invoice Summary", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            
                            state.draftOrderItems.forEach { (item, qty) ->
                                val p = item["price"].asNumber()
                                val d = item["discount"].asNumber()
                                val handling = item["handling_charges"]?.asNumber() ?: 0.0
                                
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item["product_name"].toDisplayText()} (x$qty)", fontSize = 13.sp)
                                    Text("Rs ${(p - d) * qty}", fontSize = 13.sp)
                                }
                                if (handling > 0) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("  + Handling", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Rs ${handling * qty}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            val totalHandling = state.draftOrderItems.sumOf { (item, qty) -> (item["handling_charges"]?.asNumber() ?: 0.0) * qty }
                            val deliveryCharges = if (orderType == "delivery") {
                                // Find any item in draft that has delivery charges
                                state.draftOrderItems.maxOfOrNull { (item, _) -> item["delivery_charges"]?.asNumber() ?: 0.0 } ?: 0.0
                            } else 0.0

                            if (deliveryCharges > 0) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Delivery Charges", fontSize = 13.sp)
                                    Text("Rs $deliveryCharges", fontSize = 13.sp)
                                }
                            }

                            val grandTotal = total + totalHandling + deliveryCharges
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Grand Total", fontWeight = FontWeight.Bold)
                                Text("Rs $grandTotal", fontWeight = FontWeight.Black, fontSize = 20.sp, color = StatusGreen)
                            }
                            Text("Payment: CASH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        
        FormActions(
            onBack = onBack,
            onSave = onSave,
            isSaving = state.isSaving,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
