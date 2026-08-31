package com.jp.foodyvilla_backoffice.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

enum class DashboardFilter {
    TODAY, YESTERDAY, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH, LAST_MONTH, CUSTOM
}

data class DateRange(val start: LocalDate, val end: LocalDate)

// Core UI State
data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: DashboardFilter = DashboardFilter.LAST_7_DAYS,
    val customDateRange: DateRange? = null,
    val financialMetrics: FinancialAnalytics = FinancialAnalytics(),
    val operationalMetrics: OperationalAnalytics = OperationalAnalytics(),
    val catalogMetrics: CatalogAnalytics = CatalogAnalytics(),
    val workforceMetrics: WorkforceAnalytics = WorkforceAnalytics()
)

// Sub-domain Matrix Elements
data class FinancialAnalytics(
    val totalRevenue: Double = 0.0,
    val revenueGrowthPercentage: Double = 0.0,
    val revenueTrend: List<TimeSeriesPoint> = emptyList(),
    val revenueByOutlet: List<NamedMetric> = emptyList(),
    val revenueByCategory: List<NamedMetric> = emptyList()
)

data class OperationalAnalytics(
    val totalOrders: Int = 0,
    val averageOrderValue: Double = 0.0,
    val orderTrend: List<TimeSeriesPoint> = emptyList(),
    val statusDistribution: List<NamedMetric> = emptyList(),
    val typeDistribution: List<NamedMetric> = emptyList()
)

data class CatalogAnalytics(
    val topSellingProducts: List<ProductRank> = emptyList(),
    val mostLovedProducts: List<ProductRank> = emptyList(),
    val outOfStockCount: Int = 0
)

data class WorkforceAnalytics(
    val totalEmployees: Int = 0,
    val activeEmployees: Int = 0,
    val employeeLeaderboard: List<EmployeeRank> = emptyList()
)

// Primitive Data Containers for Visualizations
data class TimeSeriesPoint(val label: String, val value: Double)
data class NamedMetric(val name: String, val value: Double)

data class ProductRank(val id: Long, val name: String, val unitsSold: Int, val rating: Double, val isAvailable: Boolean)
data class EmployeeRank(val id: Long, val name: String, val ordersHandled: Int, val revenueHandled: Double, val attendanceRate: Double)