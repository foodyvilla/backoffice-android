package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Cart
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun CartScreen(
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onSendOfferToAll: (String, String) -> Unit,
    onSendFcm: (String, String, String) -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    var showOfferDialog by remember { mutableStateOf(false) }
    var offerTitle by remember { mutableStateOf("Special Offer!") }
    var offerBody by remember { mutableStateOf("Check out our new items in your cart.") }

    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = { showOfferDialog = false },
            title = { Text("Send Notification to Cart Customers") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = offerTitle, onValueChange = { offerTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = offerBody, onValueChange = { offerBody = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSendOfferToAll(offerTitle, offerBody)
                    showOfferDialog = false
                }) { Text("Send to All") }
            },
            dismissButton = { TextButton(onClick = { showOfferDialog = false }) { Text("Cancel") } }
        )
    }

    val carts = remember(state.rows, state.searchQuery) {
        state.rows.map { it.toModel<Cart>() }.filter {
            it.customerName?.contains(state.searchQuery, ignoreCase = true) == true ||
                    it.productName?.contains(state.searchQuery, ignoreCase = true) == true ||
                    state.searchQuery.isBlank()
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
                placeholder = "Search carts"
            )
        }

        item {
            PremiumCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(AdminRoute.Cart.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${carts.size} Active Carts",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Customers with items in cart",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showOfferDialog = true },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Campaign, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Push Offer")
                    }
                }
            }
        }

        if (state.isLoading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary) }
        } else if (carts.isEmpty()) {
            item {
                EmptyState(
                    title = "No active carts",
                    message = "Carts appear when customers add items."
                )
            }
        } else {
            items(state.rows) { row ->
                val cart = row.toModel<Cart>()
                CartListItem(
                    cart = cart,
                    onSendNotification = { token -> onSendFcm(token, "Foody Villa", "Items are waiting in your cart!") },
                    onClick = { onOpenDetails(row) }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
