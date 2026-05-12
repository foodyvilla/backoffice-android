package com.jp.foodyvilla_backoffice.data.repo

import Banner
import com.jp.foodyvilla_backoffice.data.model.OfferFood
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OfferRepo(private val client : SupabaseClient) {

    fun getOffers(): Flow<List<OfferFood>> = flow {
        try {
            val res = client
                .from("offers")
                .select()
                .decodeList<OfferFood>()

            emit(res)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun getBanners(): Flow<List<Banner>> = flow {
        try {
            val res = client
                .from("banners")
                .select()
                .decodeList<Banner>()

            emit(res)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

}