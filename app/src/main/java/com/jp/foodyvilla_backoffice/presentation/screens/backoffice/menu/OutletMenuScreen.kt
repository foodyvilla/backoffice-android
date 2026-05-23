package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OutletMenuScreen(
    session: UserSession?,
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onCreate: () -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val menuItems = remember(state.outletMenuItems, state.searchQuery) {
        state.outletMenuItems.filter {
            it.productName?.contains(state.searchQuery, ignoreCase = true) == true ||
                    it.productCategory?.contains(state.searchQuery, ignoreCase = true) == true ||
                    state.searchQuery.isBlank()
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SearchAndFilterBar(
                query = state.searchQuery,
                onQueryChange = onSearch,
                placeholder = "Search menu items"
            )
        }

        item {
            PremiumCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(AdminRoute.OutletMenu.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${menuItems.size} Menu Items",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Local pricing and availability",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (session?.canCreate("outlet_menu_items") == true) {
                        Button(
                            onClick = onCreate,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("New")
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary) }
        } else if (menuItems.isEmpty()) {
            item {
                EmptyState(
                    title = "No menu items found",
                    message = "Add products to this outlet's menu.",
                    actionLabel = "Add to Menu",
                    onAction = onCreate
                )
            }
        } else {
            items(menuItems, key = { it.id ?: it.hashCode() }) { item ->
                OutletMenuListItem(
                    item = item,
                    onClick = { 
                        state.rows.firstOrNull { it["id"].toDisplayText() == item.id.toString() }?.let {
                            onOpenDetails(it)
                        }
                    }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
