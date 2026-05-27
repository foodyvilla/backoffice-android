package com.jp.foodyvilla_backoffice.presentation.new_backoffice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.orders.NewOrderType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.UnifiedOrderControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCreateOrderScreen(
    viewModel: UnifiedOrderControlViewModel,
    onNavigateBack: () -> Unit,
    onOrderFinishedSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var rawPhoneInput by remember { mutableStateOf("") }

    // Track manually entered names for new customers
    var customCustomerNameInput by remember { mutableStateOf("") }

    // Synchronize or clear manual text inputs if the phone sequence drops below 10 digits
    LaunchedEffect(state.resolvedCustomer) {
        customCustomerNameInput = state.resolvedCustomer?.name ?: ""
    }

    state.summaryPendingDialogOrder?.let { pendingSummary ->
        AlertDialog(
            onDismissRequest = viewModel::dismissSummaryDialog,
            title = { Text("Order Created Successfully", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.bindDp())) {
                    Text("Order Reference ID: ${pendingSummary.orderId}", style = MaterialTheme.typography.bodySmall)
                    Text("Customer: ${pendingSummary.customerName}")
                    Text("Fulfillment Type: ${pendingSummary.orderType}")
                    Text("Grand Total Collected: ₹${pendingSummary.totalAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.dismissSummaryDialog()
                    onOrderFinishedSuccess()
                }) {
                    Text("Back to Dashboard")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Invoice Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ====================================================
                // SECTION 1: CUSTOMER RECOGNITION INTERFACE
                // ====================================================
                item {
                    Text(
                        text = "Customer Identification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = rawPhoneInput,
                            onValueChange = { input ->
                                val cleanDigits = input.filter { it.isDigit() }
                                if (cleanDigits.length <= 10) {
                                    rawPhoneInput = cleanDigits
                                    if (cleanDigits.length == 10) {
                                        viewModel.updateCustomerPhone("+91$cleanDigits")
                                    } else {
                                        viewModel.updateCustomerPhone("")
                                        customCustomerNameInput = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Mobile Phone Number") },
                            prefix = { Text("+91 ") },
                            placeholder = { Text("Enter 10-digit number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )

                        // DYNAMIC LOGICAL FIELD: Name Input area opens smoothly only when a 10-digit phone is ready
                        AnimatedVisibility(
                            visible = rawPhoneInput.length == 10,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val isExistingCustomer = state.resolvedCustomer != null

                            OutlinedTextField(
                                value = customCustomerNameInput,
                                onValueChange = { typedName ->
                                    if (!isExistingCustomer) {
                                        customCustomerNameInput = typedName
                                        // Update local state engine mock model so checkout catches the custom entry string
                                        viewModel.updateCustomCustomerName(typedName)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(
                                        if (isExistingCustomer) "Customer Profile Name (Saved)"
                                        else "Enter Walk-In Customer Name"
                                    )
                                },
                                placeholder = { Text("e.g. Ramesh Kumar") },
                                readOnly = isExistingCustomer, // Locked if profile already exists in DB
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = if (isExistingCustomer) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = if (isExistingCustomer) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }

                // Customer Info Status Profile Display Card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Account Status Meta Profile",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Target Name: ${customCustomerNameInput.ifBlank { "Walk-In Consumer Account" }}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            if (state.checkoutOrderType == NewOrderType.DELIVERY) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = state.checkoutAddress,
                                    onValueChange = viewModel::updateCheckoutAddress,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Fulfillment Shipping Destination Address") },
                                    minLines = 2
                                )
                            }
                        }
                    }
                }

                // ====================================================
                // SECTION 2: FULFILLMENT METHOD TYPE LOGISTICS
                // ====================================================
                item {
                    Text(
                        text = "Fulfillment Logistics Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NewOrderType.entries.forEach { methodType ->
                            val isSelected = state.checkoutOrderType == methodType

                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateCheckoutOrderType(methodType) },
                                label = { Text(methodType.name.replace("_", " ")) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ====================================================
                // SECTION 3: INLINE DISPATCHING SPECIAL INSTRUCTIONS
                // ====================================================
                item {
                    OutlinedTextField(
                        value = state.checkoutInstruction,
                        onValueChange = viewModel::updateCheckoutInstruction,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Kitchen Cooking & Delivery Instructions (Optional)") },
                        placeholder = { Text("e.g., Make it extra spicy, Leave at gate") },
                        singleLine = true
                    )
                }

                // ====================================================
                // SECTION 4: BASKET INVOICE ITEMS LIST
                // ====================================================
                item {
                    Text(
                        text = "Review Ordered Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(state.cartSelectedItems.values.toList(), key = { it.menuItemId }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "₹${item.price} x ${item.qty}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "₹${item.totalPrice}",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // ====================================================
                // SECTION 5: ITEMIZED RECEIPT INVOICE CHARGES BREAKDOWN
                // ====================================================
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp)
                    Text(
                        text = "Bill Receipt Summary Statement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val rawCartBaseSum = state.cartSelectedItems.values.sumOf { it.totalPrice }

                    val packagingMaterialsFee = if (state.cartSelectedItems.isNotEmpty()) 15.0 else 0.0
                    val cgstTaxCalculated = rawCartBaseSum * 0.025
                    val sgstTaxCalculated = rawCartBaseSum * 0.025
                    val handlingLogisticsDeliveryFee = if (state.checkoutOrderType == NewOrderType.DELIVERY) 30.0 else 0.0

                    val grandInvoiceTotalSum = rawCartBaseSum + packagingMaterialsFee + cgstTaxCalculated + sgstTaxCalculated + handlingLogisticsDeliveryFee

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InvoiceRow(label = "Items Basket Subtotal", amount = rawCartBaseSum)
                        InvoiceRow(label = "Restaurant Packaging Materials Fee", amount = packagingMaterialsFee)
                        InvoiceRow(label = "Central CGST (2.5%)", amount = cgstTaxCalculated)
                        InvoiceRow(label = "State SGST (2.5%)", amount = sgstTaxCalculated)

                        if (state.checkoutOrderType == NewOrderType.DELIVERY) {
                            InvoiceRow(label = "Delivery Handling Rider Logistics Fee", amount = handlingLogisticsDeliveryFee, isHighlight = true)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Grand Total Payable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "₹${String.format("%.2f", grandInvoiceTotalSum)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (state.globalErrorMessage != null) {
                Text(
                    text = state.globalErrorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ====================================================
            // FOOTER: ACTION PLACEMENT BUTTON INTERCEPTOR
            // ====================================================
            Button(
                onClick = viewModel::executeOrderPlacement,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .imePadding(),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isPlacingOrderState && state.cartSelectedItems.isNotEmpty() && rawPhoneInput.length == 10 && customCustomerNameInput.isNotBlank(),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (state.isPlacingOrderState) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Place Terminal Counter Order",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(
    label: String,
    amount: Double,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isHighlight) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = "₹${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun Int.bindDp(): androidx.compose.ui.unit.Dp = this.dp