package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OrderDto
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.TableManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OrderHistoryViewModel(
    private val repository: TableManagementRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OrderHistoryUiState())
    val state = _state.asStateFlow()

    fun loadOrders(outletId: Long, date: LocalDate) {
        _state.update { it.copy(isLoading = true, selectedDate = date) }
        viewModelScope.launch {
            runCatching {
                repository.getOrdersByDate(outletId, date)
            }.onSuccess { orders ->
                _state.update { it.copy(orders = orders, isLoading = false) }
            }.onFailure { err ->
                _state.update { it.copy(error = err.localizedMessage, isLoading = false) }
            }
        }
    }
}

data class OrderHistoryUiState(
    val orders: List<OrderDto> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    outletId: Long,
    viewModel: OrderHistoryViewModel = koinViewModel(),
    onMenuClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(value = false) }

    LaunchedEffect(outletId) {
        viewModel.loadOrders(outletId, state.selectedDate)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                            viewModel.loadOrders(outletId, newDate)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order History") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "Orders for ${state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (state.orders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders found for this date")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.orders) { order ->
                        OrderHistoryCard(order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: OrderDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order #${order.id.takeLast(6)}", fontWeight = FontWeight.Bold)
                StatusChip(order.status ?: "pending")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Table: ${order.table_id ?: "N/A"}")
            Text("Customer: ${order.customer_name ?: "Walk-in"}")
            Text("Type: ${order.order_type}")
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> Color(0xFFFFA000)
        "accepted" -> Color(0xFF1976D2)
        "preparing" -> Color(0xFF1976D2)
        "completed" -> Color(0xFF43A047)
        "cancelled" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
