package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.customers

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
import com.jp.foodyvilla_backoffice.data.model.backoffice.User
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun CustomerScreen(
    session: UserSession?,
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onCreate: () -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val customers = remember(state.customers, state.searchQuery) {
        state.customers.filter {
            it.name?.contains(state.searchQuery, ignoreCase = true) == true ||
                    it.phone?.contains(state.searchQuery) == true ||
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
                placeholder = "Search customers"
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
                    Icon(AdminRoute.Customers.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${customers.size} Customers",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Customer profiles and activity",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (session?.canCreate("users") == true) {
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
        } else if (customers.isEmpty()) {
            item {
                EmptyState(
                    title = "No customers found",
                    message = "No customers matched your search.",
                    actionLabel = "Add Customer",
                    onAction = onCreate
                )
            }
        } else {
            items(customers, key = { it.id ?: it.hashCode() }) { customer ->
                CustomerListItem(
                    customer = customer,
                    onClick = { 
                        state.rows.firstOrNull { it["id"].toDisplayText() == customer.id.toString() }?.let {
                            onOpenDetails(it)
                        }
                    }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
