package com.jp.foodyvilla_backoffice.domain.repository

import com.jp.foodyvilla_backoffice.domain.security.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentSession: StateFlow<UserSession?>

    suspend fun loginOutlet(username: String, password: String): Result<UserSession.OutletSession>
    suspend fun loginEmployee(identifier: String, password: String): Result<UserSession.EmployeeSession>
    suspend fun logout()
}
