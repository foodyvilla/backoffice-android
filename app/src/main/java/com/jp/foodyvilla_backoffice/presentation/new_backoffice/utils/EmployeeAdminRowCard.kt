package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeAdminUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeRole
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeAdminRowCard(
    record: EmployeeAdminUiModel,
    onModifyClick: () -> Unit,
    onPurgeClick: () -> Unit,
    onProfileImageClick: () -> Unit
) {
    ElevatedCard(
        onClick = onModifyClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Profile Photo + Name & Outlet + Role Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile Avatar (Clickable to open zoomable preview)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = record.profileImgUrl.isNotBlank()) {
                            onProfileImageClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (record.profileImgUrl.isNotBlank()) {
                        AsyncImage(
                            model = record.profileImgUrl,
                            contentDescription = "${record.name}'s Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Name and Branch Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Branch: ${record.outletName.ifBlank { "Unassigned" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Role Badge
                RoleIndicatorBadge(role = record.role)
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Details Row: Contact & Shift Hours
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contact Number
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = "CONTACT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = record.contact.ifBlank { "N/A" },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Shift Hours
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SHIFT HOURS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        val shiftText = formatShiftDisplay(record.punchInTime, record.punchOutTime)
                        Text(
                            text = shiftText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Bottom Row: Status Badge + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active/Inactive Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = if (record.isActive) "ACTIVE STAFF" else "DEACTIVATED"
                    val statusColor = if (record.isActive) AppTheme.colors.success else MaterialTheme.colorScheme.error
                    val icon = if (record.isActive) Icons.Default.Badge else Icons.Default.Block

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Edit & Delete Action Buttons
                Row {
                    IconButton(onClick = onModifyClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Staff Configuration",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onPurgeClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Purge Roster Row Log",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleIndicatorBadge(role: EmployeeRole) {
    val extendedColors = AppTheme.colors
    val (contentColor, containerColor, icon) = when (role) {
        EmployeeRole.OWNER -> Triple(extendedColors.info, extendedColors.infoContainer, Icons.Default.Security)
        EmployeeRole.HEAD -> Triple(extendedColors.warning, extendedColors.warningContainer, Icons.Default.Engineering)
        EmployeeRole.CHEF -> Triple(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer, Icons.Default.Restaurant)
        EmployeeRole.EMPLOYEE -> Triple(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer, Icons.Default.Person)
    }

    Surface(color = containerColor, shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Text(
                text = role.name, color = contentColor,
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatShiftDisplay(punchIn: String, punchOut: String): String {
    if (punchIn.isBlank() && punchOut.isBlank()) return "Outlet Default"
    val inFormatted = formatTimeStrDisplay(punchIn)
    val outFormatted = formatTimeStrDisplay(punchOut)
    return "$inFormatted - $outFormatted"
}

private fun formatTimeStrDisplay(timeStr: String): String {
    if (timeStr.isBlank()) return "Default"
    return try {
        val clean = timeStr.split("+")[0].split("-")[0].replace("Z", "").trim()
        val parts = clean.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return timeStr
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val localTime = LocalTime.of(hour, minute)
        localTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
    } catch (e: Exception) {
        timeStr
    }
}
