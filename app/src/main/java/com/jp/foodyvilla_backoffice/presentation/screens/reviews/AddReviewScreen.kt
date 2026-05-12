package com.jp.foodyvilla_backoffice.presentation.screens.reviews

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    viewModel: ReviewsViewModel,
    onBack: () -> Unit
) {

    val state by viewModel.addState.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.addImage(it) }
    }

    // ✅ Auto back on success
    LaunchedEffect(state.success) {
        if (state.success) onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Review ✍️") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🧾 FORM CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // 👤 Name
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Customer Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ⭐ Rating
                    Column {
                        Text(
                            "Rating",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { viewModel.onRatingChange(star) }
                                ) {
                                    Icon(
                                        imageVector = if (star <= state.rating)
                                            Icons.Default.Star
                                        else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA000)
                                    )
                                }
                            }
                        }
                    }

                    // 💬 Description
                    OutlinedTextField(
                        value = state.desc,
                        onValueChange = viewModel::onDescChange,
                        label = { Text("Write your review...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            // 🖼️ IMAGE SECTION
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "Add Images (max 2)",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        // Existing images
                        state.images.forEach { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                IconButton(
                                    onClick = { viewModel.removeImage(uri) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(22.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.6f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Add image button
                        if (state.images.size < 2) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { launcher.launch("image/*") }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 🚀 SUBMIT BUTTON
            Button(
                onClick = { viewModel.submit(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit Review")
                }
            }

            // ❌ Error
            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}