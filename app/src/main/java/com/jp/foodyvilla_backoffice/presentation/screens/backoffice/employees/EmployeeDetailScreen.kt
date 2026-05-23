package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.employees

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
import com.jp.foodyvilla_backoffice.data.model.backoffice.Employee
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*
import kotlinx.serialization.json.JsonObject

@Composable
fun EmployeeDetailScreen(
    session: UserSession?,
    row: JsonObject?,
    onEdit: () -> Unit
) {
    if (row == null) {
        EmptyState("No employee selected", "Go back and select an employee.")
        return
    }

    val employee = remember(row) { row.toModel<Employee>() }

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
                    LargeRecordImage(employee.profileImg, employee.name)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (session?.canEdit("employee") == true) {
                            Button(
                                onClick = onEdit,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Text("Edit Employee", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        StatusPill(if (employee.isActive) "Active" else "Inactive", if (employee.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }

                    DetailLine("Name", employee.name)
                    DetailLine("Role", employee.role ?: "-")
                    DetailLine("Contact", employee.contact ?: "-")
                    DetailLine("Aadhar No", employee.aadharNo ?: "-")
                    DetailLine("Salary", employee.salary?.toString() ?: "-")
                    DetailLine("Joining Date", employee.joiningDate ?: "-")
                    DetailLine("Outlet ID", employee.outletId?.toString() ?: "-")
                    DetailLine("Address", employee.address ?: "-")
                }
            }
        }
    }
}
