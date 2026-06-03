package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminPaymentMethod
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminPaymentStatus
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.PaymentAdminUiModel

@Composable
fun PaymentAdminRowCard(
    transaction: PaymentAdminUiModel,
    onModifyClick: () -> Unit,
    onPurgeClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = transaction.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Contact Phone: ${transaction.customerContact}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = "₹${String.format("%.2f", transaction.amountDisplay)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "SYSTEM ORDER ID REF", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = transaction.orderId.take(12) + "...", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "GATEWAY METHOD", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = transaction.method.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PaymentStatusBadge(status = transaction.status)
                    Text(text = "Date: ${transaction.createdAtDate}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                }

                Row {
                    IconButton(onClick = onModifyClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Transaction Log", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onPurgeClick) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Purge Row Log Entry", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusBadge(status: AdminPaymentStatus) {
    val tintColor = when (status) {
        AdminPaymentStatus.CAPTURED -> Color(0xFF2E7D32)
        AdminPaymentStatus.AUTHORIZED -> Color(0xFF1565C0)
        AdminPaymentStatus.REFUNDED -> Color(0xFF00838F)
        AdminPaymentStatus.FAILED -> Color(0xFFC62828)
        AdminPaymentStatus.CREATED -> Color(0xFF424242)
    }
    val backgroundContainerColor = when (status) {
        AdminPaymentStatus.CAPTURED -> Color(0xFFE8F5E9)
        AdminPaymentStatus.AUTHORIZED -> Color(0xFFE3F2FD)
        AdminPaymentStatus.REFUNDED -> Color(0xFFE0F7FA)
        AdminPaymentStatus.FAILED -> Color(0xFFFFEBEE)
        AdminPaymentStatus.CREATED -> Color(0xFFF5F5F5)
    }

    Surface(color = backgroundContainerColor, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = status.name, color = tintColor,
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}