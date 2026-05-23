package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.*

@Composable
internal fun CustomerRecordCard(user: User, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(user.fcmToken?.extractUrl() ?: user.address?.extractUrl(), user.name ?: "Customer", 64)
            Column(Modifier.weight(1f)) {
                Text(user.name ?: "No name", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(user.phone ?: "No phone", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(user.email ?: "No email", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun CartRecordCard(
    cart: Cart,
    onSendNotification: (String) -> Unit,
    onClick: () -> Unit
) {
    PremiumCard(onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                RecordImage(cart.productImage?.firstOrNull(), cart.productName ?: "Product", 64)
                Column(Modifier.weight(1f)) {
                    Text(cart.customerName ?: "Customer", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(cart.productName ?: "Product", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusPill("Qty ${cart.qty}", MaterialTheme.colorScheme.primary)
                        StatusPill("Rs ${cart.productPrice ?: 0.0}", MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Added on", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Text(cart.createdAt?.formatTimestamp() ?: "-", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                
                val fcmToken = cart.customerFcm
                if (!fcmToken.isNullOrBlank() && fcmToken != "-") {
                    Button(
                        onClick = { onSendNotification(fcmToken) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Notifications, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Remind", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ReviewRecordCard(review: Review, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RecordImage(null, "Review", 68)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(review.title ?: "Review", fontWeight = FontWeight.Bold)
                Text(review.description ?: "No description", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    StatusPill("${review.rating} stars", MaterialTheme.colorScheme.tertiary)
                    Text(review.createdAt?.formatTimestamp() ?: "-", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
        }
    }
}
