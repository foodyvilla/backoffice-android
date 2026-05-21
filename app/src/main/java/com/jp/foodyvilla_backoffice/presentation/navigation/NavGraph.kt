package com.jp.foodyvilla_backoffice.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jp.foodyvilla_backoffice.presentation.screens.MainScreen
import com.jp.foodyvilla_backoffice.presentation.screens.account.ProfileScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.CRMHomeScreen
import com.jp.foodyvilla_backoffice.presentation.screens.cart.CartScreen
import com.jp.foodyvilla_backoffice.presentation.screens.contactUs.ContactUsScreen
import com.jp.foodyvilla_backoffice.presentation.screens.detail.DetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.login.MobileLoginScreen
import com.jp.foodyvilla_backoffice.presentation.screens.login.OtpVerificationScreen
import com.jp.foodyvilla_backoffice.presentation.screens.login.BackOfficeLoginScreen
import com.jp.foodyvilla_backoffice.presentation.screens.menuOnline.OrderOnlineScreen
import com.jp.foodyvilla_backoffice.presentation.screens.reviews.AddReviewScreen
import com.jp.foodyvilla_backoffice.presentation.screens.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun FoodyVillaNavGraph() {
    val navController = rememberNavController()
    val loginViewModel = koinViewModel<LoginViewModel>()
    val currentSession = loginViewModel.currentSession.collectAsStateWithLifecycle().value

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = Screen.Splash
    )
    {
        composable<Screen.Splash> {
            SplashScreen(
                loginViewModel = loginViewModel, navController = navController
            )
        }


        composable<Screen.Login> {

            MobileLoginScreen(
                loginViewModel = loginViewModel,
                navController = navController,
                onGetOtp = {
                    loginViewModel.updateOtp("")
                    loginViewModel.login()
                })
        }

        composable<Screen.Otp> {
            val maskedPhone = loginViewModel.phoneNumber.collectAsStateWithLifecycle().value
            OtpVerificationScreen(
                maskedPhone = maskedPhone.dropLast(4) + "****",
                loginViewModel = loginViewModel,
                navController = navController,
                onVerify = {
                    loginViewModel.login(otp = it)
//                    navController.navigate(Screen.Home)
                },
            ) {
                loginViewModel.updateOtp("")
                loginViewModel.login()
            }
        }

        composable<Screen.BackOfficeLogin> {
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
        }

        composable<Screen.Home> {
            val homeViewModel = koinViewModel<HomeViewModel>()
            MainScreen(navController = navController, viewModel = homeViewModel)


        }

        composable<Screen.BackOffice> {
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
                CRMHomeScreen(
                    session = currentSession,
                    onLogout = {
                        loginViewModel.logoutBackOffice()
                        navController.navigate(Screen.BackOfficeLogin) {
                            popUpTo(Screen.BackOffice) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        composable<Screen.Detail> { backStack ->
            val homeViewModel = koinViewModel<HomeViewModel>()
            val detail: Screen.Detail = backStack.toRoute()
            DetailScreen(
                itemId = detail.itemId,
                onBack = { navController.popBackStack() },

                onItemClick = { navController.navigate(Screen.Detail(it)) },
                onCartClick = { navController.navigate(Screen.Cart) }, homeViewModel = homeViewModel
            )
        }

        composable<Screen.OnLineMenu> { backStack ->
            OrderOnlineScreen(onBackClick = { navController.navigateUp() })
        }
//
        composable<Screen.Cart> {
            val homeViewModel = koinViewModel<HomeViewModel>()
            CartScreen(
                onBack = { navController.popBackStack() },
                onBrowseMenu = { navController.navigate(Screen.Home) }, viewModel = homeViewModel, loginViewModel = loginViewModel
            )
        }



        composable<Screen.CustomerSupport> {
            ContactUsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<Screen.AddReviews> {
            AddReviewScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() }
            )
        }


//
//        composable<Screen.Login> {
//            LoginScreen(
//                onLoginSuccess = {
//                    navController.navigate(Screen.Home) {
//                        popUpTo(Screen.Login) { inclusive = true }
//                    }
//                },
//                onNavigateToRegister = { navController.navigate(Screen.Register) }
//            )
//        }
//
//        composable<Screen.Register> {
//            RegisterScreen(
//                onRegisterSuccess = {
//                    navController.navigate(Screen.Home) {
//                        popUpTo(Screen.Register) { inclusive = true }
//                    }
//                },
//                onNavigateToLogin = { navController.popBackStack() }
//            )
//        }
//
        composable<Screen.Profile> {
            ProfileScreen(
                viewModel = loginViewModel,
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Profile) {
                            inclusive = true
                        }
                    }
                },
                onSaveChanges = {},
                onNavigateBack = {
                    navController.navigateUp()
                }

            )
        }
    }


}
