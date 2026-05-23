package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.outlets

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
import com.jp.foodyvilla_backoffice.data.model.backoffice.Outlet
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OutletDetailScreen(
    session: UserSession?,
    row: JsonObject?,
    onEdit: () -> Unit
) {
    if (row == null) {
        EmptyState("No outlet selected", "Go back and select an outlet.")
        return
    }

    val outlet = remember(row) { row.toModel<Outlet>() }

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
                    LargeRecordImage(outlet.logoUrl, outlet.name)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (session?.canEdit("outlets") == true) {
                            Button(
                                onClick = onEdit,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Text("Edit Outlet", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        StatusPill(if (outlet.isActive == true) "Active" else "Inactive", if (outlet.isActive == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    DetailLine("Name", outlet.name)
                    DetailLine("City", outlet.city ?: "-")
                    DetailLine("Phone", outlet.phone ?: "-")
                    DetailLine("Email", outlet.email ?: "-")
                    DetailLine("Address", outlet.address ?: "-")
                    DetailLine("Radius (KM)", outlet.radiusKm.toString())
                    DetailLine("Operating Hours", "${outlet.opensAt ?: "-"} to ${outlet.closesAt ?: "-"}")
                }
            }
        }
    }
}
