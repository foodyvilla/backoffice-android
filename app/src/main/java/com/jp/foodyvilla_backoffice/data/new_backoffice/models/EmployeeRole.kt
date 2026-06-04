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
    val aadhar_no: String? = null,
    val emergency_contact: String? = null,
    val salary: Long? = null,
    val profile_img: String? = null,
    val joining_date: String? = null,
    val role: String,
    val password_hash: String? = null,
    val is_active: Boolean = true
)

data class EmployeeAdminUiModel(
    val id: Long = 0L,
    val outletId: Long? = null,
    val outletName: String = "Unassigned Outlet",
    val name: String = "",
    val address: String = "",
    val contact: String = "",
    val aadharNo: String = "",
    val emergencyContact: String = "",
    val salary: String = "",
    val profileImgUrl: String = "",
    val joiningDate: String = "",
    val passwordText: String = "",
    val role: EmployeeRole = EmployeeRole.EMPLOYEE,
    val isActive: Boolean = true
)

fun EmployeeAdminResponse.toUiModel(outletName: String) = EmployeeAdminUiModel(
    id = id,
    outletId = outlet_id,
    outletName = outletName,
    name = name,
    address = address.orEmpty(),
    contact = contact.orEmpty(),
    aadharNo = aadhar_no.orEmpty(),
    emergencyContact = emergency_contact.orEmpty(),
    salary = salary?.toString().orEmpty(),
    profileImgUrl = profile_img.orEmpty(),
    joiningDate = joining_date.orEmpty(),
    passwordText = password_hash.orEmpty(),
    role = when (role.lowercase()) {
        "head" -> EmployeeRole.HEAD
        "owner" -> EmployeeRole.OWNER
        "chef" -> EmployeeRole.CHEF
        else -> EmployeeRole.EMPLOYEE
    },
    isActive = is_active
)