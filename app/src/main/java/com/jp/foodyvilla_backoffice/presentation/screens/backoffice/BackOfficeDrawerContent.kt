//package com.jp.foodyvilla_backoffice.presentation.screens.backoffice
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ExitToApp
//import androidx.compose.material.icons.outlined.Badge
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
//import com.jp.foodyvilla_backoffice.R
//import com.jp.foodyvilla_backoffice.domain.security.UserSession
//
//@Composable
//fun BackOfficeDrawerContent(
//    session: UserSession?,
//    selectedRoute: AdminRoute,
//    onRouteSelected: (AdminRoute) -> Unit,
//    onLogout: () -> Unit
//) {
//    val userName = when (session) {
//        is UserSession.EmployeeSession -> session.name ?: "Employee"
//        is UserSession.OutletSession -> "Outlet Admin"
//        else -> "Guest"
//    }
//    val roleName = when (session) {
//        is UserSession.EmployeeSession -> session.role?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "employee"
//        is UserSession.OutletSession -> session.role.name.lowercase().replaceFirstChar { it.uppercase() }
//        else -> "Backoffice"
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxHeight()
//            .width(320.dp)
//            .background(MaterialTheme.colorScheme.surface)
//    ) {
//        LazyColumn(
//            modifier = Modifier.weight(1f),
//            horizontalAlignment = Alignment.Start
//        ) {
//            item {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(0.dp, 0.dp, 30.dp, 0.dp))
//                        .background(MaterialTheme.colorScheme.secondaryContainer)
//                        .height(200.dp)
//                ) {
//                    Image(
//                        painter = painterResource(R.drawable.logo_new), // Using existing logo
//                        contentDescription = null,
//                        contentScale = ContentScale.Inside,
//                        modifier = Modifier.fillMaxSize().padding(32.dp).alpha(0.1f)
//                    )
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(20.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = userName,
//                                style = MaterialTheme.typography.titleLarge,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text(
//                                text = roleName,
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
//                            )
//
//                            if (session is UserSession.EmployeeSession) {
//                                Spacer(modifier = Modifier.height(8.dp))
//                                AssistChip(
//                                    onClick = { },
//                                    label = { Text("ID: ${session.empId}") },
//                                    leadingIcon = { Icon(Icons.Outlined.Badge, null, Modifier.size(16.dp)) },
//                                    border = null,
//                                    colors = AssistChipDefaults.assistChipColors(
//                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
//                                    )
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            drawerGroups.forEach { (group, routes) ->
//                val allowedRoutes = routes.filter { route ->
//                    session.isRouteAllowed(route, AdminRoute.Dashboard)
//                }
//                if (allowedRoutes.isEmpty()) return@forEach
//
//                item {
//                    Text(
//                        text = group,
//                        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp),
//                        style = MaterialTheme.typography.labelLarge,
//                        color = MaterialTheme.colorScheme.primary,
//                        fontWeight = FontWeight.ExtraBold
//                    )
//                }
//
//                items(allowedRoutes) { route ->
//                    DrawerItemUi(
//                        title = route.title,
//                        icon = route.icon,
//                        isSelected = route == selectedRoute,
//                        onClick = { onRouteSelected(route) }
//                    )
//                }
//            }
//
//            item {
//                Spacer(modifier = Modifier.height(16.dp))
//                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
//                RedLogoutButton(onClick = onLogout)
//                Spacer(modifier = Modifier.height(24.dp))
//            }
//        }
//    }
//}
//
//@Composable
//fun DrawerItemUi(
//    title: String,
//    icon: ImageVector,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    Surface(
//        selected = isSelected,
//        onClick = onClick,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 12.dp, vertical = 2.dp),
//        shape = RoundedCornerShape(12.dp),
//        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
//    ) {
//        Row(
//            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = null,
//                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
//            )
//            Spacer(Modifier.width(16.dp))
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyLarge,
//                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
//                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}
//
//@Composable
//fun RedLogoutButton(onClick: () -> Unit) {
//    Surface(
//        onClick = onClick,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 12.dp, vertical = 12.dp),
//        shape = RoundedCornerShape(12.dp),
//        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
//    ) {
//        Row(
//            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.error
//            )
//            Spacer(Modifier.width(16.dp))
//            Text(
//                text = "Log Out",
//                style = MaterialTheme.typography.bodyLarge,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.error
//            )
//        }
//    }
//}
