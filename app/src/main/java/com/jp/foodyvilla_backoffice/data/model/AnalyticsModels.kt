package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsRequest(
    val start_date: String,
    val end_date: String
)

@Serializable
data class AnalyticsResponse(
    val success: Boolean,
    val data: AnalyticsSummary? = null,
    val error: String? = null
)

@Serializable
data class AnalyticsSummary(
    val date_range_processed: AnalyticsDateRange,
    val orders: OrderMetrics,
    val customers: CustomerMetrics
)

@Serializable
data class AnalyticsDateRange(
    val start: String,
    val end: String
)

@Serializable
data class OrderMetrics(
    val total: Int,
    val pending: Int,
    val delivered: Int,
    val other_statuses: Int
)

@Serializable
data class CustomerMetrics(
    val total_active_buyers: Int,
    val new_registrations: Int
)

data class AnalyticsUiState(
    val summary: AnalyticsSummary? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val startDate: java.time.LocalDate = java.time.LocalDate.now().minusDays(30),
    val endDate: java.time.LocalDate = java.time.LocalDate.now()
)
