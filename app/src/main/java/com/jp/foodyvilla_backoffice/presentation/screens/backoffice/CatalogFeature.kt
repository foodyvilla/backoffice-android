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
import kotlinx.serialization.json.JsonObject

@Composable
internal fun ProductRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(), row["name"].toDisplayText("Product"), 84)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(row["name"].toDisplayText("Untitled product"), fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row["category"].toDisplayText("No category"), color = Muted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("Rs ${row["price"].toDisplayText("0")}", RoyalBlue)
                    if (row["is_bestseller"].toDisplayText().equals("true", true)) StatusPill("Bestseller", Success)
                }
            }
        }
    }
}

@Composable
internal fun OutletMenuRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(), row["product_name"].toDisplayText("Menu item"), 84)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(row["product_name"].toDisplayText("Product"), fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row["product_category"].toDisplayText("No category"), color = Muted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("Rs ${row["price"].toDisplayText("0")}", RoyalBlue)
                    StatusPill(if (row["is_available"].toDisplayText().equals("true", true)) "Available" else "Hidden", if (row["is_available"].toDisplayText().equals("true", true)) Success else Muted)
                    if (row["is_out_of_stock"].toDisplayText().equals("true", true)) StatusPill("Out of stock", Danger)
                }
            }
        }
    }
}

@Composable
internal fun MenuDetailsSection(row: JsonObject) {
    PremiumCard {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Product catalog details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            DetailLine("Product", row["product_name"].toDisplayText("Product"))
            DetailLine("Category", row["product_category"].toDisplayText())
            DetailLine("Description", row["product_description"].toDisplayText())
            DetailLine("Veg", row["product_is_veg"].toDisplayText())
            DetailLine("Prep time", row["product_prep_time"].toDisplayText())
            DetailLine("Menu price", "Rs ${row["price"].toDisplayText("0")}")
            DetailLine("Discount", row["discount"].toDisplayText("0"))
            DetailLine("Available", row["is_available"].toDisplayText())
            DetailLine("Out of stock", row["is_out_of_stock"].toDisplayText())
        }
    }
}
