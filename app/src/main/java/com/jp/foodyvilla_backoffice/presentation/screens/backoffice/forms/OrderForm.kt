package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
internal fun OrderForm(
    state: AdminUiState,
    onBack: () -> Unit,
    onFormChange: (String, String) -> Unit,
    onSave: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Customer Information", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = state.formValues["customer_name"] ?: "",
                            onValueChange = { onFormChange("customer_name", it) },
                            label = { Text("Customer Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.formValues["phone"] ?: "",
                            onValueChange = { onFormChange("phone", it) },
                            label = { Text("Phone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Order Details", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = state.formValues["status"] ?: "",
                            onValueChange = { onFormChange("status", it) },
                            label = { Text("Status") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.formValues["address"] ?: "",
                            onValueChange = { onFormChange("address", it) },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }
        }
        
        FormActions(
            onBack = onBack,
            onSave = onSave,
            isSaving = state.isSaving,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        )
    }
}
