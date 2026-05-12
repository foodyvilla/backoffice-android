package com.jp.foodyvilla_backoffice.presentation.screens


// Compose UI


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person3
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jp.foodyvilla_backoffice.R
import com.jp.foodyvilla_backoffice.presentation.components.FoodyVillaNavBar
import com.jp.foodyvilla_backoffice.presentation.navigation.Screen
import com.jp.foodyvilla_backoffice.presentation.screens.home.HomeScreen
import com.jp.foodyvilla_backoffice.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.menu.MenuScreen
import com.jp.foodyvilla_backoffice.presentation.screens.offers.OffersScreen
import com.jp.foodyvilla_backoffice.presentation.screens.orders.OrderHistoryScreen
import com.jp.foodyvilla_backoffice.presentation.screens.reviews.ReviewsScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.getCartItems()
    }
    val selectedPage = viewModel.selectedPage.collectAsStateWithLifecycle().value
    val homeState = viewModel.uiState.collectAsStateWithLifecycle().value
    val titles = listOf(
        "Good day, Foodie 👋",
        "Order Menu",
        "Offers",
        "Reviews",
        "Contact Us"
    )
    val title = titles[selectedPage]
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Change from Transparent
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title ?: "Foody Villa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    IconButton(onClick = {

                        navController.navigate(Screen.CustomerSupport)

                    }) {
                        Icon(
                            Icons.Default.SupportAgent,
                            contentDescription = "customer support",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = {

                        navController.navigate(Screen.Cart)

                    }) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }


                    IconButton(onClick = {

                        navController.navigate(Screen.Profile)

                    }) {
                        Icon(
                            Icons.Default.Person3,
                            contentDescription = "Cart",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
//        floatingActionButton = {
//            CartFab(
//                cartItemCount = homeState.cartItems.size,
//                onClick = { navController.navigate(Screen.Cart) }
//            )
//        },
        bottomBar = {


            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp) ){
                ZomatoCartBar(
                    cartItemCount = homeState.cartItems.size,
                    totalPrice = homeState.cartItems.sumOf { it.totalPrice ?: 0.0 },
                    homeState.cartItems.mapNotNull {
                        it.products?.image?.firstOrNull()
                    }
                ) {
                    navController.navigate(Screen.Cart)
                }
                FoodyVillaNavBar(
                    selectedPage = selectedPage,
                    onPageChange = { viewModel.updateSelectedPage(it) },
                    modifier = modifier
                        .fillMaxWidth()
                        .height(84.dp)
                )
            }


        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(top = innerPadding.calculateTopPadding()) ,
      contentAlignment = Alignment.Center
        ) {

            // MenuWebViewScreen stays alive in the background — never recomposed away


            // Overlay the other pages on top, just hide them when not needed
            when (selectedPage) {
                0 -> HomeScreen({ itemId ->
                    navController.navigate(Screen.Detail(itemId))
                }, viewModel = viewModel, onMenuClick = {
                    viewModel.updateSelectedPage(1)
                })

                1 -> MenuScreen(
                    navController = navController,
                    viewModel = viewModel,
                    onItemClick = { navController.navigate(Screen.Detail(it)) })

                2 -> OffersScreen()
                3 -> ReviewsScreen() {
                    navController.navigate(Screen.AddReviews)
                }

                4 -> OrderHistoryScreen(viewModel = viewModel )
                else -> HomeScreen({ itemId ->
                    navController.navigate(Screen.Detail(itemId))
                }, viewModel = viewModel, onMenuClick = {
                    viewModel.updateSelectedPage(1)
                })

            }


        }
    }
}





@Composable
fun ZomatoCartBar(
    cartItemCount: Int,
    totalPrice: Double,
    items: List<String> = emptyList(),
    onClick: () -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.cart_red)
    )

    var isAnimating by remember { mutableStateOf(false) }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isAnimating,
        iterations = 1,
        speed = 1.5f
    )

    LaunchedEffect(cartItemCount) {
        if (cartItemCount > 0) {
            isAnimating = false
            isAnimating = true
        }
    }
    LaunchedEffect(progress) {
        if (progress == 1f) isAnimating = false
    }

    AnimatedVisibility(
        visible = cartItemCount > 0,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(.9f)
//                .background(Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable { onClick() },
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFE23744) // Zomato red
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
// Option A: Simple Row with negative padding for overlap
                    Row(
                        modifier = Modifier
                            .height(60.dp)

                    ) {
                        items.take(3).forEachIndexed { index, imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null, contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .offset(x = (-30 * index).dp)  // 👈 negative = overlap left
                                    .zIndex((5 - index).toFloat())  // 👈 first image on top
                                    .border(2.dp, Color(0xFFE23744), CircleShape)
                                    .clip(CircleShape)
                            )
                        }

                    }
                    // Left — item count badge


                    // Center — Lottie + "View Cart"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
//                        LottieAnimation(
//                            composition = composition,
//                            progress = { progress },
//                            modifier = Modifier.size(28.dp)
//                        )
                        Text(
                            text = "View Cart",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.3.sp
                        )
                    }

                    // Right — price + arrow
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "₹%.0f".format(totalPrice),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartFab(
    cartItemCount: Int,
    onClick: () -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.cart_red)
    )

    // Trigger animation when cart count changes
    var isAnimating by remember { mutableStateOf(false) }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isAnimating,
        iterations = 1,
        speed = 1.5f
    )

    // Re-trigger on item count change
    LaunchedEffect(cartItemCount) {
        if (cartItemCount > 0) {
            isAnimating = false
            isAnimating = true
        }
    }

    // Reset isAnimating when animation completes
    LaunchedEffect(progress) {
        if (progress == 1f) isAnimating = false
    }

    // Scale bounce on item add
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_scale"
    )

    if (cartItemCount > 0) {
        Box(contentAlignment = Alignment.TopEnd) {
            FloatingActionButton(
                onClick = onClick,
                modifier = Modifier
                    .scale(scale)
                    .size(64.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(36.dp)
                )
            }

            // Badge
            AnimatedVisibility(
                visible = cartItemCount > 0,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Badge(
                    modifier = Modifier.offset(x = 4.dp, y = (-4).dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}