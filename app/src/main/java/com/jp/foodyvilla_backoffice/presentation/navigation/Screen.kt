package com.jp.foodyvilla_backoffice.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// Type-safe navigation destinations (Compose Navigation 2.8+ with KSP)
sealed interface Screen {

    @Serializable
    data object Splash : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object BackOffice : Screen

    @Serializable
    data object BackOfficeLogin : Screen



    @Serializable
    data class Detail(val itemId: Int) : Screen

    @Serializable
    data object Cart : Screen

    @Serializable
    data object Menu : Screen


    @Serializable
    data object OnLineMenu : Screen
    @Serializable
    data object Offers : Screen

    @Serializable
    data object Reviews : Screen


    @Serializable
    data object AddReviews : Screen

    @Serializable
    data object CustomerSupport : Screen

    @Serializable
    data object Login : Screen

    @Serializable
    data object Otp : Screen

    @Serializable
    data object Profile : Screen
}

// Bottom nav items
enum class BottomNavItem(val route: Any, val label: String, val icon: ImageVector) {
    Home(Screen.Home, "Home", Icons.Filled.Home),
    Menu(Screen.Menu, "Menu", Icons.Filled.RestaurantMenu),
    Offers(Screen.Offers, "Offers", Icons.Filled.LocalOffer),
    Reviews(Screen.Reviews, "Reviews", Icons.Filled.StarRate),
    Profile(Screen.Profile, "Profile", Icons.Filled.Person);
}
