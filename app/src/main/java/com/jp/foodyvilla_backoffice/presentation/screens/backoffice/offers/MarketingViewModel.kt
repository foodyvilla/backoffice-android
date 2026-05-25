package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.Banner
import com.jp.foodyvilla_backoffice.data.model.backoffice.Offer
import com.jp.foodyvilla_backoffice.data.repo.backoffice.MarketingBackOfficeRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class MarketingUiState(
    val banners: List<Banner> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val rawRows: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class MarketingViewModel(private val repository: MarketingBackOfficeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketingUiState())
    val uiState = _uiState.asStateFlow()

    fun loadBanners() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getBanners() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(
                        banners = rows.mapNotNull { runCatching { it.toModel<Banner>() }.getOrNull() },
                        rawRows = rows,
                        isLoading = false
                    ) }
                }
                .onFailure { t -> _uiState.update { it.copy(error = t.message, isLoading = false) } }
        }
    }

    fun loadOffers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getOffers() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(
                        offers = rows.mapNotNull { runCatching { it.toModel<Offer>() }.getOrNull() },
                        rawRows = rows,
                        isLoading = false
                    ) }
                }
                .onFailure { t -> _uiState.update { it.copy(error = t.message, isLoading = false) } }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
