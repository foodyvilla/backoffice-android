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
import androidx.compose.material.icons.filled.Menu
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
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.DialogType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.OperationResultDialog
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
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
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

        // =====================================================================
        // OPERATION SUCCESS & ERROR DIALOGS
        // =====================================================================
        if (state.successMessage != null) {
            OperationResultDialog(
                type = DialogType.SUCCESS,
                title = "Success",
                message = state.successMessage.orEmpty(),
                onDismiss = viewModel::dismissSuccessDialog
            )
        }

        if (state.errorMessage != null) {
            OperationResultDialog(
                type = DialogType.ERROR,
                title = "Operation Failed",
                message = state.errorMessage.orEmpty(),
                onDismiss = viewModel::dismissErrorMessage
            )
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
        onResult = { uri -> if (uri != null) viewModel.uploadSelectedImage("banners", uri, context) }
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (state.bannerImageUrl.isNotBlank()) {
                        AsyncImage(model = state.bannerImageUrl, contentDescription = "Banner Image Preview", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = "Upload Banner Image", modifier = Modifier.size(48.dp))
                            Text("Tap to select image asset")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.bannerTitle, onValueChange = viewModel::onBannerTitleChanged,
                label = { Text("Banner Heading Title (Optional)") }, modifier = Modifier.fillMaxWidth(),
                enabled = state.isWriteAllowed, singleLine = true
            )

            OutlinedTextField(
                value = state.bannerDisplayOrder, onValueChange = viewModel::onBannerOrderChanged,
                label = { Text("Display Order Sequence (e.g. 1, 2, 3)") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = state.isWriteAllowed, singleLine = true
            )

            Button(
                onClick = { viewModel.saveBanner(bannerId) }, modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = state.isWriteAllowed && !state.isLoading
            ) {
                Text(if (state.isLoading) "Processing..." else "Publish Banner")
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) viewModel.uploadSelectedImage("offers", uri, context) }
    )

    LaunchedEffect(offerId) { viewModel.prepareOfferForm(offerId) }
    LaunchedEffect(state.operationSuccess) { if (state.operationSuccess) { viewModel.clearFlags(); onNavigateBack() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (offerId == null) "Add New Offer" else "Modify Offer Configuration") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp), enabled = state.isWriteAllowed,
                onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (state.offerImageUrl.isNotBlank()) {
                        AsyncImage(model = state.offerImageUrl, contentDescription = "Offer Image Preview", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = "Upload Offer Banner", modifier = Modifier.size(48.dp))
                            Text("Tap to select promo banner image (Optional)")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.offerTitle, onValueChange = viewModel::onOfferTitleChanged,
                label = { Text("Offer Title Heading") }, modifier = Modifier.fillMaxWidth(),
                enabled = state.isWriteAllowed, singleLine = true
            )

            OutlinedTextField(
                value = state.offerDescription, onValueChange = viewModel::onOfferDescChanged,
                label = { Text("Offer Description Details") }, modifier = Modifier.fillMaxWidth(),
                enabled = state.isWriteAllowed, minLines = 2
            )

            OutlinedTextField(
                value = state.offerLinkedUrl, onValueChange = viewModel::onOfferLinkedUrlChanged,
                label = { Text("Action Deep Link URL (Optional)") }, modifier = Modifier.fillMaxWidth(),
                enabled = state.isWriteAllowed, singleLine = true
            )

            OutlinedTextField(
                value = state.offerExpiresAt, onValueChange = viewModel::onOfferExpiryChanged,
                label = { Text("Expiration Date (YYYY-MM-DD) (Optional)") }, modifier = Modifier.fillMaxWidth(),
                enabled = state.isWriteAllowed, singleLine = true
            )

            Button(
                onClick = { viewModel.saveOffer(offerId) }, modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = state.isWriteAllowed && !state.isLoading
            ) {
                Text(if (state.isLoading) "Processing..." else "Publish Offer")
            }
        }
    }
}