import io.github.jan.supabase.postgrest.Postgrest

//package com.jp.foodyvilla_backoffice.data.new_backoffice.repo
//
//import com.foodsys.analytics.domain.model.*
//import io.github.janatenvelden.supabase.postgrest.Postgrest
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.json.*
//import java.time.LocalDate
//import java.time.ZoneOffset
//import java.time.format.DateTimeFormatter
//
class AnalyticsRepositoryImpl(private val postgrest: Postgrest) {
//
//    suspend fun fetchDashboardMetrics(filter: DashboardFilter, customRange: DateRange?): DashboardUiState = withContext(Dispatchers.IO) {
//        val (start, end) = calculateDateRange(filter, customRange)
//        val (prevStart, prevEnd) = calculatePreviousDateRange(start, end)
//        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
//
//        val params = mapOf(
//            "p_start_time" to start.atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter),
//            "p_end_time" to end.atTime(23, 59, 59).atOffset(ZoneOffset.UTC).format(formatter),
//            "p_prev_start_time" to prevStart.atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter),
//            "p_prev_end_time" to prevEnd.atTime(23, 59, 59).atOffset(ZoneOffset.UTC).format(formatter)
//        )
//
//        return@withContext try {
//            // Native execution syntax via supabase-kt client
//            val response = postgrest.rpc(function = "get_dashboard_analytics_v2", parameters = params)
//            val json = Json.parseToJsonElement(response.data).jsonObject
//
//            mapJsonToUiState(json, filter, customRange)
//        } catch (e: Exception) {
//            DashboardUiState(isLoading = false, error = e.localizedMessage ?: "Supabase Sync Fault", selectedFilter = filter)
//        }
//    }
//
//    private fun calculateDateRange(filter: DashboardFilter, range: DateRange?): Pair<LocalDate, LocalDate> {
//        val today = LocalDate.now()
//        return when (filter) {
//            DashboardFilter.TODAY -> Pair(today, today)
//            DashboardFilter.YESTERDAY -> Pair(today.minusDays(1), today.minusDays(1))
//            DashboardFilter.LAST_7_DAYS -> Pair(today.minusDays(7), today)
//            DashboardFilter.LAST_30_DAYS -> Pair(today.minusDays(30), today)
//            DashboardFilter.THIS_MONTH -> Pair(today.withDayOfMonth(1), today)
//            DashboardFilter.LAST_MONTH -> {
//                val lastMonth = today.minusMonths(1)
//                Pair(lastMonth.withDayOfMonth(1), lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()))
//            }
//            DashboardFilter.CUSTOM -> Pair(range?.start ?: today.minusDays(7), range?.end ?: today)
//        }
//    }
//
//    private fun calculatePreviousDateRange(start: LocalDate, end: LocalDate): Pair<LocalDate, LocalDate> {
//        val duration = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1
//        return Pair(start.minusDays(duration), end.minusDays(duration))
//    }
//
//    private fun mapJsonToUiState(json: JsonObject, filter: DashboardFilter, range: DateRange?): DashboardUiState {
//        val fin = json["financials"]?.jsonObject
//        val ops = json["operationals"]?.jsonObject
//        val cat = json["catalog"]?.jsonObject
//        val wrk = json["workforce"]?.jsonObject
//
//        return DashboardUiState(
//            isLoading = false,
//            selectedFilter = filter,
//            customDateRange = range,
//            financialMetrics = FinancialAnalytics(
//                totalRevenue = fin?.get("total_revenue")?.jsonPrimitive?.double ?: 0.0,
//                revenueGrowthPercentage = fin?.get("revenue_growth")?.jsonPrimitive?.double ?: 0.0,
//                revenueTrend = fin?.get("revenue_trend")?.jsonArray?.map {
//                    TimeSeriesPoint(it.jsonObject["label"]!!.jsonPrimitive.content, it.jsonObject["value"]!!.jsonPrimitive.double)
//                } ?: emptyList(),
//                revenueByOutlet = fin?.get("revenue_by_outlet")?.jsonArray?.map {
//                    NamedMetric(it.jsonObject["name"]!!.jsonPrimitive.content, it.jsonObject["value"]!!.jsonPrimitive.double)
//                } ?: emptyList(),
//                revenueByCategory = fin?.get("revenue_by_category")?.jsonArray?.map {
//                    NamedMetric(it.jsonObject["name"]!!.jsonPrimitive.content, it.jsonObject["value"]!!.jsonPrimitive.double)
//                } ?: emptyList()
//            ),
//            operationalMetrics = OperationalAnalytics(
//                totalOrders = ops?.get("total_orders")?.jsonPrimitive?.int ?: 0,
//                averageOrderValue = ops?.get("average_order_value")?.jsonPrimitive?.double ?: 0.0,
//                statusDistribution = ops?.get("status_distribution")?.jsonArray?.map {
//                    NamedMetric(it.jsonObject["name"]!!.jsonPrimitive.content, it.jsonObject["value"]!!.jsonPrimitive.double)
//                } ?: emptyList(),
//                typeDistribution = ops?.get("type_distribution")?.jsonArray?.map {
//                    NamedMetric(it.jsonObject["name"]!!.jsonPrimitive.content, it.jsonObject["value"]!!.jsonPrimitive.double)
//                } ?: emptyList()
//            ),
//            catalogMetrics = CatalogAnalytics(
//                topSellingProducts = cat?.get("top_selling")?.jsonArray?.map {
//                    val o = it.jsonObject
//                    ProductRank(o["id"]!!.jsonPrimitive.long, o["name"]!!.jsonPrimitive.content, o["units_sold"]!!.jsonPrimitive.int, o["rating"]!!.jsonPrimitive.double, o["is_available"]!!.jsonPrimitive.boolean)
//                } ?: emptyList(),
//                outOfStockCount = cat?.get("out_of_stock_count")?.jsonPrimitive?.int ?: 0
//            ),
//            workforceMetrics = WorkforceAnalytics(
//                totalEmployees = wrk?.get("total_employees")?.jsonPrimitive?.int ?: 0,
//                activeEmployees = wrk?.get("active_employees")?.jsonPrimitive?.int ?: 0,
//                employeeLeaderboard = wrk?.get("leaderboard")?.jsonArray?.map {
//                    val o = it.jsonObject
//                    EmployeeRank(o["id"]!!.jsonPrimitive.long, o["name"]!!.jsonPrimitive.content, o["orders_handled"]!!.jsonPrimitive.int, o["revenue_handled"]!!.jsonPrimitive.double, o["attendance_rate"]!!.jsonPrimitive.double)
//                } ?: emptyList()
//            )
//        )
//    }
}