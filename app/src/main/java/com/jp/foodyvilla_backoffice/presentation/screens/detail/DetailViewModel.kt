package com.jp.foodyvilla_backoffice.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.jp.foodyvilla_backoffice.data.model.FoodItem
import com.jp.foodyvilla_backoffice.data.repo.CartRepository
import com.jp.foodyvilla_backoffice.data.repo.ProductRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val item: FoodItem? = null,
    val quantity: Int = 1,
    val isWishlisted: Boolean = false,
    val errorMessage: String? = null
)

class DetailViewModel(
    private val foodRepository: ProductRepo,
    private  val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()




    fun loadItem(itemId : Int) {
        viewModelScope.launch {
            foodRepository.getProductById(itemId).collect { item ->
                _uiState.update { it.copy(isLoading = false, item = item) }
            }
        }
    }



    fun updateQuantity(quantity: Int) {
        _uiState.update { it.copy(quantity = quantity) }
        viewModelScope.launch {
            val itemId = _uiState.value.item?.id ?: return@launch
            cartRepository.addToCart(itemId,quantity).collectLatest { println("Details Screen item quantity changed $it") }
        }

    }
    fun increment() = _uiState.update { it.copy(quantity = it.quantity + 1) }
    fun decrement() = _uiState.update { it.copy(quantity = maxOf(1, it.quantity - 1)) }
    fun toggleWishlist() = _uiState.update { it.copy(isWishlisted = !it.isWishlisted) }
}
