package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
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
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme

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
                    Text(text = "SYSTEM ORDER ID REF", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(text = transaction.orderId.take(12) + "...", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "GATEWAY METHOD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text(text = transaction.method.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PaymentStatusBadge(status = transaction.status)
                    Text(text = "Date: ${transaction.createdAtDate}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outline)
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
    val extendedColors = AppTheme.colors
    val (contentColor, containerColor, icon) = when (status) {
        AdminPaymentStatus.CAPTURED -> Triple(extendedColors.success, extendedColors.successContainer, Icons.Default.CheckCircle)
        AdminPaymentStatus.AUTHORIZED -> Triple(extendedColors.info, extendedColors.infoContainer, Icons.Default.Info)
        AdminPaymentStatus.REFUNDED -> Triple(extendedColors.info, extendedColors.infoContainer, Icons.Default.History)
        AdminPaymentStatus.FAILED -> Triple(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer, Icons.Default.Cancel)
        AdminPaymentStatus.CREATED -> Triple(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer, Icons.Default.Info)
    }

    Surface(color = containerColor, shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Text(
                text = status.name, color = contentColor,
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
            )
        }
    }
}