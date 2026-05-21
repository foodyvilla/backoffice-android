package com.jp.foodyvilla_backoffice.data.domain.model

sealed interface ScreenState<out T> {
    data object Loading : ScreenState<Nothing>
    data object Empty : ScreenState<Nothing>
    data class Success<T>(val data: T) : ScreenState<T>
    data class Error(val message: String, val cause: Throwable? = null) : ScreenState<Nothing>
}

