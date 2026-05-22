package com.jp.foodyvilla_backoffice.data.repo

import com.jp.foodyvilla_backoffice.data.model.user.UserProfile
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UserRepository(private val supabase: SupabaseClient) {

     fun getCurrentUserProfile(): Flow<UiState<UserProfile>> = flow {
        emit(UiState.Loading)
        try {
            val session = supabase.auth.currentSessionOrNull()

            if (session == null) {
                emit(UiState.Error(Exception("user not logged in")))
                return@flow
            }
            val user = session.user
            val authUserId = user?.id

            if (authUserId == null) {
                emit(UiState.Error(Exception("user not logged in")))
                return@flow
            }
            val response = supabase.postgrest["users"]
                .select {
                    filter {
                        eq("auth_user_id", authUserId)
                    }
                }
                .decodeSingleOrNull<UserProfile>()

            println("User $response")
            if (response == null) {
                emit(UiState.Error(Exception("User Not found")))
                return@flow
            }
            emit(UiState.Success(response))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(UiState.Error(e))
        }
    }


    suspend fun updateFcmToken(
        fcmToken: String
    ) {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User not logged in")

        try {
            // Update users table
            supabase.from("users").update(
                {
                    set("fcm_token", fcmToken)
                }
            ) {
                filter {
                    eq("auth_user_id", userId)
                }
            }

            // Also update employee table for backoffice users
            supabase.from("employee").update(
                {
                    set("fcm_token", fcmToken)
                }
            ) {
                filter {
                    eq("auth_user_id", userId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @OptIn(ExperimentalTime::class)
    suspend fun updateUserProfile(
        userProfile: UserProfile
    ): UiState<String> {

        return try {

            val userId = supabase.auth.currentUserOrNull()?.id

            if (userId == null) {
                return UiState.Error(
                    Exception("User not logged in")
                )
            }

            supabase.from("users").update({

                set("name", userProfile.name)
                set("email", userProfile.email)
                set("phone", userProfile.phone)
                set("address", userProfile.address)
                set("lat", userProfile.lat)
                set("long", userProfile.long)
                set("updated_at", Clock.System.now())
            }) {

                filter {
                    eq("auth_user_id", userId)
                }
            }

            UiState.Success("Profile updated successfully")

        } catch (e: Exception) {

            UiState.Error(e)
        }
    }
}