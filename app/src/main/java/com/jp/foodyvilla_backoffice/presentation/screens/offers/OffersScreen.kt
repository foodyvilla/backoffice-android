package com.jp.foodyvilla_backoffice.presentation.screens.offers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.data.model.OfferFood
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(viewModel: OffersViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Offers & Deals 🎉", style = MaterialTheme.typography.headlineLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 100.dp,
                start = 20.dp,
                end = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.offers) { offer ->
                OfferCard(offer)
            }
        }
    }
}

@Composable
private fun OfferCard(offer: OfferFood) {
    val color = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(color.primaryContainer.copy(alpha = 0.1f))
            ) {
                AsyncImage(
                    model = offer.img_url,
                    contentDescription = offer.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
//                Surface(
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(12.dp),
//                    shape = RoundedCornerShape(12.dp),
//                    color = color
//                ) {
//                    Text(
//                        offer.discount,
//                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                        style = MaterialTheme.typography.labelLarge,
//                        color = Color.White
//                    )
//                }
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(offer.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    offer.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    Surface(
//                        shape = RoundedCornerShape(8.dp),
//                        color = color.copy(alpha = 0.1f)
//                    ) {
//                        Row(
//                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
//                            horizontalArrangement = Arrangement.spacedBy(6.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(offer.code, style = MaterialTheme.typography.labelLarge.copy(color = color, fontWeight = FontWeight.Black))
//                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = color, modifier = Modifier.size(16.dp))
//                        }
//                    }
//                    Text(
//                        "Valid: ${offer.validUntil}",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
                }
            }
        }
    }
}
