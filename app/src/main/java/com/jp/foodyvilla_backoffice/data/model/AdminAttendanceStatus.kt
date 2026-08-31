package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AdminAttendanceStatus {
    PRESENT, LATE, HALF_DAY, ABSENT, PENDING
}

@Serializable
data class AttendanceAdminResponse(
    val id: Long = 0L,
    val created_at: String? = null,
    val emp_id: Long,
    val status: String? = null,
    val in_time: String? = null,
    val out_time: String? = null,
    val in_lat: Double? = null,
    val in_lng: Double? = null,
    val out_lat: Double? = null,
    val out_lng: Double? = null
)

@Serializable
data class EmployeeAdminMinimalResponse(
    val id: Long,
    val name: String,
    val role: String,
    val outlet_id: Long? = null
)

data class AttendanceAdminUiModel(
    val id: Long = 0L,
    val empId: Long,
    val employeeName: String = "Unknown Staff",
    val status: AdminAttendanceStatus = AdminAttendanceStatus.PRESENT,
    val inTime: String = "",
    val outTime: String = "",
    val inLat: String = "",
    val inLng: String = "",
    val outLat: String = "",
    val outLng: String = ""
)

fun AttendanceAdminResponse.toUiModel(employeeName: String) = AttendanceAdminUiModel(
    id = id,
    empId = emp_id,
    employeeName = employeeName,
    status = when (status?.uppercase()) {
        "PRESENT" -> AdminAttendanceStatus.PRESENT
        "LATE" -> AdminAttendanceStatus.LATE
        "HALF_DAY" -> AdminAttendanceStatus.HALF_DAY
        "ABSENT" -> AdminAttendanceStatus.ABSENT
        else -> AdminAttendanceStatus.PENDING
    },
    inTime = in_time.orEmpty().replace("T", " "),
    outTime = out_time.orEmpty().replace("T", " "),
    inLat = in_lat?.toString().orEmpty(),
    inLng = in_lng?.toString().orEmpty(),
    outLat = out_lat?.toString().orEmpty(),
    outLng = out_lng?.toString().orEmpty()
)