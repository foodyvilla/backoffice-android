package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.AnalyticsSummary
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.AnalyticsViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    viewModel: AnalyticsViewModel = koinViewModel(),
    onMenuClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePickerRange by remember { mutableStateOf(value = false) }

    if (showDatePickerRange) {
        // For simplicity in this backoffice, using two separate date pickers or a range picker if available
        // Here we'll just implement a simple DateRange Selection logic
        DateRangePickerDialog(
            initialStart = state.startDate,
            initialEnd = state.endDate,
            onDismiss = { showDatePickerRange = false }
        ) { start, end ->
            viewModel.loadAnalytics(start, end)
            showDatePickerRange = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePickerRange = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Filter Date")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (state.summary != null) {
                AnalyticsContent(state.summary!!, state.startDate, state.endDate)
            }
        }
    }
}

@Composable
private fun AnalyticsContent(summary: AnalyticsSummary, start: LocalDate, end: LocalDate) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "${start.format(DateTimeFormatter.ISO_DATE)} to ${end.format(DateTimeFormatter.ISO_DATE)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Text("Order Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Total Orders",
                    value = summary.orders.total.toString(),
                    icon = Icons.Default.ShoppingCart,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Delivered",
                    value = summary.orders.delivered.toString(),
                    icon = Icons.Default.RestaurantMenu,
                    containerColor = Color(0xFFC8E6C9), // Light Green
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Pending",
                    value = summary.orders.pending.toString(),
                    icon = Icons.Default.ShoppingCart,
                    containerColor = Color(0xFFFFF9C4), // Light Yellow
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Others",
                    value = summary.orders.other_statuses.toString(),
                    icon = Icons.Default.ShoppingCart,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Customer Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Active Buyers",
                    value = summary.customers.total_active_buyers.toString(),
                    icon = Icons.Default.People,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "New Users",
                    value = summary.customers.new_registrations.toString(),
                    icon = Icons.Default.People,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    initialStart: LocalDate,
    initialEnd: LocalDate,
    onDismiss: () -> Unit,
    onConfirmed: (LocalDate, LocalDate) -> Unit,
) {
    // Standard Material 3 DateRangePicker is available in newer versions.
    // For consistency with existing code, using a custom simple dialog or 2 steps.
    // Let's use two DatePicker dialogs in sequence for precision.

    var showEndPicker by remember { mutableStateOf(false) }
    var selectedStart by remember { mutableStateOf(initialStart) }

    if (!showEndPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedStart.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedStart = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                            showEndPicker = true
                        }
                    }
                ) { Text("Next: End Date") }
            }
        ) {
            DatePicker(state = datePickerState, title = { Text("Select Start Date", modifier = Modifier.padding(16.dp)) })
        }
    } else {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialEnd.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val end = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                            onConfirmed(selectedStart, end)
                        }
                    }
                ) { Text("Confirm Range") }
            }
        ) {
            DatePicker(state = datePickerState, title = { Text("Select End Date", modifier = Modifier.padding(16.dp)) })
        }
    }
}
