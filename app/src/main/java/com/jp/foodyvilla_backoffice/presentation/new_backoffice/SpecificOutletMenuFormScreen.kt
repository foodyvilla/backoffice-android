package com.jp.foodyvilla_backoffice.presentation.new_backoffice

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.OutletManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificOutletMenuFormScreen(
    outletId: Long,
    editId: Long?, // Passed as null on Add routes, or a row ID value on Edit blocks
    viewModel: OutletManagementViewModel,
    onNavigateBackAction: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var catalogDropdownOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Initialize form states matching the current entry
    LaunchedEffect(editId, outletId) {
        viewModel.setupMenuFormWorkspace(editId, outletId)
    }

    // MULTIPLE IMAGES BATCH SELECTION LAUNCHER CONTRACT
    val multiImagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                viewModel.uploadMenuImagesList(context, uris)
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editId == null) "Map Catalog Item to Menu" else "Configure Pricing Threshold Rules",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackAction) {
                        Icon(
                            Icons.Default.ArrowBack,
                            null
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // HORIZONTAL MULTI-IMAGE PREVIEW GRID ROW
                Text(
                    "Product Media Images Gallery Array",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(state.mMenuImagesList) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    item {
                        Card(
                            onClick = { multiImagePicker.launch("image/*") },
                            modifier = Modifier.size(70.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                if (state.mMenuImagesList.isNotEmpty()) {
                    TextButton(onClick = viewModel::clearDishImagesArray) {
                        Text(
                            "Clear Images Selection Array",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Master Product Link Dropdown Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.mSelectedProduct?.name
                            ?: "Link central static product definition",
                        onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Core Catalog Product Template Blueprint") },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (editId == null) catalogDropdownOpen = true
                            }) { Icon(Icons.Default.Add, null) }
                        }
                    )
                    DropdownMenu(
                        expanded = catalogDropdownOpen,
                        onDismissRequest = { catalogDropdownOpen = false }) {
                        state.globalProductCatalogOptions.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text(prod.name) },
                                onClick = {
                                    viewModel.onMProductSelected(prod); catalogDropdownOpen = false
                                })
                        }
                    }
                }

                OutlinedTextField(
                    value = state.mPrice,
                    onValueChange = viewModel::onMPriceChanged,
                    label = { Text("Outlet Target Price Point Value (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.mDiscount,
                    onValueChange = viewModel::onMDiscountChanged,
                    label = { Text("Franchise Flat Item Campaign Discount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.mHandlingCharges,
                    onValueChange = viewModel::onMHandlingChargesChanged,
                    label = { Text("Restaurant Kitchen Packaging Handling Fee (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.mDeliveryCharges,
                    onValueChange = viewModel::onMDeliveryChargesChanged,
                    label = { Text("Rider Logistics Delivery Fulfillment Fee (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Apply Free Delivery to this dish template mapping"); Switch(
                    checked = state.mIsFreeDelivery,
                    onCheckedChange = viewModel::onMFreeDeliveryToggled
                )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Expose visibility in mobile client applications"); Switch(
                    checked = state.mIsAvailable,
                    onCheckedChange = viewModel::onMAvailableToggled
                )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mark item Out of Stock / Kitchen shortages"); Switch(
                    checked = state.mIsOutOfStock,
                    onCheckedChange = viewModel::onMStockToggled
                )
                }

                if (state.errorText != null) {
                    Text(
                        state.errorText.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        viewModel.commitOutletMenuAction(
                            outletId,
                            onSuccess = onNavigateBackAction
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Save Menu Configuration Metrics", fontWeight = FontWeight.Bold) }
            }
        }
    }
}