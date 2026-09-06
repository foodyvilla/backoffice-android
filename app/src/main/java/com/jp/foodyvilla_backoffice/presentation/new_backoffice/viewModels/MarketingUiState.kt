package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.NewBannerUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.NewOfferUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletDropdownUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.MarketingRepository
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MarketingUiState(
    val banners: List<NewBannerUiModel> = emptyList(),
    val offers: List<NewOfferUiModel> = emptyList(),
    val outlets: List<OutletDropdownUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val operationSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

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

    private fun emitTemporaryError(message: String) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }

    fun dismissSuccessDialog() { _uiState.update { it.copy(successMessage = null) } }
    fun dismissErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearFlags() { _uiState.update { it.copy(operationSuccess = false, errorMessage = null, successMessage = null) } }

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
                            state.copy(bannerImageUrl = generatedPublicUrl, isLoading = false, successMessage = "Image uploaded successfully!")
                        } else {
                            state.copy(offerImageUrl = generatedPublicUrl, isLoading = false, successMessage = "Image uploaded successfully!")
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
                _uiState.update { it.copy(operationSuccess = true, successMessage = if (id == null) "Banner created successfully!" else "Banner updated successfully!", isLoading = false) }
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
                _uiState.update { it.copy(operationSuccess = true, successMessage = if (id == null) "Offer promotion created successfully!" else "Offer promotion updated successfully!", isLoading = false) }
                refreshDashboardData()
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Failed to publish promotional changes")
            }
        }
    }

    fun removeBanner(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.deleteBanner(id) }
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Banner deleted successfully!", isLoading = false) }
                    refreshDashboardData()
                }
                .onFailure { emitTemporaryError(it.localizedMessage ?: "Delete failed") }
        }
    }

    fun removeOffer(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.deleteOffer(id) }
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Offer promotion deleted successfully!", isLoading = false) }
                    refreshDashboardData()
                }
                .onFailure { emitTemporaryError(it.localizedMessage ?: "Delete failed") }
        }
    }

    private fun String.toLongOrNull(): Long? = this.replace(Regex("[^0-9]"), "").toLongOrNull()
}