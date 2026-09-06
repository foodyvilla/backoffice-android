package com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.CustomerDetailsScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.NewBackOfficeNavigationScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.NewCreateOrderScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.NewOrderDetailsScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.OutletFormWorkspaceScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.SpecificOutletMenuFormScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.AddEditBannerFormScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.AddEditOfferFormScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.AddEmployeeScreen

import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.SpecificOutletMenuHandlingScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceAdminViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.CustomerManagementViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.MarketingViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.OutletManagementViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.ProductCatalogViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.UnifiedOrderControlViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.login.BackOfficeLoginScreen
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NewFoodyVillaNavGraph() {
    val navController = rememberNavController()
    val loginViewModel = koinViewModel<LoginViewModel>()
    val marketingViewModel = koinViewModel<MarketingViewModel>()
    val unifiedViewModel = koinViewModel<UnifiedOrderControlViewModel>()
    val productCatalogViewModel = koinViewModel<ProductCatalogViewModel>()
    val outletMenuManagementViewModel = koinViewModel<OutletManagementViewModel>()
    val customerManagementViewModel  = koinViewModel<CustomerManagementViewModel>()
    val attendanceViewModel = koinViewModel<AttendanceViewModel>()
    val attendanceAdminViewModel = koinViewModel<AttendanceAdminViewModel>()
    val currentSession = loginViewModel.currentSession.collectAsStateWithLifecycle().value

    LaunchedEffect(currentSession) {
        if (currentSession == null) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && !currentRoute.contains("Splash") && !currentRoute.contains("BackOfficeLogin")) {
                navController.navigate(ScreenDestinations.BackOfficeLogin) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ScreenDestinations.Splash
    ) {


        composable<ScreenDestinations.Splash> {

            SplashScreen(
                loginViewModel = loginViewModel, navController = navController
            )
        }

        composable<ScreenDestinations.BackOfficeLogin> {

            BackOfficeLoginScreen(
                loginViewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(ScreenDestinations.BackOffice) {
                        popUpTo(ScreenDestinations.BackOfficeLogin) {
                            inclusive = true
                        }
                    }
                }
            )
        }



        composable<ScreenDestinations.BackOffice> {
            NewBackOfficeNavigationScreen(
                loginViewModel = loginViewModel,
                marketingViewModel = marketingViewModel,
                unifiedViewModel = unifiedViewModel,
                productCatalogViewModel = productCatalogViewModel,
                outletMenuManagementViewModel = outletMenuManagementViewModel,
                customerManagementViewModel   = customerManagementViewModel,
                attendanceViewModel = attendanceViewModel,
                attendanceAdminViewModel  = attendanceAdminViewModel,
                navController = navController
            )
        }

        composable<ScreenDestinations.AddBanner> {
            AddEditBannerFormScreen(
                bannerId = null,
                viewModel = marketingViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. Edit Existing Banner Form Route
        composable<ScreenDestinations.EditBanner> { backStackEntry ->
            val args = backStackEntry.toRoute<ScreenDestinations.EditBanner>()
            AddEditBannerFormScreen(
                bannerId = args.bannerId,
                viewModel = marketingViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Add New Offer Form Route
        composable<ScreenDestinations.AddOffer> {
            AddEditOfferFormScreen(
                offerId = null,
                viewModel = marketingViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Edit Existing Offer Form Route
        composable<ScreenDestinations.EditOffer> { backStackEntry ->
            val args = backStackEntry.toRoute<ScreenDestinations.EditOffer>()
            AddEditOfferFormScreen(
                offerId = args.offerId,
                viewModel = marketingViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ScreenDestinations.CreateOrder> {
            NewCreateOrderScreen(
                viewModel = unifiedViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOrderFinishedSuccess = {
                    navController.popBackStack(ScreenDestinations.BackOffice, inclusive = false)
                }
            )
        }

        composable<ScreenDestinations.OrderDetails> {
            val id = it.toRoute<ScreenDestinations.OrderDetails>().id
            NewOrderDetailsScreen(
                orderId = id,
                viewModel = unifiedViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ScreenDestinations.Customer> {
            val data = it.toRoute<ScreenDestinations.Customer>()
            CustomerDetailsScreen(
                customerId = data.id,
                viewModel = customerManagementViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable<ScreenDestinations.OutletMenu> {
            val args = it.toRoute<ScreenDestinations.OutletMenu>()
            SpecificOutletMenuHandlingScreen(
                outletId = args.outletId,
                outletName = args.outletName,
                viewModel = outletMenuManagementViewModel,
                onNavigateToMenuFormAdd = { targetOutletId ->
                    navController.navigate(ScreenDestinations.AddOutletMenuItem(outletId = targetOutletId))
                },
                onNavigateToMenuFormEdit = { targetOutletId, targetMenuId ->
                    navController.navigate(ScreenDestinations.EditOutletMenuItem(outletId = targetOutletId, id = targetMenuId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable<ScreenDestinations.AddOutlet> {
            OutletFormWorkspaceScreen(
                editId = null, // Null triggers insertion rules engine inside form layouts
                viewModel = outletMenuManagementViewModel,
                onNavigateBackAction = { navController.popBackStack() }
            )
        }

        // =====================================================================
        // ROUTE: EDIT OUTLET (EXTRACTS LONG PARAMETER SECURELY)
        // =====================================================================
        composable<ScreenDestinations.EditOutlet> { backStackEntry ->
            val args = backStackEntry.toRoute<ScreenDestinations.EditOutlet>()
            OutletFormWorkspaceScreen(
                editId = args.id, // ID forces viewmodel to load active row specs first
                viewModel = outletMenuManagementViewModel,
                onNavigateBackAction = { navController.navigateUp() }
            )
        }

        composable<ScreenDestinations.AddOutletMenuItem> { backStackEntry ->
            val args = backStackEntry.toRoute<ScreenDestinations.AddOutletMenuItem>()
            SpecificOutletMenuFormScreen(
                outletId = args.outletId,
                editId = null, // Null triggers new catalog link layout sheet inside form views
                viewModel = outletMenuManagementViewModel,
                onNavigateBackAction = { navController.popBackStack() }
            )
        }

        // =====================================================================
        // ROUTE: EDIT OUTLET MENU ITEM PRICING SPECIFICATIONS
        // =====================================================================
        composable<ScreenDestinations.EditOutletMenuItem> { backStackEntry ->
            val args = backStackEntry.toRoute<ScreenDestinations.EditOutletMenuItem>()
            SpecificOutletMenuFormScreen(
                outletId = args.outletId,
                editId = args.id, // Extracted menu row index points
                viewModel = outletMenuManagementViewModel,
                onNavigateBackAction = { navController.popBackStack() }
            )
        }

        composable<ScreenDestinations.AddEmployee> {
            AddEmployeeScreen(
                employeeId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ScreenDestinations.EditEmployee> { backStackEntry ->
            val args = backStackEntry.toRoute<ScreenDestinations.EditEmployee>()
            AddEmployeeScreen(
                employeeId = args.employeeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}