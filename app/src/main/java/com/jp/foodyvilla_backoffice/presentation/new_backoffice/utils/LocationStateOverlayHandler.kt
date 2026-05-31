package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import android.content.Intent
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceUiState
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AttendanceViewModel

@Composable
fun LocationStateOverlayHandler(
    state: AttendanceUiState,
    viewModel: AttendanceViewModel
) {
    val context = LocalContext.current

    // System Permission Request Contract Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val fineGranted = permissionsMap[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissionsMap[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (fineGranted || coarseGranted) {
            viewModel.dismissPermissionRationale()
            // Re-trigger location lock if permission was successfully authorized
            state.activeEmployee?.id?.let { empId ->
                // Assumes a check for punch-out state or triggers standard workflow
                viewModel.executeTransactionalPunchWorkflow(empId = empId, isPunchOut = false)
            }
        }
    }

    // =====================================================================
    // 1. FULL-SCREEN INTERACTION BLOCKER & PROGRESS OVERLAY
    // =====================================================================
    AnimatedVisibility(
        visible = state.locationUiState.isFetchingLocation,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Dark background tint to intercept background input taps completely
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}, // Blank block short-circuits tap-through leaks down to forms
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
                        text = "Locking High-Accuracy GPS...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // =====================================================================
    // 2. HARDWARE GPS DISABLED RUNTIME DIALOG
    // =====================================================================
    if (state.locationUiState.showGpsDisabledDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissGpsDialog,
            title = { 
                Text(text = "GPS Sensor Switched Off", fontWeight = FontWeight.Bold) 
            },
            text = { 
                Text("Hardware location services are switched off. Please enable device location services inside your system settings panel to authorize shift validation checks.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissGpsDialog()
                        // Native system intent launch to redirect directly to Location Panel
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
    // 3. RUNTIME PERMISSION RATIONALE CONTEXT DIALOG
    // =====================================================================
    if (state.locationUiState.showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPermissionRationale,
            title = { 
                Text(text = "Location Access Required", fontWeight = FontWeight.Bold) 
            },
            text = { 
                Text("This application requires high-precision GPS coordinate tracking parameters to authorize attendance logging records against your outlet's geofencing criteria coordinates.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Authorize")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPermissionRationale) {
                    Text("Cancel")
                }
            }
        )
    }
}