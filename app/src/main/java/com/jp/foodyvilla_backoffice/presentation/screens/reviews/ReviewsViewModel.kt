package com.jp.foodyvilla_backoffice.presentation.screens.reviews

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.model.Review
import com.jp.foodyvilla_backoffice.data.model.ReviewRequest
import com.jp.foodyvilla_backoffice.data.repo.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ----------------------
// UI STATE (LIST)
// ----------------------
data class ReviewsUiState(
    val isLoading: Boolean = true,
    val reviews: List<Review> = emptyList()
)

// ----------------------
// ADD REVIEW STATE
// ----------------------
data class AddReviewState(
    val name: String = "",
    val desc: String = "",
    val rating: Int = 0,
    val images: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

// ----------------------
// VIEWMODEL
// ----------------------
class ReviewsViewModel(
    private val repo: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    private val _addState = MutableStateFlow(AddReviewState())
    val addState: StateFlow<AddReviewState> = _addState.asStateFlow()

    init {
        loadReviews()
    }

     fun loadReviews() {
        viewModelScope.launch {
            repo.getReviews().collect { reviews ->

                println("REviews : $reviews")
                _uiState.value = ReviewsUiState(
                    isLoading = false,
                    reviews = reviews
                )
            }
        }
    }

    // ----------------------
    // INPUT HANDLERS
    // ----------------------

    fun onNameChange(value: String) {
        _addState.value = _addState.value.copy(name = value)
    }

    fun onDescChange(value: String) {
        _addState.value = _addState.value.copy(desc = value)
    }

    fun onRatingChange(value: Int) {
        _addState.value = _addState.value.copy(rating = value)
    }

    fun addImage(uri: Uri) {
        val current = _addState.value
        if (current.images.size < 2) {
            _addState.value = current.copy(images = current.images + uri)
        }
    }

    fun removeImage(uri: Uri) {
        val current = _addState.value
        _addState.value = current.copy(images = current.images - uri)
    }

    // ----------------------
    // SUBMIT REVIEW
    // ----------------------

    fun submit(context: Context) {
        viewModelScope.launch {

            val current = _addState.value

            _addState.value = current.copy(isLoading = true, error = null)

            try {
                val uploadedUrls = repo.uploadImages(
                    context,
                    current.images
                )

                repo.insertReview(
                    ReviewRequest(
                        customerName = current.name,
                        desc = current.desc,
                        rating = current.rating,
                        imageUrls = uploadedUrls
                    )
                )

                _addState.value = AddReviewState(success = true)

                // refresh list
                loadReviews()

            } catch (e: Exception) {
                _addState.value = current.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}