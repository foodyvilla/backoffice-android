package com.jp.foodyvilla_backoffice.data.repo

import android.content.Context
import android.util.Log
import com.jp.foodyvilla_backoffice.data.model.login.LoginResponse
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

class AuthRepo(
    private val supabase: SupabaseClient,
    private val context: Context
) {

    // client id : 936302589972-bjm4nnn6sie66eaqalfi0l6re6ja7fju.apps.googleusercontent.com
    fun logout(): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)

        try {
            supabase.auth.signOut()
            supabase.auth.clearSession() // 👈 ensures local session removed
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }

    @OptIn(ExperimentalTime::class, InternalAPI::class)
    fun loginWithOtp(
        phone: String,
        otp: String? = null
    ): Flow<UiState<String>> = flow {

        emit(UiState.Loading)

        try {
            val response = supabase.functions.invoke("login") {
                body = """
        {
            "phone": "$phone"
            ${if (otp != null) """, "otp": "$otp"""" else ""}
        }
    """.trimIndent()
            }

            val bodyString = response.bodyAsText()

            val loginResponse = Json {
                ignoreUnknownKeys = true
            }.decodeFromString<LoginResponse>(bodyString)

            println("Parsed response $loginResponse")

            // ❌ API failure
            if (!loginResponse.success) {
                emit(UiState.Error(Exception(loginResponse.message)))
                return@flow
            }

            // ✅ OTP sent (no otp provided)
            if (otp == null) {
                emit(UiState.Success("OTP_SENT"))
                return@flow
            }

            // ✅ Login success
            val accessToken = loginResponse.access_token
                ?: throw Exception("Missing access token")

            val refreshToken = loginResponse.refresh_token
                ?: throw Exception("Missing refresh token")

            val expiresIn = loginResponse.expires_in ?: 3600L

            val session = UserSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = expiresIn,
                tokenType = "bearer",
                user = null
            )

            supabase.auth.importSession(session)
            supabase.auth.refreshCurrentSession()
            emit(UiState.Success("LOGIN_SUCCESS"))

        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }


    fun signInWithSupabase(idToken: String): Flow<UiState<UserInfo>> = flow {

        println("Token : $idToken")
        emit(UiState.Loading)
        try {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                emit(UiState.Success(user))
            } else {
                emit(UiState.Error(Exception("User not found")))
            }
            Log.d("SupabaseAuth", "Logged in: ${user?.email}")

        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Error: ${e.message}")
            emit(UiState.Error(e))
        }
    }



    fun isLoggedIn(): Flow<UiState<Boolean>> = flow {

        try {

            emit(UiState.Loading)

            supabase.auth.sessionStatus
                .filter { it !is SessionStatus.Initializing }
                .map { status ->

                    println("is Logged in $status")

                    when (status) {

                        is SessionStatus.Authenticated -> true

                        is SessionStatus.NotAuthenticated -> false

                        is SessionStatus.RefreshFailure -> false

                        else -> false
                    }
                }
                .collect { isLogged ->

                    emit(UiState.Success(isLogged))
                }

        } catch (e: Exception) {

            println("Login Check Error: ${e.message}")

            emit(
                UiState.Error(
                    e
                )
            )
        }
    }


}