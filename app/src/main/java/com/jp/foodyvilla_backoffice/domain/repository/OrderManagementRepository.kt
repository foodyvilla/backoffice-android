package com.jp.foodyvilla_backoffice.domain.repository

import com.jp.foodyvilla_backoffice.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface OrderManagementRepository {
    suspend fun resolveEmployeeRowId(authUserId: String): Long?
    
    suspend fun fetchOutletOrdersSnapshot(
        outletId: Long?,
        isOwner: Boolean,
        filterDate: LocalDate = LocalDate.now()
    ): List<NewDetailedOrderUiModel>

    fun observeOutletOrdersRealTime(
        outletId: Long?,
        isOwner: Boolean,
        filterDate: LocalDate
    ): Flow<List<NewDetailedOrderUiModel>>

    suspend fun fetchActiveOutletsList(): List<OutletDropdownUiModel>

    suspend fun getOutletMenu(outletId: Long): List<NewOutletMenuUiModel>

    suspend fun getCategories(): List<NewCategoryUiModel>

    suspend fun findCustomerByPhone(phone: String): NewCustomerUiModel?

    suspend fun updateOrderDetails(
        orderId: String, status: String, address: String, instruction: String,
        orderType: String, outletId: Long, customerPhone: String, internalEmpId: Long?,
        tableId: Long? = null
    )

    suspend fun triggerFcmUpdateNotification(token: String, title: String, body: String)

    suspend fun triggerOutletEdgeNotification(outletId: Long, orderId: String, alertMessage: String)

    suspend fun placeOrder(
        outletId: Long, customer: NewCustomerUiModel?, phone: String,
        address: String, orderType: NewOrderType, instruction: String,
        items: List<NewSelectedMenuItem>, internalEmpId: Long?,
        tableId: Long? = null
    ): NewOrderUiModel

    suspend fun getAnalyticsSummary(startDate: LocalDate, endDate: LocalDate): AnalyticsResponse
}
