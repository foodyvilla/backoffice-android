package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

public enum class AttendanceStatus {
    PRESENT, LATE, HALF_DAY, ABSENT, PENDING
}

@Serializable
data class AttendanceResponse(
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
data class EmployeeResponse(
    val id: Long,
    val name: String,
    val outlet_id: Long? = null,
    val salary: Long? = null,
    val role: String? = null,
    val punch_in_time: String? = null,
    val punch_out_time: String? = null
)

data class AttendanceUiModel(
    val id: Long,
    val date: LocalDate,
    val empId: Long,
    val status: AttendanceStatus,
    val inTime: LocalDateTime?,
    val outTime: LocalDateTime?,
    val inLat: Double?,
    val inLng: Double?,
    val outLat: Double?,
    val outLng: Double?
)