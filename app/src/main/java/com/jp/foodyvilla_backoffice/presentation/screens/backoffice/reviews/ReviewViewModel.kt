package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.backoffice.Review
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeReviewRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class ReviewUiState(
    val reviews: List<Review> = emptyList(),
    val rawRows: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class ReviewViewModel(private val repository: BackOfficeReviewRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState = _uiState.asStateFlow()

    fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getReviews() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(
                        rawRows = rows,
                        reviews = rows.mapNotNull { runCatching { it.toModel<Review>() }.getOrNull() },
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
