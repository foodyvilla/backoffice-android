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
import com.jp.foodyvilla_backoffice.data.model.backoffice.OutletMenuItem
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun OutletMenuScreen(
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onCreate: () -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val menuItems = remember(state.rows, state.searchQuery) {
        state.rows.map { it.toModel<OutletMenuItem>() }.filter {
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
                    Icon(AdminRoute.OutletMenu.icon, contentDescription = null, tint = RoyalBlue)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${menuItems.size} Menu Items",
                            color = Ink,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Local pricing and availability",
                            color = Muted
                        )
                    }
                    Button(
                        onClick = onCreate,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("New")
                    }
                }
            }
        }

        if (state.isLoading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue) }
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
            items(state.rows) { row ->
                val item = row.toModel<OutletMenuItem>()
                OutletMenuListItem(
                    item = item,
                    onClick = { onOpenDetails(row) }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
