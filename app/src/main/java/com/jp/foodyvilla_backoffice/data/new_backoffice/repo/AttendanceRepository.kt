package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import android.location.Location
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class AttendanceRepository(private val supabase: SupabaseClient) {

    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private fun parseTime(timeStr: String?): LocalTime? {
        if (timeStr.isNullOrBlank()) return null
        return try {
            val clean = timeStr.split("+")[0].split("-")[0].replace("Z", "").trim()
            val parts = clean.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val second = parts.getOrNull(2)?.split(".")?.get(0)?.toIntOrNull() ?: 0
            LocalTime.of(hour, minute, second)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchEmployeeProfile(empId: Long): EmployeeResponse =
        supabase.from("employee").select { filter { eq("id", empId) } }.decodeSingle<EmployeeResponse>()

    suspend fun fetchOutletSpecs(outletId: Long): OutletResponse =
        supabase.from("outlets").select { filter { eq("id", outletId) } }.decodeSingle<OutletResponse>()

    suspend fun fetchEmployeesByOutlet(outletId: Long): List<EmployeeResponse> =
        supabase.from("employee").select { filter { eq("outlet_id", outletId) } }.decodeList<EmployeeResponse>()

    suspend fun fetchAttendanceHistory(empId: Long, month: YearMonth? = null): List<AttendanceUiModel> {
        val rawList = supabase.from("attendance")
            .select {
                filter {
                    eq("emp_id", empId)
                    if (month != null) {
                        val start = month.atDay(1).atStartOfDay().format(dateTimeFormatter)
                        val end = month.atEndOfMonth().atTime(23, 59, 59).format(dateTimeFormatter)
                        gte("in_time", start)
                        lte("in_time", end)
                    }
                }
            }
            .decodeList<AttendanceResponse>()

        return rawList.map { raw ->
            val parsedInTime = raw.in_time?.let { LocalDateTime.parse(it, dateTimeFormatter) }
            val parsedOutTime = raw.out_time?.let { LocalDateTime.parse(it, dateTimeFormatter) }
            val parsedDate = parsedInTime?.toLocalDate() ?: LocalDate.now()

            AttendanceUiModel(
                id = raw.id, empId = raw.emp_id, date = parsedDate,
                status = when (raw.status?.uppercase()) {
                    "PRESENT" -> AttendanceStatus.PRESENT
                    "LATE" -> AttendanceStatus.LATE
                    "HALF_DAY" -> AttendanceStatus.HALF_DAY
                    "ABSENT" -> AttendanceStatus.ABSENT
                    else -> AttendanceStatus.PENDING
                },
                inTime = parsedInTime, outTime = parsedOutTime,
                inLat = raw.in_lat, inLng = raw.in_lng, outLat = raw.out_lat, outLng = raw.out_lng
            )
        }
    }

    fun getCurrentSessionUser(): UserInfo? {
        return supabase.auth.currentSessionOrNull()?.user
    }

    suspend fun fetchEmployeeProfileByAuthId(authUserId: String): EmployeeResponse? = runCatching {
        supabase.from("employee")
            .select { filter { eq("auth_user_id", authUserId) } }
            .decodeSingle<EmployeeResponse>()
    }.getOrNull()

    suspend fun recordPunchIn(empId: Long, currentLat: Double, currentLng: Double, currentDateTime: LocalDateTime): Result<Unit> = runCatching {
        val employee = fetchEmployeeProfile(empId)
        val outletId = employee.outlet_id ?: throw Exception("Employee is not linked to any operational outlet node.")
        val outlet = fetchOutletSpecs(outletId)

        val results = FloatArray(1)
        Location.distanceBetween(currentLat, currentLng, outlet.lat, outlet.lng, results)
        val distanceMeters = results[0]
        val allowedRadius = outlet.attendance_radius_meters

        if (distanceMeters > allowedRadius) {
            throw Exception("Geofencing verification failed. Distance: ${distanceMeters.toInt()}m from outlet perimeter window. Max allowed range: ${allowedRadius.toInt()}m.")
        }

        // Prioritize employee.punch_in_time, fallback to outlet.opens_at, fallback to 09:00:00
        val expectedInTime = parseTime(employee.punch_in_time)
            ?: parseTime(outlet.opens_at)
            ?: LocalTime.of(9, 0, 0)

        val targetPunchTime = currentDateTime.toLocalTime()

        val calculatedStatus = when {
            targetPunchTime.isBefore(expectedInTime.plusMinutes(15)) -> AttendanceStatus.PRESENT
            targetPunchTime.isBefore(expectedInTime.plusMinutes(45)) -> AttendanceStatus.LATE
            else -> AttendanceStatus.HALF_DAY
        }

        supabase.from("attendance").insert(buildJsonObject {
            put("emp_id", empId)
            put("status", calculatedStatus.name)
            put("in_time", currentDateTime.format(dateTimeFormatter))
            put("in_lat", currentLat)
            put("in_lng", currentLng)
        })
    }

    suspend fun recordPunchOut(attendanceId: Long, currentLat: Double, currentLng: Double, currentDateTime: LocalDateTime): Result<Unit> = runCatching {
        println("Punch out $attendanceId")
        try{
            supabase.from("attendance").update(buildJsonObject {
                put("out_time", currentDateTime.format(dateTimeFormatter))
                put("out_lat", currentLat)
                put("out_lng", currentLng)
            }) { filter { eq("id", attendanceId) } }
        }catch(e: Exception){
            println("Punch out error $e")
        }

    }
}