package com.jp.foodyvilla_backoffice.presentation.screens.detail


// ... existing imports


import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.data.model.FoodItem
import com.jp.foodyvilla_backoffice.presentation.screens.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: Int,
    onBack: () -> Unit,
    onCartClick: () -> Unit,

    onItemClick: (Int) -> Unit,
    viewModel: DetailViewModel = koinViewModel(parameters = { parametersOf(itemId) }),
    homeViewModel: HomeViewModel,
) {

    val primaryRed = MaterialTheme.colorScheme.primary
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val item = state.item


    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    LaunchedEffect(homeState.cartItems) {

        val cartItem = homeState.cartItems
            .firstOrNull { it.product_id == itemId }

        viewModel.updateQuantity(cartItem?.qty ?: 1)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FoodyVilla",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = primaryRed,
                            fontWeight = FontWeight.Black
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (item != null) {
                        IconButton(onClick = viewModel::toggleWishlist) {
                            Icon(
                                if (state.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                null,
                                tint = primaryRed
                            )
                        }
                    }
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, null, tint = primaryRed)
                    }
                }
            )
        },

        bottomBar = {
            if (item != null) {
                Surface(shadowElevation = 12.dp) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuantitySelector(
                            quantity = state.quantity,
                            onDecrement = { viewModel.updateQuantity(state.quantity - 1) },
                            onIncrement = { viewModel.updateQuantity(state.quantity + 1) }
                        )

                        if (homeState.cartItems.any { it.products?.id == item.id }) {
                            Button(
                                onClick = { homeViewModel.updateCartItemQuantity(item, 0) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryRed)
                            ) {
                                Text(
                                    "Remove From Cart",
                                    color = Color.White
                                )
                            }
                        } else {
                            Button(
                                onClick = { homeViewModel.updateCartItemQuantity(item, state.quantity) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryRed)
                            ) {
                                Text(
                                    "Add to Cart • ₹${"%.2f".format(item.price * state.quantity)}",
                                    color = Color.White
                                )
                            }
                        }

                    }
                }
            }
        }
    ) { padding ->

        if (state.isLoading || item == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryRed)
            }
            return@Scaffold
        }

        // ✅ FIX: Use LazyColumn instead of Column + scroll
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // 🔥 HERO IMAGE
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    FoodImageSlider(images = item.image)

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                                    startY = 500f
                                )
                            )
                    )
                }
            }

            // 🔥 MAIN CONTENT
            item {
                Column(
                    Modifier
                        .offset(y = (-24).dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                ) {

                    Text(item.name, style = MaterialTheme.typography.headlineMedium)

                    Text(
                        "₹${item.price}",
                        style = MaterialTheme.typography.headlineSmall.copy(color = primaryRed)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(item.description)

                    Spacer(Modifier.height(20.dp))
                }
            }

            // 🔥 SUGGESTED ITEMS GRID (FIXED)
            val suggestedItems = homeState.allItems.filter {
                it.category.equals(item.category, true) && it.id != item.id
            }.take(6)

            if (suggestedItems.isNotEmpty()) {

                item {
                    Text(
                        "More from ${item.category}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // ✅ KEY FIX: No nested scroll issue now
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .height(400.dp) // IMPORTANT: give bounded height
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(suggestedItems) {
                            SuggestedDishCard(
                                item = it,
                                onClick = { onItemClick(it.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun SuggestedDishCard(item: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.4f
            )
        )
    ) {
        Column {
            AsyncImage(
                model = item.image.firstOrNull(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                error = painterResource(android.R.drawable.ic_menu_report_image)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "₹${item.price}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(" ${item.rating}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconColor: Color = Color.Gray
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}




@Composable
fun FoodImageSlider(images: List<String>) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            AsyncImage(
                model = images[page],
                contentDescription = "Food Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 🔵 Optional: Indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White else Color.Gray
                        )
                )
            }
        }
    }
}

@Composable
private fun NutritionChip(label: String, value: String, modifier: Modifier = Modifier) {
    val PrimaryRed = MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = PrimaryRed,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun QuantitySelector(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val PrimaryRed = MaterialTheme.colorScheme.primary

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
                tint = PrimaryRed,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(quantity.toString(), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase",
                tint = PrimaryRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}