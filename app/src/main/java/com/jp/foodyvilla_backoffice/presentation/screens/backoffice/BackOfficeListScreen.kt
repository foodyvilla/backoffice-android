package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
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
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.domain.security.OutletRole
import kotlinx.serialization.json.JsonObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private fun UserSession?.roleOrNull(): OutletRole? = when (this) {
    is UserSession.EmployeeSession -> role
    is UserSession.OutletSession -> role
    else -> null
}

@Composable
internal fun BackOfficeListScreen(
    session: UserSession?,
    route: AdminRoute,
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onCreate: () -> Unit,
    onOrderStatusChange: (JsonObject, String) -> Unit,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    onSendFcm: (String, String, String) -> Unit,
    onSendOfferToAll: (String, String) -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val context = LocalContext.current
    var showOfferDialog by remember { mutableStateOf(false) }
    var offerTitle by remember { mutableStateOf("Special Offer!") }
    var offerBody by remember { mutableStateOf("Check out our new items in your cart.") }

    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = { showOfferDialog = false },
            title = { Text("Send Notification") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = offerTitle,
                        onValueChange = { offerTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = offerBody,
                        onValueChange = { offerBody = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSendOfferToAll(offerTitle, offerBody)
                    showOfferDialog = false
                }) { Text("Send to All") }
            },
            dismissButton = {
                TextButton(onClick = { showOfferDialog = false }) { Text("Cancel") }
            }
        )
    }

    val rows = remember(state.rows, state.searchQuery) {
        if (state.searchQuery.isBlank()) state.rows else state.rows.filter {
            it.toString().contains(state.searchQuery, ignoreCase = true)
        }
    }

    val todayRecord = remember(state.rows, session) {
        if (route != AdminRoute.Attendance || session !is UserSession.EmployeeSession) null
        else {
            val today = LocalDate.now()
            state.rows.firstOrNull { row ->
                val createdAt = row["created_at"].toDisplayText()
                runCatching {
                    OffsetDateTime.parse(createdAt).toLocalDate() == today
                }.getOrDefault(false) && row["emp_id"].toDisplayText() == session.empId.toString()
            }
        }
    }

    val punchStatus = when {
        todayRecord == null -> "Punch In"
        todayRecord["out_time"].toDisplayText() == "-" -> "Punch Out"
        else -> "Completed"
    }

    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            if (punchStatus == "Punch In") onPunchIn() else if (punchStatus == "Punch Out") onPunchOut()
        } else {
            android.widget.Toast.makeText(context, "Location permission required", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun handlePunch() {
        val fineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
        
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, fineLocation) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            
            if (!isGpsEnabled) {
                android.widget.Toast.makeText(context, "Please enable GPS", android.widget.Toast.LENGTH_LONG).show()
                context.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                if (punchStatus == "Punch In") onPunchIn() else if (punchStatus == "Punch Out") onPunchOut()
            }
        } else {
            locationPermissionLauncher.launch(arrayOf(fineLocation, coarseLocation))
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
                        if (route.canCreate && (route != AdminRoute.Attendance || session?.roleOrNull() in listOf(OutletRole.OWNER, OutletRole.HEAD))) {
                            Button(
                                onClick = onCreate,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Text("New")
                            }
                        }
                        if (route == AdminRoute.Cart || route == AdminRoute.Customers) {
                            Button(
                                onClick = { showOfferDialog = true },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Orange)
                            ) {
                                Icon(androidx.compose.material.icons.Icons.Default.Campaign, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Send Offer")
                            }
                        }
                    }
                    if (route == AdminRoute.Attendance && session is UserSession.EmployeeSession) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { handlePunch() },
                                enabled = !state.isSaving && punchStatus != "Completed",
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (punchStatus == "Punch In") Success else Orange
                                )
                            ) {
                                Text(punchStatus)
                            }
                        }
                        if (route == AdminRoute.Cart || route == AdminRoute.Customers) {
                            Button(
                                onClick = { showOfferDialog = true },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Orange)
                            ) {
                                Icon(androidx.compose.material.icons.Icons.Default.Campaign, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Send Offer")
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
                    AdminRoute.Cart -> CartRecordCard(
                        row = row,
                        onSendNotification = { token -> onSendFcm(token, "Foody Villa", "Items are waiting in your cart!") },
                        onClick = { onOpenDetails(row) }
                    )
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
    customerOrders: List<JsonObject> = emptyList(),
    customerCart: List<JsonObject> = emptyList(),
    productsById: Map<String, JsonObject>,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    if (row == null) {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { EmptyState("No row selected", "Go back and select a Supabase row.") }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                OrderDetailsSection(orderItems, productsById)
            }
        }
        if (table.name == "outlet_menu_items") {
            item {
                MenuDetailsSection(row)
            }
        }
        if (table.name == "users") {
            item {
                Text("Customer Activity", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink)
            }
            if (customerOrders.isNotEmpty()) {
                item {
                    Text("Recent Orders", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
                }
                items(customerOrders) { order ->
                    OrderRecordCard(row = order, onClick = {})
                }
            }
            if (customerCart.isNotEmpty()) {
                item {
                    Text("Items in Cart", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
                }
                items(customerCart) { cartItem ->
                    CartRecordCard(row = cartItem, onSendNotification = {}, onClick = {})
                }
            }
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}
