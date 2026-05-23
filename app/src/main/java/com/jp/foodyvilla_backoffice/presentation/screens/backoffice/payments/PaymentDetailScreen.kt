package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Payment
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun PaymentDetailScreen(
    row: JsonObject?
) {
    if (row == null) {
        EmptyState("No payment selected", "Go back and select a payment.")
        return
    }

    val payment = remember(row) { row.toModel<Payment>() }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Payment Record",
                            style = MaterialTheme.typography.titleLarge
                        )
                        StatusPill(
                            label = payment.paymentStatus.uppercase(),
                            color = if (payment.paymentStatus.lowercase() == "captured") Success else Warning
                        )
                    }

                    DetailLine("Customer", payment.orderCustomer ?: "-")
                    DetailLine("Amount", "Rs ${payment.amount / 100.0}")
                    DetailLine("Currency", payment.currency)
                    DetailLine("Method", payment.paymentMethod ?: "-")
                    DetailLine("Razorpay Payment ID", payment.razorpayPaymentId ?: "-")
                    DetailLine("Razorpay Order ID", payment.razorpayOrderId ?: "-")
                    DetailLine("Order ID", payment.orderId ?: "-")
                    DetailLine("Created At", payment.createdAt ?: "-")
                    
                    if (payment.errorDescription != null) {
                        DetailLine("Error", payment.errorDescription ?: "-")
                    }
                }
            }
        }
    }
}
