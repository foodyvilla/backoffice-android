package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletMenuItemUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCatalogUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.OutletManagementRepository
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.ProductCatalogRepository
import com.jp.foodyvilla_backoffice.data.repo.LocationRepository
import com.jp.foodyvilla_backoffice.data.repo.LocationUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OutletManagementUiState(
    val outletsList: List<OutletUiModel> = emptyList(),
    val currentOutletMenu: List<OutletMenuItemUiModel> = emptyList(),
    val globalProductCatalogOptions: List<ProductCatalogUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorText: String? = null,

    val outletSearchQuery: String = "",
    val menuSearchQuery: String = "",

    // Operational Form State Pointers
    val targetOutletId: Long? = null,
    val oName: String = "",
    val oAddress: String = "",
    val oCity: String = "",
    val oPhone: String = "",
    val oEmail: String = "",
    val oRadius: String = "5.0",
    val oLat: String = "21.1983",
    val oLng: String = "81.9614",
    val oOpensAt: String = "09:00:00",
    val oClosesAt: String = "23:00:00",
    val oRazorPayKey: String = "rzp_test_ShBw7mlCM6gT6y",
    val oUploadedLogoUrl: String? = null,
    val oUploadedBannerUrl: String? = null,
    val oIsActive: Boolean = true,
    val oAttendanceRadius : Int = 50,

    val targetMenuItemId: Long? = null,
    val mSelectedProduct: ProductCatalogUiModel? = null,
    val mPrice: String = "",
    val mDiscount: String = "0",
    val mHandlingCharges: String = "0.0",
    val mDeliveryCharges: String = "0.0",
    val mIsFreeDelivery: Boolean = false,
    val mIsAvailable: Boolean = true,
    val mIsOutOfStock: Boolean = false,
    val mMenuImagesList: List<String> = emptyList(),


    val locationUiState: LocationUiState = LocationUiState(),
    val canManageMenu: Boolean = false,
    val isOwner: Boolean = false
)

class OutletManagementViewModel(
    private val repository: OutletManagementRepository,
    private val catalogRepository: ProductCatalogRepository,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OutletManagementUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentSession.collect { session ->
                _state.update { 
                    it.copy(
                        canManageMenu = session?.canManageMenu() ?: false,
                        isOwner = session?.isOwner() ?: false
                    )
                }
            }
        }
    }

    fun updateOutletSearch(q: String) {
        _state.update { it.copy(outletSearchQuery = q) }
    }

    fun updateMenuSearch(q: String) {
        _state.update { it.copy(menuSearchQuery = q) }
    }

    fun loadAllOutletsData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.fetchAllOutlets() }
                .onSuccess { list ->
                    _state.update {
                        it.copy(
                            outletsList = list,
                            isLoading = false
                        )
                    }
                }
                .onFailure { err -> emitError(err.localizedMessage ?: "Failed to sync branches.") }
        }
    }

    fun loadMenuForSpecificOutlet(outletId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val menu = repository.fetchMenuForOutlet(outletId)
                val catalog = catalogRepository.getAllCatalogProducts()
                Pair(menu, catalog)
            }.onSuccess { (menu, catalog) ->
                _state.update {
                    it.copy(
                        currentOutletMenu = menu,
                        globalProductCatalogOptions = catalog,
                        isLoading = false
                    )
                }
            }.onFailure { err -> emitError(err.localizedMessage ?: "Failed to sync linked menu.") }
        }
    }

    private fun emitError(msg: String) {
        viewModelScope.launch {
            _state.update { it.copy(errorText = msg, isLoading = false) }
            delay(4000)
            _state.update { s -> if (s.errorText == msg) s.copy(errorText = null) else s }
        }
    }

    // --- AUTOMATED IMAGES WORKSPACE UPLOAD HOOKS ---

    // Handles multiple dish file images array arrays via the working batch method
    fun uploadMenuImagesList(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.uploadMultipleImages(context, uris, "product_img", "menu")
            }.onSuccess { uploadedUrls ->
                _state.update {
                    it.copy(
                        mMenuImagesList = it.mMenuImagesList + uploadedUrls,
                        isLoading = false
                    )
                }
            }.onFailure { err ->
                emitError("Menu images upload failed: ${err.localizedMessage}")
            }
        }
    }

    // FIXED: Maps single upload patterns to repository.uploadSingleImage explicitly
    fun uploadOutletLogo(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.uploadSingleImage(context, uri, "outlet_logo", "logos")
            }.onSuccess { url ->
                _state.update { it.copy(oUploadedLogoUrl = url, isLoading = false) }
            }.onFailure { err -> emitError("Logo upload failed: ${err.localizedMessage}") }
        }
    }

    // FIXED: Maps single upload patterns to repository.uploadSingleImage explicitly
    fun uploadOutletBanner(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.uploadSingleImage(context, uri, "outlet_logo", "banners")
            }.onSuccess { url ->
                _state.update { it.copy(oUploadedBannerUrl = url, isLoading = false) }
            }.onFailure { err -> emitError("Banner upload failed: ${err.localizedMessage}") }
        }
    }

    fun clearDishImagesArray() {
        _state.update { it.copy(mMenuImagesList = emptyList()) }
    }

    // --- INITIALIZERS FOR TYPE-SAFE NAVIGATION WORKSPACES ---
    fun setupOutletFormWorkspace(editId: Long?) {
        viewModelScope.launch {
            if (editId == null) {
                _state.update {
                    it.copy(
                        targetOutletId = null,
                        oName = "",
                        oAddress = "",
                        oCity = "",
                        oPhone = "",
                        oEmail = "",
                        oRadius = "5.0",
                        oLat = "0.00",
                        oLng = "0.00",
                        oOpensAt = "09:00:00",
                        oClosesAt = "23:00:00",
                        oRazorPayKey = "",
                        oUploadedLogoUrl = null,
                        oUploadedBannerUrl = null,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = true) }
                runCatching { repository.fetchOutletById(editId) }.onSuccess { o ->
                    _state.update {
                        it.copy(
                            targetOutletId = o.id,
                            oName = o.name,
                            oAddress = o.address,
                            oCity = o.city,
                            oPhone = o.phone,
                            oEmail = o.email,
                            oRadius = o.radiusKm.toString(),
                            oIsActive = o.isActive,
                            oAttendanceRadius = o.attendanceRadius,
                            oLat = o.lat.toString(),
                            oLng = o.lng.toString(),
                            oOpensAt = o.opensAt ?: "00:00:00",
                            oClosesAt = o.closesAt ?: "00:00:00",
                            oRazorPayKey = o.razorPayKey,
                            oUploadedLogoUrl = o.logoUrl,
                            oUploadedBannerUrl = o.bannerUrl,
                            isLoading = false
                        )
                    }
                }.onFailure { err -> emitError(err.localizedMessage ?: "Failed to fetch outlet details") }
            }
        }
    }

    fun setupMenuFormWorkspace(editId: Long?, activeOutletId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val catalog = catalogRepository.getAllCatalogProducts()
            if (editId == null) {
                _state.update {
                    it.copy(
                        globalProductCatalogOptions = catalog,
                        targetMenuItemId = null,
                        mSelectedProduct = null,
                        mPrice = "",
                        mDiscount = "0",
                        mHandlingCharges = "0.0",
                        mDeliveryCharges = "0.0",
                        mIsFreeDelivery = false,
                        mIsAvailable = true,
                        mIsOutOfStock = false,
                        mMenuImagesList = emptyList(),
                        isLoading = false
                    )
                }
            } else {
                runCatching { repository.fetchMenuItemById(editId) }.onSuccess { m ->
                    val prodMatch = catalog.find { it.id == m.productId }
                    _state.update {
                        it.copy(
                            globalProductCatalogOptions = catalog,
                            targetMenuItemId = m.id,
                            mSelectedProduct = prodMatch,
                            mPrice = m.price.toString(),
                            mDiscount = m.discount.toString(),
                            mHandlingCharges = m.handlingCharges.toString(),
                            mDeliveryCharges = m.deliveryCharges.toString(),
                            mIsFreeDelivery = m.isFreeDelivery,
                            mIsAvailable = m.isAvailable,
                            mIsOutOfStock = m.isOutOfStock,
                            mMenuImagesList = m.imagesList,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    // --- FORM COMPONENT VALUE SETTERS ---
    fun onONameChanged(v: String) {
        _state.update { it.copy(oName = v) }
    }

    fun onOAddressChanged(v: String) {
        _state.update { it.copy(oAddress = v) }
    }

    fun updateAddressUsingCurrentLocation(){

    }
    fun onOAttendanceRadiusChanged(v: String) {
        if( v.isBlank())return
        _state.update { it.copy(oAttendanceRadius = v.toInt()) }
    }

    fun onOCityChanged(v: String) {
        _state.update { it.copy(oCity = v) }
    }

    fun onOPhoneChanged(v: String) {
        _state.update { it.copy(oPhone = v) }
    }

    fun onOEmailChanged(v: String) {
        _state.update { it.copy(oEmail = v) }
    }

    fun onORadiusChanged(v: String) {
        _state.update { it.copy(oRadius = v) }
    }

    fun onOLatChanged(v: String) {
        _state.update { it.copy(oLat = v) }
    }

    fun onOLngChanged(v: String) {
        _state.update { it.copy(oLng = v) }
    }

    fun onOOpensAtChanged(v: String) {
        _state.update { it.copy(oOpensAt = v) }
    }

    fun onOClosesAtChanged(v: String) {
        _state.update { it.copy(oClosesAt = v) }
    }

    fun onORazorPayKeyChanged(v: String) {
        _state.update { it.copy(oRazorPayKey = v) }
    }

    fun onOIsActiveChanged(v: Boolean) {
        _state.update { it.copy(oIsActive = v) }
    }

    fun onMProductSelected(v: ProductCatalogUiModel) {
        _state.update { it.copy(mSelectedProduct = v) }
    }

    fun onMPriceChanged(v: String) {
        _state.update { it.copy(mPrice = v) }
    }

    fun onMDiscountChanged(v: String) {
        _state.update { it.copy(mDiscount = v) }
    }

    fun onMHandlingChargesChanged(v: String) {
        _state.update { it.copy(mHandlingCharges = v) }
    }

    fun onMDeliveryChargesChanged(v: String) {
        _state.update { it.copy(mDeliveryCharges = v) }
    }

    fun onMFreeDeliveryToggled(v: Boolean) {
        _state.update { it.copy(mIsFreeDelivery = v) }
    }

    fun onMAvailableToggled(v: Boolean) {
        _state.update { it.copy(mIsAvailable = v) }
    }

    fun onMStockToggled(v: Boolean) {
        _state.update { it.copy(mIsOutOfStock = v) }
    }

    fun commitOutletAction(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.oName.isBlank()) return emitError("Outlet name is required.")
        val payload = OutletUiModel(
            id = s.targetOutletId ?: 0L,
            name = s.oName,
            address = s.oAddress,
            city = s.oCity,
            phone = s.oPhone,
            email = s.oEmail,
            logoUrl = s.oUploadedLogoUrl,
            bannerUrl = s.oUploadedBannerUrl,
            lat = s.oLat.toDoubleOrNull() ?: 21.1983,
            lng = s.oLng.toDoubleOrNull() ?: 81.9614,
            radiusKm = s.oRadius.toDoubleOrNull() ?: 5.0,
            isActive = s.oIsActive,
            attendanceRadius = s.oAttendanceRadius,
            opensAt = s.oOpensAt,
            closesAt = s.oClosesAt,
            razorPayKey = s.oRazorPayKey
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                if (s.targetOutletId == null) repository.insertNewOutlet(payload) else repository.updateOutletRow(
                    payload
                )
            }
                .onSuccess { onSuccess() }.onFailure { err ->
                    emitError(
                        err.localizedMessage ?: "Failed to save branch parameters."
                    )
                }
        }
    }

    fun commitOutletMenuAction(activeOutletId: Long, onSuccess: () -> Unit) {
        val s = _state.value
        val prodId = s.mSelectedProduct?.id
            ?: return emitError("Linking a valid product catalog template is required.")
        val payload = OutletMenuItemUiModel(
            id = s.targetMenuItemId ?: 0L,
            outletId = activeOutletId,
            productId = prodId,
            imagesList = s.mMenuImagesList,
            price = s.mPrice.toDoubleOrNull() ?: 0.0,
            discount = s.mDiscount.toLongOrNull() ?: 0L,
            isAvailable = s.mIsAvailable,
            isOutOfStock = s.mIsOutOfStock,
            handlingCharges = s.mHandlingCharges.toDoubleOrNull() ?: 0.0,
            deliveryCharges = s.mDeliveryCharges.toDoubleOrNull() ?: 0.0,
            isFreeDelivery = s.mIsFreeDelivery
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                if (s.targetMenuItemId == null) repository.addProductToOutletMenu(payload) else repository.updateOutletMenuRow(
                    payload
                )
            }
                .onSuccess { onSuccess() }.onFailure { err ->
                    emitError(
                        err.localizedMessage ?: "Failed to save menu changes."
                    )
                }
        }
    }

    fun removeProductFromMenu(id: Long, activeOutletId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.removeProductFromMenu(id) }
                .onSuccess { loadMenuForSpecificOutlet(activeOutletId) }
        }
    }
    fun dismissPermissionRationale() {
        _state.update { it.copy(
            locationUiState = it.locationUiState.copy(showPermissionRationaleDialog = false)
        )}
    }

    fun dismissGpsDialog() {
        _state.update { it.copy(
            locationUiState = it.locationUiState.copy(showGpsDisabledDialog = false)
        )}
    }

    /**
     * Executes location permissions and sensor assertions against the nested state.
     * Hydrates the physical form coordinate inputs (oLat, oLng, oAddress) automatically on success.
     */
    fun acquireOutletDeviceLocation() {
        // 1. Check system manifest permission conditions
        if (!locationRepository.hasLocationPermission()) {
            _state.update { it.copy(
                locationUiState = it.locationUiState.copy(showPermissionRationaleDialog = true)
            )}
            return
        }

        // 2. Check hardware GPS provider sensor arrays status
        if (!locationRepository.isGpsEnabled()) {
            _state.update { it.copy(
                locationUiState = it.locationUiState.copy(showGpsDisabledDialog = true)
            )}
            return
        }

        // 3. Request high-accuracy coordinate fix
        viewModelScope.launch {
            _state.update { it.copy(
                locationUiState = it.locationUiState.copy(isFetchingLocation = true, locationErrorMessage = null)
            )}

            locationRepository.fetchLocation()
                .onSuccess { coordinates ->
                    // Mutate nested location state, and copy matching coordinates directly into form text parameters
                    _state.update { it.copy(
                        oLat = coordinates.first.toString(),
                        oLng = coordinates.second.toString(),
                        locationUiState = it.locationUiState.copy(
                            latitude = coordinates.first,
                            longitude = coordinates.second
                        )
                    )}
                    // Immediately trigger background reverse geocoding map text strings
                    lookupReverseGeocodedAddress(coordinates.first, coordinates.second)
                }
                .onFailure { err ->
                    emitLocationError(err.localizedMessage ?: "Failed to lock high-precision GPS signal.")
                }
        }
    }

    /**
     * Translates coordinates to a human-readable text block and drops it directly into the form address slot
     */
    private fun lookupReverseGeocodedAddress(lat: Double, lng: Double) {
        viewModelScope.launch {
            locationRepository.getAddressFromLocation(lat, lng)
                .onSuccess { addressText ->
                    _state.update { it.copy(
                        oAddress = addressText,
                        locationUiState = it.locationUiState.copy(
                            reverseGeocodedAddress = addressText,
                            isFetchingLocation = false
                        )
                    )}
                }
                .onFailure {
                    _state.update { it.copy(
                        locationUiState = it.locationUiState.copy(
                            reverseGeocodedAddress = "Coordinates mapped successfully, address text lookup failed.",
                            isFetchingLocation = false
                        )
                    )}
                }
        }
    }

    private fun emitLocationError(msg: String) {
        viewModelScope.launch {
            _state.update { it.copy(
                locationUiState = it.locationUiState.copy(locationErrorMessage = msg, isFetchingLocation = false)
            )}
            delay(4000)
            _state.update { current ->
                if (current.locationUiState.locationErrorMessage == msg) {
                    current.copy(locationUiState = current.locationUiState.copy(locationErrorMessage = null))
                } else current
            }
        }
    }


}