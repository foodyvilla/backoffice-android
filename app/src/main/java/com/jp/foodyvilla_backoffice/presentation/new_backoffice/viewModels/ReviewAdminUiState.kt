package com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.ReviewAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewAdminUiState(
    val reviewsList: List<ReviewAdminUiModel> = emptyList(),
    val cachedUsers: List<UserResponse> = emptyList(),
    val reviewsSearchQuery: String = "",
    val isLoading: Boolean = false,
    val dynamicErrorMessage: String? = null,

    // Filters
    val filterCustomerId: Long? = null,
    val filterType: AdminReviewType? = null,
    val filterRating: Int? = null,

    // Workspace Sheet Input bindings
    val isFormWindowOpen: Boolean = false,
    val formWorkspaceTargetId: Long? = null,
    val formCustomerId: Long = 0L,
    val formReviewType: AdminReviewType = AdminReviewType.PRODUCT,
    val formOrderId: String = "",
    val formMenuItemId: String = "",
    val formOutletId: String = "",
    val formRating: Int = 5,
    val formTitle: String = "",
    val formDescription: String = "",
    val formImages: String = ""
)

class ReviewAdminViewModel(private val repository: ReviewAdminRepository) : ViewModel() {

    private val _state = MutableStateFlow(ReviewAdminUiState())
    val state = _state.asStateFlow()

    fun updateSearchFilter(q: String) { _state.update { it.copy(reviewsSearchQuery = q) } }
    fun onFilterCustomerSelected(id: Long?) { _state.update { it.copy(filterCustomerId = id) } }
    fun onFilterTypeSelected(t: AdminReviewType?) { _state.update { it.copy(filterType = t) } }
    fun onFilterRatingSelected(r: Int?) { _state.update { it.copy(filterRating = r) } }

    fun onFormCustomerSelected(id: Long) { _state.update { it.copy(formCustomerId = id) } }
    fun onFormTypeChanged(t: AdminReviewType) { _state.update { it.copy(formReviewType = t) } }
    fun onFormOrderIdChanged(v: String) { _state.update { it.copy(formOrderId = v) } }
    fun onFormMenuItemIdChanged(v: String) { _state.update { it.copy(formMenuItemId = v) } }
    fun onFormOutletIdChanged(v: String) { _state.update { it.copy(formOutletId = v) } }
    fun onFormRatingChanged(r: Int) { _state.update { it.copy(formRating = r.coerceIn(1, 5)) } }
    fun onFormTitleChanged(v: String) { _state.update { it.copy(formTitle = v) } }
    fun onFormDescChanged(v: String) { _state.update { it.copy(formDescription = v) } }
    fun onFormImagesChanged(v: String) { _state.update { it.copy(formImages = v) } }
    
    fun dismissFormWorkspace() { _state.update { it.copy(isFormWindowOpen = false) } }

    fun loadAllReviewsDirectory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, dynamicErrorMessage = null) }
            runCatching {
                val customerList = repository.fetchAllUsersMinimal()
                val rawReviews = repository.fetchRawReviewsAdmin()
                val mappedUiReviews = rawReviews.map { rev ->
                    val match = customerList.find { it.id == rev.customer_id }
                    rev.toUiModel(match?.name ?: "User ID: ${rev.customer_id}")
                }
                Pair(customerList, mappedUiReviews)
            }.onSuccess { (users, reviews) ->
                _state.update { it.copy(cachedUsers = users, reviewsList = reviews, isLoading = false) }
            }.onFailure { err ->
                _state.update { it.copy(dynamicErrorMessage = err.localizedMessage, isLoading = false) }
            }
        }
    }

    fun initFormWorkspace(target: ReviewAdminUiModel?) {
        val userCache = _state.value.cachedUsers
        val fallbackId = userCache.firstOrNull()?.id ?: 0L

        if (target == null) {
            _state.update { it.copy(
                formWorkspaceTargetId = null, formCustomerId = fallbackId, formReviewType = AdminReviewType.PRODUCT,
                formOrderId = "", formMenuItemId = "", formOutletId = "", formRating = 5, formTitle = "", formDescription = "", formImages = "",
                isFormWindowOpen = true
            )}
        } else {
            _state.update { it.copy(
                formWorkspaceTargetId = target.id, formCustomerId = target.customerId, formReviewType = target.reviewType,
                formOrderId = target.orderId, formMenuItemId = target.menuItemId, formOutletId = target.outletId,
                formRating = target.rating, formTitle = target.title, formDescription = target.description, formImages = target.images.joinToString(" "),
                isFormWindowOpen = true
            )}
        }
    }

    fun dispatchCommitCrudAction() {
        val s = _state.value
        if (s.formDescription.isBlank()) {
            _state.update { it.copy(dynamicErrorMessage = "Review feedback description cannot be blank.") }
            return
        }

        val payload = ReviewAdminUiModel(
            id = s.formWorkspaceTargetId ?: 0L, customerId = s.formCustomerId, reviewType = s.formReviewType,
            orderId = s.formOrderId, menuItemId = s.formMenuItemId, outletId = s.formOutletId,
            rating = s.formRating, title = s.formTitle, description = s.formDescription, images = s.formImages.split(" ", ",").filter { it.isNotBlank() }
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                if (s.formWorkspaceTargetId == null) repository.createReviewRecord(payload)
                else repository.updateReviewRecord(payload)
            }.onSuccess {
                _state.update { it.copy(isFormWindowOpen = false) }
                loadAllReviewsDirectory()
            }.onFailure { err ->
                _state.update { it.copy(dynamicErrorMessage = err.localizedMessage, isLoading = false) }
            }
        }
    }

    fun dispatchPurgeAction(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.purgeReviewLogRecord(id) }
                .onSuccess { loadAllReviewsDirectory() }
                .onFailure { err -> _state.update { it.copy(dynamicErrorMessage = err.localizedMessage, isLoading = false) } }
        }
    }
}