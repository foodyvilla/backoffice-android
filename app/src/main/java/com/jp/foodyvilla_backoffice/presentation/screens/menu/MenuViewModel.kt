package com.jp.foodyvilla_backoffice.presentation.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.Category
import com.jp.foodyvilla_backoffice.data.model.FoodItem
import com.jp.foodyvilla_backoffice.data.model.MockData
import com.jp.foodyvilla_backoffice.data.repo.ProductRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuUiState(
    val isLoading: Boolean = true,
    val allItems: List<FoodItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "",
    val searchQuery : String = ""
) {
    val filteredItems: List<FoodItem>
        get() {
            val query = searchQuery.trim()

            return allItems.filter { item ->

                val matchesSearch =
                    query.isBlank() ||
                            item.name.contains(query, true) ||
                            item.description.contains(query, true) ||
                            item.category.contains(query, true) ||
                            item.prepTime.contains(query, true) ||
                            item.price.toString().contains(query) ||
                            item.rating.toString().contains(query) ||
                            (query.equals("veg", true) && item.isVeg) ||
                            (query.equals("vegan", true) && item.isVegan)

                val matchesCategory =
                    selectedCategory == "all" ||
                            item.category.equals(selectedCategory, true) ||
                            // 🔥 ALSO allow category match via search
                            item.name.contains(selectedCategory, true) ||
                            item.description.contains(selectedCategory, true)

                matchesSearch && matchesCategory
            }
        }
}

class MenuViewModel(private val foodRepository: ProductRepo) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            foodRepository.getProducts().collect { items ->
                _uiState.update {
                    it.copy(isLoading = false, allItems = items, categories = MockData.categories)
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    fun selectCategory(name: String) = _uiState.update { it.copy(selectedCategory = if(name.contains("all",true)){""}else name) }
}
