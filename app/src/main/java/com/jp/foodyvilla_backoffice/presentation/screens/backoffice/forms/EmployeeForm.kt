package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.forms

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
internal fun EmployeeForm(
    state: AdminUiState,
    onBack: () -> Unit,
    onFormChange: (String, String) -> Unit,
    onUploadImage: (Uri, String) -> Unit,
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
                        Text("Personal Info", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = state.formValues["name"] ?: "",
                            onValueChange = { onFormChange("name", it) },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.formValues["contact"] ?: "",
                            onValueChange = { onFormChange("contact", it) },
                            label = { Text("Contact") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.formValues["role"] ?: "",
                            onValueChange = { onFormChange("role", it) },
                            label = { Text("Role") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Employment Info", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = state.formValues["salary"] ?: "",
                            onValueChange = { onFormChange("salary", it) },
                            label = { Text("Salary") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.formValues["joining_date"] ?: "",
                            onValueChange = { onFormChange("joining_date", it) },
                            label = { Text("Joining Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
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
