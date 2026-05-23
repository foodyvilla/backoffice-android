package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.banners

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Banner
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.Muted
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.PremiumCard
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.RecordImage

@Composable
fun BannerListItem(
    banner: Banner,
    onClick: () -> Unit
) {
    PremiumCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecordImage(
                url = banner.imgUrl,
                label = banner.title ?: "Banner",
                size = 86
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = banner.title ?: "No Title",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Order: ${banner.displayOrder}",
                    color = Muted,
                    fontSize = 14.sp
                )
                Text(
                    text = banner.createdAt ?: "-",
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp
                )
            }
        }
    }
}
