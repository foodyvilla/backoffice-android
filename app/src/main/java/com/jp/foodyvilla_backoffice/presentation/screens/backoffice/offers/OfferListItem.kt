package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.offers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Offer
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.Muted
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.PremiumCard
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.RecordImage

@Composable
fun OfferListItem(
    offer: Offer,
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
                url = offer.imgUrl,
                label = offer.title ?: "Offer",
                size = 86
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = offer.title ?: "No Title",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = offer.description ?: "-",
                    color = Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
                offer.expiresAt?.let {
                    Text(
                        text = "Expires: $it",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
