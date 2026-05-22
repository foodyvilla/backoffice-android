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
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.MainBackOfficeScreen
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
                MainBackOfficeScreen(
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
    }
}
