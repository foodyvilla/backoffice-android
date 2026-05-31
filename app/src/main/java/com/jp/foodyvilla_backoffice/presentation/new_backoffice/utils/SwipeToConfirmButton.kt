package com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SwipeToConfirmButton0(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    isRightToLeft: Boolean = false,
    enabled: Boolean = true,
    onConfirm: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val buttonWidth = maxWidth
        val handleSize = 56.dp
        val maxOffsetPx = with(density) { (buttonWidth - handleSize).toPx() }

        // This tracks how many pixels the user has dragged from their starting edge
        val dragAmountPx = remember { Animatable(0f) }

        // Dynamic, live calculation tracking completion progress ratio loops
        val progress = if (maxOffsetPx > 0f) (dragAmountPx.value / maxOffsetPx).coerceIn(0f, 1f) else 0f

        // Convert the raw pixel drag position over to a safe Compose Dp padding unit
        val dynamicPaddingDp: Dp = with(density) { dragAmountPx.value.toDp() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(handleSize)
                .clip(RoundedCornerShape(handleSize / 2))
                .background(if (enabled) containerColor.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f))
        ) {
            // Dynamic Background Progress Track Fill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonWidth * progress)
                    .align(if (isRightToLeft) Alignment.CenterEnd else Alignment.CenterStart)
                    .background(containerColor.copy(alpha = 0.3f))
            )

            // Central Context Prompts and Hint Indicators Row
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isRightToLeft) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft, null,
                        tint = containerColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp).alpha((1f - progress).coerceIn(0f, 1f))
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (enabled) containerColor else Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp).alpha((1f - progress * 1.5f).coerceIn(0f, 1f))
                )
                if (!isRightToLeft) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                        tint = containerColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp).alpha((1f - progress).coerceIn(0f, 1f))
                    )
                }
            }

            // --- BULLETPROOF HANDLE INTERFACE (DRIVEN COMPLETELY BY PADDING BLOCKS) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Replaces the broken absolute offset system with clean, edge-safe padding injections
                    .padding(
                        start = if (isRightToLeft) 0.dp else dynamicPaddingDp,
                        end = if (isRightToLeft) dynamicPaddingDp else 0.dp
                    ),
                contentAlignment = if (isRightToLeft) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(handleSize)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (enabled) containerColor else Color.Gray)
                        .draggable(
                            state = rememberDraggableState { delta ->
                                if (enabled) {
                                    scope.launch {
                                        // Standardizes drag direction handling mechanics cleanly
                                        val adjustedDelta = if (isRightToLeft) -delta else delta
                                        val targetValue = (dragAmountPx.value + adjustedDelta).coerceIn(0f, maxOffsetPx)
                                        dragAmountPx.snapTo(targetValue)
                                    }
                                }
                            },
                            orientation = Orientation.Horizontal,
                            onDragStopped = {
                                if (enabled) {
                                    if (progress > 0.85f) {
                                        onConfirm()
                                    }
                                    // Smoothly snap back to the base starting state positions
                                    scope.launch { dragAmountPx.animateTo(0f) }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Swipe Handle Receptacle Control",
                        tint = contentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}