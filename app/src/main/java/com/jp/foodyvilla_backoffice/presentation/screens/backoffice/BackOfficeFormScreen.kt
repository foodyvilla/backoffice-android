package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumn
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import kotlinx.serialization.json.JsonObject

@Composable
internal fun BackOfficeFormScreen(
    session: UserSession?,
    state: AdminUiState,
    mode: FormMode,
    onBack: () -> Unit,
    onFormChange: (String, String) -> Unit,
    onUploadImage: (Uri, String) -> Unit,
    onCreateProduct: (() -> Unit)? = null,
    onSave: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val imageColumns = state.selectedTable.editableColumns.filter { it.isImageColumn() }
                if (imageColumns.isNotEmpty()) {
                    PremiumCard {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Images", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            imageColumns.forEach { column ->
                                ImageUploadField(
                                    column = column,
                                    value = state.formValues[column.name].orEmpty(),
                                    isUploading = state.uploadingColumn == column.name,
                                    onUpload = { uri -> onUploadImage(uri, column.name) },
                                    onChange = { onFormChange(column.name, it) }
                                )
                            }
                        }
                    }
                }
            }
            item {
                PremiumCard {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Fields", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        state.selectedTable.editableColumns
                            .filterNot { it.isImageColumn() }
                            .filter { column ->
                                // ID Field Protection:
                                // 1. Never show outlet_id to non-owners (it's forced in repository)
                                // 2. Never show emp_id in attendance to anyone (it's forced from session)
                                if (column.name == "outlet_id" && session?.isOwner() == false) false
                                else if (column.name == "emp_id" && state.selectedTable.name == "attendance") false
                                else true
                            }
                            .forEach { column ->
                                AdminFormField(
                                    column = column,
                                    value = state.formValues[column.name].orEmpty(),
                                    options = column.reference?.let { state.lookupRows[it.table].orEmpty() }.orEmpty(),
                                    onChange = { onFormChange(column.name, it) }
                                )
                                if (state.selectedTable.name == "outlet_menu_items" && column.name == "product_id" && onCreateProduct != null) {
                                    OutlinedButton(onClick = onCreateProduct, modifier = Modifier.fillMaxWidth()) {
                                        Text("Add new product catalog item")
                                    }
                                }
                            }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(54.dp)) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving && state.uploadingColumn == null,
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

private fun AdminColumn.isImageColumn(): Boolean {
    return name.contains("img", true) || name.contains("image", true) || name.contains("photo", true)
}
