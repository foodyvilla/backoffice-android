package com.jp.foodyvilla_backoffice.presentation.new_backoffice


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.attendance.EmployeeAdminConsoleScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.AttendanceAdminConsoleScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.CustomerDirectoryScreen

import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.EmployeePunchReportScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.MarketingTabsDashboardScreen

import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.ProductCatalogManagementScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.ProductCategoryManagementScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.BackOfficeRoute
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.ScreenDestinations
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.chefDrawerItems
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.employeeDrawerItems
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.headDrawerItems
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.ownerDrawerItems
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.OutletListDirectoryScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.PaymentAdminConsoleScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.ReviewAdminConsoleScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceAdminViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.CustomerManagementViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.MarketingViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.OutletManagementViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.ProductCatalogViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.UnifiedOrderControlViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel

import kotlinx.coroutines.launch

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
    attendanceAdminViewModel: AttendanceAdminViewModel
) {
    val userSession = loginViewModel.currentSession.collectAsStateWithLifecycle().value

    val drawerItems = remember(userSession?.role()) {

        when ((userSession?.role() ?: "employee").lowercase()) {

            "owner" -> ownerDrawerItems
            "head" -> headDrawerItems
            "chef" -> chefDrawerItems
            else -> employeeDrawerItems
        }
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

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 24.dp)
            ) {

                Text(
                    text = "FoodyVilla Backoffice",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn {

                    items(drawerItems) { item ->

                        NavigationDrawerItem(
                            selected = currentRoute == item.route,

                            onClick = {

                                currentRoute = item.route

                                scope.launch {
                                    drawerState.close()
                                }
                            },

                            label = {
                                Text(item.name)
                            },

                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name
                                )
                            },

                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            )
                        )
                    }
                }
            }
        }
    ) {

        Scaffold(
            topBar = {

                TopAppBar(
                    title = {

                        Text(
                            text = currentRoute.title()
                        )
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = {

                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                when (currentRoute) {

                    BackOfficeRoute.Dashboard -> {

                        DashboardPlaceholderScreen(
                            title = "Dashboard"
                        )
                    }

                    BackOfficeRoute.Orders -> {
                        NewOrdersListScreen(viewModel = unifiedViewModel, navController = navController){

                        }
//                        OrderScreen()
                    }

                    BackOfficeRoute.OutletMenu -> {

                        NewCreateOrderMenuSelectionScreen(viewModel = unifiedViewModel){
                           navController.navigate(ScreenDestinations.CreateOrder)

                        }
                    }

                    BackOfficeRoute.Outlet -> {

//                        NewCreateOrderScreen(
//                            outletId = outletId,
//                            items = emptyList(),
//                            onOrderPlaced = {
//
//                            },
//                            selectedItems = TODO(),
//                            onOrderFinished = TODO(),
//                            viewModel = TODO()
//                        )
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
                            }
                        )

                    }

                    BackOfficeRoute.Products -> {

                        ProductCatalogManagementScreen(viewModel =  productCatalogViewModel)
                    }

                    BackOfficeRoute.Categories -> {

                        ProductCategoryManagementScreen(viewModel = productCatalogViewModel)
                    }

                    BackOfficeRoute.Customers -> {

//                        DashboardPlaceholderScreen(
//                            title = "Customers"
//                        )

                        CustomerDirectoryScreen(viewModel = customerManagementViewModel) {id, phone->
                            navController.navigate(ScreenDestinations.Customer(id = id, phone = phone))
                        }
                    }

                    BackOfficeRoute.Payments -> {
                        PaymentAdminConsoleScreen()
                    }

                    BackOfficeRoute.Employees -> {

//                        DashboardPlaceholderScreen(
//                            title = "Employees"
//                        )

                        EmployeeAdminConsoleScreen()
                    }

                    BackOfficeRoute.Attendance -> {


                        EmployeePunchReportScreen(viewModel = attendanceViewModel, onNavigateBack = {
                            scope.launch {
                                drawerState.open()
                            }
                        } )

                    }

                    BackOfficeRoute.PunchReports -> {
                        AttendanceAdminConsoleScreen(viewModel = attendanceAdminViewModel)

                    }

                    BackOfficeRoute.Analytics -> {

                        DashboardPlaceholderScreen(
                            title = "Analytics"
                        )
                    }

                    BackOfficeRoute.Offers -> {

//                        DashboardPlaceholderScreen(
//                            title = "Offers"
//                        )

                        MarketingTabsDashboardScreen(
                            viewModel = marketingViewModel,
                            onNavigateToBannerForm ={
                                if( it == null){
                                    navController.navigate(ScreenDestinations.AddBanner)

                                }else{
                                    navController.navigate(ScreenDestinations.EditBanner(it))
                                }
                            },
                            onNavigateToOfferForm = {
                                if( it == null){
                                    navController.navigate(ScreenDestinations.AddOffer)

                                }else{
                                    navController.navigate(ScreenDestinations.EditOffer(it))
                                }                            }
                        )

                    }

                    BackOfficeRoute.Reviews -> {
                        ReviewAdminConsoleScreen()
                    }

                    BackOfficeRoute.Notifications -> {

                        DashboardPlaceholderScreen(
                            title = "Notifications"
                        )
                    }

                    BackOfficeRoute.Settings -> {

                        DashboardPlaceholderScreen(
                            title = "Settings"
                        )
                    }

                    BackOfficeRoute.Profile -> {

                        DashboardPlaceholderScreen(
                            title = "Profile"
                        )
                    }

                    BackOfficeRoute.Logout -> {

                        loginViewModel.logout()
                    }
                }

                HandleRealTimeInterceptedOrders(viewModel  = unifiedViewModel){order->
                    navController.navigate(ScreenDestinations.OrderDetails(order.id))
                }
            }
        }

    }



}

@Composable
private fun DashboardPlaceholderScreen(
    title: String
) {

    Box(
        modifier = Modifier.fillMaxSize(),
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

private fun BackOfficeRoute.title(): String {

    return when (this) {

        BackOfficeRoute.Dashboard -> "Dashboard"
        BackOfficeRoute.Orders -> "Orders"
        BackOfficeRoute.OutletMenu -> "Outlet Menu"
        BackOfficeRoute.Outlet -> "Outlets"
        BackOfficeRoute.Products -> "Products"
        BackOfficeRoute.Categories -> "Categories"
        BackOfficeRoute.Customers -> "Customers"
        BackOfficeRoute.Payments -> "Payments"
        BackOfficeRoute.Employees -> "Employees"
        BackOfficeRoute.Attendance -> "Attendance"
        BackOfficeRoute.PunchReports -> "Punch Reports"
        BackOfficeRoute.Analytics -> "Analytics"
        BackOfficeRoute.Offers -> "Offers"
        BackOfficeRoute.Reviews -> "Reviews"
        BackOfficeRoute.Notifications -> "Notifications"
        BackOfficeRoute.Settings -> "Settings"
        BackOfficeRoute.Profile -> "Profile"
        BackOfficeRoute.Logout -> "Logout"
    }
}
