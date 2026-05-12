package com.jp.foodyvilla_backoffice.presentation.screens.cart

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.data.model.cart.CartItem
import com.jp.foodyvilla_backoffice.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.home.QuantitySelector
import com.jp.foodyvilla_backoffice.presentation.screens.home.VegDot
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import com.razorpay.Checkout
import kotlinx.coroutines.launch
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onBrowseMenu: () -> Unit,
    viewModel: HomeViewModel,
    loginViewModel: LoginViewModel
) {

    val context = LocalContext.current
    val user = loginViewModel.user.collectAsStateWithLifecycle().value

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.getCartItems()
    }
    LaunchedEffect(user) {
        if(user is UiState.Success){
            viewModel.updateCustomerDetailsForOrder(user = user.data)

        }
    }
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()

    var isSaving by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }


    when (val state = locationState) {

        is UiState.Success -> {

            isFetchingLocation = false
        }

        is UiState.Error -> {

            isFetchingLocation = false
            Toast.makeText(context, (locationState as UiState.Error).msg, Toast.LENGTH_SHORT).show()

        }

        is UiState.Loading -> {

            isFetchingLocation = true
        }

        else -> {
            isFetchingLocation = false
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            if (viewModel.isGpsEnabled()) {

                isFetchingLocation = true

                viewModel.fetchCurrentLocation()
            } else {
                showGpsDialog = true
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Location permission required.") }


            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {
                data = Uri.fromParts(
                    "package",
                    context.packageName,
                    null
                )
            }

            context.startActivity(intent)
        }
    }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
//    var customerName by remember { mutableStateOf("") }
//    var phone by remember { mutableStateOf("") }
//    var address by remember { mutableStateOf("") }
//    var instructions by remember { mutableStateOf("") }
//    var orderType by remember { mutableStateOf("Delivery") }

    // Track whether user has touched each field (so errors don't show before interaction)
    var nameTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var addressTouched by remember { mutableStateOf(false) }


    // Validation
    val nameError =
        if (nameTouched && orderState.customerName.isBlank()) "Name is required" else null
    val phoneError = when {
        phoneTouched && orderState.phone.isBlank() -> "Phone number is required"
        phoneTouched && !orderState.phone.matches(Regex("^[+]?[0-9]{7,15}$")) -> "Enter a valid phone number"
        else -> null
    }
    val addressError =
        if (orderState.orderType == "Delivery" && addressTouched && orderState.address.isBlank()) "Address is required for delivery" else null

    val isFormValid = orderState.customerName.isNotBlank()
            && orderState.phone.matches(Regex("^[+]?[0-9]{10,13}$"))
            && (orderState.orderType != "Delivery" || orderState.address.isNotBlank())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart", style = MaterialTheme.typography.headlineLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },

        bottomBar = {
            if (state.cartItems.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = {

                            initiatePayment(
                                context = context,
                                name = orderState.customerName,
                                contact = orderState.phone,
                                amount = (viewModel.getTotalCartValue() * 100).toString()
                            )
//                            viewModel.placeOrder(
//                                address = address,
//                                customerName = customerName,
//                                phone = phone,
//                                orderType = orderType,
//                                instruction = instructions
//                            )

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        Text(
                            "Place Order • ₹${"%.2f".format(viewModel.getTotalCartValue())}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isFormValid) Color.White
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }
    ) { padding ->

        if (state.cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Your cart is empty", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add something delicious!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onBrowseMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Browse Menu", color = Color.White)
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.cartItems, key = { it.products!!.id }) { cartItem ->
                CartItemCard(
                    cartItem = cartItem,
                    onIncrement = {

                        viewModel.updateCartItemQuantity(cartItem.products!!, cartItem.qty + 1)
                    },
                    onDecrement = {
                        viewModel.updateCartItemQuantity(cartItem.products!!, cartItem.qty - 1)
                    },
                    onRemove = { viewModel.removeFromCart(cartItem.products!!.id) }
                )
            }

            // Order details form
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Order Details", style = MaterialTheme.typography.titleLarge)

                        // Order Type chips — placed first so address field
                        // appears/disappears before the user reaches it
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Delivery", "Pickup", "Dine-In").forEach { type ->
                                FilterChip(
                                    selected = orderState.orderType == type,
                                    onClick = {
                                        viewModel.updateOrderType(type)
                                        // Reset address touched state when switching away from Delivery
                                        if (type != "Delivery") addressTouched = false
                                    },
                                    label = { Text(type) }
                                )
                            }
                        }

                        // Customer Name
                        OutlinedTextField(
                            value = orderState.customerName,
                            onValueChange = {
                                viewModel.updateCustomerName(it)
                                nameTouched = true
                            },
                            label = { Text("Customer Name *") },
                            isError = nameError != null,
                            supportingText = {
                                if (nameError != null) {
                                    Text(nameError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Phone Number
                        OutlinedTextField(
                            value = orderState.phone,
                            onValueChange = {
                                viewModel.updatePhone(it)
                                phoneTouched = true
                            },
                            label = { Text("Phone Number *") },
                            isError = phoneError != null,
                            supportingText = {
                                if (phoneError != null) {
                                    Text(phoneError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Address — required only for Delivery
                        if (orderState.orderType == "Delivery") {
                            OutlinedTextField(
                                value = orderState.address,
                                onValueChange = {
                                    viewModel.updateAddress(it)
                                    addressTouched = true
                                },
                                label = { Text("Delivery Address *") },
                                isError = addressError != null,
                                supportingText = {
                                    if (addressError != null) {
                                        Text(addressError, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                },
                                minLines = 2,
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            FilledTonalButton(
                                onClick = {

                                    println("clicked ")
                                    if (viewModel.hasLocationPermission()) {
                                        if (viewModel.isGpsEnabled()) {
                                            isFetchingLocation = true
                                            println("location permission and gps ")


                                            viewModel.fetchCurrentLocation()
                                        } else {
                                            showGpsDialog = true
                                        }
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small
                            ) {
                                if (isFetchingLocation) {
                                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Rounded.MyLocation, null, Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Deliver To  Current Location")
                                }
                            }
                        }

                        // Special Instructions — optional
                        OutlinedTextField(
                            value = orderState.instructions,
                            onValueChange = { viewModel.updateInstructions(it) },
                            label = { Text("Special Instructions") },
                            placeholder = { Text("Optional — e.g. extra spicy, no onions…") },
                            leadingIcon = {
                                Icon(Icons.Default.EditNote, contentDescription = null)
                            },
                            minLines = 2,
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Order summary
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Order Summary", style = MaterialTheme.typography.titleLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "₹${"%.2f".format(viewModel.getTotalCartValue())}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            confirmButton = {
                Button(onClick = {
                    showGpsDialog = false
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) { Text("Open Settings") }
            },
            title = { Text("GPS Disabled") },
            text = { Text("Please turn on your device location to auto-detect your address.") }
        )
    }
}


fun initiatePayment(
    context: Context,
    name: String,
    email: String = "",
    contact: String,
    amount: String,
) {

    try {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_ShBw7mlCM6gT6y") // ✅ dummy test key

        val options = JSONObject().apply {
            put("name", "FoodyVilla") // App name
            put("description", "Online Order")
            put("currency", "INR")
            put("amount", amount) // ₹499.00 (amount in paise)
            put("theme.color", "#E23744")

            put("prefill", JSONObject().apply {
                put("name", name)
                put("email", email)
                put("contact", contact)
            })


        }

        checkout.open(context as Activity, options)
    } catch (e: Exception) {
        println("Payment Error $e")
    }

}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = cartItem.products!!.image[0],
                contentDescription = cartItem.products!!.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VegDot(isVeg = cartItem.products?.isVeg ?: false)
                    Text(
                        cartItem.products?.name ?: "N/A",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                }
                Text(
                    "₹${cartItem.products?.price}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                QuantitySelector(
                    quantity = cartItem.qty,
                    onDecrement = onDecrement,
                    onIncrement = onIncrement
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    "₹${"%.2f".format(cartItem.totalPrice)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

