package com.jp.foodyvilla_backoffice.data.repository

import com.jp.foodyvilla_backoffice.data.model.*
import com.jp.foodyvilla_backoffice.domain.repository.AttendanceAdminRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AttendanceAdminRepositoryImpl(private val supabase: SupabaseClient) : AttendanceAdminRepository {

    override suspend fun fetchAllEmployeesMinimal(): List<EmployeeAdminMinimalResponse> =
        supabase.from("employee").select().decodeList()

    override suspend fun fetchRawLogsAdmin(): List<AttendanceAdminResponse> =
        supabase.from("attendance").select {
            order("in_time", order = Order.DESCENDING)
        }.decodeList()

    override suspend fun createAttendanceRecord(model: AttendanceAdminUiModel) {
        supabase.from("attendance").insert(buildJsonObject {
            put("emp_id", model.empId)
            put("status", model.status.name)
            put("in_time", model.inTime.trim().replace(" ", "T"))
            if (model.outTime.isNotBlank()) put("out_time", model.outTime.trim().replace(" ", "T"))
            put("in_lat", model.inLat.toDoubleOrNull())
            put("in_lng", model.inLng.toDoubleOrNull())
            put("out_lat", model.outLat.toDoubleOrNull())
            put("out_lng", model.outLng.toDoubleOrNull())
        })
    }

    override suspend fun updateAttendanceRecord(model: AttendanceAdminUiModel) {
        supabase.from("attendance").update(buildJsonObject {
            put("emp_id", model.empId)
            put("status", model.status.name)
            put("in_time", model.inTime.trim().replace(" ", "T"))
            put("out_time", model.outTime.trim().replace(" ", "T").ifBlank { null })
            put("in_lat", model.inLat.toDoubleOrNull())
            put("in_lng", model.inLng.toDoubleOrNull())
            put("out_lat", model.outLat.toDoubleOrNull())
            put("out_lng", model.outLng.toDoubleOrNull())
        }) { filter { eq("id", model.id) } }
    }

    override suspend fun purgeAttendanceLogRecord(id: Long) {
        supabase.from("attendance").delete { filter { eq("id", id) } }
    }
}
