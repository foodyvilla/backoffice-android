package com.jp.foodyvilla_backoffice.data.domain.presentation.navigation

import com.jp.foodyvilla_backoffice.domain.security.OutletRole
import com.jp.foodyvilla_backoffice.domain.security.UserSession

enum class BackOfficeDestination {
    Dashboard,
    KitchenTerminal,
    Orders,
    Attendance,
    Profile,
    Offers,
    Banners,
    Customers,
    Reviews,
    Payments,
    EmployeeRoster,
    MenuSettings,
    Analytics,
    OutletsRegistry,
    MasterEmployeeDirectory,
    Settings
}

data class NavigationMatrix(
    val primary: List<BackOfficeDestination>,
    val drawer: List<BackOfficeDestination>
)

//object BackOfficeNavigationPolicy {
//    fun forSession(session: UserSession?): NavigationMatrix {
//        return when (session?.role()) {
//            OutletRole.OWNER -> NavigationMatrix(
//                primary = listOf(
//                    BackOfficeDestination.Dashboard,
//                    BackOfficeDestination.Orders,
//                    BackOfficeDestination.OutletsRegistry,
//                    BackOfficeDestination.MasterEmployeeDirectory,
//                    BackOfficeDestination.Analytics
//                ),
//                drawer = listOf(
//                    BackOfficeDestination.Attendance,
//                    BackOfficeDestination.MenuSettings,
//                    BackOfficeDestination.Offers,
//                    BackOfficeDestination.Banners,
//                    BackOfficeDestination.Reviews,
//                    BackOfficeDestination.Payments,
//                    BackOfficeDestination.Settings
//                )
//            )
//            OutletRole.HEAD -> NavigationMatrix(
//                primary = listOf(
//                    BackOfficeDestination.Dashboard,
//                    BackOfficeDestination.Orders,
//                    BackOfficeDestination.EmployeeRoster,
//                    BackOfficeDestination.MenuSettings,
//                    BackOfficeDestination.Profile
//                ),
//                drawer = listOf(
//                    BackOfficeDestination.Attendance,
//                    BackOfficeDestination.Offers,
//                    BackOfficeDestination.Banners,
//                    BackOfficeDestination.Reviews,
//                    BackOfficeDestination.Payments,
//                    BackOfficeDestination.Analytics,
//                    BackOfficeDestination.Settings
//                )
//            )
//            OutletRole.CHEF -> NavigationMatrix(
//                primary = listOf(
//                    BackOfficeDestination.KitchenTerminal,
//                    BackOfficeDestination.Orders,
//                    BackOfficeDestination.Attendance,
//                    BackOfficeDestination.Profile
//                ),
//                drawer = listOf(
//                    BackOfficeDestination.Offers,
//                    BackOfficeDestination.Banners,
//                    BackOfficeDestination.Reviews
//                )
//            )
//            else -> NavigationMatrix(
//                primary = listOf(
//                    BackOfficeDestination.Dashboard,
//                    BackOfficeDestination.Orders,
//                    BackOfficeDestination.Attendance,
//                    BackOfficeDestination.Profile
//                ),
//                drawer = listOf(
//                    BackOfficeDestination.Offers,
//                    BackOfficeDestination.Banners,
//                    BackOfficeDestination.Customers,
//                    BackOfficeDestination.Reviews,
//                    BackOfficeDestination.Payments
//                )
//            )
//        }
//    }
//}

private fun UserSession.role(): OutletRole? = when (this) {
    is UserSession.EmployeeSession -> role
    is UserSession.OutletSession -> role
}

