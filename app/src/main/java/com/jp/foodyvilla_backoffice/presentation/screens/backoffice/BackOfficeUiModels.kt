package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumn
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumnType
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminTable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

internal val RoyalBlue = Color(0xFF1238D8)
internal val Ink = Color(0xFF111827)
internal val Muted = Color(0xFF667085)
internal val CanvasColor = Color(0xFFF5F7FB)
internal val SoftLine = Color(0xFFE5EAF3)
internal val Success = Color(0xFF16A34A)
internal val Warning = Color(0xFFF59E0B)
internal val Orange = Color(0xFFF97316)
internal val Purple = Color(0xFF7C3AED)
internal val Danger = Color(0xFFDC2626)

internal enum class AdminRoute(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tableName: String? = null,
    val canCreate: Boolean = false
) {
    Dashboard("Dashboard", "Live store overview", Icons.Default.Home),
    Products("Products", "Menu items and stock", Icons.Default.RestaurantMenu, "products", true),
    Categories("Categories", "Menu hierarchy", Icons.Default.Category),
    Orders("Orders", "Kitchen and delivery queue", Icons.Default.ReceiptLong, "orders", true),
    Customers("Customers", "Profiles and activity", Icons.Default.People, "users", true),
    Reviews("Reviews", "Ratings and moderation", Icons.Default.Star, "reviews", true),
    Offers("Offers/Coupons", "Campaigns and coupons", Icons.Default.LocalOffer, "offers", true),
    Banners("Banners", "Promotional media", Icons.Default.Campaign, "banners", true),
    Employees("Employees", "Team and roles", Icons.Default.Badge, "employee", true),
    Attendance("Attendance", "Shifts and punches", Icons.Default.DateRange, "attendance", true),
    Notifications("Notifications", "Push and in-app alerts", Icons.Default.Notifications),
    Analytics("Analytics", "Revenue and operational trends", Icons.Default.Analytics),
    Settings("Settings", "Store, tax, printer, permissions", Icons.Default.Settings),
    Profile("Profile/Store", "Outlet profile", Icons.Default.Storefront),
    Details("Details", "Full screen details", Icons.Default.Inventory),
    Form("Create/Edit", "Full screen editor", Icons.Default.Edit)
}

internal enum class FormMode { Create, Edit }

internal val drawerGroups = listOf(
    "Overview" to listOf(AdminRoute.Dashboard, AdminRoute.Analytics, AdminRoute.Notifications),
    "Operations" to listOf(AdminRoute.Orders, AdminRoute.Attendance),
    "Catalog" to listOf(AdminRoute.Products, AdminRoute.Categories, AdminRoute.Offers, AdminRoute.Banners),
    "Customers" to listOf(AdminRoute.Customers, AdminRoute.Reviews),
    "Team" to listOf(AdminRoute.Employees),
    "Control" to listOf(AdminRoute.Settings, AdminRoute.Profile)
)

internal fun routeForTable(tableName: String): AdminRoute = when (tableName) {
    "orders" -> AdminRoute.Orders
    "products" -> AdminRoute.Products
    "users" -> AdminRoute.Customers
    "reviews" -> AdminRoute.Reviews
    "offers" -> AdminRoute.Offers
    "banners" -> AdminRoute.Banners
    "employee" -> AdminRoute.Employees
    "attendance" -> AdminRoute.Attendance
    else -> AdminRoute.Dashboard
}

internal fun statusColor(status: String): Color = when (status.normalizeOrderStatus().lowercase()) {
    "placed" -> Warning
    "preparing" -> Orange
    "ready" -> RoyalBlue
    "out for delivery" -> Purple
    "cancelled" -> Danger
    "delivered" -> Success
    else -> Muted
}

internal fun String.normalizeOrderStatus(): String = when {
    contains("PREPAR", true) -> "Preparing"
    contains("READY", true) -> "Ready"
    contains("OUT", true) -> "Out for Delivery"
    contains("CANCEL", true) -> "Cancelled"
    contains("DELIVER", true) -> "Delivered"
    contains("PLACED", true) || this == "-" -> "Placed"
    else -> replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}

internal fun JsonElement?.toDisplayText(fallback: String = "-"): String {
    return when (this) {
        null, JsonNull -> fallback
        is JsonPrimitive -> contentOrNull?.takeIf { it.isNotBlank() } ?: fallback
        is JsonArray -> joinToString(", ") { it.toDisplayText(fallback) }.ifBlank { fallback }
        is JsonObject -> entries.take(3).joinToString(", ") { "${it.key}: ${it.value.toDisplayText(fallback)}" }.ifBlank { fallback }
    }
}

internal fun JsonElement?.asNumber(): Double {
    return toDisplayText("0").filter { it.isDigit() || it == '.' || it == '-' }.toDoubleOrNull() ?: 0.0
}

internal fun JsonObject.firstImageUrl(table: AdminTable? = null): String? {
    val names = table?.columns?.map { it.name }.orEmpty() + keys
    val imageColumns = names.distinct().filter { name ->
        name.contains("img", true) || name.contains("image", true) || name.contains("photo", true)
    }
    return imageColumns.firstNotNullOfOrNull { this[it].firstUrlOrNull() }
}

internal fun JsonElement?.firstUrlOrNull(): String? {
    return when (this) {
        null, JsonNull -> null
        is JsonArray -> firstNotNullOfOrNull { it.firstUrlOrNull() }
        is JsonObject -> values.firstNotNullOfOrNull { it.firstUrlOrNull() }
        is JsonPrimitive -> contentOrNull?.extractUrl()
    }
}

internal fun String.extractUrl(): String? {
    val trimmed = trim().trim('"')
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    return Regex("""https?://[^\s,\]"}]+""").find(trimmed)?.value?.trimEnd('.', ')')
}

internal fun JsonElement?.toCompactText(column: AdminColumn): String {
    val value = toDisplayText()
    if (value == "-") return value
    return when (column.type) {
        AdminColumnType.Timestamp -> value.formatTimestamp()
        AdminColumnType.Date -> value.formatDate()
        AdminColumnType.TextArray,
        AdminColumnType.Json -> firstUrlOrNull()?.shortUrlLabel() ?: value
        else -> if (column.name.contains("url", true)) value.extractUrl()?.shortUrlLabel() ?: value else value
    }
}

internal fun String.formatTimestamp(): String {
    val datePart = substringBefore("T").takeIf { it != this } ?: substringBefore(" ")
    val timePart = substringAfter("T", substringAfter(" ", "")).take(5)
    val formattedDate = datePart.formatDate()
    return if (timePart.isNotBlank() && timePart.contains(":")) "$formattedDate, $timePart" else formattedDate
}

internal fun String.formatDate(): String {
    val pieces = substringBefore("T").substringBefore(" ").split("-")
    if (pieces.size != 3) return this
    val month = pieces[1].toIntOrNull()?.let {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").getOrNull(it - 1)
    } ?: return this
    return "${pieces[2].trimStart('0')} $month ${pieces[0]}"
}

internal fun String.shortUrlLabel(): String {
    return removePrefix("https://").removePrefix("http://").substringBefore("/")
}
