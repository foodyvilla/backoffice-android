package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jp.foodyvilla_backoffice.data.model.backoffice.*
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import kotlinx.serialization.json.JsonObject

// Utility function to clean image URL (local copy to avoid visibility issues)
fun String?.cleanImageUrl(): String? {
    if (this == null) return null
    return if (this.startsWith("http")) this else "https://jpfoodyvilla.com/storage/v1/object/public/$this"
}

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                ModernProfileHeader(session, employee)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val latest = attendance.firstOrNull()
                    QuickStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Last Status",
                        value = latest?.status ?: "No record",
                        icon = Icons.Default.Timer,
                        color = MaterialTheme.colorScheme.primary
                    )
                    QuickStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Shifts",
                        value = attendance.size.toString(),
                        icon = Icons.Default.EventAvailable,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            item {
                ModernProfileSection(
                    title = "Professional Info",
                    icon = Icons.Default.WorkOutline
                ) {
                    ModernDetailLine("Employee ID", employee?.id?.toString() ?: (session as? UserSession.EmployeeSession)?.empId?.toString() ?: "-", Icons.Default.Badge)
                    ModernDetailLine("Designation", employee?.role?.replaceFirstChar { it.uppercase() } ?: (session as? UserSession.EmployeeSession)?.role?.name ?: "-", Icons.Default.AssignmentInd)
                    ModernDetailLine("Joining Date", employee?.joiningDate?.formatDate() ?: "-", Icons.Default.CalendarToday)
                    ModernDetailLine("Employment", if (employee?.isActive == true) "Full Time" else "Inactive", Icons.Default.VerifiedUser)
                }
            }

            item {
                ModernProfileSection(
                    title = "Personal & Contact",
                    icon = Icons.Default.PersonOutline
                ) {
                    ModernDetailLine("Phone", employee?.contact ?: (session as? UserSession.EmployeeSession)?.contact ?: "-", Icons.Default.Phone)
                    ModernDetailLine("Aadhar", employee?.aadharNo?.let { "**** **** ${it.takeLast(4)}" } ?: "-", Icons.Default.CreditCard)
                    ModernDetailLine("Emergency", employee?.emergencyContact ?: "-", Icons.Default.ContactEmergency)
                    ModernDetailLine("Address", employee?.address ?: "-", Icons.Default.HomeWork)
                }
            }

            item {
                ModernProfileSection(
                    title = "Workplace",
                    icon = Icons.Default.Storefront
                ) {
                    ModernDetailLine("Outlet Name", outlet?.name ?: "Foody Villa", Icons.Default.Store)
                    ModernDetailLine("Location", outlet?.city ?: "-", Icons.Default.LocationOn)
                    ModernDetailLine("Work Hours", "${outlet?.opensAt ?: "-"} to ${outlet?.closesAt ?: "-"}", Icons.Default.Schedule)
                    ModernDetailLine("Punch Location", employeePunchLocation(employee), Icons.Default.GpsFixed)
                }
            }

            item {
                if (session is UserSession.EmployeeSession && session.permissions.isNotEmpty()) {
                    ModernProfileSection(
                        title = "System Access",
                        icon = Icons.Default.Security
                    ) {
                        Text(
                            "Granted Permissions",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FlowRow(
                            mainAxisSpacing = 8.dp,
                            crossAxisSpacing = 8.dp
                        ) {
                            session.permissions.forEach { perm ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(perm) },
                                    border = null,
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernProfileHeader(session: UserSession?, employee: Employee?) {
    val name = employee?.name ?: (session as? UserSession.EmployeeSession)?.name ?: "Employee"
    val role = (employee?.role ?: (session as? UserSession.EmployeeSession)?.role?.dbValue ?: "Staff").uppercase()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Background Gradient & Shape
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
        )

        // Profile Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp
                ) {
                    if (employee?.profileImg.isNullOrBlank()) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = name.take(1).uppercase(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(employee?.profileImg?.cleanImageUrl())
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            
            Text(
                name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                role,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModernProfileSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ModernDetailLine(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// Simple FlowRow equivalent if not using foundation layout
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    mainAxisSpacing: androidx.compose.ui.unit.Dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = { content() }
    )
}


private fun employeePunchLocation(employee: Employee?): String {
    val lat = employee?.punchLat
    val lng = employee?.punchLng
    return if (lat == null && lng == null) "-" else "$lat, $lng"
}
