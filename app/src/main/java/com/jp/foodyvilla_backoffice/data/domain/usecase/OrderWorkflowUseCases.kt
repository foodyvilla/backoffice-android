package com.jp.foodyvilla_backoffice.data.domain.usecase

import com.jp.foodyvilla_backoffice.data.domain.model.OrderStatus
import com.jp.foodyvilla_backoffice.data.domain.repository.OrderWorkflowRepository

class ObserveIncomingOrdersUseCase(
    private val repository: OrderWorkflowRepository
) {
    operator fun invoke(outletId: Long) = repository.observeIncomingOrders(outletId)
}

class AcceptOrderUseCase(
    private val repository: OrderWorkflowRepository
) {
    suspend operator fun invoke(orderId: String) =
        repository.transitionOrder(orderId, OrderStatus.Accepted)
}

class MoveOrderStatusUseCase(
    private val repository: OrderWorkflowRepository
) {
    suspend operator fun invoke(orderId: String, status: OrderStatus) =
        repository.transitionOrder(orderId, status)
}

class RejectOrderUseCase(
    private val repository: OrderWorkflowRepository
) {
    suspend operator fun invoke(orderId: String, reason: String): Result<Unit> {
        if (reason.isBlank()) return Result.failure(IllegalArgumentException("Rejection reason is required"))
        return repository.rejectOrder(orderId, reason.trim())
    }
}

