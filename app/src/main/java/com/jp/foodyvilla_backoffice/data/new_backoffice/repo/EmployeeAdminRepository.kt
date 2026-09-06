package com.jp.foodyvilla_backoffice.data.new_backoffice.repo

import android.content.Context
import android.net.Uri
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

class EmployeeAdminRepository(private val supabase: SupabaseClient, private val context: Context) {

    suspend fun fetchAllOutletsMinimal(): List<OutletResponse> =
        supabase.from("outlets").select().decodeList()

    suspend fun fetchRawEmployeesAdmin(): List<EmployeeAdminResponse> =
        supabase.from("employee").select {
            order("created_at", order = Order.DESCENDING)
        }.decodeList()

    suspend fun uploadProfileImage(uri: Uri): String {
        val bucket = supabase.storage.from("employee")
        val fileName = "profile_${UUID.randomUUID()}.jpg"
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: throw Exception("Failed to read binary input streams from uri source pointer.")

        bucket.upload(fileName, bytes)
        return bucket.publicUrl(fileName)
    }

    suspend fun uploadImageBytes(bytes: ByteArray, prefix: String = "doc"): String {
        val bucket = supabase.storage.from("employee")
        val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
        bucket.upload(fileName, bytes)
        return bucket.publicUrl(fileName)
    }

    suspend fun createEmployee(model: EmployeeAdminUiModel, imgUrl: String?) {
        supabase.from("employee").insert(buildJsonObject {
            put("outlet_id", model.outletId)
            put("name", model.name.trim())
            put("address", model.address.trim())
            put("contact", model.contact.trim())
            put("aadhar_no", model.aadharNo.trim().ifBlank { "[Aadhaar Redacted]" })
            put("emergency_contact", model.emergencyContact.trim())
            put("salary", model.salary.toLongOrNull())
            put("profile_img", imgUrl ?: model.profileImgUrl.ifBlank { null })
            put("joining_date", model.joiningDate.trim().ifBlank { null })
            put("password_hash", model.passwordText.trim().ifBlank { null })
            put("role", model.role.name.lowercase())
            put("is_active", model.isActive)
            put("punch_in_time", model.punchInTime.trim().ifBlank { null })
            put("punch_out_time", model.punchOutTime.trim().ifBlank { null })
        })
    }

    suspend fun updateEmployeeRecord(model: EmployeeAdminUiModel, imgUrl: String?) {
        supabase.from("employee").update(buildJsonObject {
            put("outlet_id", model.outletId)
            put("name", model.name.trim())
            put("address", model.address.trim())
            put("contact", model.contact.trim())
            put("aadhar_no", model.aadharNo.trim().ifBlank { "[Aadhaar Redacted]" })
            put("emergency_contact", model.emergencyContact.trim())
            put("salary", model.salary.toLongOrNull())
            put("joining_date", model.joiningDate.trim().ifBlank { null })
            put("role", model.role.name.lowercase())
            put("profile_img", imgUrl ?: model.profileImgUrl.ifBlank { null })
            put("password_hash", model.passwordText.trim().ifBlank { null })
            put("is_active", model.isActive)
            put("punch_in_time", model.punchInTime.trim().ifBlank { null })
            put("punch_out_time", model.punchOutTime.trim().ifBlank { null })
        }) { filter { eq("id", model.id) } }
    }

    suspend fun deleteEmployeeRecord(id: Long) {
        supabase.from("employee").delete { filter { eq("id", id) } }
    }
}