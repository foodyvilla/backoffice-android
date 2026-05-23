package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.offers

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
fun OfferScreen(
    session: UserSession?,
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onCreate: () -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val offers = remember(state.offers, state.searchQuery) {
        state.offers.filter {
            it.title?.contains(state.searchQuery, ignoreCase = true) == true ||
                    it.description?.contains(state.searchQuery, ignoreCase = true) == true ||
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
                placeholder = "Search offers"
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
                    Icon(AdminRoute.Offers.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${offers.size} Offers",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Marketing campaigns and coupons",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (session?.canCreate("offers") == true) {
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
        } else if (offers.isEmpty()) {
            item {
                EmptyState(
                    title = "No offers found",
                    message = "Start a new campaign to boost sales.",
                    actionLabel = "Create Offer",
                    onAction = onCreate
                )
            }
        } else {
            items(offers, key = { it.id ?: it.hashCode() }) { offer ->
                OfferListItem(
                    offer = offer,
                    onClick = { 
                        state.rows.firstOrNull { it["id"].toDisplayText() == offer.id }?.let {
                            onOpenDetails(it)
                        }
                    }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
