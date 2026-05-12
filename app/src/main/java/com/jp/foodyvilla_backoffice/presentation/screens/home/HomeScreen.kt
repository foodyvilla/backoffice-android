package com.jp.foodyvilla_backoffice.presentation.screens.home

import Banner
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jp.foodyvilla_backoffice.data.model.FoodItem
import com.jp.foodyvilla_backoffice.presentation.utils.RequestNotificationPermission
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onItemClick: (Int) -> Unit,
    onMenuClick : ()->Unit = {},
    viewModel: HomeViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    var isNotificationPermissionEnabled by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val activity = context as Activity
    val permission = Manifest.permission.POST_NOTIFICATIONS

    var hasRequestedBefore by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isNotificationPermissionEnabled = isGranted
    }

    RequestNotificationPermission {
        isNotificationPermissionEnabled = it
    }




    Scaffold(

        containerColor = colors.background
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = 100.dp
            )
        ) {



            // ───────── HERO (Material Only)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colors.primaryContainer,
                            RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {

                        Text(
                            "Order Delicious\nFood Today",
                            style = MaterialTheme.typography.displayMedium,
                            color = colors.onPrimaryContainer
                        )

                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = {
                                viewModel.onSearchQueryChange(it)
                                onMenuClick()
                            },
                            placeholder = {
                                Text(
                                    "Search dishes, cuisines...",
                                    color = colors.onSurfaceVariant
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant
                                )
                            },
//                            enabled = false,
                            modifier = Modifier.fillMaxWidth().clickable{
                                onMenuClick()
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface,
                                focusedBorderColor = colors.outline,
                                unfocusedBorderColor = colors.outlineVariant
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions {
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }

            item {
                BannerSlider(banners = state.banners)
            }

            if(!isNotificationPermissionEnabled){
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Enable notifications to stay updated",
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@Button

                                when {
                                    // ✅ Already granted
                                    isNotificationPermissionEnabled -> Unit

                                    // ✅ Show rationale (denied once)
                                    ActivityCompat.shouldShowRequestPermissionRationale(
                                        activity,
                                        permission
                                    ) -> {
                                        Toast.makeText(
                                            context,
                                            "Notifications help you stay updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        permissionLauncher.launch(permission)
                                    }

                                    // ✅ First time request
                                    !hasRequestedBefore -> {
                                        hasRequestedBefore = true
                                        permissionLauncher.launch(permission)
                                    }

                                    // ❌ Permanently denied → go to settings
                                    else -> {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }
            // ───────── Categories
            item {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Text(
                        "Categories",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.categories) { cat ->
                            CategoryChip(
                                label = cat.name,
                                emoji = cat.emoji,
                                selected = state.selectedCategory == cat.id,
                                onClick = { viewModel.selectCategory(cat.id) }
                            )
                        }
                    }
                }
            }

            // ───────── Top Selling
            if (state.filteredBestSellersItems.isNotEmpty() && state.searchQuery.isBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Top Selling 🔥", style = MaterialTheme.typography.headlineLarge)


                    }
                }


                val chunkedList = state.filteredBestSellersItems.chunked(2)

                items(chunkedList) { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    ) {
                        rowItems.forEach { item ->
                            FoodCard(
                                item = item,
                                onAddToCart = {  viewModel.updateCartItemQuantity(item) },
                                onClick = { onItemClick(item.id)},
                                homeViewModel = viewModel,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // If odd number of items → fill empty space
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

//                items(state.bestSellers) { item ->
//                    FoodCard(
//                        item = item,
//                        onAddToCart = {
//                            viewModel.updateCartItemQuantity(item)
//                        },
//                        homeViewModel = viewModel,
//                        onClick = { onItemClick(item.id) },
//                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
//                    )
//                }
            }


            // ───────── Popular
            item {
                Text(
                    if (state.searchQuery.isNotBlank()) "Results" else "Popular Dishes",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(20.dp)
                )
            }

            val displayItems =
                if (state.searchQuery.isNotBlank() || state.selectedCategory != "all")
                    state.filteredItems else state.popularItems



            if (displayItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No dishes found 😔",
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(displayItems) { item ->
                    HorizontalFoodCard(
                        item = item,
                        onAddToCart = { /* TODO */ },
                        onClick = { onItemClick(item.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
                    )
                }
            }
        }
    }
}


@Composable
fun CategoryChip(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor =
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        modifier = Modifier
            .width(72.dp)
            .height(88.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(color = textColor),
                maxLines = 1
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerSlider(
    banners: List<Banner>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            if (banners.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(modifier = modifier) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->

            val banner = banners[page]
            Column() {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium
            ) {

                    AsyncImage(
                        model = banner.img_url,
                        contentDescription = banner.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Optional title overlay

                }
                Text(
                    text = banner.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
//                            .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
        }

        // Dots Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}


@Composable
fun FoodCard(
    item: FoodItem,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val homeState = homeViewModel.uiState.collectAsStateWithLifecycle().value

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.image.firstOrNull())
                        .crossfade(true)
                        .listener(
                            onError = { _, result ->
                                Log.e("CoilError", result.throwable.toString())
                            }
                        )
                        .build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                RatingChip(
                    rating = item.rating,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
                if (item.isBestSeller) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            "Best Seller",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VegDot(isVeg = item.isVeg)
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "₹${item.price}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                )


                if(!homeState.cartItems.any { it.products?.id == item.id }){
                    FilledIconButton(
                        onClick = onAddToCart,
//                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add to cart",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

            }
        }
    }
}

val SecondaryGreen = Color(0xFF43A047)

@Composable
fun HorizontalFoodCard(
    item: FoodItem,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isItemInCart: Boolean = false
) {
    val imageUrl = if (item.image.isNotEmpty()) {
        item.image[0]
    } else {
        "https://cdn.tmbill.com/tmpos/backoffice/49166729/products/1856724d-61f1-47b5-97a5-d94efe64554b.webp"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VegDot(isVeg = item.isVeg)
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    "${item.nutritionalInfo.protein} Protein • ${item.nutritionalInfo.energy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (item.isVegan) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SecondaryGreen.copy(alpha = 0.1f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            "VEGAN",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryGreen
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "₹${item.price}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                AnimatedVisibility(!isItemInCart) {
                    FilledIconButton(
                        onClick = onAddToCart,
//                    containerColor = PrimaryRed,
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun RatingChip(rating: Double, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(12.dp)
            )
            Text(
                rating.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            )
        }
    }
}

@Composable
fun VegDot(isVeg: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(14.dp)
            .border(
                1.5.dp,
                if (isVeg) SecondaryGreen else MaterialTheme.colorScheme.primary,
                RoundedCornerShape(2.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(
                    if (isVeg) SecondaryGreen else MaterialTheme.colorScheme.primary,
                    CircleShape
                )
        )
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(quantity.toString(), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

