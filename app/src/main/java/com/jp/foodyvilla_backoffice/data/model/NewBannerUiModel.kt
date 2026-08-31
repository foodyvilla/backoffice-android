package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NewBannerUiModel(
    val id: Long,
    val outletId: Long?,
    val title: String?,
    val imageUrl: String,
    val displayOrder: Int
)

@Serializable
data class NewOfferUiModel(
    val id: String,
    val outletId: Long?,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val linkedUrl: String?,
    val expiresAt: String?
)


@Serializable
data class BannerResponse(
    val id: Long,
    val outlet_id: Long? = null,
    val title: String? = null,
    val img_url: String? = null,
    val display_order: Int
)

@Serializable
data class OfferResponse(
    val id: String,
    val outlet_id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val img_url: String? = null,
    val linked_url: String? = null,
    val expires_at: String? = null
)


fun BannerResponse.toUiModel(): NewBannerUiModel = NewBannerUiModel(
    id = id, outletId = outlet_id, title = title, imageUrl = img_url.orEmpty(), displayOrder = display_order
)

fun OfferResponse.toUiModel(): NewOfferUiModel = NewOfferUiModel(
    id = id, outletId = outlet_id, title = title, description = description, imageUrl = img_url, linkedUrl = linked_url, expiresAt = expires_at
)