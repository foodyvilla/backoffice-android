package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.products

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.ProductCatalog
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
fun ProductListItem(
    product: ProductCatalog,
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
            // ProductCatalog usually doesn't have an image directly, 
            // but we can try to find one if the model is extended or use a placeholder
            RecordImage(url = null, label = product.name, size = 84)
            
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category ?: "No category",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (product.isBestseller) StatusPill("Bestseller", StatusGreen)
                    if (product.isVeg == true) StatusPill("Veg", StatusGreen)
                    else if (product.isVeg == false) StatusPill("Non-Veg", StatusRed)
                }
            }
        }
    }
}
