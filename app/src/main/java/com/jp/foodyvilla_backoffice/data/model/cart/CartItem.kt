package com.jp.foodyvilla_backoffice.data.model.cart

import com.jp.foodyvilla_backoffice.data.model.FoodItem
import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val id: Int,
    val customer_id: Int,
    val product_id: Int,
    val qty : Int = 1,
    val products: FoodItem? = null// relation alias
){
    val totalPrice: Double? get() = products?.price?.times(qty)

}