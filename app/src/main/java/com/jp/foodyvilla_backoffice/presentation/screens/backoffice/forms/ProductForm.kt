package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
internal fun ProductForm(
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
                        Text("Basic Info", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = state.formValues["name"] ?: "",
                            onValueChange = { onFormChange("name", it) },
                            label = { Text("Product Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.formValues["category"] ?: "",
                            onValueChange = { onFormChange("category", it) },
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.formValues["description"] ?: "",
                            onValueChange = { onFormChange("description", it) },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }
            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Flags", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            LabelledCheckbox(
                                label = "Is Veg",
                                checked = state.formValues["is_veg"] == "true",
                                onCheckedChange = { onFormChange("is_veg", it.toString()) }
                            )
                            LabelledCheckbox(
                                label = "Is Bestseller",
                                checked = state.formValues["is_bestseller"] == "true",
                                onCheckedChange = { onFormChange("is_bestseller", it.toString()) }
                            )
                        }
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

@Composable
private fun LabelledCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
