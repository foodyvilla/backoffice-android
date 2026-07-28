package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.util.UUID
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.MarketingViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ==========================================
// Type-Safe Route Structure Destinations
// ==========================================

// ==========================================
// Domain & Data Models (DTOs)
// ==========================================


// ==========================================
// Marketing Repository Implementation
// ==========================================


// ==========================================
// ViewModels State Management Layer
// ==========================================



// ==========================================
// Composable Interface Presentation Layer
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingTabsDashboardScreen(
    viewModel: MarketingViewModel,
    onNavigateToBannerForm: (Long?) -> Unit,
    onNavigateToOfferForm: (String?) -> Unit,
    onMenuClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIdx by remember { mutableStateOf(0) }
    val tabLabels = listOf("Banners", "Offers")
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refreshDashboardData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marketing Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isWriteAllowed) {
                FloatingActionButton(onClick = {
                    if (selectedTabIdx == 0) onNavigateToBannerForm(null) else onNavigateToOfferForm(null)
                }) { Icon(Icons.Default.Add, contentDescription = "Create Item") }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            if (state.isOwnerUser) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.targetSelectedOutlet?.name ?: "All Outlets (Global Master View)",
                            onValueChange = {}, readOnly = true, label = { Text("Business Location Filter Scope") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                            DropdownMenuItem(text = { Text("All Outlets (Global Master View)") }, onClick = { viewModel.updateSelectedOutletScope(null); dropdownExpanded = false })
                            state.outlets.forEach { outletItem ->
                                DropdownMenuItem(text = { Text(outletItem.name) }, onClick = { viewModel.updateSelectedOutletScope(outletItem); dropdownExpanded = false })
                            }
                        }
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTabIdx) {
                tabLabels.forEachIndexed { idx, title ->
                    Tab(selected = selectedTabIdx == idx, onClick = { selectedTabIdx = idx }, text = { Text(title) })
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIdx) {
                    0 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.banners, key = { it.id }) { banner ->
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    AsyncImage(
                                        model = banner.imageUrl, contentDescription = banner.title,
                                        modifier = Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop
                                    )
                                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = banner.title ?: "Untitled Sliders Image", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text(text = "Order Sequence Priority: ${banner.displayOrder}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (state.isWriteAllowed) {
                                            Row {
                                                IconButton(onClick = { onNavigateToBannerForm(banner.id) }) { Icon(Icons.Default.Edit, "Edit") }
                                                IconButton(onClick = { viewModel.removeBanner(banner.id) }) { Icon(Icons.Default.Delete, "Delete") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.offers, key = { it.id }) { offer ->
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    if (!offer.imageUrl.isNullOrBlank() && offer.imageUrl != "null") {
                                        AsyncImage(
                                            model = offer.imageUrl, contentDescription = offer.title,
                                            modifier = Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop
                                        )
                                    }
                                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = offer.title.orEmpty(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text(text = offer.description ?: "No description provided", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (!offer.expiresAt.isNullOrBlank() && offer.expiresAt != "null") {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = "Expiry Boundary: ${offer.expiresAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        if (state.isWriteAllowed) {
                                            Row {
                                                IconButton(onClick = { onNavigateToOfferForm(offer.id) }) { Icon(Icons.Default.Edit, "Edit") }
                                                IconButton(onClick = { viewModel.removeOffer(offer.id) }) { Icon(Icons.Default.Delete, "Delete") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBannerFormScreen(
    bannerId: Long?,
    viewModel: MarketingViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) viewModel.uploadSelectedImage("banner", uri, context) }
    )

    LaunchedEffect(bannerId) { viewModel.prepareBannerForm(bannerId) }
    LaunchedEffect(state.operationSuccess) { if (state.operationSuccess) { viewModel.clearFlags(); onNavigateBack() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (bannerId == null) "Add New Banner" else "Modify Banner Configuration") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp), enabled = state.isWriteAllowed,
                onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            ) {
                if (state.bannerImageUrl.isNotBlank()) {
                    AsyncImage(model = state.bannerImageUrl, contentDescription = "Preview", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(40.dp))
                            Text("Upload Image Layout graphic asset", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            OutlinedTextField(value = state.bannerTitle, onValueChange = viewModel::onBannerTitleChanged, label = { Text("Banner Title (Optional)") }, modifier = Modifier.fillMaxWidth(), readOnly = !state.isWriteAllowed)
            OutlinedTextField(value = state.bannerImageUrl, onValueChange = viewModel::onBannerImageUrlChanged, label = { Text("Banner Image URL Hosted Path Link") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
            OutlinedTextField(
                value = state.bannerDisplayOrder, onValueChange = viewModel::onBannerOrderChanged,
                label = { Text("Display Sequence Priority Order") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), readOnly = !state.isWriteAllowed
            )

            if (state.errorMessage != null) Text(text = state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
            if (state.isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.weight(1f))
            if (state.isWriteAllowed) {
                Button(onClick = { viewModel.saveBanner(bannerId) }, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading) {
                    Text("Save Infrastructure Banner Asset")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOfferFormScreen(
    offerId: String?,
    viewModel: MarketingViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) viewModel.uploadSelectedImage("offers", uri, context) }
    )

    LaunchedEffect(offerId) { viewModel.prepareOfferForm(offerId) }
    LaunchedEffect(state.operationSuccess) { if (state.operationSuccess) { viewModel.clearFlags(); onNavigateBack() } }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        val formatted = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE) + "T23:59:59Z"
                        viewModel.onOfferExpiryChanged(formatted)
                    }
                    showDatePickerDialog = false
                }) { Text("Confirm") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (offerId == null) "Create Deal Offer" else "Update Voucher Offer Spec") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().height(160.dp), enabled = state.isWriteAllowed, onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        if (state.offerImageUrl.isNotBlank()) {
                            AsyncImage(model = state.offerImageUrl, contentDescription = "Preview", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(40.dp))
                                    Text("Select & Upload Campaign Graphic Image", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
                item { OutlinedTextField(value = state.offerTitle, onValueChange = viewModel::onOfferTitleChanged, label = { Text("Offer Title") }, modifier = Modifier.fillMaxWidth(), readOnly = !state.isWriteAllowed) }
                item { OutlinedTextField(value = state.offerDescription, onValueChange = viewModel::onOfferDescChanged, label = { Text("Description Subtitle") }, modifier = Modifier.fillMaxWidth(), readOnly = !state.isWriteAllowed) }
                item { OutlinedTextField(value = state.offerImageUrl, onValueChange = viewModel::onOfferImageUrlChanged, label = { Text("Image Destination Remote Public Path") }, modifier = Modifier.fillMaxWidth(), readOnly = true) }
                item { OutlinedTextField(value = state.offerLinkedUrl, onValueChange = viewModel::onOfferLinkedUrlChanged, label = { Text("Deep-link Redirection Action Target URL") }, modifier = Modifier.fillMaxWidth(), readOnly = !state.isWriteAllowed) }
                item {
                    OutlinedTextField(
                        value = state.offerExpiresAt, onValueChange = {}, label = { Text("Expiry Timestamp boundary parameters") }, modifier = Modifier.fillMaxWidth(), readOnly = true,
                        trailingIcon = { if (state.isWriteAllowed) { IconButton(onClick = { showDatePickerDialog = true }) { Icon(Icons.Default.CalendarMonth, null) } } }
                    )
                }
                if (state.errorMessage != null) item { Text(text = state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error) }
                if (state.isLoading) item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            }
            if (state.isWriteAllowed) {
                Button(onClick = { viewModel.saveOffer(offerId) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), enabled = !state.isLoading) {
                    Text("Publish Dynamic Voucher Coupon")
                }
            }
        }
    }
}

