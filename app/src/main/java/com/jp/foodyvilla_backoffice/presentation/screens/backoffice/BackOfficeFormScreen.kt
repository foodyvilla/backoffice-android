package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumn
import com.jp.foodyvilla_backoffice.data.model.backoffice.AdminColumnType
import kotlinx.serialization.json.JsonObject

@Composable
internal fun BackOfficeFormScreen(
    state: AdminUiState,
    mode: FormMode,
    onBack: () -> Unit,
    onFormChange: (String, String) -> Unit,
    onUploadImage: (Uri, String) -> Unit,
    onCreateProduct: (() -> Unit)? = null,
    onSave: () -> Unit
) {
    val title = if (mode == FormMode.Create) state.selectedTable.createLabel else "Edit ${state.selectedTable.title.removeSuffix("s")}"

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PremiumTopBar(
                    title = title,
                    subtitle = "Full screen editor",
                    icon = AdminRoute.Form.icon,
                    onBack = onBack
                )
            }
            item {
                val imageColumns = state.selectedTable.editableColumns.filter { it.isImageColumn() }
                if (imageColumns.isNotEmpty()) {
                    PremiumCard {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Images", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        Text("Fields", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        state.selectedTable.editableColumns
                            .filterNot { it.isImageColumn() }
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
            color = androidx.compose.ui.graphics.Color.White,
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
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), color = androidx.compose.ui.graphics.Color.White)
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

@Composable
private fun ImageUploadField(
    column: AdminColumn,
    value: String,
    isUploading: Boolean,
    onUpload: (Uri) -> Unit,
    onChange: (String) -> Unit
) {
    val context = LocalContext.current
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            previewUri = uri
            onUpload(uri)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(column.label, color = Muted, fontSize = 13.sp)
        LargeRecordImage(previewUri?.toString() ?: value.extractUrl(), column.label)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { picker.launch("image/*") },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isUploading) "Uploading" else "Upload")
            }
            OutlinedButton(onClick = { onChange("") }, enabled = !isUploading && value.isNotBlank()) {
                Text("Clear")
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Stored URL") },
            minLines = 2,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue, unfocusedBorderColor = SoftLine)
        )
    }
}

@Composable
private fun AdminFormField(
    column: AdminColumn,
    value: String,
    options: List<JsonObject>,
    onChange: (String) -> Unit
) {
    if (column.reference != null && options.isNotEmpty()) {
        ReferenceDropdownField(
            column = column,
            value = value,
            options = options,
            onChange = onChange
        )
    } else if (column.type == AdminColumnType.Boolean) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(column.label, color = Muted, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = value.equals("true", true), onClick = { onChange("true") }, label = { Text("True") })
                FilterChip(selected = value.equals("false", true) || value.isBlank(), onClick = { onChange("false") }, label = { Text("False") })
            }
        }
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (column.required) "${column.label} *" else column.label) },
            supportingText = column.helper?.let { helper -> { Text(helper) } },
            minLines = if (column.multiline) 4 else 1,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalBlue,
                unfocusedBorderColor = SoftLine,
                focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.White
            )
        )
    }
}

@Composable
private fun ReferenceDropdownField(
    column: AdminColumn,
    value: String,
    options: List<JsonObject>,
    onChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val reference = column.reference ?: return
    val selected = options.firstOrNull { it[reference.valueColumn].toDisplayText() == value }
    val selectedLabel = selected?.referenceLabel(reference.labelColumns)
        ?: value.takeIf { it.isNotBlank() }
        ?: "Select ${column.label}"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
        ) {
            Text(
                text = selectedLabel,
                modifier = Modifier.weight(1f),
                color = Ink
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { row ->
                val id = row[reference.valueColumn].toDisplayText()
                DropdownMenuItem(
                    text = { Text(row.referenceLabel(reference.labelColumns)) },
                    onClick = {
                        expanded = false
                        onChange(id)
                    }
                )
            }
        }
    }
}

private fun JsonObject.referenceLabel(columns: List<String>): String {
    val pieces = columns.mapNotNull { column ->
        this[column].toDisplayText().takeIf { it != "-" }
    }
    val fallback = this["id"].toDisplayText()
    return pieces.joinToString(" - ").ifBlank { fallback }
}

private fun AdminColumn.isImageColumn(): Boolean {
    return name.contains("img", true) || name.contains("image", true) || name.contains("photo", true)
}
