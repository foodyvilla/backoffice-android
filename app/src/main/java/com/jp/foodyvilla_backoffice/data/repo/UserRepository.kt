package com.jp.foodyvilla_backoffice.data.repo

import com.jp.foodyvilla_backoffice.data.model.user.UserProfile
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ==========================================
// Domain & Data Models
// ==========================================

@Serializable
enum class EmployeeRole {
    HEAD, OWNER, CHEF, EMPLOYEE
}

@Serializable
data class NewEmployeeProfileUiModel(
    val id: Long,
    val outletId: Long?,
    val name: String,
    val address: String?,
    val contact: String?,
    val aadharNo: String?,
    val emergencyContact: String?,
    val salary: Long?,
    val profileImg: String?,
    val joiningDate: String?,
    val punchLat: Double?,
    val punchLng: Double?,
    val role: EmployeeRole,
    val fcmToken: String?,
    val isActive: Boolean
)

@Serializable
private data class NewEmployeeResponse(
    val id: Long,
    val outlet_id: Long? = null,
    val name: String,
    val address: String? = null,
    val contact: String? = null,
    val aadhar_no: String? = null,
    val emergency_contact: String? = null,
    val salary: Long? = null,
    val profile_img: String? = null,
    val joining_date: String? = null,
    val punch_lat: Double? = null,
    val punch_lng: Double? = null,
    val role: String? = null,
    val is_active: Boolean,
    val auth_user_id: String? = null,
    val fcm_token: String? = null
)

@Serializable
data class NewUserProfileUiModel(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val address: String?,
    val lat: Double?,
    val lng: Double?,
    val fcmToken: String?
)

@Serializable
private data class NewUserResponse(
    val id: Long,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val long: Double? = null,
    val fcm_token: String? = null,
    val auth_user_id: String? = null
)

// ==========================================
// Mappers
// ==========================================

private fun NewEmployeeResponse.toUiModel(): NewEmployeeProfileUiModel {
    val matchedRole = runCatching {
        EmployeeRole.valueOf(role.orEmpty().uppercase())
    }.getOrElse { EmployeeRole.EMPLOYEE }

    return NewEmployeeProfileUiModel(
        id = id, outletId = outlet_id, name = name, address = address,
        contact = contact, aadharNo = aadhar_no, emergencyContact = emergency_contact,
        salary = salary, profileImg = profile_img, joiningDate = joining_date,
        punchLat = punch_lat, punchLng = punch_lng, role = matchedRole,
        fcmToken = fcm_token, isActive = is_active
    )
}

private fun NewUserResponse.toProfileUiModel(): NewUserProfileUiModel {
    return NewUserProfileUiModel(
        id = id, name = name.orEmpty().ifBlank { "User #$id" }, email = email.orEmpty(),
        phone = phone.orEmpty(), address = address, lat = lat, lng = long, fcmToken = fcm_token
    )
}

// ==========================================
// Repository Implementation
// ==========================================

class UserRepository(private val supabase: SupabaseClient) {

    /**
     * Retrieves the employee profile matching the current session metadata context.
     */
    suspend fun getCurrentEmployeeProfile(): NewEmployeeProfileUiModel? {
        return runCatching {
            val authUser = supabase.auth.currentUserOrNull() ?: return null
            supabase
                .from("employee")
                .select {
                    filter {
                        eq("auth_user_id", authUser.id)
                        eq("is_active", true)
                    }
                }
                .decodeSingleOrNull<NewEmployeeResponse>()
                ?.toUiModel()
        }.getOrNull()
    }

    /**
     * Retrieves the customer user profile matching the current session metadata context.
     */
    suspend fun getCurrentLoggedInUserProfile(): NewUserProfileUiModel? {
        return runCatching {
            val currentAuthUser = supabase.auth.currentUserOrNull() ?: return null
            supabase
                .from("users")
                .select {
                    filter {
                        eq("auth_user_id", currentAuthUser.id)
                    }
                }
                .decodeSingleOrNull<NewUserResponse>()
                ?.toProfileUiModel()
        }.getOrNull()
    }

    /**
     * Safely updates an employee's FCM Token either via Auth User ID context or a direct database row ID match.
     */
    suspend fun updateFcmToken(
        fcmToken: String?,
        empId: String? = null
    ) {
        try {
            val authUserId = supabase.auth.currentUserOrNull()?.id

            val updatePayload = buildJsonObject {
                put("fcm_token", fcmToken)
            }

            // 1. Update by authUserId in employee table
            if (authUserId != null) {
                supabase.from("employee").update(updatePayload) {
                    filter {
                        eq("auth_user_id", authUserId)
                    }
                }
            }

            // 2. Update by empId in employee table
            if (empId != null) {
                supabase.from("employee").update(updatePayload) {
                    filter {
                        eq("id", empId.toLong()) // Safe casting pattern to match database structure types
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}