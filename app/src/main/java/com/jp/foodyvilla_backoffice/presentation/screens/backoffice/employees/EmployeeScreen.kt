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
import com.jp.foodyvilla_backoffice.data.model.backoffice.Employee
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun EmployeeScreen(
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onCreate: () -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val employees = remember(state.rows, state.searchQuery) {
        state.rows.map { it.toModel<Employee>() }.filter {
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
                    Icon(AdminRoute.Employees.icon, contentDescription = null, tint = RoyalBlue)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${employees.size} Employees",
                            color = Ink,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Team and roles",
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
        } else if (employees.isEmpty()) {
            item {
                EmptyState(
                    title = "No employees found",
                    message = "Add your team members here.",
                    actionLabel = "Add Employee",
                    onAction = onCreate
                )
            }
        } else {
            items(state.rows) { row ->
                val employee = row.toModel<Employee>()
                EmployeeListItem(
                    employee = employee,
                    onClick = { onOpenDetails(row) }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
