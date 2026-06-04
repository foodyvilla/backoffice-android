package com.jp.foodyvilla_backoffice.data.repo

import android.content.Context
import android.util.Log
import com.jp.foodyvilla_backoffice.data.model.security.BackOfficeLoginResponseDto
import com.jp.foodyvilla_backoffice.data.model.security.BackOfficeSessionDto
import com.jp.foodyvilla_backoffice.data.model.security.BackOfficeTokenDto
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.OutletRole
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession as SupabaseUserSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.ExperimentalTime

class SupabaseAuthRepository(
    private val supabase: SupabaseClient,
    context: Context,
    private val supabaseUrl: String,
    private val supabaseAnonKey: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private val prefs =
        context.getSharedPreferences("backoffice_session", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _currentSession =
        MutableStateFlow(readStoredSession())

    override val currentSession: StateFlow<UserSession?> =
        _currentSession

    init {
        // Restore Supabase session on startup if tokens were persisted
        restoreSupabaseSession()

        // Listen for auth state changes to keep SharedPreferences and StateFlow in sync
        kotlinx.coroutines.CoroutineScope(ioDispatcher).launch {
            supabase.auth.sessionStatus.collect { status ->
                Log.d(TAG, "Auth status changed: $status")
                when (status) {
                    is SessionStatus.Authenticated -> {
                        // Update stored tokens whenever session is refreshed or changed
                        val session = status.session
                        updateStoredTokens(session.accessToken, session.refreshToken ?: "")
                    }
                    is SessionStatus.NotAuthenticated -> {
                        // If we have an active session in our StateFlow but Supabase says we aren't authenticated,
                        // it might mean the session expired or the refresh token is invalid.
                        if (_currentSession.value != null) {
                            Log.w(TAG, "Session invalidated by Supabase; clearing local session.")
                            logout()
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateStoredTokens(accessToken: String, refreshToken: String) {
        val raw = prefs.getString(KEY_SESSION, null) ?: return
        try {
            val obj = json.parseToJsonElement(raw) as? JsonObject ?: return
            val newObj = buildJsonObject {
                obj.forEach { (key, value) ->
                    if (key != "access_token" && key != "refresh_token") {
                        put(key, value)
                    }
                }
                put("access_token", accessToken)
                put("refresh_token", refreshToken)
            }
            prefs.edit().putString(KEY_SESSION, newObj.toString()).apply()
            Log.d(TAG, "Stored tokens updated in SharedPreferences")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update stored tokens: ${e.message}")
        }
    }

    private fun restoreSupabaseSession() {
        val raw = prefs.getString(KEY_SESSION, null) ?: return
        try {
            val obj = json.parseToJsonElement(raw) as JsonObject
            val accessToken = obj["access_token"]?.jsonPrimitive?.contentOrNull
            val refreshToken = obj["refresh_token"]?.jsonPrimitive?.contentOrNull
            if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                val token = BackOfficeTokenDto(accessToken, refreshToken)
                // Use background scope to avoid blocking init
                kotlinx.coroutines.CoroutineScope(ioDispatcher).launch {
                    importSupabaseSession(token)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not restore Supabase session: ${e.message}")
        }
    }

    // =====================================================
    // OUTLET LOGIN
    // =====================================================

    override suspend fun loginOutlet(
        username: String,
        password: String
    ): Result<UserSession.OutletSession> = login(
        phone = username,
        password = password
    ).mapCatching { (dto, token) ->

        Log.d(
            TAG,
            "Outlet-compatible login success: outletId=${dto.outletId}, username=${dto.username}, role=${dto.role}"
        )

        val role = OutletRole.fromDbValue(dto.role.orEmpty())
            ?: throw IllegalStateException(
                "Unknown outlet role: ${dto.role}"
            )

        UserSession.OutletSession(
            outletId = dto.outletId,
            username = dto.username ?: username,
            role = role
        ).also { persistSession(it, token) }
    }

    // =====================================================
    // EMPLOYEE LOGIN
    // =====================================================

    override suspend fun loginEmployee(
        identifier: String,
        password: String
    ): Result<UserSession.EmployeeSession> = login(
        phone = identifier,
        password = password
    ).mapCatching { (dto, token) ->

        Log.d(
            TAG,
            "Employee login success: empId=${dto.empId}, outletId=${dto.outletId}"
        )

        UserSession.EmployeeSession(
            empId = dto.empId
                ?: throw IllegalStateException("Missing employee id"),

            outletId = dto.outletId,

            designationId = dto.designationId,

            permissions = dto.permissions.toSet(),

            role = OutletRole.fromDbValue(
                dto.role.orEmpty()
            ),

            name = dto.name,

            contact = dto.contact

        ).also { persistSession(it, token) }
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    override suspend fun logout() {
        _currentSession.value = null
        prefs.edit().clear().apply()
    }

    // =====================================================
    // LOGIN REQUEST
    // =====================================================

    private suspend fun login(
        phone: String,
        password: String
    ): Result<Pair<BackOfficeSessionDto, BackOfficeTokenDto?>> = withContext(ioDispatcher) {

        runCatching {
            val normalizedPhone = phone.trim()

            Log.d(
                TAG,
                "Calling backoffice_login for phone=$normalizedPhone"
            )

            val requestBody = buildJsonObject {
                put("phone", normalizedPhone)
                put("password", password)
            }.toString()

            val responseText =
                invokeBackOfficeLogin(requestBody)

            Log.d(
                TAG,
                "backoffice_login response: $responseText"
            )

            val decoded =
                json.decodeFromString<BackOfficeLoginResponseDto>(
                    responseText
                )

            if (!decoded.success) {

                Log.e(
                    TAG,
                    "Login failed: ${decoded.message}"
                )

                throw IllegalArgumentException(
                    decoded.message ?: "Invalid credentials"
                )
            }

            importSupabaseSession(decoded.token)

            val session = decoded.session ?: decoded.employee?.let { employee ->
                BackOfficeSessionDto(
                    type = "employee",
                    outletId = employee.outletId,
                    username = employee.contact ?: normalizedPhone,
                    role = employee.role,
                    name = employee.name,
                    contact = employee.contact,
                    empId = employee.id,
                    permissions = defaultPermissionsFor(employee.role)
                )
            } ?: throw IllegalStateException("Login success but employee session missing")

            session to decoded.token
        }.onFailure { throwable ->

            Log.e(
                TAG,
                "backoffice_login request failed: ${throwable.message}",
                throwable
            )
        }
    }

    // =====================================================
    // EDGE FUNCTION CALL
    // =====================================================

    private fun invokeBackOfficeLogin(
        requestBody: String
    ): String {

        val endpoint =
            "${supabaseUrl.trimEnd('/')}/functions/v1/backoffice_login"

        Log.d(TAG_HTTP, ">>> POST $endpoint")
        Log.d(TAG_HTTP, ">>> headers={Content-Type=application/json, Accept=application/json, apikey=<redacted>}")
        Log.d(TAG_HTTP, ">>> body=$requestBody")

        val connection =
            (URL(endpoint).openConnection() as HttpURLConnection).apply {

                requestMethod = "POST"

                connectTimeout = 20_000

                readTimeout = 20_000

                doOutput = true

                setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                setRequestProperty(
                    "Accept",
                    "application/json"
                )

                // REQUIRED FOR SUPABASE EDGE FUNCTIONS

                setRequestProperty(
                    "apikey",
                    supabaseAnonKey
                )
            }

        val startedAt = System.nanoTime()
        val statusCode: Int
        val responseText: String
        try {
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody)
            }

            statusCode = connection.responseCode

            responseText = if (statusCode in 200..299) {
                connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }
            } else {
                connection.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()
            }
        } catch (throwable: Throwable) {
            val elapsedMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
            Log.e(TAG_HTTP, "xxx network error after ${elapsedMs}ms: POST $endpoint", throwable)
            throw throwable
        } finally {
            connection.disconnect()
        }

        val elapsedMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
        val responseLog = "<<< $statusCode POST $endpoint (${elapsedMs}ms)\n" +
            "<<< body=${responseText.ifBlank { "<empty>" }}"
        if (statusCode in 200..299) {
            Log.d(TAG_HTTP, responseLog)
        } else {
            Log.e(TAG_HTTP, responseLog)
        }

        Log.d(
            TAG,
            "backoffice_login httpStatus=$statusCode"
        )

        if (statusCode !in 200..299) {

            throw IllegalStateException(
                responseText.ifBlank {
                    "backoffice_login failed with HTTP $statusCode"
                }
            )
        }

        return responseText
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun importSupabaseSession(token: BackOfficeTokenDto?) {
        val accessToken = token?.accessToken
        val refreshToken = token?.refreshToken
        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
            Log.w(TAG, "backoffice_login returned no Supabase auth token; RLS calls may fail")
            return
        }

        try {
            supabase.auth.importSession(
                SupabaseUserSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = 10L, // Short duration to ensure refresh logic checks it soon
                    tokenType = "bearer",
                    user = null
                )
            )
            // Explicitly refresh to ensure we have a valid non-expired JWT immediately
            supabase.auth.refreshCurrentSession()
        } catch (e: Exception) {
            Log.e(TAG, "Could not refresh imported session: ${e.message}")
            // If refresh fails (e.g. refresh token expired), clear the local session
            logout()
        }
    }

    private fun defaultPermissionsFor(role: String?): List<String> {
        return when (OutletRole.fromDbValue(role.orEmpty())) {
            OutletRole.OWNER -> listOf(
                "ANALYTICS_READ",
                "REPORTS_VIEW",
                "ORDERS_READ",
                "ORDERS_WRITE",
                "INVENTORY_READ",
                "INVENTORY_WRITE",
                "EMPLOYEES_READ",
                "EMPLOYEES_WRITE",
                "CUSTOMERS_READ",
                "SETTINGS_WRITE"
            )
            OutletRole.HEAD -> listOf(
                "ANALYTICS_READ",
                "REPORTS_VIEW",
                "ORDERS_READ",
                "ORDERS_WRITE",
                "INVENTORY_READ",
                "INVENTORY_WRITE",
                "EMPLOYEES_READ",
                "EMPLOYEES_WRITE",
                "CUSTOMERS_READ"
            )
            OutletRole.CHEF -> listOf("ORDERS_READ", "ORDERS_WRITE", "INVENTORY_READ", "ATTENDANCE_READ", "ATTENDANCE_WRITE")
            else -> listOf("ORDERS_READ", "ORDERS_WRITE", "CUSTOMERS_READ", "ATTENDANCE_READ", "ATTENDANCE_WRITE")
        }
    }

    // =====================================================
    // SAVE SESSION
    // =====================================================

    private fun persistSession(
        session: UserSession,
        token: BackOfficeTokenDto? = null
    ) {

        val basePayload = when (session) {

            is UserSession.OutletSession -> buildJsonObject {

                put("type", "outlet")

                put("outlet_id", session.outletId)

                put("username", session.username)

                put("role", session.role.dbValue)
            }

            is UserSession.EmployeeSession -> buildJsonObject {

                put("type", "employee")

                put("emp_id", session.empId)

                put("outlet_id", session.outletId)

                session.designationId?.let {
                    put("designation_id", it)
                }

                session.role?.let {
                    put("role", it.dbValue)
                }

                session.name?.let {
                    put("name", it)
                }

                session.contact?.let {
                    put("contact", it)
                }

                put(
                    "permissions",
                    session.permissions.joinToString("|")
                )
            }
        }

        val finalPayload = buildJsonObject {
            basePayload.forEach { (key, value) -> put(key, value) }
            token?.accessToken?.let { put("access_token", it) }
            token?.refreshToken?.let { put("refresh_token", it) }
        }

        prefs.edit()
            .putString(KEY_SESSION, finalPayload.toString())
            .apply()

        _currentSession.value = session
    }

    // =====================================================
    // READ STORED SESSION
    // =====================================================

    private fun readStoredSession(): UserSession? {

        val raw =
            prefs.getString(KEY_SESSION, null)
                ?: return null

        return runCatching {

            val obj =
                json.parseToJsonElement(raw) as JsonObject

            when (
                obj["type"]
                    ?.jsonPrimitive
                    ?.contentOrNull
            ) {

                "outlet" -> {

                    val role =
                        OutletRole.fromDbValue(
                            obj["role"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                .orEmpty()
                        ) ?: return null

                    UserSession.OutletSession(

                        outletId =
                            obj["outlet_id"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                ?.toLongOrNull()
                                ?: return null,

                        username =
                            obj["username"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                .orEmpty(),

                        role = role
                    )
                }

                "employee" -> {

                    UserSession.EmployeeSession(

                        empId =
                            obj["emp_id"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                .orEmpty(),

                        outletId =
                            obj["outlet_id"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                ?.toLongOrNull()
                                ?: return null,

                        designationId =
                            obj["designation_id"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                ?.toLongOrNull(),

                        role =
                            OutletRole.fromDbValue(
                                obj["role"]
                                    ?.jsonPrimitive
                                    ?.contentOrNull
                                    .orEmpty()
                            ),

                        name =
                            obj["name"]
                                ?.jsonPrimitive
                                ?.contentOrNull,

                        contact =
                            obj["contact"]
                                ?.jsonPrimitive
                                ?.contentOrNull,

                        permissions =
                            obj["permissions"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                .orEmpty()
                                .split("|")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .toSet()
                    )
                }

                else -> null
            }

        }.getOrNull()
    }

    private companion object {

        const val TAG = "BackOfficeAuth"

        const val KEY_SESSION = "session"

        const val TAG_HTTP = "SupabaseHTTP"
    }
}
