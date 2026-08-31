package com.jp.foodyvilla_backoffice.domain.repository

import com.jp.foodyvilla_backoffice.data.model.*

interface AttendanceAdminRepository {
    suspend fun fetchAllEmployeesMinimal(): List<EmployeeAdminMinimalResponse>
    suspend fun fetchRawLogsAdmin(): List<AttendanceAdminResponse>
    suspend fun createAttendanceRecord(model: AttendanceAdminUiModel)
    suspend fun updateAttendanceRecord(model: AttendanceAdminUiModel)
    suspend fun purgeAttendanceLogRecord(id: Long)
}
