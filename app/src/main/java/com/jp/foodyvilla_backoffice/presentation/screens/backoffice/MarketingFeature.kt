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
import kotlinx.serialization.json.JsonObject

@Composable
internal fun MediaRecordCard(table: AdminTable, row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val (title, description, imageUrl) = when (table.name) {
                "banners" -> {
                    val banner = row.toModel<Banner>()
                    Triple(banner.title, banner.createdAt ?: "-", banner.imgUrl)
                }
                "offers" -> {
                    val offer = row.toModel<Offer>()
                    Triple(offer.title, offer.description, offer.imgUrl)
                }
                else -> Triple(table.title, row["created_at"].toDisplayText(), row.firstImageUrl())
            }
            
            RecordImage(imageUrl, title ?: table.title, 86)
            Column(Modifier.weight(1f)) {
                Text(title ?: table.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(description ?: "-", color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
