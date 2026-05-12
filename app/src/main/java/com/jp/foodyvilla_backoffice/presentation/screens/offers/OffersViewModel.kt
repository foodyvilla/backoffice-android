package com.jp.foodyvilla_backoffice.presentation.screens.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.OfferFood
import com.jp.foodyvilla_backoffice.data.repo.OfferRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OffersUiState(val isLoading: Boolean = true, val offers: List<OfferFood> = emptyList())

class OffersViewModel(private val offerRepository: OfferRepo) : ViewModel() {
    private val _uiState = MutableStateFlow(OffersUiState())
    val uiState: StateFlow<OffersUiState> = _uiState.asStateFlow()

    init {
        getOffers()
    }

    fun getOffers(){
        viewModelScope.launch {
            offerRepository.getOffers().collect { offers ->
                _uiState.update { it.copy(isLoading = false, offers = offers) }
            }
        }
    }
}
