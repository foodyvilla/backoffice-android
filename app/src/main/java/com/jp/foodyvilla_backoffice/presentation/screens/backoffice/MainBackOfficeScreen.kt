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
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.attendance.AttendanceCalendarScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.attendance.PunchReportListItem
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.attendance.PunchReportScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.banners.BannerDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.banners.BannerScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.cart.CartDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.cart.CartScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.customers.CustomerDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.customers.CustomerScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.employees.EmployeeDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.employees.EmployeeScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.forms.*
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.menu.OutletMenuDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.menu.OutletMenuScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.offers.OfferDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.offers.OfferScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.order_items.OrderItemDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.order_items.OrderItemScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders.OrderDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders.OrderScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.outlets.OutletDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.outlets.OutletScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.payments.PaymentDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.payments.PaymentScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.products.ProductDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.products.ProductScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.reviews.ReviewDetailScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.reviews.ReviewScreen
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.utils.GlobalOrderNotification
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
    loginViewModel: LoginViewModel = koinViewModel(),
    orderViewModel: com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders.OrderViewModel = koinViewModel(),
    productViewModel: com.jp.foodyvilla_backoffice.presentation.screens.backoffice.products.ProductViewModel = koinViewModel(),
    marketingViewModel: com.jp.foodyvilla_backoffice.presentation.screens.backoffice.offers.MarketingViewModel = koinViewModel(),
    teamViewModel: com.jp.foodyvilla_backoffice.presentation.screens.backoffice.employees.TeamViewModel = koinViewModel(),
    customerViewModel: com.jp.foodyvilla_backoffice.presentation.screens.backoffice.customers.CustomerViewModel = koinViewModel(),
    reviewViewModel: com.jp.foodyvilla_backoffice.presentation.screens.backoffice.reviews.ReviewViewModel = koinViewModel(),
    financeViewModel: com.jp.foodyvilla_backoffice.presentation.screens.backoffice.payments.FinanceViewModel = koinViewModel(),
    outletViewModel: com.jp.foodyvilla_backoffice.presentation.screens.backoffice.outlets.OutletViewModel = koinViewModel(),
    dashboardViewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val orderState by orderViewModel.uiState.collectAsStateWithLifecycle()
    val productState by productViewModel.uiState.collectAsStateWithLifecycle()
    val marketingState by marketingViewModel.uiState.collectAsStateWithLifecycle()
    val teamState by teamViewModel.uiState.collectAsStateWithLifecycle()
    val customerState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val reviewState by reviewViewModel.uiState.collectAsStateWithLifecycle()
    val financeState by financeViewModel.uiState.collectAsStateWithLifecycle()
    val outletState by outletViewModel.uiState.collectAsStateWithLifecycle()
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val pendingOrderIds = state.pendingOrders.map { it.id.orEmpty() }.toSet()
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
    var cancelledOrderIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(state.rows) {
        val cancelled = state.rows.filter { 
            it["status"].toDisplayText().normalizeOrderStatus().lowercase() == "cancelled" 
        }.map { it["id"].toDisplayText() }.toSet()
        
        val newCancelled = cancelled - cancelledOrderIds
        if (newCancelled.isNotEmpty()) {
            runCatching {
                RingtoneManager.getRingtone(
                    context,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                )?.play()
            }
        }
        cancelledOrderIds = cancelled
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        // viewModel.refresh() // Removed to avoid loading all data at once
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
        val refactoredRoutes = setOf(
            AdminRoute.Orders, AdminRoute.OrderItems, AdminRoute.Products, 
            AdminRoute.Banners, AdminRoute.Offers, AdminRoute.Employees, 
            AdminRoute.PunchReport, AdminRoute.Customers, AdminRoute.Reviews,
            AdminRoute.Payments, AdminRoute.Cart, AdminRoute.OutletMenu,
            AdminRoute.Outlets, AdminRoute.Categories
        )
        if (currentRoute !in refactoredRoutes) {
            currentRoute.tableName?.let { tableName ->
                state.tables.firstOrNull { it.name == tableName }?.let(viewModel::selectTable)
            }
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
                        
                        currentRoute == AdminRoute.Dashboard -> {
                            LaunchedEffect(Unit) { dashboardViewModel.loadDashboardData() }
                            DashboardScreen(
                                state = state.copy(
                                    dashboardData = dashboardState.dashboardData,
                                    dashboardRows = dashboardState.dashboardRows,
                                    isLoading = dashboardState.isLoading
                                ),
                                onOpenRoute = { next ->
                                    if (session.isRouteAllowed(next, currentRoute)) {
                                        currentRoute = next
                                        previousListRoute = next
                                    }
                                }
                            )
                        }

                        currentRoute == AdminRoute.Banners -> {
                            LaunchedEffect(Unit) { marketingViewModel.loadBanners() }
                            BannerScreen(
                                session = session,
                                state = state.copy(
                                    rows = marketingState.rawRows,
                                    isLoading = marketingState.isLoading,
                                    searchQuery = marketingState.searchQuery
                                ),
                                onSearch = marketingViewModel::setSearchQuery,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Offers -> {
                            LaunchedEffect(Unit) { marketingViewModel.loadOffers() }
                            OfferScreen(
                                session = session,
                                state = state.copy(
                                    rows = marketingState.rawRows,
                                    isLoading = marketingState.isLoading,
                                    searchQuery = marketingState.searchQuery
                                ),
                                onSearch = marketingViewModel::setSearchQuery,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Products -> {
                            LaunchedEffect(Unit) { productViewModel.loadProducts() }
                            ProductScreen(
                                session = session,
                                state = state.copy(
                                    rows = productState.rawRows,
                                    isLoading = productState.isLoading,
                                    searchQuery = productState.searchQuery
                                ),
                                onSearch = productViewModel::setSearchQuery,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Orders -> {
                            LaunchedEffect(Unit) { orderViewModel.loadOrders() }
                            OrderScreen(
                                session = session,
                                state = state.copy(
                                    orders = orderState.orders,
                                    rows = orderState.rawRows,
                                    isLoading = orderState.isLoading,
                                    searchQuery = orderState.searchQuery,
                                    orderDateFilter = orderState.dateFilter,
                                    orderStatusFilter = orderState.statusFilter
                                ),
                                onSearch = orderViewModel::setSearchQuery,
                                onOrderDateChange = orderViewModel::setDateFilter,
                                onOrderStatusFilterChange = orderViewModel::setStatusFilter,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOrderStatusChange = { row, status ->
                                    orderViewModel.updateStatus(row["id"].toDisplayText(), status)
                                },
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Customers -> {
                            LaunchedEffect(Unit) { customerViewModel.loadCustomers() }
                            CustomerScreen(
                                session = session,
                                state = state.copy(
                                    rows = customerState.rawRows,
                                    isLoading = customerState.isLoading,
                                    searchQuery = customerState.searchQuery
                                ),
                                onSearch = customerViewModel::setSearchQuery,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                    row["id"]?.toDisplayText()?.let { customerViewModel.loadCustomerDetails(it) }
                                }
                            )
                        }

                        currentRoute == AdminRoute.Reviews -> {
                            LaunchedEffect(Unit) { reviewViewModel.loadReviews() }
                            ReviewScreen(
                                session = session,
                                state = state.copy(
                                    rows = reviewState.rawRows,
                                    isLoading = reviewState.isLoading,
                                    searchQuery = reviewState.searchQuery
                                ),
                                onSearch = reviewViewModel::setSearchQuery,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Employees -> {
                            LaunchedEffect(Unit) { teamViewModel.loadEmployees() }
                            EmployeeScreen(
                                session = session,
                                state = state.copy(
                                    rows = teamState.rawRows,
                                    isLoading = teamState.isLoading,
                                    searchQuery = teamState.searchQuery
                                ),
                                onSearch = teamViewModel::setSearchQuery,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Outlets -> {
                            LaunchedEffect(Unit) { outletViewModel.loadOutlets() }
                            OutletScreen(
                                session = session,
                                state = state.copy(
                                    rows = outletState.rawRows,
                                    isLoading = outletState.isLoading,
                                    searchQuery = outletState.searchQuery
                                ),
                                onSearch = outletViewModel::setSearchQuery,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Payments -> {
                            LaunchedEffect(Unit) { financeViewModel.loadPayments() }
                            PaymentScreen(
                                session = session,
                                state = state.copy(
                                    rows = financeState.rawRows,
                                    isLoading = financeState.isLoading,
                                    searchQuery = financeState.searchQuery
                                ),
                                onSearch = financeViewModel::setSearchQuery,
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Cart -> {
                            LaunchedEffect(Unit) { financeViewModel.loadCartItems() }
                            CartScreen(
                                session = session,
                                state = state.copy(
                                    rows = financeState.rawRows,
                                    isLoading = financeState.isLoading,
                                    searchQuery = financeState.searchQuery
                                ),
                                onSearch = financeViewModel::setSearchQuery,
                                onSendOfferToAll = viewModel::sendOfferToCart,
                                onSendFcm = viewModel::sendFcmToToken,
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.OutletMenu -> {
                            LaunchedEffect(Unit) { productViewModel.loadOutletMenu() }
                            OutletMenuScreen(
                                session = session,
                                state = state.copy(
                                    rows = productState.rawRows,
                                    isLoading = productState.isLoading,
                                    searchQuery = productState.searchQuery
                                ),
                                onSearch = productViewModel::setSearchQuery,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.OrderItems -> {
                            LaunchedEffect(Unit) { orderViewModel.loadAllOrderItems() }
                            OrderItemScreen(
                                session = session,
                                state = state.copy(
                                    rows = orderState.rawRows,
                                    isLoading = orderState.isLoading,
                                    searchQuery = orderState.searchQuery
                                ),
                                onSearch = orderViewModel::setSearchQuery,
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.PunchReport -> {
                            LaunchedEffect(Unit) { teamViewModel.loadAttendance() }
                            PunchReportScreen(
                                session = session,
                                state = state.copy(
                                    rows = teamState.rawRows,
                                    isLoading = teamState.isLoading,
                                    attendanceSearchQuery = teamState.attendanceSearchQuery,
                                    attendanceDateFilter = teamState.attendanceDateFilter,
                                    attendanceOutletFilter = teamState.attendanceOutletFilter
                                ),
                                onSearch = teamViewModel::setAttendanceSearch,
                                onDateChange = teamViewModel::setAttendanceDateFilter,
                                onOutletChange = teamViewModel::setAttendanceOutletFilter,
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Categories -> {
                            LaunchedEffect(Unit) { productViewModel.loadCategories() }
                            BackOfficeListScreen(
                                session = session,
                                route = currentRoute,
                                state = state.copy(
                                    rows = productState.rawRows,
                                    isLoading = productState.isLoading,
                                    searchQuery = productState.searchQuery
                                ),
                                onSearch = productViewModel::setSearchQuery,
                                onOrderDateChange = viewModel::updateOrderDateFilter,
                                onOrderStatusFilterChange = viewModel::updateOrderStatusFilter,
                                onAttendanceDateChange = viewModel::updateAttendanceDateFilter,
                                onAttendanceOutletChange = viewModel::updateAttendanceOutletFilter,
                                onAttendanceSearch = viewModel::updateAttendanceSearch,
                                onPunchIn = viewModel::punchIn,
                                onPunchOut = viewModel::punchOut,
                                onSendFcm = viewModel::sendFcmToToken,
                                onSendOfferToAll = viewModel::sendOfferToCart,
                                onCreate = {
                                    previousListRoute = currentRoute
                                    selectedRow = null
                                    formMode = FormMode.Create
                                    viewModel.startCreate()
                                    currentRoute = AdminRoute.Form
                                },
                                onOrderStatusChange = viewModel::updateOrderStatus,
                                onOpenDetails = { row ->
                                    previousListRoute = currentRoute
                                    selectedRow = row
                                    currentRoute = AdminRoute.Details
                                }
                            )
                        }

                        currentRoute == AdminRoute.Analytics -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Analytics Screen (Coming Soon)") }
                        currentRoute == AdminRoute.Notifications -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Notifications Screen (Coming Soon)") }
                        currentRoute == AdminRoute.Settings -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Settings Screen (Coming Soon)") }
                        currentRoute == AdminRoute.Categories -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Categories Screen (Coming Soon)") }

                        currentRoute == AdminRoute.Details -> {
                            when (previousListRoute) {
                                AdminRoute.Banners -> BannerDetailScreen(
                                    session = session,
                                    row = selectedRow,
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.Offers -> OfferDetailScreen(
                                    session = session,
                                    row = selectedRow,
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.Products -> ProductDetailScreen(
                                    session = session,
                                    row = selectedRow,
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.Orders -> OrderDetailScreen(
                                    session = session,
                                    row = selectedRow,
                                    orderItems = orderState.orderItemsByOrderId[selectedRow?.get("id").toDisplayText()].orEmpty(),
                                    productsById = orderState.productsById,
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.Customers -> CustomerDetailScreen(
                                    session = session,
                                    row = selectedRow,
                                    customerOrders = customerState.customerOrders,
                                    customerCart = customerState.customerCart,
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.Reviews -> ReviewDetailScreen(
                                    session = session,
                                    row = selectedRow,
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.Employees -> EmployeeDetailScreen(
                                    session = session,
                                    row = selectedRow,
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.Outlets -> OutletDetailScreen(
                                    session = session,
                                    row = selectedRow,
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.Payments -> PaymentDetailScreen(
                                    row = selectedRow
                                )
                                AdminRoute.Cart -> CartDetailScreen(
                                    row = selectedRow
                                )
                                AdminRoute.OrderItems -> OrderItemDetailScreen(
                                    row = selectedRow
                                )
                                AdminRoute.PunchReport -> BackOfficeDetailScreen(
                                    session = session,
                                    table = state.selectedTable,
                                    row = selectedRow,
                                    orderItems = emptyList(),
                                    productsById = emptyMap(),
                                    onBack = { currentRoute = previousListRoute },
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.OutletMenu -> OutletMenuDetailScreen(
                                    session = session,
                                    row = selectedRow,
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                AdminRoute.Categories -> BackOfficeDetailScreen(
                                    session = session,
                                    table = state.selectedTable,
                                    row = selectedRow,
                                    orderItems = emptyList(),
                                    productsById = emptyMap(),
                                    onBack = { currentRoute = previousListRoute },
                                    onEdit = {
                                        selectedRow?.let(viewModel::startEdit)
                                        formMode = FormMode.Edit
                                        currentRoute = AdminRoute.Form
                                    }
                                )
                                else -> BackOfficeDetailScreen(
                                    session = session,
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
                            }
                        }

                        currentRoute == AdminRoute.Form -> {
                            when (previousListRoute) {
                                AdminRoute.Orders -> OrderForm(
                                    session = session,
                                    state = state,
                                    onBack = { currentRoute = previousListRoute },
                                    onFormChange = viewModel::updateFormValue,
                                    onAddDraftItem = viewModel::addDraftOrderItem,
                                    onRemoveDraftItem = viewModel::removeDraftOrderItem,
                                    onSave = {
                                        viewModel.save()
                                        currentRoute = previousListRoute
                                    }
                                )
                                AdminRoute.Products -> ProductForm(
                                    state = state,
                                    onBack = { currentRoute = previousListRoute },
                                    onFormChange = viewModel::updateFormValue,
                                    onSave = {
                                        viewModel.save()
                                        currentRoute = previousListRoute
                                    }
                                )
                                AdminRoute.Employees -> EmployeeForm(
                                    state = state,
                                    onBack = { currentRoute = previousListRoute },
                                    onFormChange = viewModel::updateFormValue,
                                    onUploadImage = { uri, column -> viewModel.uploadImage(context, uri, column) },
                                    onSave = {
                                        viewModel.save()
                                        currentRoute = previousListRoute
                                    }
                                )
                                AdminRoute.Banners -> BannerForm(
                                    session = session,
                                    state = state,
                                    onBack = { currentRoute = previousListRoute },
                                    onFormChange = viewModel::updateFormValue,
                                    onUploadImage = { uri, column -> viewModel.uploadImage(context, uri, column) },
                                    onSave = {
                                        viewModel.save()
                                        currentRoute = previousListRoute
                                    }
                                )
                                AdminRoute.Offers -> OfferForm(
                                    state = state,
                                    onBack = { currentRoute = previousListRoute },
                                    onFormChange = viewModel::updateFormValue,
                                    onUploadImage = { uri, column -> viewModel.uploadImage(context, uri, column) },
                                    onSave = {
                                        viewModel.save()
                                        currentRoute = previousListRoute
                                    }
                                )
                                else -> BackOfficeFormScreen(
                                    session = session,
                                    state = state,
                                    mode = formMode,
                                    onBack = { currentRoute = previousListRoute },
                                    onFormChange = viewModel::updateFormValue,
                                    onUploadImage = { uri, column -> viewModel.uploadImage(context, uri, column) },
                                    onCreateProduct = if (previousListRoute == AdminRoute.OutletMenu) {
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
                            }
                        }

                        currentRoute == AdminRoute.Attendance -> AttendanceCalendarScreen(
                            session = session,
                            state = state,
                            onPunchIn = viewModel::punchIn,
                            onPunchOut = viewModel::punchOut
                        )

                        currentRoute == AdminRoute.Profile -> {
                            LaunchedEffect(Unit) { dashboardViewModel.loadDashboardData() }
                            EmployeeProfileScreen(
                                session = session,
                                state = state.copy(
                                    dashboardRows = dashboardState.dashboardRows,
                                    isLoading = dashboardState.isLoading
                                )
                            )
                        }

                        else -> AccessDeniedView(currentRoute)
                    }
                }

                // Global Order Notification Overlay (Supabase Realtime)
                val visibleOrders = state.pendingOrders.filter { 
                    it.id !in dismissedOrderIds 
                }
                
                visibleOrders.firstOrNull()?.let { order ->
                    val orderId = order.id ?: ""
                    val items = state.orderItemsByOrderId[orderId].orEmpty()
                    
                    GlobalOrderNotification(
                        title = "New Order Received!",
                        orderId = orderId,
                        customerName = order.customerName ?: "Customer",
                        itemCount = items.sumOf { it.qty }.toInt().toString(),
                        amount = "Rs %.0f".format(items.sumOf { it.totalPrice }),
                        onDismiss = { dismissedOrderIds = dismissedOrderIds + orderId },
                        onAccept = {
                            // Find the original JsonObject to update status
                            state.pendingOrderRows.firstOrNull { it["id"].toDisplayText() == orderId }?.let { row ->
                                viewModel.updateOrderStatus(row, "accepted")

                                dismissedOrderIds = dismissedOrderIds + orderId
                            }
                        },
                        onView = {
                            state.pendingOrderRows.firstOrNull { it["id"].toDisplayText() == orderId }?.let { row ->
                                selectedRow = row
                                previousListRoute = AdminRoute.Orders
                                currentRoute = AdminRoute.Details
                            }
                        }
                    )
                }
            }
        }
    }
}

//@Composable
//fun GlobalOrderNotification(
//    title: String,
//    orderId: String,
//    customerName: String,
//    itemCount: String = "",
//    amount: String = "",
//    onDismiss: () -> Unit,
//    onAccept: () -> Unit,
//    onView: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//            .statusBarsPadding(),
//        elevation = CardDefaults.cardElevation(8.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
//                Spacer(Modifier.width(8.dp))
//                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//            }
//            Spacer(Modifier.height(8.dp))
//            Text("Order #${orderId.take(8)} from $customerName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
//            if (itemCount.isNotBlank() && itemCount != "0") {
//                Text("Items: $itemCount | Total: $amount", style = MaterialTheme.typography.bodySmall)
//            }
//            Spacer(Modifier.height(12.dp))
//            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
//                TextButton(onClick = onDismiss) { Text("Dismiss") }
//                TextButton(onClick = onView) { Text("View") }
//                Button(onClick = onAccept) { Text("Accept") }
//            }
//        }
//    }
//}

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
        OutletRole.OWNER -> AdminRoute.entries.toSet()
        OutletRole.HEAD, OutletRole.MANAGER -> {
            AdminRoute.entries.toSet() - AdminRoute.Outlets
        }
        else -> setOf(
            AdminRoute.Dashboard,
            AdminRoute.Orders,
            AdminRoute.OrderItems,
            AdminRoute.Products,
            AdminRoute.OutletMenu,
            AdminRoute.Categories,
            AdminRoute.Offers,
            AdminRoute.Banners,
            AdminRoute.Attendance,
            AdminRoute.Profile,
            AdminRoute.Customers,
            AdminRoute.Reviews,
            AdminRoute.Payments,
            AdminRoute.Cart
        )
    }

    if (effectiveRoute == AdminRoute.PunchReport && role !in listOf(OutletRole.OWNER, OutletRole.HEAD, OutletRole.MANAGER)) {
        return false
    }
    
    if (effectiveRoute == AdminRoute.Employees && role !in listOf(OutletRole.OWNER, OutletRole.HEAD, OutletRole.MANAGER)) {
        return false
    }

    if (effectiveRoute == AdminRoute.Analytics && role !in listOf(OutletRole.OWNER, OutletRole.HEAD, OutletRole.MANAGER)) {
        return false
    }

    return effectiveRoute in allowedRoutes && effectiveRoute.gatedFeature(previousListRoute).isAccessibleBy(session)
}
