package com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jp.foodyvilla_backoffice.presentation.navigation.Screen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.NewBackOfficeNavigationScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.NewCreateOrderScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.NewOrderDetailsScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.AddEditBannerFormScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.AddEditOfferFormScreen
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.MarketingViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.UnifiedOrderControlViewModel
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
    val currentSession = loginViewModel.currentSession.collectAsStateWithLifecycle().value
    NavHost(
        navController = navController,
        startDestination = ScreenDestinations.Splash
    ) {


        composable<ScreenDestinations.Splash> {

            SplashScreen(
                loginViewModel = loginViewModel, navController = navController
            )
        }




        composable<ScreenDestinations.BackOffice> {
            if (currentSession == null) {
                BackOfficeLoginScreen(
                    loginViewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.BackOffice) {
                            popUpTo(Screen.BackOfficeLogin) {
                                inclusive = true
                            }
                        }
                    }
                )

            } else {
                NewBackOfficeNavigationScreen(
                    loginViewModel = loginViewModel,
                    marketingViewModel = marketingViewModel,
                    unifiedViewModel = unifiedViewModel,
                    navController = navController
                )

            }
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

        composable < ScreenDestinations.CreateOrder>{

            NewCreateOrderScreen(viewModel = unifiedViewModel,{},{


            })
        }


        composable < ScreenDestinations.OrderDetails>{
            val id = it.toRoute<ScreenDestinations.OrderDetails>().id
            NewOrderDetailsScreen(orderId = id, viewModel  = unifiedViewModel) {


            }
        }

    }
}