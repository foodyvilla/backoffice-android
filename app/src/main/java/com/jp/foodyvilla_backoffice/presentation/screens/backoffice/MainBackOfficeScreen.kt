package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.R
import com.jp.foodyvilla_backoffice.domain.security.AppFeature
import com.jp.foodyvilla_backoffice.domain.security.OutletRole
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.fcm.MyFirebaseMessagingService
import com.jp.foodyvilla_backoffice.presentation.components.security.FeatureGate
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import org.koin.androidx.compose.koinViewModel

data class FcmOrderPopup(
    val title: String,
    val body: String,
    val orderId: String,
    val customerName: String,
    val amount: String,
    val itemCount: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBackOfficeScreen(
    session: UserSession?,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = koinViewModel(),
    loginViewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val pendingOrderIds = state.pendingOrders.map { it["id"].toDisplayText() }.toSet()
    var knownPendingOrderIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(pendingOrderIds) {
        val newIds = pendingOrderIds - knownPendingOrderIds
        if (newIds.isNotEmpty()) {
            runCatching {
                RingtoneManager.getRingtone(
                    context,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                )?.play()
            }
        }
        knownPendingOrderIds = pendingOrderIds
    }

    val firstAllowedRoute = remember(session) {
        drawerGroups.asSequence()
            .flatMap { it.second.asSequence() }
            .firstOrNull { route -> session.isRouteAllowed(route, AdminRoute.Dashboard) }
            ?: AdminRoute.Dashboard
    }
    
    var currentRoute by remember(session) { mutableStateOf(firstAllowedRoute) }
    var previousListRoute by remember { mutableStateOf(AdminRoute.Orders) }
    var selectedRow by remember { mutableStateOf<JsonObject?>(null) }
    var formMode by remember { mutableStateOf(FormMode.Create) }
    var dismissedOrderIds by remember { mutableStateOf(setOf<String>()) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        // Refresh FCM token on backoffice open
        loginViewModel.updateFcmToken()
    }

    LaunchedEffect(state.error, state.successMessage) {
        val message = state.error ?: state.successMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(currentRoute.tableName) {
        currentRoute.tableName?.let { tableName ->
            state.tables.firstOrNull { it.name == tableName }?.let(viewModel::selectTable)
        }
    }

    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentRoute != AdminRoute.Dashboard) {
            currentRoute = AdminRoute.Dashboard
        } else {
            (context as? Activity)?.finish()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BackOfficeDrawerContent(
                session = session,
                selectedRoute = if (currentRoute in listOf(AdminRoute.Form, AdminRoute.Details)) previousListRoute else currentRoute,
                onRouteSelected = { next ->
                    if (session.isRouteAllowed(next, currentRoute)) {
                        currentRoute = next
                        selectedRow = null
                    }
                    scope.launch { drawerState.close() }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(currentRoute.title, style = MaterialTheme.typography.titleLarge)
                            if (currentRoute.subtitle.isNotBlank()) {
                                Text(
                                    text = currentRoute.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        val isBack = currentRoute in listOf(AdminRoute.Form, AdminRoute.Details)
                        IconButton(
                            onClick = {
                                if (isBack) {
                                    currentRoute = previousListRoute
                                } else {
                                    scope.launch { drawerState.open() }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isBack) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                                contentDescription = if (isBack) "Back" else "Menu"
                            )
                        }
                    },
                    actions = {
                        if (currentRoute !in listOf(AdminRoute.Form, AdminRoute.Details)) {
                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                        Image(
                            painter = painterResource(R.drawable.logo_new),
                            contentDescription = "Logo",
                            modifier = Modifier.size(40.dp).padding(end = 8.dp)
                        )
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                FeatureGate(feature = currentRoute.gatedFeature(previousListRoute)) {
                    when {
                        !session.isRouteAllowed(currentRoute, previousListRoute) -> AccessDeniedView(currentRoute)
                        
                        currentRoute == AdminRoute.Dashboard -> DashboardScreen(
                            state = state,
                            onOpenRoute = { next ->
                                if (session.isRouteAllowed(next, currentRoute)) {
                                    currentRoute = next
                                    previousListRoute = next
                                }
                            }
                        )

                        currentRoute.tableName != null -> BackOfficeListScreen(
                            session = session,
                            route = currentRoute,
                            state = state,
                            onSearch = viewModel::updateSearch,
                            onCreate = {
                                previousListRoute = currentRoute
                                selectedRow = null
                                formMode = FormMode.Create
                                viewModel.startCreate()
                                currentRoute = AdminRoute.Form
                            },
                            onOrderStatusChange = viewModel::updateOrderStatus,
                            onPunchIn = viewModel::punchIn,
                            onPunchOut = viewModel::punchOut,
                            onSendFcm = viewModel::sendFcmToToken,
                            onSendOfferToAll = viewModel::sendOfferToCart,
                            onOpenDetails = { row ->
                                previousListRoute = currentRoute
                                selectedRow = row
                                currentRoute = AdminRoute.Details
                                if (state.selectedTable.name == "users") {
                                    row["id"]?.toDisplayText()?.let { viewModel.loadCustomerDetails(it) }
                                }
                            }
                        )

                        currentRoute == AdminRoute.Details -> BackOfficeDetailScreen(
                            table = state.selectedTable,
                            row = selectedRow,
                            orderItems = state.orderItemsByOrderId[selectedRow?.get("id").toDisplayText()].orEmpty(),
                            customerOrders = state.customerOrders,
                            customerCart = state.customerCart,
                            productsById = state.productsById,
                            onBack = { currentRoute = previousListRoute },
                            onEdit = {
                                selectedRow?.let(viewModel::startEdit)
                                formMode = FormMode.Edit
                                currentRoute = AdminRoute.Form
                            }
                        )

                        currentRoute == AdminRoute.Form -> BackOfficeFormScreen(
                            state = state,
                            mode = formMode,
                            onBack = { currentRoute = previousListRoute },
                            onFormChange = viewModel::updateFormValue,
                            onUploadImage = { uri, column -> viewModel.uploadImage(context, uri, column) },
                            onCreateProduct = if (state.selectedTable.name == "outlet_menu_items") {
                                {
                                    previousListRoute = AdminRoute.OutletMenu
                                    formMode = FormMode.Create
                                    viewModel.startCreateFor("product_catalog")
                                    currentRoute = AdminRoute.Form
                                }
                            } else {
                                null
                            },
                            onSave = {
                                viewModel.save()
                                currentRoute = previousListRoute
                            }
                        )

                        currentRoute == AdminRoute.Profile -> EmployeeProfileScreen(
                            session = session,
                            state = state
                        )

                        else -> AccessDeniedView(currentRoute)
                    }
                }

                // Global Order Notification Overlay (Supabase Realtime)
                val visibleOrders = state.pendingOrders.filter { 
                    it["id"].toDisplayText() !in dismissedOrderIds 
                }
                
                visibleOrders.firstOrNull()?.let { order ->
                    val orderId = order["id"].toDisplayText()
                    val items = state.orderItemsByOrderId[orderId].orEmpty()
                    
                    GlobalOrderNotification(
                        title = "New Order Received!",
                        orderId = orderId,
                        customerName = order["customer_name"].toDisplayText("Customer"),
                        itemCount = items.sumOf { it["qty"].asNumber() }.toInt().toString(),
                        amount = "Rs %.0f".format(items.sumOf { it["total_price"].asNumber() }),
                        onDismiss = { dismissedOrderIds = dismissedOrderIds + orderId },
                        onAccept = {
                            viewModel.updateOrderStatus(order, "accepted")
                        },
                        onView = {
                            selectedRow = order
                            previousListRoute = AdminRoute.Orders
                            currentRoute = AdminRoute.Details
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GlobalOrderNotification(
    title: String,
    orderId: String,
    customerName: String,
    itemCount: String = "",
    amount: String = "",
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onView: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding(),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Order #${orderId.take(8)} from $customerName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            if (itemCount.isNotBlank() && itemCount != "0") {
                Text("Items: $itemCount | Total: $amount", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss) { Text("Dismiss") }
                TextButton(onClick = onView) { Text("View") }
                Button(onClick = onAccept) { Text("Accept") }
            }
        }
    }
}

@Composable
fun AccessDeniedView(route: AdminRoute) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Lock, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Text("Access Denied", style = MaterialTheme.typography.headlineSmall)
            Text("You don't have permission to access ${route.title}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun AdminRoute.gatedFeature(previousListRoute: AdminRoute): AppFeature {
    val effectiveRoute = if (this in listOf(AdminRoute.Form, AdminRoute.Details)) previousListRoute else this
    return when (effectiveRoute) {
        AdminRoute.Dashboard, AdminRoute.Analytics, AdminRoute.Notifications, AdminRoute.Profile, AdminRoute.Outlets -> AppFeature.Dashboard
        AdminRoute.Products, AdminRoute.OutletMenu, AdminRoute.Categories, AdminRoute.Offers, AdminRoute.Banners -> AppFeature.InventoryManagement
        AdminRoute.Orders, AdminRoute.OrderItems, AdminRoute.Cart -> AppFeature.OrderProcessing
        AdminRoute.Customers, AdminRoute.Reviews -> AppFeature.CustomerManagement
        AdminRoute.Employees -> AppFeature.EmployeeManagement
        AdminRoute.Attendance -> AppFeature.BiometricAttendance
        AdminRoute.Payments -> AppFeature.Dashboard
        AdminRoute.Settings -> AppFeature.Settings
        else -> AppFeature.Dashboard
    }
}

fun UserSession?.isRouteAllowed(route: AdminRoute, previousListRoute: AdminRoute): Boolean {
    val session = this ?: return false
    val effectiveRoute = if (route in listOf(AdminRoute.Form, AdminRoute.Details)) previousListRoute else route
    
    val role = when (session) {
        is UserSession.EmployeeSession -> session.role
        is UserSession.OutletSession -> session.role
    } ?: return false

    val allowedRoutes = when (role) {
        OutletRole.OWNER -> AdminRoute.values().toSet()
        OutletRole.HEAD -> AdminRoute.values().toSet() - AdminRoute.Payments
        else -> setOf(
            AdminRoute.Dashboard,
            AdminRoute.Orders,
            AdminRoute.OrderItems,
            AdminRoute.Products,
            AdminRoute.OutletMenu,
            AdminRoute.Offers,
            AdminRoute.Banners,
            AdminRoute.Attendance,
            AdminRoute.Profile
        )
    }

    return effectiveRoute in allowedRoutes && effectiveRoute.gatedFeature(previousListRoute).isAccessibleBy(session)
}
