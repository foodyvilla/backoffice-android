package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.products

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
import com.jp.foodyvilla_backoffice.data.model.backoffice.ProductCatalog
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun ProductDetailScreen(
    session: UserSession?,
    row: JsonObject?,
    onEdit: () -> Unit
) {
    if (row == null) {
        EmptyState("No product selected", "Go back and select a product.")
        return
    }

    val product = remember(row) { row.toModel<ProductCatalog>() }

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
                    LargeRecordImage(null, product.name)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (session?.canEdit("product_catalog") == true) {
                            Button(
                                onClick = onEdit,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Text("Edit Product", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }

                    DetailLine("Name", product.name)
                    DetailLine("Category", product.category ?: "-")
                    DetailLine("Description", product.description ?: "-")
                    DetailLine("Prep Time", product.prepTime ?: "-")
                    DetailLine("Veg", if (product.isVeg == true) "Yes" else "No")
                    DetailLine("Vegan", if (product.isVegan == true) "Yes" else "No")
                    DetailLine("Bestseller", if (product.isBestseller) "Yes" else "No")
                    DetailLine("Created At", product.createdAt ?: "-")
                }
            }
        }
    }
}
