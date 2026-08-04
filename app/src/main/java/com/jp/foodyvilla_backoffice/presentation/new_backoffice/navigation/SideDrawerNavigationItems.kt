package com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.ui.graphics.vector.ImageVector
import com.jp.foodyvilla_backoffice.domain.security.OutletRole
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import kotlinx.serialization.Serializable

data class BackOfficeDrawerItem(
    val icon: ImageVector,
    val route: BackOfficeRoute,
    val name: String
)

@Serializable
sealed interface BackOfficeRoute {
    @Serializable data object Dashboard : BackOfficeRoute
    @Serializable data object Orders : BackOfficeRoute
    @Serializable data object TableOrder : BackOfficeRoute
    @Serializable data object OutletMenu : BackOfficeRoute
    @Serializable data object Outlet : BackOfficeRoute
    @Serializable data object Products : BackOfficeRoute
    @Serializable data object Categories : BackOfficeRoute
    @Serializable data object Tables : BackOfficeRoute
    @Serializable data object Customers : BackOfficeRoute
    @Serializable data object Payments : BackOfficeRoute
    @Serializable data object Employees : BackOfficeRoute
    @Serializable data object Attendance : BackOfficeRoute
    @Serializable data object PunchReports : BackOfficeRoute
    @Serializable data object Analytics : BackOfficeRoute
    @Serializable data object Offers : BackOfficeRoute
    @Serializable data object Reviews : BackOfficeRoute
    @Serializable data object Notifications : BackOfficeRoute
    @Serializable data object Settings : BackOfficeRoute
    @Serializable data object Profile : BackOfficeRoute
    @Serializable data object Logout : BackOfficeRoute
    @Serializable data object OrderHistory : BackOfficeRoute
    @Serializable data object MenuManagement : BackOfficeRoute
}

fun getDrawerItemsForSession(session: UserSession?): List<BackOfficeDrawerItem> {
    if (session == null) return emptyList()

    val role = when (session) {
        is UserSession.OutletSession -> session.role
        is UserSession.EmployeeSession -> session.role ?: OutletRole.EMPLOYEE
    }

    return when (role) {
        OutletRole.OWNER -> ownerDrawerItems
        OutletRole.MANAGER, OutletRole.STORE_SUPERVISOR, OutletRole.HEAD -> managerDrawerItems
        OutletRole.HEAD_CHEF, OutletRole.CHEF, OutletRole.KITCHEN, OutletRole.HELPER -> chefDrawerItems
        OutletRole.DELIVERY_BOY -> deliveryDrawerItems
        OutletRole.WAITER, OutletRole.CASHIER, OutletRole.EMPLOYEE -> employeeDrawerItems
    }
}

val ownerDrawerItems = listOf(
    BackOfficeDrawerItem(Icons.Default.Dashboard, BackOfficeRoute.Dashboard, "Dashboard"),
    BackOfficeDrawerItem(Icons.Default.ReceiptLong, BackOfficeRoute.Orders, "Orders"),
    BackOfficeDrawerItem(Icons.Default.TableBar, BackOfficeRoute.TableOrder, "Table Orders"),
    BackOfficeDrawerItem(Icons.Default.MenuBook, BackOfficeRoute.OutletMenu, "Outlet Menu"),
    BackOfficeDrawerItem(Icons.Default.Storefront, BackOfficeRoute.Outlet, "Outlets"),
    BackOfficeDrawerItem(Icons.Default.Inventory, BackOfficeRoute.Products, "Products"),
    BackOfficeDrawerItem(Icons.Default.Category, BackOfficeRoute.Categories, "Categories"),
    BackOfficeDrawerItem(Icons.Default.TableBar, BackOfficeRoute.Tables, "Tables Management"),
    BackOfficeDrawerItem(Icons.Default.Groups, BackOfficeRoute.Customers, "Customers"),
    BackOfficeDrawerItem(Icons.Default.LocalOffer, BackOfficeRoute.Offers, "Offers"),
    BackOfficeDrawerItem(Icons.Default.Payments, BackOfficeRoute.Payments, "Payments"),
    BackOfficeDrawerItem(Icons.Default.Badge, BackOfficeRoute.Employees, "Employees"),
    BackOfficeDrawerItem(Icons.Default.CoPresent, BackOfficeRoute.Attendance, "Attendance"),
    BackOfficeDrawerItem(Icons.Default.Summarize, BackOfficeRoute.PunchReports, "Punch Reports"),
    BackOfficeDrawerItem(Icons.Default.BarChart, BackOfficeRoute.Analytics, "Analytics"),
    BackOfficeDrawerItem(Icons.Default.Reviews, BackOfficeRoute.Reviews, "Reviews"),
    BackOfficeDrawerItem(Icons.Default.Notifications, BackOfficeRoute.Notifications, "Notifications"),
    BackOfficeDrawerItem(Icons.Default.History, BackOfficeRoute.OrderHistory, "Order History"),
    BackOfficeDrawerItem(Icons.Default.Settings, BackOfficeRoute.Settings, "Settings"),
    BackOfficeDrawerItem(Icons.Default.Person, BackOfficeRoute.Profile, "Profile"),
    BackOfficeDrawerItem(Icons.Default.Logout, BackOfficeRoute.Logout, "Logout")
)

val managerDrawerItems = listOf(
    BackOfficeDrawerItem(Icons.Default.Dashboard, BackOfficeRoute.Dashboard, "Dashboard"),
    BackOfficeDrawerItem(Icons.Default.ReceiptLong, BackOfficeRoute.Orders, "Orders"),
    BackOfficeDrawerItem(Icons.Default.TableBar, BackOfficeRoute.TableOrder, "Table Orders"),
    BackOfficeDrawerItem(Icons.Default.MenuBook, BackOfficeRoute.MenuManagement, "Manage Menu"),
    BackOfficeDrawerItem(Icons.Default.RestaurantMenu, BackOfficeRoute.OutletMenu, "POS Menu"),
    BackOfficeDrawerItem(Icons.Default.Inventory, BackOfficeRoute.Products, "Products"),
    BackOfficeDrawerItem(Icons.Default.Category, BackOfficeRoute.Categories, "Categories"),
    BackOfficeDrawerItem(Icons.Default.TableBar, BackOfficeRoute.Tables, "Tables Management"),
    BackOfficeDrawerItem(Icons.Default.Groups, BackOfficeRoute.Customers, "Customers"),
    BackOfficeDrawerItem(Icons.Default.CoPresent, BackOfficeRoute.Attendance, "Attendance"),
    BackOfficeDrawerItem(Icons.Default.Person, BackOfficeRoute.Profile, "Profile"),
    BackOfficeDrawerItem(Icons.Default.Logout, BackOfficeRoute.Logout, "Logout")
)

val chefDrawerItems = listOf(
    BackOfficeDrawerItem(Icons.Default.Dashboard, BackOfficeRoute.Dashboard, "Kitchen View"),
    BackOfficeDrawerItem(Icons.Default.ReceiptLong, BackOfficeRoute.Orders, "Active KOTs"),
    BackOfficeDrawerItem(Icons.Default.MenuBook, BackOfficeRoute.OutletMenu, "View Menu"),
    BackOfficeDrawerItem(Icons.Default.CoPresent, BackOfficeRoute.Attendance, "Attendance"),
    BackOfficeDrawerItem(Icons.Default.Person, BackOfficeRoute.Profile, "Profile"),
    BackOfficeDrawerItem(Icons.Default.Logout, BackOfficeRoute.Logout, "Logout")
)

val deliveryDrawerItems = listOf(
    BackOfficeDrawerItem(Icons.Default.DeliveryDining, BackOfficeRoute.Orders, "My Deliveries"),
    BackOfficeDrawerItem(Icons.Default.CoPresent, BackOfficeRoute.Attendance, "Attendance"),
    BackOfficeDrawerItem(Icons.Default.Person, BackOfficeRoute.Profile, "Profile"),
    BackOfficeDrawerItem(Icons.Default.Logout, BackOfficeRoute.Logout, "Logout")
)

val employeeDrawerItems = listOf(
    BackOfficeDrawerItem(Icons.Default.Dashboard, BackOfficeRoute.Dashboard, "Dashboard"),
    BackOfficeDrawerItem(Icons.Default.ReceiptLong, BackOfficeRoute.Orders, "Orders"),
    BackOfficeDrawerItem(Icons.Default.TableBar, BackOfficeRoute.TableOrder, "Table Orders"),
    BackOfficeDrawerItem(Icons.Default.MenuBook, BackOfficeRoute.OutletMenu, "Outlet Menu"),
    BackOfficeDrawerItem(Icons.Default.CoPresent, BackOfficeRoute.Attendance, "Attendance"),
    BackOfficeDrawerItem(Icons.Default.Person, BackOfficeRoute.Profile, "Profile"),
    BackOfficeDrawerItem(Icons.Default.Logout, BackOfficeRoute.Logout, "Logout")
)
