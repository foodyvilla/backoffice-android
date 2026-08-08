package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AttendanceUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeResponse
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletResponse
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.EmployeeProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeProfileNewScreen(
    viewModel: EmployeeProfileViewModel = koinViewModel(),
    onMenuClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    ModernProfileHeader(state.employee)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val latest = state.attendanceHistory.firstOrNull()
                        QuickStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Last Status",
                            value = latest?.status?.name ?: "No record",
                            icon = Icons.Default.Timer,
                            color = MaterialTheme.colorScheme.primary
                        )
                        QuickStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Recent Logs",
                            value = state.attendanceHistory.size.toString(),
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
                        ModernDetailLine("Employee ID", state.employee?.id?.toString() ?: "-", Icons.Default.Badge)
                        ModernDetailLine("Designation", state.employee?.role?.replaceFirstChar { it.uppercase() } ?: "-", Icons.Default.AssignmentInd)
                        ModernDetailLine("Salary", state.employee?.salary?.let { "₹$it" } ?: "-", Icons.Default.Payments)
                    }
                }

                item {
                    ModernProfileSection(
                        title = "Workplace",
                        icon = Icons.Default.Storefront
                    ) {
                        ModernDetailLine("Outlet Name", state.outlet?.name ?: "Foody Villa", Icons.Default.Store)
                        ModernDetailLine("Location", state.outlet?.city ?: "-", Icons.Default.LocationOn)
                        ModernDetailLine("Work Hours", "${state.outlet?.opens_at ?: "-"} to ${state.outlet?.closes_at ?: "-"}", Icons.Default.Schedule)
                    }
                }

                if (state.error != null) {
                    item {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernProfileHeader(employee: EmployeeResponse?) {
    val name = employee?.name ?: "Employee"
    val role = (employee?.role ?: "Staff").uppercase()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
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
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = name.take(1).uppercase(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
