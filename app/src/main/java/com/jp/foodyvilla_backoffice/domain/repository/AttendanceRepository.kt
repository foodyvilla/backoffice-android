package com.jp.foodyvilla_backoffice.domain.repository

import com.jp.foodyvilla_backoffice.data.model.*
import io.github.jan.supabase.auth.user.UserInfo
import java.time.LocalDateTime
import java.time.YearMonth

interface AttendanceRepository {
    suspend fun fetchEmployeeProfile(empId: Long): EmployeeResponse
    suspend fun fetchOutletSpecs(outletId: Long): OutletResponse
    suspend fun fetchEmployeesByOutlet(outletId: Long): List<EmployeeResponse>
    suspend fun fetchAttendanceHistory(empId: Long, month: YearMonth? = null): List<AttendanceUiModel>
    fun getCurrentSessionUser(): UserInfo?
    suspend fun fetchEmployeeProfileByAuthId(authUserId: String): EmployeeResponse?
    suspend fun recordPunchIn(empId: Long, currentLat: Double, currentLng: Double, currentDateTime: LocalDateTime): Result<Unit>
    suspend fun recordPunchOut(attendanceId: Long, currentLat: Double, currentLng: Double, currentDateTime: LocalDateTime): Result<Unit>
}
