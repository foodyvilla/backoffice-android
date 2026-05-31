package com.jp.foodyvilla_backoffice.presentation.new_backoffice

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.CustomerManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    customerId: Long,
    viewModel: CustomerManagementViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var fcmDialogOpen by remember { mutableStateOf(false) }
    var waDialogOpen by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<com.jp.foodyvilla_backoffice.data.new_backoffice.models.CustomerUiModel?>(null) }

    // Sync analytics payload indices
    LaunchedEffect(customerId) {
        viewModel.loadCustomerCompleteProfileDetails(customerId)
        selectedCustomer = state.customersList.find { it.id == customerId }
    }

    // Dynamic campaign fields bindings
    var fcmTitle by remember { mutableStateOf("") }
    var fcmDesc by remember { mutableStateOf("") }
    var fcmUrl by remember { mutableStateOf("") }
    
    var waMessage by remember { mutableStateOf("") }
    var waSelectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        waSelectedImageUri = uri
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(selectedCustomer?.name ?: "Customer Insights Summary", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Analytics Summary Metrics Section Grid Layer Row Card
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Financial Transaction Summaries", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("Total Spend Vector: ₹${state.activeDetails.totalSpend}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text("Total Successful Orders Dispatched: ${state.activeDetails.totalOrders}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Customer Preference Affinities", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                        Text("Favorite Mapping Menu Item: ${state.activeDetails.favoriteItemName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Preferred Base Store Unit: ${state.activeDetails.favoriteOutletName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Action Operations Row For Targeting Edge Broadcasters
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { fcmDialogOpen = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.NotificationsActive, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Push Broadcast")
                    }
                    Button(onClick = { waDialogOpen = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                        Icon(Icons.AutoMirrored.Filled.Message, null)
                        Spacer(Modifier.width(4.dp))
                        Text("WhatsApp Template")
                    }
                }

                Text("Historical System App Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(state.activeDetails.reviewsList) { rev ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(rev.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("⭐ ${rev.rating}/5", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                                }
                                Text(rev.description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- FCM EDGE NOTIFICATION EMISSION MODAL ---
        if (fcmDialogOpen) {
            AlertDialog(
                onDismissRequest = { fcmDialogOpen = false },
                title = { Text("Deploy Push Notification Cloud Token Payload") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = fcmTitle, onValueChange = { fcmTitle = it }, label = { Text("Notification Header Title") })
                        OutlinedTextField(value = fcmDesc, onValueChange = { fcmDesc = it }, label = { Text("Body Text Content Description") })
                        OutlinedTextField(value = fcmUrl, onValueChange = { fcmUrl = it }, label = { Text("DeepLink Redirection Intent Web URL String") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.sendFcmPushNotification(selectedCustomer?.fcmToken.orEmpty(), fcmTitle, fcmDesc, fcmUrl) {
                            fcmDialogOpen = false
                        }
                    }) { Text("Send Event") }
                },
                dismissButton = { TextButton(onClick = { fcmDialogOpen = false }) { Text("Cancel") } }
            )
        }

        // --- WHATSAPP TEMPLATE ENGINE EMISSION MODAL ---
        if (waDialogOpen) {
            AlertDialog(
                onDismissRequest = { waDialogOpen = false },
                title = { Text("Deploy WhatsApp Media Channel Broadcast") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = waMessage, onValueChange = { waMessage = it }, label = { Text("Template Interceptor Message Body CopyText") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Button(onClick = { photoLauncher.launch("image/*") }) {
                            Text(if (waSelectedImageUri == null) "Attach Campaign Rich Media Image File" else "Image Attached Successfully ✅")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.sendWhatsAppTemplateBroadcast(context, selectedCustomer?.phone.orEmpty(), waMessage, waSelectedImageUri) {
                            waDialogOpen = false
                        }
                    }) { Text("Send Messaging Payload") }
                },
                dismissButton = { TextButton(onClick = { waDialogOpen = false }) { Text("Cancel") } }
            )
        }
    }
}