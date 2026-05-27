package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletMenuItemUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCatalogUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.OutletManagementRepository
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.ProductCatalogRepository
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
    
    // Independent Search States
    val outletSearchQuery: String = "",
    val menuSearchQuery: String = "",

    // Branch Form State
    val isOutletFormOpen: Boolean = false,
    val targetOutlet: OutletUiModel? = null,
    val oName: String = "",
    val oAddress: String = "",
    val oCity: String = "",
    val oPhone: String = "",
    val oEmail: String = "",
    val oRadius: String = "5.0",
    val oIsActive: Boolean = true,

    // Menu Item Form State
    val isMenuFormOpen: Boolean = false,
    val targetMenuItem: OutletMenuItemUiModel? = null,
    val mSelectedProduct: ProductCatalogUiModel? = null,
    val mPrice: String = "",
    val mDiscount: String = "0",
    val mIsAvailable: Boolean = true,
    val mIsOutOfStock: Boolean = false
)

class OutletManagementViewModel(
    private val repository: OutletManagementRepository,
    private val catalogRepository: ProductCatalogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OutletManagementUiState())
    val state = _state.asStateFlow()

    init {
        loadAllOutletsData()
    }

    fun updateOutletSearch(q: String) { _state.update { it.copy(outletSearchQuery = q) } }
    fun updateMenuSearch(q: String) { _state.update { it.copy(menuSearchQuery = q) } }

    fun loadAllOutletsData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.fetchAllOutlets() }
                .onSuccess { list -> _state.update { it.copy(outletsList = list, isLoading = false) } }
                .onFailure { err -> emitError(err.localizedMessage ?: "Failed to load branches configurations.") }
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
                _state.update { it.copy(currentOutletMenu = menu, globalProductCatalogOptions = catalog, isLoading = false) }
            }.onFailure { err ->
                emitError(err.localizedMessage ?: "Failed to sync branch menu.")
            }
        }
    }

    private fun emitError(msg: String) {
        viewModelScope.launch {
            _state.update { it.copy(errorText = msg, isLoading = false) }
            delay(4000)
            _state.update { s -> if (s.errorText == msg) s.copy(errorText = null) else s }
        }
    }

    // --- OUTLET FORM HANDLING CONFIGS ---
    fun openOutletCreationForm() { _state.update { it.copy(isOutletFormOpen = true, targetOutlet = null, oName = "", oAddress = "", oCity = "", oPhone = "", oEmail = "", oRadius = "5.0", oIsActive = true) } }
    fun openOutletEditionForm(o: OutletUiModel) { _state.update { it.copy(isOutletFormOpen = true, targetOutlet = o, oName = o.name, oAddress = o.address, oCity = o.city, oPhone = o.phone, oEmail = o.email, oRadius = o.radiusKm.toString(), oIsActive = o.isActive) } }
    fun closeOutletForm() { _state.update { it.copy(isOutletFormOpen = false) } }
    fun onONameChanged(v: String) { _state.update { it.copy(oName = v) } }
    fun onOAddressChanged(v: String) { _state.update { it.copy(oAddress = v) } }
    fun onOCityChanged(v: String) { _state.update { it.copy(oCity = v) } }
    fun onOPhoneChanged(v: String) { _state.update { it.copy(oPhone = v) } }
    fun onOEmailChanged(v: String) { _state.update { it.copy(oEmail = v) } }
    fun onORadiusChanged(v: String) { _state.update { it.copy(oRadius = v) } }
    fun onOActiveToggled(v: Boolean) { _state.update { it.copy(oIsActive = v) } }

    fun commitOutletAction() {
        val s = _state.value
        if (s.oName.isBlank()) return emitError("Outlet profile title label is required.")
        val payload = OutletUiModel(id = s.targetOutlet?.id ?: 0L, name = s.oName, address = s.oAddress, city = s.oCity, phone = s.oPhone, email = s.oEmail, radiusKm = s.oRadius.toDoubleOrNull() ?: 5.0, isActive = s.oIsActive)
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isOutletFormOpen = false) }
            runCatching { if (s.targetOutlet == null) repository.insertNewOutlet(payload) else repository.updateOutletRow(payload) }
                .onSuccess { loadAllOutletsData() }.onFailure { err -> emitError(err.localizedMessage ?: "Failed to save branch parameters.") }
        }
    }

    // --- BRANCH MENU FORM HANDLING CONFIGS ---
    fun openMenuCreationForm() { _state.update { it.copy(isMenuFormOpen = true, targetMenuItem = null, mSelectedProduct = null, mPrice = "", mDiscount = "0", mIsAvailable = true, mIsOutOfStock = false) } }
    fun openMenuEditionForm(m: OutletMenuItemUiModel) {
        val prodMatch = _state.value.globalProductCatalogOptions.find { it.id == m.productId }
        _state.update { it.copy(isMenuFormOpen = true, targetMenuItem = m, mSelectedProduct = prodMatch, mPrice = m.price.toString(), mDiscount = m.discount.toString(), mIsAvailable = m.isAvailable, mIsOutOfStock = m.isOutOfStock) }
    }
    fun closeMenuForm() { _state.update { it.copy(isMenuFormOpen = false) } }
    fun onMProductSelected(v: ProductCatalogUiModel) { _state.update { it.copy(mSelectedProduct = v) } }
    fun onMPriceChanged(v: String) { _state.update { it.copy(mPrice = v) } }
    fun onMDiscountChanged(v: String) { _state.update { it.copy(mDiscount = v) } }
    fun onMAvailableToggled(v: Boolean) { _state.update { it.copy(mIsAvailable = v) } }
    fun onMStockToggled(v: Boolean) { _state.update { it.copy(mIsOutOfStock = v) } }

    fun commitOutletMenuAction(activeOutletId: Long) {
        val s = _state.value
        val prodId = s.mSelectedProduct?.id ?: return emitError("Linking a valid catalog product item reference is mandatory.")
        val parsedPrice = s.mPrice.toDoubleOrNull() ?: 0.0
        val payload = OutletMenuItemUiModel(id = s.targetMenuItem?.id ?: 0L, outletId = activeOutletId, productId = prodId, price = parsedPrice, discount = s.mDiscount.toLongOrNull() ?: 0L, isAvailable = s.mIsAvailable, isOutOfStock = s.mIsOutOfStock)

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isMenuFormOpen = false) }
            runCatching { if (s.targetMenuItem == null) repository.addProductToOutletMenu(payload) else repository.updateOutletMenuRow(payload) }
                .onSuccess { loadMenuForSpecificOutlet(activeOutletId) }.onFailure { err -> emitError(err.localizedMessage ?: "Failed to update branch menu mapping document.") }
        }
    }

    fun removeProductFromMenu(id: Long, activeOutletId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.removeProductFromOutletMenu(id) }
                .onSuccess { loadMenuForSpecificOutlet(activeOutletId) }.onFailure { err -> emitError(err.localizedMessage ?: "Delete request denied.") }
        }
    }
}