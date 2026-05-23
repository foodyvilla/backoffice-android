package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.attendance

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
fun PunchReportScreen(
    session: UserSession?,
    state: AdminUiState,
    onSearch: (String) -> Unit,
    onDateChange: (String?) -> Unit,
    onOutletChange: (String?) -> Unit,
    onOpenDetails: (JsonObject) -> Unit
) {
    val records = remember(state.attendance, state.attendanceSearchQuery, state.attendanceDateFilter, state.attendanceOutletFilter) {
        state.attendance.filter { model ->
            val matchesSearch = state.attendanceSearchQuery.isBlank() || 
                    model.employee?.name?.contains(state.attendanceSearchQuery, true) == true ||
                    model.employee?.contact?.contains(state.attendanceSearchQuery) == true
            
            val matchesDate = state.attendanceDateFilter == null || 
                    model.createdAt?.startsWith(state.attendanceDateFilter!!) == true
            
            val matchesOutlet = state.attendanceOutletFilter == null ||
                    model.employee?.outletId?.toString() == state.attendanceOutletFilter
            
            matchesSearch && matchesDate && matchesOutlet
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SearchAndFilterBar(
                query = state.attendanceSearchQuery,
                onQueryChange = onSearch,
                placeholder = "Search employee name or contact"
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePickerField(
                    label = "Date",
                    value = state.attendanceDateFilter,
                    onValueChange = onDateChange,
                    modifier = Modifier.weight(1f)
                )
                if (session?.isOwner() == true) {
                    OutlinedTextField(
                        value = state.attendanceOutletFilter ?: "",
                        onValueChange = { onOutletChange(it.ifBlank { null }) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Outlet ID") },
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                }
            }
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
                    Icon(AdminRoute.PunchReport.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${records.size} Records",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Employee attendance history",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary) }
        } else if (records.isEmpty()) {
            item {
                EmptyState(
                    title = "No records found",
                    message = "Try changing your filters."
                )
            }
        } else {
            items(records, key = { it.id ?: it.hashCode() }) { attendance ->
                PunchReportListItem(
                    attendance = attendance,
                    onClick = { 
                        state.rows.firstOrNull { it["id"].toDisplayText() == attendance.id.toString() }?.let {
                            onOpenDetails(it)
                        }
                    }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
