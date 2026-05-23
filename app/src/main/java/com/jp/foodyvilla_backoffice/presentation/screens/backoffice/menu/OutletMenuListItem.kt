package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.OutletMenuItem
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
fun OutletMenuListItem(
    item: OutletMenuItem,
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
            RecordImage(url = item.image?.firstOrNull(), label = item.productName, size = 84)
            
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = item.productName ?: "Product",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.productCategory ?: "No category",
                    color = Muted,
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("Rs ${item.price}", RoyalBlue)
                    StatusPill(if (item.isAvailable) "Available" else "Hidden", if (item.isAvailable) Success else Muted)
                    if (item.isOutOfStock) StatusPill("Out of stock", Danger)
                }
            }
        }
    }
}
