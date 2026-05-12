







package com.jp.foodyvilla_backoffice.presentation.screens.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.data.model.Review
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    viewModel: ReviewsViewModel = koinViewModel(),
    onAddReviewClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadReviews()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Customer Reviews",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddReviewClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Write Review") }
            )
        }
    ) { padding ->

        if (state.reviews.isEmpty() && !state.isLoading) {
            // Empty State UI
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No reviews yet. Be the first to rate!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ⭐ Overall Summary Card
                if (state.reviews.isNotEmpty()) {
                    val avg = state.reviews.map { it.rating }.average()
                    item {
                        SummaryCard(avg = avg, totalReviews = state.reviews.size)
                    }
                }

                items(state.reviews, key = { it.id }) { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(avg: Double, totalReviews: Int) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFF5722), Color(0xFFFF9800))
                    )
                )
                .padding(24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Overall Rating",
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White.copy(alpha = 0.9f))
                )
                Text(
                    "%.1f".format(avg),
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                )
                StarRow(rating = avg, size = 24)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Based on $totalReviews reviews",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                )
            }
        }
    }
}
@Composable
private fun ReviewCard(review: Review) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fixed Avatar Logic
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // Check if review has a user image property (adjust 'userImageUrl' to your model's field name)


                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.userName.takeIf { !it.isNullOrBlank() } ?: "Anonymous User",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (!review.foodItem.isNullOrBlank()) {
                        Text(
                            text = review.foodItem,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (!review.date.isNullOrBlank()) {
                    Text(
                        review.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ⭐ Rating and Title
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StarRow(rating = review.rating, size = 14)
                if (!review.title.isNullOrBlank()) {
                    Text(
                        text = review.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // 💬 Comment Body
            if (!review.desc.isNullOrBlank()) {
                Text(
                    text = review.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            // 🖼️ Images (Horizontal scrollable if many, or grid-like)
            if (!review.img_url.isNullOrEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    review.img_url.filter { it.isNotBlank() }.take(3).forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Review Image",
                            contentScale = ContentScale.Crop,
                                   modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun StarRow(rating: Double, size: Int = 20) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        val filled = rating.coerceIn(0.0, 5.0).roundToInt()
        repeat(5) { i ->
            Icon(
                imageVector = if (i < filled) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (i < filled) Color(0xFFFFA000) else Color.LightGray,
                modifier = Modifier.size(size.dp)
            )
        }
    }
}




//package com.jp.foodyvilla_backoffice.presentation.screens.reviews
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material.icons.filled.StarBorder
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import coil.compose.AsyncImage
//import com.jp.foodyvilla_backoffice.data.model.Review
//import org.koin.androidx.compose.koinViewModel
//import kotlin.math.roundToInt
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ReviewsScreen(
//    viewModel: ReviewsViewModel = koinViewModel(),
//    onAddReviewClick: () -> Unit // ✅ NEW
//) {
//    val state by viewModel.uiState.collectAsStateWithLifecycle()
//
//
//    LaunchedEffect(Unit) {
//        viewModel.loadReviews()
//    }
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = {
//                    Text(
//                        "Reviews ⭐",
//                        style = MaterialTheme.typography.headlineLarge
//                    )
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = onAddReviewClick) {
//                Text("+") // or Icon(Icons.Default.Add, null)
//            }
//        }
//    ) { padding ->
//
//        LazyColumn(
//            contentPadding = PaddingValues(
//                top = padding.calculateTopPadding() + 8.dp,
//                bottom = 100.dp,
//                start = 20.dp,
//                end = 20.dp
//            ),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//
//            // ⭐ Average rating banner (unchanged)
//            if (state.reviews.isNotEmpty()) {
//                val avg = state.reviews.map { it.rating }.average()
//
//                item {
//                    Card(
//                        shape = RoundedCornerShape(24.dp),
//                        colors = CardDefaults.cardColors(
//                            containerColor = MaterialTheme.colorScheme.primary
//                        ),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .padding(24.dp)
//                                .fillMaxWidth(),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(
//                                "Overall Rating",
//                                style = MaterialTheme.typography.bodyMedium.copy(
//                                    color = Color.White.copy(alpha = 0.8f)
//                                )
//                            )
//
//                            Text(
//                                "${"%.1f".format(avg)}",
//                                style = MaterialTheme.typography.displayLarge.copy(
//                                    color = Color.White
//                                )
//                            )
//
//                            StarRow(rating = avg)
//
//                            Text(
//                                "${state.reviews.size} reviews",
//                                style = MaterialTheme.typography.bodySmall.copy(
//                                    color = Color.White.copy(alpha = 0.7f)
//                                )
//                            )
//                        }
//                    }
//                }
//            }
//
//            items(state.reviews, key = { it.id }) { review ->
//                ReviewCard(review)
//            }
//        }
//    }
//}
//@Composable
//private fun ReviewCard(review: Review) {
//    Card(
//        shape = RoundedCornerShape(20.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//
//            // 👤 Header
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                AsyncImage(
//                    model = Icons.Default.Person,
//                    contentDescription = review.userName,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier
//                        .size(44.dp)
//                        .clip(CircleShape)
//                )
//
//
//
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(review.title, style = MaterialTheme.typography.titleMedium)
//                    Text(
//                        review.foodItem,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//
//                Text(
//                    review.date,
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            // ⭐ Rating
//            StarRow(rating = review.rating, size = 16)
//
//            // 💬 Comment
//            Text(review.desc, style = MaterialTheme.typography.bodyMedium)
//
//            // 🖼️ Images (NEW)
//            if (review.img_url.isNotEmpty()) {
//                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    review.img_url.take(2).forEach { url ->
//                        AsyncImage(
//                            model = url,
//                            contentDescription = null,
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier
//                                .size(90.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//@Composable
//private fun StarRow(rating: Double, size: Int = 20) {
//    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
//        val filled = rating.roundToInt()
//        repeat(5) { i ->
//            Icon(
//                if (i < filled) Icons.Default.Star else Icons.Default.StarBorder,
//                contentDescription = null,
//                tint = Color(0xFFFFA000),
//                modifier = Modifier.size(size.dp)
//            )
//        }
//    }
//}
