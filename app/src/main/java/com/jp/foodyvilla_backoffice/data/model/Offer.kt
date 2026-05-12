package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OfferFood(
    val id: String,
    val created_at: String,
    val title: String,
    val desc: String,
    val img_url: String
)