package com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation

import kotlinx.serialization.Serializable

// ==========================================
// Type-Safe Route Structure Destinations
// ==========================================
object ScreenDestinations {

    @Serializable
    data class OutletMenu(val outletId: Long)


    @Serializable
    data object  Splash



    @Serializable
    data object AddBanner

    @Serializable
    data class EditBanner(val bannerId: Long)

    @Serializable
    data object AddOffer

    @Serializable
    data class EditOffer(val offerId: String?)
    @Serializable
    data object BackOfficeLogin
    @Serializable
    data object BackOffice
    @Serializable
    data object CreateOrder


    @Serializable
    data class OrderDetails( val id : String)
}

// ==========================================
// Core NavHost Setup Definition Graph
// ==========================================
