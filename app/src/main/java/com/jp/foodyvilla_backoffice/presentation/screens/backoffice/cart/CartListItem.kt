package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.cart

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Cart
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
fun CartListItem(
    cart: Cart,
    onSendNotification: (String) -> Unit,
    onClick: () -> Unit
) {
    PremiumCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecordImage(url = cart.productImage?.firstOrNull(), label = cart.productName, size = 62)
            
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = cart.customerName ?: "Unknown Customer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${cart.productName ?: "Product"} x ${cart.qty}",
                    color = Muted,
                    fontSize = 14.sp
                )
                Text(
                    text = cart.outletName ?: "Outlet",
                    color = RoyalBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            cart.customerFcm?.let { token ->
                IconButton(onClick = { onSendNotification(token) }) {
                    Icon(Icons.Default.Notifications, "Notify", tint = Orange)
                }
            }
        }
    }
}
