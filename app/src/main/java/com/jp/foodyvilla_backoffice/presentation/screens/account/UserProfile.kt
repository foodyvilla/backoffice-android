package com.jp.foodyvilla_backoffice.presentation.screens.account

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.model.user.UserProfile
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import kotlinx.coroutines.launch

// ─── Permission & Location Helpers ───────────────────────────────────────────


// ─── Main Screen Entry ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LoginViewModel,
    onSaveChanges: (UserProfile) -> Unit = {},
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val userState by viewModel.user.collectAsStateWithLifecycle()
    val userUpdateState by viewModel.updateState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.getUserProfile()
    }
    var showLoading by remember { mutableStateOf(false) }

    when (userUpdateState) {
        is UiState.Error -> {
            Toast.makeText(context, (userUpdateState as UiState.Error).msg, Toast.LENGTH_SHORT)
                .show()
            showLoading = false
        }

        UiState.Idle -> showLoading = false

        UiState.Loading -> showLoading = true

        is UiState.Success -> {
            Toast.makeText(context, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
            showLoading = false

        }
    }
    when (userState) {
        is UiState.Success -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                ProfileContent(
                    onSaveChanges = {
                        viewModel.updateProfile(it)
                    },
                    onLogout = onLogout,
                    onNavigateBack = onNavigateBack, viewModel = viewModel
                )

                if(showLoading){
                    CircularProgressIndicator()
                }
            }

        }

        is UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(

    onSaveChanges: (UserProfile) -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoginViewModel
) {
    val userProfile = viewModel.user.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    // Form State
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(userProfile) {

        if(userProfile is UiState.Success){

            name = userProfile.data.name ?: ""
            email = userProfile.data.email ?: ""
            phone = userProfile.data.phone ?: ""
            address = userProfile.data.address ?: ""
            lat = userProfile.data.lat
            lon = userProfile.data.long
        }

    }

    // UI Feedback State
    var isSaving by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }

    val saveScale by animateFloatAsState(if (isSaving) 0.96f else 1f, label = "save_anim")



    when (val state = locationState) {

        is UiState.Success -> {

            lat = state.data.first
            lon = state.data.second

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
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 10.dp) {
                Button(
                    onClick = {
                        isSaving = true
                        if (userProfile is UiState.Success) {

                            onSaveChanges(
                                userProfile.data.copy(
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    address = address,
                                    lat = lat,
                                    long = lon
                                )
                            )
                        }
                        isSaving = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                        .graphicsLayer { scaleX = saveScale; scaleY = saveScale },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isSaving) CircularProgressIndicator(
                        Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    else Text(
                        "Save Changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            // Header / Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(3.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1).uppercase().ifEmpty { "?" },
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = name.ifEmpty { "Welcome!" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Information Groups
            ProfileGroup(title = "Account Information", icon = Icons.Rounded.AccountCircle) {
                ProfileField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    icon = Icons.Rounded.Person
                )
                ProfileField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    icon = Icons.Rounded.Email,
                    type = KeyboardType.Email
                )
                ProfileField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    icon = Icons.Rounded.Phone,
                    type = KeyboardType.Phone
                )
            }

            ProfileGroup(title = "Delivery Details", icon = Icons.Rounded.Map) {
                ProfileField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Full Address",
                    icon = Icons.Rounded.Home,
                    singleLine = false
                )

                FilledTonalButton(
                    onClick = {
                        if (viewModel.hasLocationPermission()) {
                            if (viewModel.isGpsEnabled()) {
                                isFetchingLocation = true

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
                        Text("Detect Current Location")
                    }
                }

                if (lat != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.PinDrop,
                                null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lon)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                }) { Text("Cancel") }
            },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to sign out of your account?") },
            icon = { Icon(Icons.Rounded.Warning, null) }
        )
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

@Composable
private fun ProfileGroup(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    type: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.outline) },
        keyboardOptions = KeyboardOptions(keyboardType = type),
        singleLine = singleLine,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}