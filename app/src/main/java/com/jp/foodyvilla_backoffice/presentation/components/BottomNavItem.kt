package com.jp.foodyvilla_backoffice.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight

// ─────────────────────────────────────────────────────────────

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
)

private val navItems = listOf(
    BottomNavItem("Home", Icons.Filled.Home),
    BottomNavItem("Menu", Icons.Filled.Menu),
    BottomNavItem("Offers", Icons.Outlined.LocalOffer),
    BottomNavItem("Review", Icons.Filled.Reviews),
    BottomNavItem("Orders", Icons.Filled.ShoppingBag),
)

// ─────────────────────────────────────────────────────────────
// SINGLE TAB
// ─────────────────────────────────────────────────────────────

@Composable
fun FoodyVillaNavBar(
    selectedPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme

    // Store tab positions to animate the sliding pill
    val tabPositions = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }

    val targetX = tabPositions[selectedPage]?.first ?: 0f
    val targetWidth = tabPositions[selectedPage]?.second ?: 0f

    val pillX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "pillX"
    )

    val pillWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "pillWidth"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp) // Standard BottomBar height
            .padding(horizontal = 24.dp, vertical = 12.dp) // Floating effect
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Sliding Selection Pill
        if (pillWidth > 0f) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(pillX.toInt(), 0) }
                    .width(with(density) { pillWidth.toDp() })
                    .fillMaxHeight()
                    .padding(4.dp) // Inner spacing so pill doesn't touch edges
                    .background(colors.primary, RoundedCornerShape(20.dp))
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                NavTab(
                    item = item,
                    selected = selectedPage == index,
                    onClick = { onPageChange(index) },
                    modifier = Modifier.weight(1f),
                    onMeasured = { x, w -> tabPositions[index] = Pair(x, w) }
                )
            }
        }
    }
}

@Composable
private fun NavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMeasured: (x: Float, width: Float) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    // Animate content color for a smooth transition
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.7f),
        label = "color"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coords ->
                onMeasured(coords.positionInParent().x, coords.size.width.toFloat())
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        AnimatedVisibility(
            visible = true, // Keep text always visible but change style
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = item.label,
                style = if (selected)
                    MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NavTab0(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMeasured: (x: Float, width: Float) -> Unit = { _, _ -> },
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val iconOffsetY by animateFloatAsState(
        targetValue = if (selected) -3f else 0f,
        animationSpec = spring(),
        label = "iconOffsetY"
    )

    val colors = MaterialTheme.colorScheme
    val iconColor = if (selected) colors.onPrimary else colors.onSurfaceVariant
    val textColor = iconColor

    Column(
        modifier = modifier
            .onGloballyPositioned { coords ->
                onMeasured(
                    coords.positionInParent().x,
                    coords.size.width.toFloat()
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier
                .size(22.dp)
                .scale(iconScale)
                .offset { IntOffset(0, iconOffsetY.dp.roundToPx()) }
        )

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            maxLines = 1
        )
    }
}

// ─────────────────────────────────────────────────────────────
// NAV BAR
// ─────────────────────────────────────────────────────────────

@Composable
fun FoodyVillaNavBar0(
    selectedPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme

    val tabPositions = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }

    val targetX = tabPositions[selectedPage]?.first ?: 0f
    val targetWidth = tabPositions[selectedPage]?.second ?: 0f

    val pillX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "pillX"
    )

    val pillWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "pillWidth"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceVariant)
            .padding(6.dp)
    ) {

        // Sliding indicator
        if (pillWidth > 0f) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(pillX.toInt(), 0) }
                    .width(with(density) { pillWidth.toDp() })
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                NavTab(
                    item = item,
                    selected = selectedPage == index,
                    onClick = { onPageChange(index) },
                    modifier = Modifier.weight(1f),
                    onMeasured = { x, w -> tabPositions[index] = Pair(x, w) }
                )
            }
        }
    }
}