package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.reviews

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
import com.jp.foodyvilla_backoffice.data.model.backoffice.Review
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun ReviewDetailScreen(
    session: UserSession?,
    row: JsonObject?,
    onEdit: () -> Unit
) {
    if (row == null) {
        EmptyState("No review selected", "Go back and select a review.")
        return
    }

    val review = remember(row) { row.toModel<Review>() }

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
                    LargeRecordImage(review.imgUrl.firstUrlOrNull(), review.title)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (session?.canEdit("reviews") == true) {
                            Button(
                                onClick = onEdit,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Text("Edit Review", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        StatusPill("${review.rating} Stars", MaterialTheme.colorScheme.tertiary)
                    }

                    DetailLine("Title", review.title ?: "-")
                    DetailLine("Type", review.reviewType)
                    DetailLine("Description", review.description ?: "-")
                    DetailLine("Customer ID", review.customerId?.toString() ?: "-")
                    DetailLine("Order ID", review.orderId ?: "-")
                    DetailLine("Menu Item ID", review.menuItemId?.toString() ?: "-")
                    DetailLine("Created At", review.createdAt ?: "-")
                }
            }
        }
    }
}
