package com.jp.foodyvilla_backoffice.presentation.new_backoffice

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.OutletManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletFormWorkspaceScreen(
    editId: Long?, // Passed as null on Add, or an ID value on Edit routes
    viewModel: OutletManagementViewModel,
    onNavigateBackAction: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Initialize individual text field variables based on the active routing node path context
    LaunchedEffect(editId) {
        viewModel.setupOutletFormWorkspace(editId)
    }

    // Permission launcher contract to re-trigger prompt sequences smoothly
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val granted = permissionsMap[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissionsMap[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.acquireOutletDeviceLocation()
        }
    }
    // Native single file selector triggers mapped to specific ViewModel slots
    val logoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.uploadOutletLogo(context, it) }
        }
    val bannerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.uploadOutletBanner(context, it) }
        }
    AnimatedVisibility(
        visible = state.locationUiState.isFetchingLocation,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Dark translucent shade to intercept background taps completely
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}, // Intentional blank block to block tap-through leaks
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Locking GPS Coordinates...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // =====================================================================
    // 2. HARDWARE GPS SENSOR DISABLED DIALOG
    // =====================================================================
    if (state.locationUiState.showGpsDisabledDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissGpsDialog,
            title = {
                Text(text = "Location Services Disabled", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Your device's high-accuracy GPS hardware sensor is switched off. Please toggle it on inside your system configurations to map your exact outlet delivery zone coordinates.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissGpsDialog()
                        // Native intent redirection to trigger the device settings dashboard directly
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissGpsDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    // =====================================================================
    // 3. SYSTEM ACCESS PERMISSIONS RATIONALE DIALOG
    // =====================================================================
    if (state.locationUiState.showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPermissionRationale,
            title = {
                Text(text = "GPS Permissions Required", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("FoodyVilla requires system location permissions to establish geofencing parameters for staff attendance records and automatic delivery coordinate checking.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissPermissionRationale()
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPermissionRationale) {
                    Text("Not Now")
                }
            }
        )}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editId == null) "Open New Branch Unit" else "Update Branch Parameters",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackAction) {
                        Icon(
                            Icons.Default.ArrowBack,
                            null
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LOGO BRANDING MEDIA PICKER BLOCK
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = state.oUploadedLogoUrl ?: "https://placehold.co/120x120.png",
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Button(onClick = { logoLauncher.launch("image/*") }) {
                        Icon(
                            Icons.Default.CloudUpload,
                            null
                        ); Spacer(Modifier.width(6.dp)); Text("Upload Logo")
                    }
                }

                // COVER BANNER MEDIA PICKER BLOCK
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = state.oUploadedBannerUrl ?: "https://placehold.co/320x120.png",
                        contentDescription = null,
                        modifier = Modifier
                            .size(width = 130.dp, height = 55.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Button(onClick = { bannerLauncher.launch("image/*") }) {
                        Icon(
                            Icons.Default.CloudUpload,
                            null
                        ); Spacer(Modifier.width(6.dp)); Text("Upload Banner")
                    }
                }

                OutlinedTextField(
                    value = state.oName,
                    onValueChange = viewModel::onONameChanged,
                    label = { Text("Outlet Brand Unit Label Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.oCity,
                        onValueChange = viewModel::onOCityChanged,
                        label = { Text("City Node Location") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.oPhone,
                        onValueChange = viewModel::onOPhoneChanged,
                        label = { Text("Operational Contact Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = state.oEmail,
                    onValueChange = viewModel::onOEmailChanged,
                    label = { Text("Corporate Billing Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.oLat,
                        onValueChange = viewModel::onOLatChanged,
                        label = { Text("Latitude Coordinate") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.oLng,
                        onValueChange = viewModel::onOLngChanged,
                        label = { Text("Longitude Coordinate") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.oOpensAt,
                        onValueChange = viewModel::onOOpensAtChanged,
                        label = { Text("Opening Hours (HH:MM:SS)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.oClosesAt,
                        onValueChange = viewModel::onOClosesAtChanged,
                        label = { Text("Closing Hours (HH:MM:SS)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = state.oRadius,
                    onValueChange = viewModel::onORadiusChanged,
                    label = { Text("Logistics Geofencing Radius (KM)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.oRazorPayKey,
                    onValueChange = viewModel::onORazorPayKeyChanged,
                    label = { Text("Razorpay API Merchant Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.oAddress,
                    onValueChange = viewModel::onOAddressChanged,
                    label = { Text("Physical Mailing Address Location Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = state.oAttendanceRadius.toString(),
                    onValueChange = viewModel::onOAttendanceRadiusChanged,
                    label = { Text("Physical Employee attendance radius.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                val isFetching = state.locationUiState.isFetchingLocation

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.acquireOutletDeviceLocation() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isFetching, // Prevents overlapping hardware thread requests
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isFetching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Locking GPS Satellites...",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "GPS Sync Icon",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Fetch Current GPS Coordinates",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Contextual error notification block under the button asset row
                    AnimatedVisibility(visible = state.locationUiState.locationErrorMessage != null) {
                        Text(
                            text = state.locationUiState.locationErrorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Outlet active operations",
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = state.oIsActive,
                        onCheckedChange = viewModel::onOIsActiveChanged
                    )
                }
                if (state.errorText != null) {
                    Text(
                        state.errorText.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.commitOutletAction(onSuccess = onNavigateBackAction) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Commit Profile Records Updates", fontWeight = FontWeight.Bold) }
            }
        }
    }
}