package com.jp.foodyvilla_backoffice.presentation.new_backoffice

// ==========================================
// Dashboard Interception Overlay Component 
// ==========================================
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewDetailedOrderUiModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.UnifiedOrderControlViewModel


@Composable
fun HandleRealTimeInterceptedOrders(
    viewModel: UnifiedOrderControlViewModel,
    onViewOrderDetailsAction: (NewDetailedOrderUiModel) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val androidContext = LocalContext.current

    // 1. Local state persistence tracking IDs skipped by the active supervisor session
    var dismissedOrderIds by remember { mutableStateOf(setOf<String>()) }

    // 2. Clear out memory maps cleanly if active orders dataset drops to zero (e.g., date shift)
    LaunchedEffect(state.onlineOrders, state.tableOrders) {
        if (state.onlineOrders.isEmpty() && state.tableOrders.isEmpty()) {
            dismissedOrderIds = emptySet()
        }
    }

    // Isolate active candidates that match alert parameters AND haven't been dismissed locally yet
    val visibleAlertQueue = remember(state.onlineOrders, state.tableOrders, dismissedOrderIds) {
        (state.onlineOrders + state.tableOrders).filter { order ->
            val isTargetStatus = order.status.lowercase() == "pending" || order.status.lowercase() == "placed"
            isTargetStatus && order.id !in dismissedOrderIds
        }
    }

    // Select the top item from our filtered queue stream matrices
    val currentActiveAlert = visibleAlertQueue.firstOrNull()

    // 3. Audio Chime Component management setup
    val shortNotificationChime: Ringtone? = remember(androidContext) {
        runCatching {
            val chimeUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            RingtoneManager.getRingtone(androidContext, chimeUri)
        }.getOrNull()
    }

    // Play a single clean chime automatically every time a brand new row advances to the front of the line
    LaunchedEffect(currentActiveAlert?.id) {
        currentActiveAlert?.let {
            runCatching {
                if (shortNotificationChime != null && !shortNotificationChime.isPlaying) {
                    shortNotificationChime.play()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (shortNotificationChime != null && shortNotificationChime.isPlaying) {
                shortNotificationChime.stop()
            }
        }
    }

    // ====================================================
    // TOP ANCHORED STREAM VIEW TOAST BANNER OVERLAY
    // ====================================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = currentActiveAlert != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            currentActiveAlert?.let { activeOrder ->
                // Safely summarize quantitative details directly from domain models arrays
                val dynamicTotalItemCount = activeOrder.items.sumOf { it.qty }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "New Order: ${activeOrder.customerName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                color = if (activeOrder.status.lowercase() == "placed")
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = activeOrder.status.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${activeOrder.orderType} • Items Count: $dynamicTotalItemCount • Total: ₹${activeOrder.totalAmount}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Loc: ${activeOrder.address.ifBlank { "Counter / Dine-In POS" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // DISMISS ACTION: Adds active reference to hash filter list -> advances queue instantly
                            TextButton(
                                onClick = {
                                    dismissedOrderIds = dismissedOrderIds + activeOrder.id
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Dismiss", style = MaterialTheme.typography.labelLarge)
                            }

                            // DETAILS VIEW ACTION
                            TextButton(
                                onClick = {
                                    dismissedOrderIds = dismissedOrderIds + activeOrder.id
                                    onViewOrderDetailsAction(activeOrder)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Details", style = MaterialTheme.typography.labelLarge)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // ACCEPT ACTION: Updates PostgreSQL remote record status inline safely
                            FilledTonalButton(
                                onClick = {
                                    viewModel.modifyOrderStatusCardInline(activeOrder.id, "accepted")
                                    dismissedOrderIds = dismissedOrderIds + activeOrder.id
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Accept", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// NEW DETAILED ORDER PROFILE SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderDetailsScreen(
    orderId: String,
    viewModel: UnifiedOrderControlViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val availableStatuses = listOf("pending", "accepted", "preparing", "completed", "cancelled")

    // Find our current targeted order from the reactive data store context matching screen parameters
     val order = remember(state.onlineOrders, state.tableOrders, orderId) { 
        (state.onlineOrders + state.tableOrders).find { it.id == orderId } 
    }

    // Synchronize localized form editor states on initial composition entry
    LaunchedEffect(order) {
        order?.let { viewModel.launchDetailedOrderFormEdition(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Management Details Workspace", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = {
//                    viewModel.dismissDetailedEditForm();
                    onNavigateBack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { innerPadding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Error: Order reference matrix missing. Return back to dashboard.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(text = "Order Spec Matrix: #${order.id.take(12)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Date Registered: ${order.createdAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Customer Overview Matrix Summary Panel Block
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Customer Record Info Data Profile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Identity Name: ${order.customerName}", fontWeight = FontWeight.Medium)
                            Text("Primary Contact Line: ${order.phone}")
                        }
                    }
                }

                // INTERACTIVE FORM INPUT EDITORS SECTION
                item {
                    Text("Modify Order Properties Specifications", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        
                        // 1. Interactive Dropdown Fulfillment Status Changer
                        var statusDropdownShow by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.formEditStatus.uppercase(), onValueChange = {}, label = { Text("Order Fulfillment Status Matrix") },
                                modifier = Modifier.fillMaxWidth(), readOnly = true,
                                trailingIcon = { IconButton(onClick = { statusDropdownShow = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                            )
                            DropdownMenu(expanded = statusDropdownShow, onDismissRequest = { statusDropdownShow = false }) {
                                availableStatuses.forEach { itemStatus ->
                                    DropdownMenuItem(text = { Text(itemStatus.uppercase()) }, onClick = {
                                        viewModel.onFormStatusChanged(itemStatus)
                                        statusDropdownShow = false
                                    })
                                }
                            }
                        }

                        // 2. Modifiable Logistic Delivery Destination Address
                        OutlinedTextField(
                            value = state.formEditAddress, onValueChange = viewModel::onFormAddressChanged,
                            label = { Text("Delivery Destination Shipping Address Location") }, modifier = Modifier.fillMaxWidth()
                        )

                        // 3. Modifiable Production Kitchen Instructions
                        OutlinedTextField(
                            value = state.formEditInstruction, onValueChange = viewModel::onFormInstructionChanged,
                            label = { Text("Preparation Notes Kitchen Instructions Guide") }, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Itemized Receipt Breakdowns Row Sliders
                item {
                    Text("Itemized Bill Receipt Invoices", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                items(order.items) { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${item.name} x ${item.qty}", modifier = Modifier.weight(1f))
                        Text(text = "₹${item.totalPrice}", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Complete Accounting Matrix Summaries Block
                item {
                    HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                    val computedPkgSum = 15.0
                    val taxCgstSum = order.totalAmount * 0.025
                    val taxSgstSum = order.totalAmount * 0.025
                    val logisticSum = if (order.orderType == "DELIVERY") 30.0 else 0.0
                    val comprehensiveGrandInvoiceSum = order.totalAmount + computedPkgSum + taxCgstSum + taxSgstSum + logisticSum

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Items Base Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${order.totalAmount}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Packaging Materials Surcharge", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹$computedPkgSum")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST Central Tax (2.5%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%.2f", taxCgstSum)}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST State Tax (2.5%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%.2f", taxSgstSum)}")
                        }
                        if (logisticSum > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Logistics handling Rider Fee", color = MaterialTheme.colorScheme.primary)
                                Text("₹$logisticSum", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Grand Total Due", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("₹${String.format("%.2f", comprehensiveGrandInvoiceSum)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.commitDetailedFormModifications(); onNavigateBack() },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isLoading
                    ) {
                        Text("Save & Overwrite Document modifications Parameters", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}