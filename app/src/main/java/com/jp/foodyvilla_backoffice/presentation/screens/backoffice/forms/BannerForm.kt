package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.forms

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp.foodyvilla_backoffice.data.model.backoffice.adminTables
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
internal fun BannerForm(
    session: UserSession?,
    state: AdminUiState,
    onBack: () -> Unit,
    onFormChange: (String, String) -> Unit,
    onUploadImage: (Uri, String) -> Unit,
    onSave: () -> Unit
) {
    val table = remember { adminTables.first { it.name == "banners" } }
    
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Banner Info", style = MaterialTheme.typography.titleMedium)
                        
                        AdminFormField(
                            column = table.columns.first { it.name == "title" },
                            value = state.formValues["title"] ?: "",
                            options = emptyList(),
                            onChange = { onFormChange("title", it) }
                        )

                        // If owner, show outlet dropdown
                        if (session?.isOwner() == true) {
                            val outletColumn = table.columns.first { it.name == "outlet_id" }
                            AdminFormField(
                                column = outletColumn,
                                value = state.formValues["outlet_id"] ?: "",
                                options = state.lookupRows["outlets"].orEmpty(),
                                onChange = { onFormChange("outlet_id", it) }
                            )
                        }
                    }
                }
            }

            item {
                PremiumCard {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Banner Image", style = MaterialTheme.typography.titleMedium)
                        
                        val imgColumn = table.columns.first { it.name == "img_url" }
                        ImageUploadField(
                            column = imgColumn,
                            value = state.formValues["img_url"].orEmpty(),
                            isUploading = state.uploadingColumn == "img_url",
                            onUpload = { onUploadImage(it, "img_url") },
                            onChange = { onFormChange("img_url", it) }
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
