package com.jp.foodyvilla_backoffice.presentation.new_backoffice

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    // Native single file selector triggers mapped to specific ViewModel slots
    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadOutletLogo(context, it) }
    }
    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadOutletBanner(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editId == null) "Open New Branch Unit" else "Update Branch Parameters", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBackAction) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = state.oUploadedLogoUrl ?: "https://placehold.co/120x120.png", contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Button(onClick = { logoLauncher.launch("image/*") }) { Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(6.dp)); Text("Upload Logo") }
                }
                
                // COVER BANNER MEDIA PICKER BLOCK
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = state.oUploadedBannerUrl ?: "https://placehold.co/320x120.png", contentDescription = null, modifier = Modifier.size(width = 130.dp, height = 55.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                    Button(onClick = { bannerLauncher.launch("image/*") }) { Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(6.dp)); Text("Upload Banner") }
                }

                OutlinedTextField(value = state.oName, onValueChange = viewModel::onONameChanged, label = { Text("Outlet Brand Unit Label Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = state.oCity, onValueChange = viewModel::onOCityChanged, label = { Text("City Node Location") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = state.oPhone, onValueChange = viewModel::onOPhoneChanged, label = { Text("Operational Contact Phone") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = state.oEmail, onValueChange = viewModel::onOEmailChanged, label = { Text("Corporate Billing Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = state.oLat, onValueChange = viewModel::onOLatChanged, label = { Text("Latitude Coordinate") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = state.oLng, onValueChange = viewModel::onOLngChanged, label = { Text("Longitude Coordinate") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = state.oOpensAt, onValueChange = viewModel::onOOpensAtChanged, label = { Text("Opening Hours (HH:MM:SS)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = state.oClosesAt, onValueChange = viewModel::onOClosesAtChanged, label = { Text("Closing Hours (HH:MM:SS)") }, modifier = Modifier.weight(1f), singleLine = true)
                }

                OutlinedTextField(value = state.oRadius, onValueChange = viewModel::onORadiusChanged, label = { Text("Logistics Geofencing Radius (KM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = state.oRazorPayKey, onValueChange = viewModel::onORazorPayKeyChanged, label = { Text("Razorpay API Merchant Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = state.oAddress, onValueChange = viewModel::onOAddressChanged, label = { Text("Physical Mailing Address Location Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                if (state.errorText != null) { 
                    Text(state.errorText.orEmpty(), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) 
                }

                Button(
                    onClick = { viewModel.commitOutletAction(onSuccess = onNavigateBackAction) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Commit Profile Records Updates", fontWeight = FontWeight.Bold) }
            }
        }
    }
}