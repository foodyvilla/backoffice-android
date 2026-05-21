package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.fcm.MyFirebaseMessagingService
import com.jp.foodyvilla_backoffice.domain.security.AppFeature
import com.jp.foodyvilla_backoffice.domain.security.OutletRole
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.components.security.FeatureGate
import com.jp.foodyvilla_backoffice.presentation.utils.RequestNotificationPermission
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CRMHomeScreen(
    session: UserSession?,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val firstAllowedRoute = remember(session) {
        drawerGroups.asSequence()
            .flatMap { it.second.asSequence() }
            .firstOrNull { route -> session.isRouteAllowed(route, AdminRoute.Dashboard) }
            ?: AdminRoute.Dashboard
    }
    var route by remember(session) { mutableStateOf(firstAllowedRoute) }
    var previousListRoute by remember { mutableStateOf(AdminRoute.Orders) }
    var selectedRow by remember { mutableStateOf<JsonObject?>(null) }
    var formMode by remember { mutableStateOf(FormMode.Create) }
    var fcmPopup by remember { mutableStateOf<FcmOrderPopup?>(null) }

    RequestNotificationPermission()

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                fcmPopup = FcmOrderPopup(
                    title = intent?.getStringExtra("title").orEmpty().ifBlank { "New order" },
                    body = intent?.getStringExtra("body").orEmpty(),
                    orderId = intent?.getStringExtra("orderId").orEmpty(),
                    customerName = intent?.getStringExtra("customerName").orEmpty(),
                    amount = intent?.getStringExtra("amount").orEmpty(),
                    itemCount = intent?.getStringExtra("itemCount").orEmpty()
                )
            }
        }
        val filter = IntentFilter(MyFirebaseMessagingService.ACTION_NEW_ORDER_FCM)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }

    LaunchedEffect(state.error, state.successMessage) {
        val message = state.error ?: state.successMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(route.tableName) {
        route.tableName?.let { tableName ->
            state.tables.firstOrNull { it.name == tableName }?.let(viewModel::selectTable)
        }
    }

    val popupKey = state.newOrder?.get("id").toDisplayText("") + fcmPopup?.orderId.orEmpty() + fcmPopup?.title.orEmpty()
    LaunchedEffect(popupKey) {
        if (state.newOrder != null || fcmPopup != null) {
            runCatching {
                RingtoneManager.getRingtone(
                    context,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                )?.play()
            }
        }
    }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
                    drawerContainerColor = Color.White,
                    modifier = Modifier.width(318.dp).fillMaxHeight()
                ) {
                    BackOfficeDrawer(
                        session = session,
                        selectedRoute = if (route in listOf(AdminRoute.Form, AdminRoute.Details)) previousListRoute else route,
                        onRouteSelected = { next ->
                            if (session.isRouteAllowed(next, route)) {
                                route = next
                                selectedRow = null
                            }
                            scope.launch { drawerState.close() }
                        },
                        onLogout = onLogout
                    )
                }
            }
        ) {
            Scaffold(
                containerColor = CanvasColor,
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().padding(padding)) {
                    FeatureGate(feature = route.gatedFeature(previousListRoute)) {
                        when {
                            !session.isRouteAllowed(route, previousListRoute) -> AccessDeniedRoute(
                                route = route,
                                onMenu = { scope.launch { drawerState.open() } }
                            )

                            route == AdminRoute.Dashboard -> DashboardScreen(
                                state = state,
                                onMenu = { scope.launch { drawerState.open() } },
                                onRefresh = viewModel::refresh,
                                onOpenRoute = { next ->
                                    if (session.isRouteAllowed(next, route)) {
                                        route = next
                                        previousListRoute = next
                                    }
                                }
                            )

                            route in listOf(
                                AdminRoute.Products,
                                AdminRoute.OutletMenu,
                                AdminRoute.Orders,
                                AdminRoute.OrderItems,
                                AdminRoute.Customers,
                                AdminRoute.Reviews,
                                AdminRoute.Offers,
                                AdminRoute.Banners,
                                AdminRoute.Employees,
                                AdminRoute.Attendance,
                                AdminRoute.Payments,
                                AdminRoute.Cart,
                                AdminRoute.Outlets
                            ) -> BackOfficeListScreen(
                                route = route,
                                state = state,
                                onMenu = { scope.launch { drawerState.open() } },
                                onRefresh = viewModel::refresh,
                                onSearch = viewModel::updateSearch,
                                onCreate = {
                                    previousListRoute = route
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    route = AdminRoute.Form
                                },
                                onOrderStatusChange = viewModel::updateOrderStatus,
                                onPunchIn = viewModel::punchIn,
                                onPunchOut = viewModel::punchOut,
                                onOpenDetails = { row ->
                                    previousListRoute = route
                                    selectedRow = row
                                    route = AdminRoute.Details
                                }
                            )

                            route == AdminRoute.Details -> BackOfficeDetailScreen(
                                table = state.selectedTable,
                                row = selectedRow,
                                orderItems = state.orderItemsByOrderId[selectedRow?.get("id").toDisplayText()].orEmpty(),
                                productsById = state.productsById,
                                onBack = { route = previousListRoute },
                                onEdit = {
                                    selectedRow?.let(viewModel::startEdit)
                                    formMode = FormMode.Edit
                                    route = AdminRoute.Form
                                }
                            )

                            route == AdminRoute.Form -> BackOfficeFormScreen(
                                state = state,
                                mode = formMode,
                                onBack = { route = previousListRoute },
                                onFormChange = viewModel::updateFormValue,
                                onUploadImage = { uri, column -> viewModel.uploadImage(context, uri, column) },
                                onCreateProduct = if (state.selectedTable.name == "outlet_menu_items") {
                                    {
                                        previousListRoute = AdminRoute.OutletMenu
                                        formMode = FormMode.Create
                                        viewModel.startCreateFor("product_catalog")
                                        route = AdminRoute.Form
                                    }
                                } else {
                                    null
                                },
                                onSave = {
                                    viewModel.save()
                                    route = previousListRoute
                                }
                            )

                            route == AdminRoute.Profile -> EmployeeProfileScreen(
                                session = session,
                                state = state,
                                onMenu = { scope.launch { drawerState.open() } }
                            )

                            route in listOf(
                                AdminRoute.Categories,
                                AdminRoute.Notifications,
                                AdminRoute.Analytics,
                                AdminRoute.Settings
                            ) -> BackOfficeUtilityScreen(
                                route = route,
                                state = state,
                                onMenu = { scope.launch { drawerState.open() } }
                            )

                            else -> AccessDeniedRoute(
                                route = route,
                                onMenu = { scope.launch { drawerState.open() } }
                            )
                        }
                    }
                }
            }
        }

    state.newOrder?.let { order ->
        val orderId = order["id"].toDisplayText()
        val items = state.orderItemsByOrderId[orderId].orEmpty()
        NewOrderDialog(
            title = "New order received",
            customerName = order["customer_name"].toDisplayText("Customer"),
            orderId = orderId,
            amount = "Rs %.0f".format(items.sumOf { it["total_price"].asNumber() }),
            itemCount = items.sumOf { it["qty"].asNumber() }.toInt().takeIf { it > 0 }?.toString().orEmpty(),
            onDismiss = viewModel::clearNewOrder,
            onAccept = {
                viewModel.updateOrderStatus(order, "accepted")
                viewModel.clearNewOrder()
            },
            onViewOrder = {
                selectedRow = order
                previousListRoute = AdminRoute.Orders
                route = AdminRoute.Details
                viewModel.clearNewOrder()
            }
        )
    }

    if (state.newOrder == null) {
        fcmPopup?.let { popup ->
            NewOrderDialog(
                title = popup.title,
                customerName = popup.customerName.ifBlank { "Customer" },
                orderId = popup.orderId,
                amount = popup.amount,
                itemCount = popup.itemCount,
                onDismiss = { fcmPopup = null },
                onAccept = { fcmPopup = null },
                onViewOrder = {
                    fcmPopup = null
                    route = AdminRoute.Orders
                    previousListRoute = AdminRoute.Orders
                }
            )
        }
    }
}

private fun AdminRoute.gatedFeature(previousListRoute: AdminRoute): AppFeature {
    val effectiveRoute = if (this in listOf(AdminRoute.Form, AdminRoute.Details)) {
        previousListRoute
    } else {
        this
    }

    return when (effectiveRoute) {
        AdminRoute.Dashboard,
        AdminRoute.Analytics,
        AdminRoute.Notifications,
        AdminRoute.Profile,
        AdminRoute.Outlets -> AppFeature.Dashboard
        AdminRoute.Products,
        AdminRoute.OutletMenu,
        AdminRoute.Categories,
        AdminRoute.Offers,
        AdminRoute.Banners -> AppFeature.InventoryManagement
        AdminRoute.Orders -> AppFeature.OrderProcessing
        AdminRoute.OrderItems -> AppFeature.OrderProcessing
        AdminRoute.Cart -> AppFeature.OrderProcessing
        AdminRoute.Customers,
        AdminRoute.Reviews -> AppFeature.CustomerManagement
        AdminRoute.Employees -> AppFeature.EmployeeManagement
        AdminRoute.Attendance -> AppFeature.BiometricAttendance
        AdminRoute.Payments -> AppFeature.Dashboard
            AdminRoute.Settings -> AppFeature.Settings
        AdminRoute.Form,
        AdminRoute.Details -> AppFeature.Dashboard
    }
}

private fun List<AdminRoute>.onlyAllowedFor(session: UserSession?): List<AdminRoute> {
    return filter { route ->
        session.isRouteAllowed(route, AdminRoute.Dashboard)
    }
}

private fun UserSession?.isRouteAllowed(route: AdminRoute, previousListRoute: AdminRoute): Boolean {
    val session = this ?: return false
    val effectiveRoute = if (route in listOf(AdminRoute.Form, AdminRoute.Details)) previousListRoute else route
    if (effectiveRoute !in session.allowedAdminRoutes()) return false
    return effectiveRoute.gatedFeature(previousListRoute).isAccessibleBy(session)
}

private fun UserSession.allowedAdminRoutes(): Set<AdminRoute> {
    val role = when (this) {
        is UserSession.EmployeeSession -> role
        is UserSession.OutletSession -> role
    }

    return when (role) {
        OutletRole.OWNER -> setOf(
            AdminRoute.Dashboard,
            AdminRoute.Orders,
            AdminRoute.OrderItems,
            AdminRoute.Employees,
            AdminRoute.Analytics,
            AdminRoute.Attendance,
            AdminRoute.Products,
            AdminRoute.OutletMenu,
            AdminRoute.Offers,
            AdminRoute.Banners,
            AdminRoute.Reviews,
            AdminRoute.Payments,
            AdminRoute.Cart,
            AdminRoute.Outlets,
            AdminRoute.Settings
        )

        OutletRole.HEAD -> setOf(
            AdminRoute.Dashboard,
            AdminRoute.Orders,
            AdminRoute.OrderItems,
            AdminRoute.Employees,
            AdminRoute.Products,
            AdminRoute.OutletMenu,
            AdminRoute.Categories,
            AdminRoute.Profile,
            AdminRoute.Attendance,
            AdminRoute.Offers,
            AdminRoute.Banners,
            AdminRoute.Reviews,
            AdminRoute.Payments,
            AdminRoute.Analytics,
            AdminRoute.Settings
        )

        OutletRole.CHEF -> setOf(
            AdminRoute.Orders,
            AdminRoute.OrderItems,
            AdminRoute.Attendance,
            AdminRoute.Profile,
            AdminRoute.Offers,
            AdminRoute.Banners,
            AdminRoute.Reviews
        )

        OutletRole.EMPLOYEE,
        OutletRole.WAITER,
        OutletRole.CASHIER,
        OutletRole.MANAGER,
        OutletRole.KITCHEN,
        null -> setOf(
            AdminRoute.Dashboard,
            AdminRoute.Orders,
            AdminRoute.OrderItems,
            AdminRoute.Attendance,
            AdminRoute.Profile,
            AdminRoute.Offers,
            AdminRoute.Banners,
            AdminRoute.Customers,
            AdminRoute.Reviews,
            AdminRoute.Payments
        )
    }
}

private data class FcmOrderPopup(
    val title: String,
    val body: String,
    val orderId: String,
    val customerName: String,
    val amount: String,
    val itemCount: String
)

@Composable
private fun AccessDeniedRoute(route: AdminRoute, onMenu: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            PremiumTopBar(
                title = route.title,
                subtitle = "This tool is not available for your role.",
                icon = route.icon,
                onMenu = onMenu
            )
        }
        item {
            EmptyState(
                title = "Access restricted",
                message = "Your employee role does not include this backoffice area."
            )
        }
    }
}

@Composable
private fun NewOrderDialog(
    title: String,
    customerName: String,
    orderId: String,
    amount: String,
    itemCount: String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onViewOrder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.ExtraBold)
                Text(customerName, color = Ink, fontWeight = FontWeight.ExtraBold)
                if (orderId.isNotBlank()) {
                    Text("Order #${orderId.take(8)}", color = Muted)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (amount.isNotBlank()) Text("Amount: $amount")
                if (itemCount.isNotBlank()) Text("Items: $itemCount")
                Text("A new customer order needs attention.")
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("Accept")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onViewOrder) {
                    Text("View Order")
                }
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    )
}

@Composable
private fun BackOfficeDrawer(
    session: UserSession?,
    selectedRoute: AdminRoute,
    onRouteSelected: (AdminRoute) -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DrawerHeader() }
        drawerGroups.forEach { (group, routes) ->
            val allowedRoutes = routes.onlyAllowedFor(session)
            if (allowedRoutes.isEmpty()) return@forEach
            item {
                Text(
                    group,
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp),
                    color = Muted,
                    fontWeight = FontWeight.Bold
                )
            }
            items(allowedRoutes) { route ->
                NavigationDrawerItem(
                    label = { Text(route.title) },
                    selected = route == selectedRoute,
                    onClick = { onRouteSelected(route) },
                    icon = { Icon(route.icon, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFFE8EEFF),
                        selectedIconColor = RoyalBlue,
                        selectedTextColor = RoyalBlue
                    )
                )
            }
            item { Spacer(Modifier.height(4.dp)) }
        }
        item {
            HorizontalDivider(Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Text("Sign out")
            }
        }
    }
}

@Composable
private fun BackOfficeUtilityScreen(route: AdminRoute, state: AdminUiState, onMenu: () -> Unit) {
    val products = state.dashboardRows["product_catalog"].orEmpty()
    val categories = products.mapNotNull { it["category"].toDisplayText().takeIf { value -> value != "-" } }.distinct()

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PremiumTopBar(
                title = route.title,
                subtitle = route.subtitle,
                icon = route.icon,
                onMenu = onMenu
            )
        }
        when (route) {
            AdminRoute.Categories -> {
                if (categories.isEmpty()) {
                    item { EmptyState("No categories", "Categories are derived from real product rows.") }
                } else {
                    items(categories) { category ->
                        PremiumCard {
                            Row(Modifier.fillMaxWidth().padding(18.dp)) {
                                Text(category, color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("${products.count { it["category"].toDisplayText() == category }} products", color = Muted)
                            }
                        }
                    }
                }
            }
            AdminRoute.Analytics -> item {
                EmptyState("Analytics ready", "Charts will appear when API-backed aggregate fields are available.")
            }
            AdminRoute.Notifications -> item {
                EmptyState("No notifications", "Notification logs from Supabase will appear here when connected.")
            }
            AdminRoute.Settings -> item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Notification settings", "Store settings", "Theme settings", "Employee permissions", "Delivery settings", "Tax settings", "Printer settings", "Account preferences").forEach {
                        PremiumCard {
                            Text(it, modifier = Modifier.fillMaxWidth().padding(18.dp), color = Ink, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            else -> item { HorizontalDivider() }
        }
    }
}
