package com.jp.foodyvilla_backoffice.data.model

data class ReviewRequest(
    val customerName: String,
    val rating: Int,
    val desc: String,
    val imageUrls: List<String>
)