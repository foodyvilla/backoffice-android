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
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Employee Photo", style = MaterialTheme.typography.titleMedium)
                        val imgColumn = state.selectedTable.columns.firstOrNull { it.name == "profile_img" }
                        if (imgColumn != null) {
                            ImageUploadField(
                                column = imgColumn,
                                value = state.formValues["profile_img"].orEmpty(),
                                isUploading = state.uploadingColumn == "profile_img",
                                onUpload = { onUploadImage(it, "profile_img") },
                                onChange = { onFormChange("profile_img", it) }
                            )
                        }
                    }
                }
            }

            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Personal Info", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = state.formValues["name"] ?: "",
                            onValueChange = { onFormChange("name", it) },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                        )
                        OutlinedTextField(
                            value = state.formValues["contact"] ?: "",
                            onValueChange = { onFormChange("contact", it) },
                            label = { Text("Contact") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                        )
                        OutlinedTextField(
                            value = state.formValues["role"] ?: "",
                            onValueChange = { onFormChange("role", it) },
                            label = { Text("Role") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                        )
                    }
                }
            }
            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Employment Info", style = MaterialTheme.typography.titleMedium)

                        val outletColumn = state.selectedTable.columns.firstOrNull { it.name == "outlet_id" }
                        if (outletColumn != null) {
                            AdminFormField(
                                column = outletColumn,
                                value = state.formValues["outlet_id"].orEmpty(),
                                options = state.lookupRows["outlets"].orEmpty(),
                                onChange = { onFormChange("outlet_id", it) }
                            )
                        }

                        OutlinedTextField(
                            value = state.formValues["salary"] ?: "",
                            onValueChange = { onFormChange("salary", it) },
                            label = { Text("Salary") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                        )
                        OutlinedTextField(
                            value = state.formValues["joining_date"] ?: "",
                            onValueChange = { onFormChange("joining_date", it) },
                            label = { Text("Joining Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
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
