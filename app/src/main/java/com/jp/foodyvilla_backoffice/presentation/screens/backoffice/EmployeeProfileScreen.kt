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
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import kotlinx.serialization.json.JsonObject

@Composable
internal fun EmployeeProfileScreen(
    session: UserSession?,
    state: AdminUiState,
    onMenu: () -> Unit
) {
    val employee = remember(session, state.dashboardRows) {
        val empId = (session as? UserSession.EmployeeSession)?.empId?.toString()
        state.dashboardRows["employee"]
            .orEmpty()
            .firstOrNull { row -> row["id"].toDisplayText() == empId }
    }
    val outlet = remember(session, state.dashboardRows, employee) {
        val outletId = employee?.get("outlet_id")?.toDisplayText()
            ?: session?.outletId?.toString()
        state.dashboardRows["outlets"]
            .orEmpty()
            .firstOrNull { row -> row["id"].toDisplayText() == outletId }
    }
    val attendance = remember(session, state.dashboardRows) {
        val empId = (session as? UserSession.EmployeeSession)?.empId?.toString()
        state.dashboardRows["attendance"]
            .orEmpty()
            .filter { row -> row["emp_id"].toDisplayText() == empId }
            .sortedByDescending { row -> row["created_at"].toDisplayText("") }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PremiumTopBar(
                title = "Employee Profile",
                subtitle = "Your account, role, outlet, and attendance details",
                icon = Icons.Default.Badge,
                onMenu = onMenu
            )
        }

        item {
            EmployeeProfileHeader(session = session, employee = employee)
        }

        item {
            ProfileSection(title = "Employee Details", icon = Icons.Default.Work) {
                DetailLine("Employee ID", employee?.get("id").toDisplayText((session as? UserSession.EmployeeSession)?.empId?.toString() ?: "-"))
                DetailLine("Name", employee?.get("name").toDisplayText((session as? UserSession.EmployeeSession)?.name ?: "-"))
                DetailLine("Role", employee?.get("role").toDisplayText((session as? UserSession.EmployeeSession)?.role?.name ?: "-"))
                DetailLine("Contact", employee?.get("contact").toDisplayText((session as? UserSession.EmployeeSession)?.contact ?: "-"))
                DetailLine("Joining date", employee?.get("joining_date").toDisplayText().formatDate())
                DetailLine("Status", employee?.get("is_active").toDisplayText("Active"))
            }
        }

        item {
            ProfileSection(title = "Personal Information", icon = Icons.Default.ContactEmergency) {
                DetailLine("Address", employee?.get("address").toDisplayText())
                DetailLine("Aadhar no", employee?.get("aadhar_no").toDisplayText())
                DetailLine("Emergency contact", employee?.get("emergency_contact").toDisplayText())
                DetailLine("Salary", employee?.get("salary").toDisplayText())
            }
        }

        item {
            ProfileSection(title = "Outlet Details", icon = Icons.Default.Business) {
                DetailLine("Outlet ID", outlet?.get("id").toDisplayText(session?.outletId?.toString() ?: "-"))
                DetailLine("Outlet", outlet?.get("name").toDisplayText("Foody Villa"))
                DetailLine("City", outlet?.get("city").toDisplayText())
                DetailLine("Phone", outlet?.get("phone").toDisplayText())
                DetailLine("Email", outlet?.get("email").toDisplayText())
                DetailLine("Address", outlet?.get("address").toDisplayText())
                DetailLine("Hours", "${outlet?.get("opens_at").toDisplayText()} - ${outlet?.get("closes_at").toDisplayText()}")
            }
        }

        item {
            ProfileSection(title = "Attendance", icon = Icons.Default.CalendarMonth) {
                val latest = attendance.firstOrNull()
                DetailLine("Last status", latest?.get("status").toDisplayText())
                DetailLine("Punch in", latest?.get("in_time").toDisplayText().formatTimestamp())
                DetailLine("Punch out", latest?.get("out_time").toDisplayText().formatTimestamp())
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
                            HorizontalDivider(color = SoftLine)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                session.permissions.sorted().forEach { permission ->
                                    StatusPill(permission, RoyalBlue)
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

        if (employee != null) {
            item {
                ProfileSection(title = "Complete Employee Record", icon = Icons.Default.CreditCard) {
                    employee.entries
                        .sortedBy { it.key }
                        .forEach { (key, value) ->
                            DetailLine(key.profileLabel(), value.toDisplayText())
                        }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun EmployeeProfileHeader(session: UserSession?, employee: JsonObject?) {
    val name = employee?.get("name").toDisplayText((session as? UserSession.EmployeeSession)?.name ?: "Employee")
    val role = employee?.get("role").toDisplayText((session as? UserSession.EmployeeSession)?.role?.dbValue ?: "Backoffice")
    val contact = employee?.get("contact").toDisplayText((session as? UserSession.EmployeeSession)?.contact ?: "-")

    PremiumCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(RoyalBlue, Color(0xFF355CFF))))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val image = employee?.firstImageUrl()
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
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE8EEFF), modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp))
                    }
                }
                Text(title, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            }
            HorizontalDivider(color = SoftLine)
            content()
        }
    }
}

private fun employeePunchLocation(employee: JsonObject?): String {
    val lat = employee?.get("punch_lat").toDisplayText()
    val lng = employee?.get("punch_lng").toDisplayText()
    return if (lat == "-" && lng == "-") "-" else "$lat, $lng"
}

private fun String.profileLabel(): String {
    return replace("_", " ").replaceFirstChar { it.uppercase() }
}
