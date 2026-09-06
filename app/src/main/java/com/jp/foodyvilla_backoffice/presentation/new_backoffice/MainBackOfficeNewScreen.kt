package com.jp.foodyvilla_backoffice.presentation.new_backoffice


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.attendance.EmployeeAdminConsoleScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.AttendanceAdminConsoleScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.CustomerDirectoryScreen

import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.EmployeePunchReportScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.EmployeeProfileNewScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.MarketingTabsDashboardScreen

import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.ProductCatalogManagementScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.ProductCategoryManagementScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.BackOfficeRoute
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.ScreenDestinations

import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.SpecificOutletMenuHandlingScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.AnalyticsDashboardScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.OrderHistoryScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.OutletListDirectoryScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.PaymentAdminConsoleScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.ReviewAdminConsoleScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.RestaurantTableManagementScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.TableManagementScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceAdminViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.CustomerManagementViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.EmployeeProfileViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.MarketingViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.OutletManagementViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.ProductCatalogViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.RestaurantTableManagementViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.UnifiedOrderControlViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel

import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.getDrawerItemsForSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.EmployeeDashboardScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBackOfficeNavigationScreen(
    loginViewModel: LoginViewModel,
    marketingViewModel: MarketingViewModel,
    unifiedViewModel: UnifiedOrderControlViewModel,
    outletMenuManagementViewModel: OutletManagementViewModel,
    navController: NavController,
    productCatalogViewModel: ProductCatalogViewModel,
    customerManagementViewModel: CustomerManagementViewModel,
    attendanceViewModel: AttendanceViewModel,
    attendanceAdminViewModel: AttendanceAdminViewModel,
    tableAdminViewModel: RestaurantTableManagementViewModel = koinViewModel(),
    profileViewModel: EmployeeProfileViewModel = koinViewModel()
) {
    val userSession = loginViewModel.currentSession.collectAsStateWithLifecycle().value

    val drawerItems = remember(userSession) {
        getDrawerItemsForSession(userSession)
    }

    var currentRoute by remember {
        mutableStateOf<BackOfficeRoute>(
            BackOfficeRoute.Dashboard
        )
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,

        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.85f),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
            ) {
                // Drawer Header with User Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 32.dp, horizontal = 20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "FoodyVilla",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Back Office Portal",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        userSession?.let { session ->
                            val name = when (val s = session) {
                                is UserSession.EmployeeSession -> s.name ?: "Staff Member"
                                is UserSession.OutletSession -> s.username
                            }
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = session.role().uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(drawerItems) { item ->
                        NavigationDrawerItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (item.route == BackOfficeRoute.Logout) {
                                    currentRoute = BackOfficeRoute.Dashboard
                                    loginViewModel.logout()
                                } else {
                                    currentRoute = item.route
                                }
                                scope.launch { drawerState.close() }
                            },
                            label = {
                                Text(
                                    text = item.name,
                                    fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name,
                                    tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            shape = MaterialTheme.shapes.medium,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                
                Text(
                    text = "v1.0.4-stable",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                )
            }
        }
    ) {

        Scaffold { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                    Unit
                }

                when (currentRoute) {

                    BackOfficeRoute.Dashboard -> {

                        EmployeeDashboardScreen(viewModel = attendanceViewModel, onNavigateBack = {
                            scope.launch {
                                drawerState.open()
                            }
                        })
                    }

                    BackOfficeRoute.Orders -> {
                        NewOrdersListScreen(
                            viewModel = unifiedViewModel,
                            navController = navController,
                            onMenuClick = onMenuClick
                        ) {
                            currentRoute = BackOfficeRoute.OutletMenu
                        }
                    }

                    BackOfficeRoute.TableOrdersList -> {
                        NewTableOrdersListScreen(
                            viewModel = unifiedViewModel,
                            navController = navController,
                            onMenuClick = onMenuClick
                        )
                    }

                    BackOfficeRoute.OutletMenu -> {

                        NewCreateOrderMenuSelectionScreen(
                            viewModel = unifiedViewModel,
                            onMenuClick = onMenuClick
                        ) {
                            navController.navigate(ScreenDestinations.CreateOrder)

                        }
                    }

                    BackOfficeRoute.Outlet -> {

                        OutletListDirectoryScreen(
                            viewModel = outletMenuManagementViewModel,
                            onNavigateToOutletFormAdd = {
                                navController.navigate(ScreenDestinations.AddOutlet)
                            },
                            onNavigateToOutletFormEdit = { targetId ->
                                navController.navigate(ScreenDestinations.EditOutlet(id = targetId))
                            },
                            onOutletNavigateLambda = { activeId, branchName ->
                                navController.navigate(ScreenDestinations.OutletMenu(outletId = activeId, outletName = branchName))
                            },
                            onMenuClick = onMenuClick
                        )

                    }

                    BackOfficeRoute.Products -> {

                        ProductCatalogManagementScreen(
                            viewModel = productCatalogViewModel,
                            onMenuClick = onMenuClick
                        )
                    }

                    BackOfficeRoute.Categories -> {

                        ProductCategoryManagementScreen(
                            viewModel = productCatalogViewModel,
                            onMenuClick = onMenuClick
                        )
                    }

                    BackOfficeRoute.Tables -> {

                        RestaurantTableManagementScreen(
                            viewModel = tableAdminViewModel,
                            onMenuClick = onMenuClick
                        )
                    }

                    BackOfficeRoute.Customers -> {

                        CustomerDirectoryScreen(
                            viewModel = customerManagementViewModel,
                            onMenuClick = onMenuClick
                        ) { id, phone ->
                            navController.navigate(ScreenDestinations.Customer(id = id, phone = phone))
                        }
                    }

                    BackOfficeRoute.Payments -> {
                        PaymentAdminConsoleScreen(onMenuClick = onMenuClick)
                    }

                    BackOfficeRoute.Employees -> {

                        EmployeeAdminConsoleScreen(onMenuClick = onMenuClick)
                    }

                    BackOfficeRoute.Attendance -> {


                        EmployeePunchReportScreen(viewModel = attendanceViewModel, onNavigateBack = {
                            scope.launch {
                                drawerState.open()
                            }
                        })

                    }

                    BackOfficeRoute.PunchReports -> {
                        AttendanceAdminConsoleScreen(
                            viewModel = attendanceAdminViewModel,
                            onMenuClick = onMenuClick
                        )

                    }

                    BackOfficeRoute.Analytics -> {
                        AnalyticsDashboardScreen(onMenuClick = onMenuClick)
                    }

                    BackOfficeRoute.Offers -> {

                        MarketingTabsDashboardScreen(
                            viewModel = marketingViewModel,
                            onNavigateToBannerForm = {
                                if (it == null) {
                                    navController.navigate(ScreenDestinations.AddBanner)

                                } else {
                                    navController.navigate(ScreenDestinations.EditBanner(it))
                                }
                            },
                            onNavigateToOfferForm = {
                                if (it == null) {
                                    navController.navigate(ScreenDestinations.AddOffer)

                                } else {
                                    navController.navigate(ScreenDestinations.EditOffer(it))
                                }
                            },
                            onMenuClick = onMenuClick
                        )

                    }

                    BackOfficeRoute.Reviews -> {
                        ReviewAdminConsoleScreen(onMenuClick = onMenuClick)
                    }

                    BackOfficeRoute.Notifications -> {

                        DashboardPlaceholderScreen(
                            title = "Notifications",
                            onMenuClick = onMenuClick
                        )
                    }

                    BackOfficeRoute.Settings -> {

                        DashboardPlaceholderScreen(
                            title = "Settings",
                            onMenuClick = onMenuClick
                        )
                    }

                    BackOfficeRoute.Profile -> {

                        EmployeeProfileNewScreen(
                            viewModel = profileViewModel,
                            onMenuClick = onMenuClick
                        )
                    }

                    BackOfficeRoute.Logout -> {
                        currentRoute = BackOfficeRoute.Dashboard
                        loginViewModel.logout()
                    }

                    BackOfficeRoute.TableOrder -> {
                        userSession?.outletId?.let { outletId ->
                            TableManagementScreen(outletId = outletId, onMenuClick = onMenuClick)
                        }
                    }

                    BackOfficeRoute.OrderHistory -> {
                        userSession?.outletId?.let { outletId ->
                            OrderHistoryScreen(outletId = outletId, onMenuClick = onMenuClick)
                        }
                    }

                    BackOfficeRoute.MenuManagement -> {
                        userSession?.outletId?.let { outletId ->
                            SpecificOutletMenuHandlingScreen(
                                outletId = outletId,
                                outletName = "My Outlet Menu",
                                viewModel = outletMenuManagementViewModel,
                                onNavigateToMenuFormAdd = { targetOutletId: Long ->
                                    navController.navigate(ScreenDestinations.AddOutletMenuItem(outletId = targetOutletId))
                                },
                                onNavigateToMenuFormEdit = { targetOutletId: Long, targetMenuId: Long ->
                                    navController.navigate(ScreenDestinations.EditOutletMenuItem(outletId = targetOutletId, id = targetMenuId))
                                },
                                onNavigateBack = {
                                    scope.launch { drawerState.open() }
                                }
                            )
                        }
                    }
                }

                HandleRealTimeInterceptedOrders(viewModel  = unifiedViewModel){order->
                    navController.navigate(ScreenDestinations.OrderDetails(order.id))
                }
            }
        }

    }



}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardPlaceholderScreen(
    title: String,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Coming Soon"
                )
            }
        }
    }
}

