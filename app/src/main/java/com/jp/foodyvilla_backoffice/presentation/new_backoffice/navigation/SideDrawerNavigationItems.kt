package com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

data class BackOfficeDrawerItem(
    val icon: ImageVector,
    val route: BackOfficeRoute,
    val name: String
)

@Serializable
sealed interface BackOfficeRoute {

    @Serializable
    data object Dashboard : BackOfficeRoute

    @Serializable
    data object Orders : BackOfficeRoute

    @Serializable
    data object OutletMenu : BackOfficeRoute

    @Serializable
    data object CreateOrder : BackOfficeRoute

    @Serializable
    data object Products : BackOfficeRoute

    @Serializable
    data object Categories : BackOfficeRoute

    @Serializable
    data object Customers : BackOfficeRoute

    @Serializable
    data object Payments : BackOfficeRoute

    @Serializable
    data object Employees : BackOfficeRoute

    @Serializable
    data object Attendance : BackOfficeRoute

    @Serializable
    data object PunchReports : BackOfficeRoute

    @Serializable
    data object Analytics : BackOfficeRoute

    @Serializable
    data object Offers : BackOfficeRoute

    @Serializable
    data object Reviews : BackOfficeRoute

    @Serializable
    data object Notifications : BackOfficeRoute

    @Serializable
    data object Settings : BackOfficeRoute

    @Serializable
    data object Profile : BackOfficeRoute

    @Serializable
    data object Logout : BackOfficeRoute
}

val ownerDrawerItems = listOf(

    BackOfficeDrawerItem(
        icon = Icons.Default.Dashboard,
        route = BackOfficeRoute.Dashboard,
        name = "Dashboard"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Assignment,
        route = BackOfficeRoute.Orders,
        name = "Orders"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.RestaurantMenu,
        route = BackOfficeRoute.OutletMenu,
        name = "Outlet Menu"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Fastfood,
        route = BackOfficeRoute.CreateOrder,
        name = "Create Order"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Inventory2,
        route = BackOfficeRoute.Products,
        name = "Products"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Category,
        route = BackOfficeRoute.Categories,
        name = "Categories"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.People,
        route = BackOfficeRoute.Customers,
        name = "Customers"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Payments,
        route = BackOfficeRoute.Payments,
        name = "Payments"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Person,
        route = BackOfficeRoute.Employees,
        name = "Employees"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.PunchClock,
        route = BackOfficeRoute.Attendance,
        name = "Attendance"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Assignment,
        route = BackOfficeRoute.PunchReports,
        name = "Punch Reports"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Analytics,
        route = BackOfficeRoute.Analytics,
        name = "Analytics"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.LocalOffer,
        route = BackOfficeRoute.Offers,
        name = "Offers"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.RateReview,
        route = BackOfficeRoute.Reviews,
        name = "Reviews"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Notifications,
        route = BackOfficeRoute.Notifications,
        name = "Notifications"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Settings,
        route = BackOfficeRoute.Settings,
        name = "Settings"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Person,
        route = BackOfficeRoute.Profile,
        name = "Profile"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Logout,
        route = BackOfficeRoute.Logout,
        name = "Logout"
    )
)




val headDrawerItems = listOf(

    BackOfficeDrawerItem(
        icon = Icons.Default.Dashboard,
        route = BackOfficeRoute.Dashboard,
        name = "Dashboard"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Assignment,
        route = BackOfficeRoute.Orders,
        name = "Orders"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.RestaurantMenu,
        route = BackOfficeRoute.OutletMenu,
        name = "Outlet Menu"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Fastfood,
        route = BackOfficeRoute.CreateOrder,
        name = "Create Order"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Inventory2,
        route = BackOfficeRoute.Products,
        name = "Products"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Category,
        route = BackOfficeRoute.Categories,
        name = "Categories"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.People,
        route = BackOfficeRoute.Customers,
        name = "Customers"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Payments,
        route = BackOfficeRoute.Payments,
        name = "Payments"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Person,
        route = BackOfficeRoute.Employees,
        name = "Employees"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.PunchClock,
        route = BackOfficeRoute.Attendance,
        name = "Attendance"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.LocalOffer,
        route = BackOfficeRoute.Offers,
        name = "Offers"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.RateReview,
        route = BackOfficeRoute.Reviews,
        name = "Reviews"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Notifications,
        route = BackOfficeRoute.Notifications,
        name = "Notifications"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Person,
        route = BackOfficeRoute.Profile,
        name = "Profile"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Logout,
        route = BackOfficeRoute.Logout,
        name = "Logout"
    )
)

val chefDrawerItems = listOf(

    BackOfficeDrawerItem(
        icon = Icons.Default.Dashboard,
        route = BackOfficeRoute.Dashboard,
        name = "Dashboard"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Assignment,
        route = BackOfficeRoute.Orders,
        name = "Orders"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.RestaurantMenu,
        route = BackOfficeRoute.OutletMenu,
        name = "Outlet Menu"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Fastfood,
        route = BackOfficeRoute.CreateOrder,
        name = "Create Order"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Inventory2,
        route = BackOfficeRoute.Products,
        name = "Products"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.PunchClock,
        route = BackOfficeRoute.Attendance,
        name = "Attendance"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Person,
        route = BackOfficeRoute.Profile,
        name = "Profile"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Logout,
        route = BackOfficeRoute.Logout,
        name = "Logout"
    )
)

val employeeDrawerItems = listOf(

    BackOfficeDrawerItem(
        icon = Icons.Default.Dashboard,
        route = BackOfficeRoute.Dashboard,
        name = "Dashboard"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Assignment,
        route = BackOfficeRoute.Orders,
        name = "Orders"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Fastfood,
        route = BackOfficeRoute.CreateOrder,
        name = "Create Order"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.PunchClock,
        route = BackOfficeRoute.Attendance,
        name = "Attendance"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Person,
        route = BackOfficeRoute.Profile,
        name = "Profile"
    ),

    BackOfficeDrawerItem(
        icon = Icons.Default.Logout,
        route = BackOfficeRoute.Logout,
        name = "Logout"
    )
)