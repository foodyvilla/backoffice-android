package com.jp.foodyvilla_backoffice.data.domain.repository

import com.jp.foodyvilla_backoffice.data.domain.model.BackOfficeOrder
import com.jp.foodyvilla_backoffice.data.domain.model.BackOfficeOrderItem
import com.jp.foodyvilla_backoffice.data.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

data class OrderAlert(
    val order: BackOfficeOrder,
    val items: List<BackOfficeOrderItem>
) {
    val totalAmount: Double = items.sumOf { it.totalPrice }
    val itemCount: Int = items.sumOf { it.qty }
}

interface OrderWorkflowRepository {
    fun observeIncomingOrders(outletId: Long): Flow<Result<OrderAlert>>
    suspend fun loadOrder(orderId: String): Result<OrderAlert>
    suspend fun transitionOrder(orderId: String, status: OrderStatus): Result<Unit>
    suspend fun rejectOrder(orderId: String, reason: String): Result<Unit>
}

