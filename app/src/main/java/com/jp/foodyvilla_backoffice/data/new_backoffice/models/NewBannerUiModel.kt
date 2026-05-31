package com.jp.foodyvilla_backoffice.data.new_backoffice.models

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
data class BannerResponse(
    val id: Long,
    val outlet_id: Long? = null,
    val title: String? = null,
    val img_url: String? = null,
    val display_order: Int
)

@Serializable
data class OfferResponse(
    val id: String,
    val outlet_id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val img_url: String? = null,
    val linked_url: String? = null,
    val expires_at: String? = null
)


fun BannerResponse.toUiModel(): NewBannerUiModel = NewBannerUiModel(
    id = id, outletId = outlet_id, title = title, imageUrl = img_url.orEmpty(), displayOrder = display_order
)

fun OfferResponse.toUiModel(): NewOfferUiModel = NewOfferUiModel(
    id = id, outletId = outlet_id, title = title, description = description, imageUrl = img_url, linkedUrl = linked_url, expiresAt = expires_at
)