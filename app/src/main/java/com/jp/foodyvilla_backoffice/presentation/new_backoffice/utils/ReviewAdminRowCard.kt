package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AdminReviewType
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ReviewAdminUiModel
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme

@Composable
fun ReviewAdminRowCard(
    record: ReviewAdminUiModel,
    onModifyClick: () -> Unit,
    onPurgeClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = record.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Rating score: " + "⭐ ".repeat(record.rating), color = Color(0xFFFFB300), style = MaterialTheme.typography.bodyMedium)
                }
                ReviewTypeBadge(type = record.reviewType)
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            if (record.title.isNotBlank()) {
                Text(text = record.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(text = record.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (record.images.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(record.images) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Review image",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (record.orderId.isNotBlank()) Text(text = "Order: ${record.orderId.take(8)}...", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                if (record.menuItemId.isNotBlank()) Text(text = "Item Ref: ${record.menuItemId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                if (record.outletId.isNotBlank()) Text(text = "Outlet Ref: ${record.outletId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onModifyClick) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Feed Entry", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onPurgeClick) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Purge Review entry row", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ReviewTypeBadge(type: AdminReviewType) {
    val extendedColors = AppTheme.colors
    val (containerColor, contentColor, icon) = when (type) {
        AdminReviewType.ORDER -> Triple(extendedColors.infoContainer, extendedColors.info, Icons.Default.ShoppingCart)
        AdminReviewType.OUTLET -> Triple(extendedColors.successContainer, extendedColors.success, Icons.Default.Storefront)
        AdminReviewType.PRODUCT -> Triple(extendedColors.warningContainer, extendedColors.warning, Icons.Default.Fastfood)
    }

    Surface(color = containerColor, shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Text(
                text = type.name, color = contentColor,
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
            )
        }
    }
}