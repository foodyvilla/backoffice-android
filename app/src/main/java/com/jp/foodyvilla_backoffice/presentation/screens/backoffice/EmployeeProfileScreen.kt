package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContactEmergency
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import kotlinx.serialization.json.JsonObject

@Composable
internal fun EmployeeProfileScreen(
    session: UserSession?,
    state: AdminUiState
) {
    val employee = remember(session, state.dashboardRows) {
        val empId = (session as? UserSession.EmployeeSession)?.empId?.toString()
        state.dashboardRows["employee"]
            .orEmpty()
            .firstOrNull { row -> row["id"].toDisplayText() == empId }
            ?.toModel<Employee>()
    }
    val outlet = remember(session, state.dashboardRows, employee) {
        val outletId = employee?.outletId?.toString()
            ?: session?.outletId?.toString()
        state.dashboardRows["outlets"]
            .orEmpty()
            .firstOrNull { row -> row["id"].toDisplayText() == outletId }
            ?.toModel<Outlet>()
    }
    val attendance = remember(session, state.dashboardRows) {
        val empId = (session as? UserSession.EmployeeSession)?.empId?.toString()
        state.dashboardRows["attendance"]
            .orEmpty()
            .filter { row -> row["emp_id"].toDisplayText() == empId }
            .map { it.toModel<Attendance>() }
            .sortedByDescending { it.createdAt ?: "" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EmployeeProfileHeader(session = session, employee = employee)
        }

        item {
            ProfileSection(title = "Employee Details", icon = Icons.Default.Work) {
                DetailLine("Employee ID", employee?.id?.toString() ?: (session as? UserSession.EmployeeSession)?.empId?.toString() ?: "-")
                DetailLine("Name", employee?.name ?: (session as? UserSession.EmployeeSession)?.name ?: "-")
                DetailLine("Role", employee?.role ?: (session as? UserSession.EmployeeSession)?.role?.name ?: "-")
                DetailLine("Contact", employee?.contact ?: (session as? UserSession.EmployeeSession)?.contact ?: "-")
                DetailLine("Joining date", employee?.joiningDate?.formatDate() ?: "-")
                DetailLine("Status", if (employee?.isActive == true) "Active" else "Inactive")
            }
        }

        item {
            ProfileSection(title = "Personal Information", icon = Icons.Default.ContactEmergency) {
                DetailLine("Address", employee?.address ?: "-")
                DetailLine("Aadhar no", employee?.aadharNo ?: "-")
                DetailLine("Emergency contact", employee?.emergencyContact ?: "-")
                DetailLine("Salary", employee?.salary?.toString() ?: "-")
            }
        }

        item {
            ProfileSection(title = "Outlet Details", icon = Icons.Default.Business) {
                DetailLine("Outlet ID", outlet?.id?.toString() ?: session?.outletId?.toString() ?: "-")
                DetailLine("Outlet", outlet?.name ?: "Foody Villa")
                DetailLine("City", outlet?.city ?: "-")
                DetailLine("Phone", outlet?.phone ?: "-")
                DetailLine("Email", outlet?.email ?: "-")
                DetailLine("Address", outlet?.address ?: "-")
                DetailLine("Hours", "${outlet?.opensAt ?: "-"} - ${outlet?.closesAt ?: "-"}")
            }
        }

        item {
            ProfileSection(title = "Attendance", icon = Icons.Default.CalendarMonth) {
                val latest = attendance.firstOrNull()
                DetailLine("Last status", latest?.status ?: "-")
                DetailLine("Punch in", latest?.inTime?.formatTimestamp() ?: "-")
                DetailLine("Punch out", latest?.outTime?.formatTimestamp() ?: "-")
                DetailLine("Punch location", employeePunchLocation(employee))
                DetailLine("Visible records", attendance.size.toString())
            }
        }

        item {
            ProfileSection(title = "Access", icon = Icons.Default.Key) {
                when (session) {
                    is UserSession.EmployeeSession -> {
                        DetailLine("Designation ID", session.designationId?.toString() ?: "-")
                        DetailLine("Role type", session.role?.dbValue ?: "-")
                        DetailLine("Permissions", session.permissions.size.toString())
                        if (session.permissions.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                session.permissions.sorted().forEach { permission ->
                                    StatusPill(permission, MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    is UserSession.OutletSession -> {
                        DetailLine("Account", session.username)
                        DetailLine("Role type", session.role.dbValue)
                    }

                    null -> DetailLine("Session", "Not available")
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun EmployeeProfileHeader(session: UserSession?, employee: Employee?) {
    val name = employee?.name ?: (session as? UserSession.EmployeeSession)?.name ?: "Employee"
    val role = employee?.role ?: (session as? UserSession.EmployeeSession)?.role?.dbValue ?: "Backoffice"
    val contact = employee?.contact ?: (session as? UserSession.EmployeeSession)?.contact ?: "-"

    PremiumCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val image = employee?.profileImg
                if (image.isNullOrBlank()) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = .18f), modifier = Modifier.size(78.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = name.take(1).uppercase().ifBlank { "E" },
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            )
                        }
                    }
                } else {
                    RecordImage(image, name, 78)
                }

                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    StatusPill(role.replaceFirstChar { it.uppercase() }, Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White.copy(alpha = .82f), modifier = Modifier.size(16.dp))
                        Text(contact, color = Color.White.copy(alpha = .86f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    PremiumCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            content()
        }
    }
}

private fun employeePunchLocation(employee: Employee?): String {
    val lat = employee?.punchLat
    val lng = employee?.punchLng
    return if (lat == null && lng == null) "-" else "$lat, $lng"
}
