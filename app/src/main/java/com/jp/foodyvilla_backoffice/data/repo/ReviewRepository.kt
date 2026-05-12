package com.jp.foodyvilla_backoffice.data.repo

import android.content.Context
import android.net.Uri
import com.jp.foodyvilla_backoffice.data.model.Review
import com.jp.foodyvilla_backoffice.data.model.ReviewInsertDto
import com.jp.foodyvilla_backoffice.data.model.ReviewRequest
import com.jp.foodyvilla_backoffice.data.utils.compressImage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class ReviewRepository(
    private val supabase: SupabaseClient
) {

    // ----------------------
    // GET REVIEWS
    // ----------------------
    fun getReviews(): Flow<List<Review>> = flow {
        val result = supabase.from("reviews")
            .select()
            .decodeList<Review>()

        emit(result)
    }

    // ----------------------
    // UPLOAD IMAGES
    // ----------------------
    suspend fun uploadImages(
        context: Context,
        uris: List<Uri>
    ): List<String> {

        return uris.map { uri ->

            val compressed = compressImage(context, uri)

            val fileName = "${UUID.randomUUID()}.jpg"
            val path = "reviews/$fileName" // ✅ IMPORTANT FIX

            supabase.storage.from("review").upload(
                path = path,
                data = compressed
            )

            supabase.storage.from("review")
                .publicUrl(path)
        }
    }

    // ----------------------
    // INSERT REVIEW
    // ----------------------
    suspend fun insertReview(request: ReviewRequest) {

        val dto = ReviewInsertDto(
            title = request.customerName,
            desc = request.desc,
            rating = request.rating,
            img_url = request.imageUrls
        )

        supabase.from("reviews").insert(dto)
    }
}