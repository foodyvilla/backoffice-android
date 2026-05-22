package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.layout.*
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
internal fun ProductRecordCard(product: ProductCatalog, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(product.firstImageUrl(), product.name, 84)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(product.category ?: "No category", color = Muted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (product.isBestseller) StatusPill("Bestseller", Success)
                }
            }
        }
    }
}

@Composable
internal fun OutletMenuRecordCard(item: OutletMenuItem, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(item.image?.firstOrNull(), item.productName ?: "Menu item", 84)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.productName ?: "Product", fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.productCategory ?: "No category", color = Muted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("Rs ${item.price}", RoyalBlue)
                    StatusPill(if (item.isAvailable) "Available" else "Hidden", if (item.isAvailable) Success else Muted)
                    if (item.isOutOfStock) StatusPill("Out of stock", Danger)
                }
            }
        }
    }
}

@Composable
internal fun MenuDetailsSection(item: OutletMenuItem) {
    PremiumCard {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Product catalog details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            DetailLine("Product", item.productName ?: "Product")
            DetailLine("Category", item.productCategory ?: "-")
            DetailLine("Description", item.productDescription ?: "-")
            DetailLine("Veg", if (item.productIsVeg == true) "Yes" else "No")
            DetailLine("Prep time", item.productPrepTime ?: "-")
            DetailLine("Menu price", "Rs ${item.price}")
            DetailLine("Discount", item.discount.toString())
            DetailLine("Available", if (item.isAvailable) "Yes" else "No")
            DetailLine("Out of stock", if (item.isOutOfStock) "Yes" else "No")
        }
    }
}

private fun ProductCatalog.firstImageUrl(): String? {
    // ProductCatalog doesn't have a direct image field in this schema, 
    // it's usually in outlet_menu_items.
    return null
}
