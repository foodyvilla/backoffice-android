package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.offers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Offer
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OfferDetailScreen(
    row: JsonObject?,
    onEdit: () -> Unit
) {
    if (row == null) {
        EmptyState("No offer selected", "Go back and select an offer.")
        return
    }

    val offer = remember(row) { row.toModel<Offer>() }

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
                    LargeRecordImage(offer.imgUrl, offer.title)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onEdit,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Text("Edit Offer", modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    DetailLine("Title", offer.title ?: "-")
                    DetailLine("Description", offer.description ?: "-")
                    DetailLine("Linked URL", offer.linkedUrl ?: "-")
                    DetailLine("Expires At", offer.expiresAt ?: "Never")
                    DetailLine("Outlet ID", offer.outletId?.toString() ?: "All")
                    DetailLine("Created At", offer.createdAt ?: "-")
                }
            }
        }
    }
}
