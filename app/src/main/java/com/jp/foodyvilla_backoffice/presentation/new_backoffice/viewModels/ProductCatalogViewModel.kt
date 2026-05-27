package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCatalogUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ProductCategoryUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.ProductCatalogRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductCatalogUiState(
    val productsList: List<ProductCatalogUiModel> = emptyList(),
    val categoriesList: List<ProductCategoryUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Independent Real-Time Search Queries
    val productSearchQuery: String = "",
    val categorySearchQuery: String = "",

    // Product Upsert Workspace Panel States
    val isProductFormOpen: Boolean = false,
    val targetProduct: ProductCatalogUiModel? = null, // Null indicates a "CREATE" operation
    val pName: String = "",
    val pDescription: String = "",
    val pSelectedCategory: ProductCategoryUiModel? = null,
    val pPrepTime: String = "",
    val pIsVeg: Boolean = true,
    val pIsVegan: Boolean = false,
    val pIsBestseller: Boolean = false,

    // Category Upsert Workspace Panel States
    val isCategoryFormOpen: Boolean = false,
    val targetCategory: ProductCategoryUiModel? = null, // Null indicates a "CREATE" operation
    val cName: String = "",
    val cEmoji: String = "",
    val cIsActive: Boolean = true
)

class ProductCatalogViewModel(private val repository: ProductCatalogRepository) : ViewModel() {

    private val _state = MutableStateFlow(ProductCatalogUiState())
    val state = _state.asStateFlow()

    init {
        refreshCatalogDataset()
    }

    // =====================================================================
    // CORE LIVE DATA SYNC PIPELINES
    // =====================================================================
    fun refreshCatalogDataset() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val products = repository.getAllCatalogProducts()
                val categories = repository.getAllCategories()
                Pair(products, categories)
            }.onSuccess { (p, c) ->
                _state.update { it.copy(productsList = p, categoriesList = c, isLoading = false) }
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Failed to synchronize master catalog records.")
            }
        }
    }

    private fun emitTemporaryError(msg: String) {
        viewModelScope.launch {
            _state.update { it.copy(errorMessage = msg, isLoading = false) }
            delay(4000)
            _state.update { s -> if (s.errorMessage == msg) s.copy(errorMessage = null) else s }
        }
    }

    // =====================================================================
    // INDEPENDENT SEARCH INPUT HANDLERS
    // =====================================================================
    fun updateProductSearch(q: String) { _state.update { it.copy(productSearchQuery = q) } }
    fun updateCategorySearch(q: String) { _state.update { it.copy(categorySearchQuery = q) } }


    // =====================================================================
    // PRODUCT CATALOG CRUD FORM OPERATIONS
    // =====================================================================
    fun openProductCreationForm() {
        _state.update {
            it.copy(
                isProductFormOpen = true, targetProduct = null,
                pName = "", pDescription = "", pSelectedCategory = null,
                pPrepTime = "", pIsVeg = true, pIsVegan = false, pIsBestseller = false
            )
        }
    }

    fun openProductEditionForm(product: ProductCatalogUiModel) {
        val linkedCategory = _state.value.categoriesList.find { it.id == product.categoryId }
        _state.update {
            it.copy(
                isProductFormOpen = true, targetProduct = product,
                pName = product.name, pDescription = product.description,
                pSelectedCategory = linkedCategory, pPrepTime = product.prepTime,
                pIsVeg = product.isVeg, pIsVegan = product.isVegan, pIsBestseller = product.isBestseller
            )
        }
    }

    fun closeProductForm() { _state.update { it.copy(isProductFormOpen = false, targetProduct = null) } }
    fun onPNameChanged(v: String) { _state.update { it.copy(pName = v) } }
    fun onPDescChanged(v: String) { _state.update { it.copy(pDescription = v) } }
    fun onPCatSelected(v: ProductCategoryUiModel) { _state.update { it.copy(pSelectedCategory = v) } }
    fun onPPrepChanged(v: String) { _state.update { it.copy(pPrepTime = v) } }
    fun onPVegToggled(v: Boolean) { _state.update { it.copy(pIsVeg = v) } }
    fun onPVeganToggled(v: Boolean) { _state.update { it.copy(pIsVegan = v) } }
    fun onPBestToggled(v: Boolean) { _state.update { it.copy(pIsBestseller = v) } }

    fun commitProductFormAction() {
        val s = _state.value
        if (s.pName.isBlank()) return emitTemporaryError("Product catalog display title cannot be empty.")

        val payload = ProductCatalogUiModel(
            id = s.targetProduct?.id ?: 0L,
            name = s.pName, description = s.pDescription,
            categoryId = s.pSelectedCategory?.id, isVeg = s.pIsVeg,
            isVegan = s.pIsVegan, isBestseller = s.pIsBestseller,
            prepTime = s.pPrepTime
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isProductFormOpen = false) }
            runCatching {
                if (s.targetProduct == null) {
                    repository.createCatalogProduct(payload)
                } else {
                    repository.updateCatalogProduct(payload)
                }
            }.onSuccess {
                refreshCatalogDataset()
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Failed to write updates to product database table.")
            }
        }
    }

    fun deleteProductRow(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.deleteCatalogProduct(id)
            }.onSuccess {
                refreshCatalogDataset()
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Database validation constraints denied product deletion.")
            }
        }
    }


    // =====================================================================
    // PRODUCT CATEGORIES CRUD FORM OPERATIONS
    // =====================================================================
    fun openCategoryCreationForm() {
        _state.update {
            it.copy(
                isCategoryFormOpen = true, targetCategory = null,
                cName = "", cEmoji = "", cIsActive = true
            )
        }
    }

    fun openCategoryEditionForm(category: ProductCategoryUiModel) {
        _state.update {
            it.copy(
                isCategoryFormOpen = true, targetCategory = category,
                cName = category.name, cEmoji = category.emoji, cIsActive = category.isActive
            )
        }
    }

    fun closeCategoryForm() { _state.update { it.copy(isCategoryFormOpen = false, targetCategory = null) } }
    fun onCNameChanged(v: String) { _state.update { it.copy(cName = v) } }
    fun onCEmojiChanged(v: String) { _state.update { it.copy(cEmoji = v) } }
    fun onCActiveToggled(v: Boolean) { _state.update { it.copy(cIsActive = v) } }

    fun commitCategoryFormAction() {
        val s = _state.value
        if (s.cName.isBlank()) return emitTemporaryError("Category title identifier is mandatory.")

        val payload = ProductCategoryUiModel(
            id = s.targetCategory?.id ?: 0L,
            name = s.cName, emoji = s.cEmoji, isActive = s.cIsActive
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isCategoryFormOpen = false) }
            runCatching {
                if (s.targetCategory == null) {
                    repository.createCategory(payload)
                } else {
                    repository.updateCategory(payload)
                }
            }.onSuccess {
                refreshCatalogDataset()
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Failed to save category structural updates.")
            }
        }
    }

    fun deleteCategoryRow(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                repository.deleteCategory(id)
            }.onSuccess {
                refreshCatalogDataset()
            }.onFailure { err ->
                emitTemporaryError(err.localizedMessage ?: "Foreign Key Constraint Warning: Cannot delete a category while active menu items are still assigned to it.")
            }
        }
    }
}