package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Cart
import com.jp.foodyvilla_backoffice.data.model.backoffice.Order
import com.jp.foodyvilla_backoffice.data.model.backoffice.User
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun CustomerDetailScreen(
    session: UserSession?,
    row: JsonObject?,
    customerOrders: List<Order>,
    customerCart: List<Cart>,
    onEdit: () -> Unit
) {
    if (row == null) {
        EmptyState("No customer selected", "Go back and select a customer.")
        return
    }

    val customer = remember(row) { row.toModel<User>() }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PremiumCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LargeRecordImage(null, customer.name)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (session?.canEdit("users") == true) {
                            Button(
                                onClick = onEdit,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Text("Edit Profile", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        if (customer.isVerified == true) {
                            StatusPill("Verified Account", MaterialTheme.colorScheme.primary)
                        }
                    }

                    DetailLine("Name", customer.name ?: "-")
                    DetailLine("Phone", customer.phone ?: "-")
                    DetailLine("Email", customer.email ?: "-")
                    DetailLine("Address", customer.address ?: "-")
                    DetailLine("Created At", customer.createdAt ?: "-")
                }
            }
        }

        if (customerOrders.isNotEmpty()) {
            item {
                Text("Recent Orders", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            items(customerOrders) { order ->
                OrderRecordCard(order = order, onClick = {})
            }
        }

        if (customerCart.isNotEmpty()) {
            item {
                Text("Items in Cart", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            items(customerCart) { cartItem ->
                CartRecordCard(cart = cartItem, onSendNotification = {}, onClick = {})
            }
        }
    }
}
