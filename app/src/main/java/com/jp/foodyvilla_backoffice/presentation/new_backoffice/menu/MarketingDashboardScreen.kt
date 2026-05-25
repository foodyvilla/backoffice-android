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

@Serializable
data class NewBannerUiModel(
    val id: Long,
    val outletId: Long?,
    val title: String?,
    val imageUrl: String,
    val displayOrder: Int
)

@Serializable
data class NewOfferUiModel(
    val id: String,
    val outletId: Long?,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val linkedUrl: String?,
    val expiresAt: String?
)

@Serializable
data class OutletDropdownUiModel(
    val id: Long,
    val name: String
)

@Serializable
private data class BannerResponse(
    val id: Long,
    val outlet_id: Long? = null,
    val title: String? = null,
    val img_url: String? = null,
    val display_order: Int
)

@Serializable
private data class OfferResponse(
    val id: String,
    val outlet_id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val img_url: String? = null,
    val linked_url: String? = null,
    val expires_at: String? = null
)

@Serializable
private data class OutletResponse(
    val id: Long,
    val name: String
)

private fun BannerResponse.toUiModel(): NewBannerUiModel = NewBannerUiModel(
    id = id, outletId = outlet_id, title = title, imageUrl = img_url.orEmpty(), displayOrder = display_order
)

private fun OfferResponse.toUiModel(): NewOfferUiModel = NewOfferUiModel(
    id = id, outletId = outlet_id, title = title, description = description, imageUrl = img_url, linkedUrl = linked_url, expiresAt = expires_at
)

// ==========================================
// Marketing Repository Implementation
// ==========================================

class MarketingRepository(private val supabase: SupabaseClient) {

    private companion object {
        const val TAG = "MarketingRepository"
    }

    suspend fun getBanners(outletId: Long?): List<NewBannerUiModel> {
        return try {
            supabase.from("banners").select {
                if (outletId != null) {
                    filter { eq("outlet_id", outletId) }
                }
                order(column = "display_order", order = Order.ASCENDING)
            }.decodeList<BannerResponse>().map { it.toUiModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching banners for outletId=$outletId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getActiveOffers(outletId: Long?): List<NewOfferUiModel> {
        return try {
            supabase.from("offers").select {
                if (outletId != null) {
                    filter { eq("outlet_id", outletId) }
                }
                order(column = "created_at", order = Order.DESCENDING)
            }.decodeList<OfferResponse>().map { it.toUiModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active offers for outletId=$outletId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchOutletsDropdown(): List<OutletDropdownUiModel> {
        return try {
            supabase.from("outlets").select {
                filter { eq("is_active", true) }
            }.decodeList<OutletResponse>().map { OutletDropdownUiModel(id = it.id, name = it.name) }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching outlets dropdown listings: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun uploadMarketingImage(bucketName: String, fileBytes: ByteArray, fileName: String): String {
        val bucket = supabase.storage.from(bucketName)
        val path = "uploads/$fileName"
        bucket.upload(path, fileBytes) { upsert = true }
        return bucket.publicUrl(path)
    }

    suspend fun upsertBanner(id: Long?, outletId: Long?, title: String?, imageUrl: String, displayOrder: Int) {
        try {
            if (id == null) {
                // 1. FOR INSERT OPERATIONS: Create a payload completely omitting the id field context
                val insertPayload = buildJsonObject {
                    put("outlet_id", outletId)
                    put("title", title)
                    put("img_url", imageUrl)
                    put("display_order", displayOrder)
                }
                // Explicit insert removes 'id' from the generated columns query string parameter
                supabase.from("banners").insert(insertPayload)
            } else {
                // 2. FOR UPDATE OPERATIONS: Exclude 'id' from the payload. 
                // Identity columns defined as GENERATED ALWAYS cannot be updated.
                val updatePayload = buildJsonObject {
                    put("outlet_id", outletId)
                    put("title", title)
                    put("img_url", imageUrl)
                    put("display_order", displayOrder)
                }
                supabase.from("banners").update(updatePayload) {
                    filter { eq("id", id) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing banner upsert execution: ${e.message}", e)
            throw e
        }
    }

    suspend fun upsertOffer(
        id: String?, outletId: Long?, title: String?, description: String?,
        imageUrl: String?, linkedUrl: String?, expiresAt: String?
    ) {
        try {
            val payload = buildJsonObject {
                put("id", id ?: UUID.randomUUID().toString())
                put("outlet_id", outletId)
                put("title", title)
                put("description", description)
                put("img_url", imageUrl)
                put("linked_url", linkedUrl)
                put("expires_at", expiresAt)
            }
            supabase.from("offers").upsert(payload)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing offer upsert execution: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteBanner(id: Long) {
        try {
            supabase.from("banners").delete { filter { eq("id", id) } }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting banner configuration with id=$id: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteOffer(id: String) {
        try {
            supabase.from("offers").delete { filter { eq("id", id) } }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting promotional offer with id=$id: ${e.message}", e)
            throw e
        }
    }
}

// ==========================================
// ViewModels State Management Layer
// ==========================================

data class MarketingUiState(
    val banners: List<NewBannerUiModel> = emptyList(),
    val offers: List<NewOfferUiModel> = emptyList(),
    val outlets: List<OutletDropdownUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val operationSuccess: Boolean = false,
    val errorMessage: String? = null,

    val isWriteAllowed: Boolean = false,
    val isOwnerUser: Boolean = false,
    val targetSelectedOutlet: OutletDropdownUiModel? = null,

    // Form States
    val bannerTitle: String = "",
    val bannerImageUrl: String = "",
    val bannerDisplayOrder: String = "0",

    val offerTitle: String = "",
    val offerDescription: String = "",
    val offerImageUrl: String = "",
    val offerLinkedUrl: String = "",
    val offerExpiresAt: String = ""
)

class MarketingViewModel(
    private val repository: MarketingRepository,
    private val backOfficeAuthRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketingUiState())
    val uiState = _uiState.asStateFlow()

    private var sessionOutletId: Long? = null
    private var currentRole: String? = null

    init {
        viewModelScope.launch {
            backOfficeAuthRepository.currentSession.collectLatest { session ->
                val employeeSession = session as? UserSession.EmployeeSession
                sessionOutletId = employeeSession?.outletId
                currentRole = employeeSession?.role()?.lowercase()?.trim() ?: "employee"

                val writePermission = currentRole == "owner" || currentRole == "head"
                val ownerCheck = currentRole == "owner"

                _uiState.update {
                    it.copy(isWriteAllowed = writePermission, isOwnerUser = ownerCheck)
                }

                if (ownerCheck) {
                    val list = repository.fetchOutletsDropdown()
                    _uiState.update { it.copy(outlets = list, targetSelectedOutlet = null) }
                } else {
                    _uiState.update {
                        it.copy(targetSelectedOutlet = sessionOutletId?.let { id -> OutletDropdownUiModel(id, "Assigned Branch Workspace") })
                    }
                }
                refreshDashboardData()
            }
        }
    }

    // FIXED: Self-resetting error utility routine that updates state flags and clears them after 4 seconds
    private fun emitTemporaryError(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = message, isLoading = false) }
            delay(4000)
            _uiState.update { state ->
                if (state.errorMessage == message) state.copy(errorMessage = null) else state
            }
        }
    }

    fun clearFlags() { _uiState.update { it.copy(operationSuccess = false, errorMessage = null) } }

    fun updateSelectedOutletScope(outlet: OutletDropdownUiModel?) {
        if (!_uiState.value.isOwnerUser) return
        _uiState.update { it.copy(targetSelectedOutlet = outlet) }
        refreshDashboardData()
    }

    fun refreshDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val activeFilterId = _uiState.value.targetSelectedOutlet?.id
            runCatching {
                val bannersList = repository.getBanners(activeFilterId)
                val offersList = repository.getActiveOffers(activeFilterId)
                _uiState.update { it.copy(banners = bannersList, offers = offersList, isLoading = false) }
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Failed to refresh listing records")
            }
        }
    }

    fun uploadSelectedImage(bucketName: String, uri: Uri, context: Context) {
        if (!_uiState.value.isWriteAllowed) {
            emitTemporaryError("Access Denied: Insufficient authorization permissions.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()

                if (fileBytes != null) {
                    val uniqueFileName = "${UUID.randomUUID()}.jpg"
                    val generatedPublicUrl = repository.uploadMarketingImage(bucketName, fileBytes, uniqueFileName)

                    _uiState.update { state ->
                        if (bucketName == "banners") {
                            state.copy(bannerImageUrl = generatedPublicUrl, isLoading = false)
                        } else {
                            state.copy(offerImageUrl = generatedPublicUrl, isLoading = false)
                        }
                    }
                } else {
                    emitTemporaryError("Unable to process selected local media payload stream.")
                }
            } catch (e: Exception) {
                emitTemporaryError(e.localizedMessage ?: "Image file processing execution error.")
            }
        }
    }

    fun onBannerTitleChanged(v: String) { _uiState.update { it.copy(bannerTitle = v) } }
    fun onBannerImageUrlChanged(v: String) { _uiState.update { it.copy(bannerImageUrl = v) } }
    fun onBannerOrderChanged(v: String) { _uiState.update { it.copy(bannerDisplayOrder = v) } }

    fun prepareBannerForm(bannerId: Long?) {
        if (bannerId == null) {
            _uiState.update { it.copy(bannerTitle = "", bannerImageUrl = "", bannerDisplayOrder = "0") }
        } else {
            _uiState.value.banners.find { it.id == bannerId }?.let { item ->
                _uiState.update { it.copy(bannerTitle = item.title.orEmpty(), bannerImageUrl = item.imageUrl, bannerDisplayOrder = item.displayOrder.toString()) }
            }
        }
    }

    fun saveBanner(id: Long?) {
        viewModelScope.launch {
            if (_uiState.value.bannerImageUrl.isBlank()) {
                emitTemporaryError("Image asset configuration upload is mandatory")
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val executionOutletId = _uiState.value.targetSelectedOutlet?.id
            runCatching {
                repository.upsertBanner(
                    id = id, outletId = executionOutletId, title = _uiState.value.bannerTitle.ifBlank { null },
                    imageUrl = _uiState.value.bannerImageUrl, displayOrder = _uiState.value.bannerDisplayOrder.toIntOrNull() ?: 0
                )
            }.onSuccess {
                _uiState.update { it.copy(operationSuccess = true) }
                refreshDashboardData()
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Failed to write system configurations")
            }
        }
    }

    fun onOfferTitleChanged(v: String) { _uiState.update { it.copy(offerTitle = v) } }
    fun onOfferDescChanged(v: String) { _uiState.update { it.copy(offerDescription = v) } }
    fun onOfferImageUrlChanged(v: String) { _uiState.update { it.copy(offerImageUrl = v) } }
    fun onOfferLinkedUrlChanged(v: String) { _uiState.update { it.copy(offerLinkedUrl = v) } }
    fun onOfferExpiryChanged(v: String) { _uiState.update { it.copy(offerExpiresAt = v) } }

    fun prepareOfferForm(offerId: String?) {
        if (offerId == null) {
            _uiState.update { it.copy(offerTitle = "", offerDescription = "", offerImageUrl = "", offerLinkedUrl = "", offerExpiresAt = "") }
        } else {
            _uiState.value.offers.find { it.id == offerId }?.let { item ->
                _uiState.update { it.copy(offerTitle = item.title.orEmpty(), offerDescription = item.description.orEmpty(), offerImageUrl = item.imageUrl.orEmpty(), offerLinkedUrl = item.linkedUrl.orEmpty(), offerExpiresAt = item.expiresAt.orEmpty()) }
            }
        }
    }

    fun saveOffer(id: String?) {
        viewModelScope.launch {
            if (_uiState.value.offerTitle.isBlank()) {
                emitTemporaryError("Offer Title text heading parameter is mandatory")
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val executionOutletId = _uiState.value.targetSelectedOutlet?.id
            runCatching {
                repository.upsertOffer(
                    id = id, outletId = executionOutletId, title = _uiState.value.offerTitle,
                    description = _uiState.value.offerDescription.ifBlank { null }, imageUrl = _uiState.value.offerImageUrl.ifBlank { null },
                    linkedUrl = _uiState.value.offerLinkedUrl.ifBlank { null }, expiresAt = _uiState.value.offerExpiresAt.ifBlank { null }
                )
            }.onSuccess {
                _uiState.update { it.copy(operationSuccess = true) }
                refreshDashboardData()
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Failed to publish promotional changes")
            }
        }
    }

    fun removeBanner(id: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteBanner(id) }.onSuccess { refreshDashboardData() }.onFailure { emitTemporaryError(it.localizedMessage ?: "Delete failed") }
        }
    }

    fun removeOffer(id: String) {
        viewModelScope.launch {
            runCatching { repository.deleteOffer(id) }.onSuccess { refreshDashboardData() }.onFailure { emitTemporaryError(it.localizedMessage ?: "Delete failed") }
        }
    }

    private fun String.toLongOrNull(): Long? = this.replace(Regex("[^0-9]"), "").toLongOrNull()
}

// ==========================================
// Composable Interface Presentation Layer
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingTabsDashboardScreen(
    viewModel: MarketingViewModel,
    onNavigateToBannerForm: (Long?) -> Unit,
    onNavigateToOfferForm: (String?) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIdx by remember { mutableStateOf(0) }
    val tabLabels = listOf("Banners", "Offers")
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refreshDashboardData() }

    Scaffold(
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

