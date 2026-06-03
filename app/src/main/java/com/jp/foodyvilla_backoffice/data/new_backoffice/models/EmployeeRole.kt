package com.jp.foodyvilla_backoffice.data.new_backoffice.models

import kotlinx.serialization.Serializable

@Serializable
enum class EmployeeRole {
    HEAD, OWNER, CHEF, EMPLOYEE
}

@Serializable
data class EmployeeAdminResponse(
    val id: Long = 0L,
    val created_at: String? = null,
    val outlet_id: Long? = null,
    val name: String,
    val address: String? = null,
    val contact: String? = null,
    val aadhar_no: String? = null, // Handled carefully down to UI forms
    val emergency_contact: String? = null,
    val salary: Long? = null,
    val profile_img: String? = null,
    val joining_date: String? = null, // Date format mapped string format
    val role: String,
    val is_active: Boolean = true,
    val auth_user_id: String? = null
)

data class EmployeeAdminUiModel(
    val id: Long = 0L,
    val outletId: Long? = null,
    val outletName: String = "Unassigned Outlet",
    val name: String = "",
    val address: String = "",
    val contact: String = "",
    val aadharMaskedPlaceholder: String = "", // Protected locally
    val emergencyContact: String = "",
    val salary: String = "",
    val profileImg: String = "",
    val joiningDate: String = "",
    val role: EmployeeRole = EmployeeRole.EMPLOYEE,
    val isActive: Boolean = true,
    val authUserId: String = ""
)

fun EmployeeAdminResponse.toUiModel(outletName: String) = EmployeeAdminUiModel(
    id = id,
    outletId = outlet_id,
    outletName = outletName,
    name = name,
    address = address.orEmpty(),
    contact = contact.orEmpty(),
    // Ensures sensitive identifiers are redacted out of local view buffers
    aadharMaskedPlaceholder = if (!aadhar_no.isNullOrBlank()) "[Aadhaar Redacted]" else "",
    emergencyContact = emergency_contact.orEmpty(),
    salary = salary?.toString().orEmpty(),
    profileImg = profile_img.orEmpty(),
    joiningDate = joining_date.orEmpty(),
    role = when (role.lowercase()) {
        "head" -> EmployeeRole.HEAD
        "owner" -> EmployeeRole.OWNER
        "chef" -> EmployeeRole.CHEF
        else -> EmployeeRole.EMPLOYEE
    },
    isActive = is_active,
    authUserId = auth_user_id.orEmpty()
)