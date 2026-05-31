package com.jp.foodyvilla_backoffice.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletUiModel

@Composable
fun OutletRowCard(
    outlet: OutletUiModel,
    onEditClick: () -> Unit,
    onRowNavigateClick: () -> Unit
) {
    ElevatedCard(
        onClick = onRowNavigateClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // --- AVATAR / REMOTE LOGO RECEPTACLE BLOCK ---
                SubcomposeAsyncImage(
                    model = outlet.logoUrl,
                    contentDescription = "${outlet.name} Logo",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        is AsyncImagePainter.State.Error -> {
                            DefaultStorefrontIcon()
                        }

                        else -> {
                            SubcomposeAsyncImageContent()
                        }
                    }
                }

                // --- TEXT SPECIFICATION DESCRIPTION MATRIX ---
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = outlet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${outlet.address} • Range: ${outlet.radiusKm} km • Shift: ${outlet.attendanceRadius}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Styled operational status chip tracking theme colors
                    Text(
                        text = if (outlet.isActive) "● Operational" else "○ Inactive Close",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (outlet.isActive) {
                            Color(0xFF25C72B)
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            // --- ACTION CONTROLS CHANNEL ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Config Parameters",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Navigate Into Menu Dashboard",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Reusable placeholder surface container matching your original design structure.
 */
@Composable
private fun DefaultStorefrontIcon() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = "Default Outlet Placeholder",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}