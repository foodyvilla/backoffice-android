package com.jp.foodyvilla_backoffice.presentation.utils

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val exception: Exception? = null, val msg : String = "Something went wrong...") : UiState<Nothing>()
    object Idle : UiState<Nothing>()

}