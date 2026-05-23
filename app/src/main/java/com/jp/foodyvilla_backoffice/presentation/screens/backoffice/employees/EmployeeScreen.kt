package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.employees

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
fun EmployeeScreen(
    session: UserSession?,
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onCreate: () -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val employees = remember(state.employees, state.searchQuery) {
        state.employees.filter {
            it.name.contains(state.searchQuery, ignoreCase = true) ||
                    it.contact?.contains(state.searchQuery) == true ||
                    it.role?.contains(state.searchQuery, ignoreCase = true) == true ||
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
                placeholder = "Search employees"
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
                    Icon(AdminRoute.Employees.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${employees.size} Employees",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Team and roles",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (session?.canCreate("employee") == true) {
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
        } else if (employees.isEmpty()) {
            item {
                EmptyState(
                    title = "No employees found",
                    message = "Add your team members here.",
                    actionLabel = if (session?.canCreate("employee") == true) "Add Employee" else null,
                    onAction = if (session?.canCreate("employee") == true) onCreate else null
                )
            }
        } else {
            items(employees, key = { it.id ?: it.hashCode() }) { employee ->
                EmployeeListItem(
                    employee = employee,
                    onClick = { 
                        state.rows.firstOrNull { it["id"].toDisplayText() == employee.id.toString() }?.let {
                            onOpenDetails(it)
                        }
                    }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
