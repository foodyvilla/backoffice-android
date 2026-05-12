package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
internal fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickModifier = if (onClick == null) modifier else modifier.clickable(onClick = onClick)
    Card(
        modifier = clickModifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.dp, Color.White)
    ) {
        content()
    }
}

@Composable
internal fun PremiumTopBar(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onMenu: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            onBack != null -> RoundIconButton(Icons.Default.ArrowBack, "Back", onBack)
            onMenu != null -> RoundIconButton(Icons.Default.Menu, "Menu", onMenu)
            else -> Surface(shape = CircleShape, color = RoyalBlue, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        onRefresh?.let { RoundIconButton(Icons.Default.Refresh, "Refresh", it) }
    }
}

@Composable
internal fun RoundIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(46.dp).clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = Ink, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
internal fun SearchAndFilterBar(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filters") },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RoyalBlue,
            unfocusedBorderColor = SoftLine,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
internal fun StatusPill(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(100), color = color.copy(alpha = .12f)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun RecordImage(url: String?, label: String?, size: Int = 72) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFE8EEFF)),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNullOrBlank()) {
            Icon(Icons.Default.Image, contentDescription = null, tint = RoyalBlue)
        } else {
            AsyncImage(
                model = url,
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
internal fun LargeRecordImage(url: String?, label: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE8EEFF)),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNullOrBlank()) {
            Icon(Icons.Default.Image, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(42.dp))
        } else {
            AsyncImage(
                model = url,
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
internal fun EmptyState(title: String, message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    PremiumCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = Color(0xFFE8EEFF), modifier = Modifier.size(68.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = RoyalBlue)
                }
            }
            Text(title, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(message, color = Muted, fontSize = 13.sp)
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
internal fun MetricCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier.height(136.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(shape = CircleShape, color = color.copy(alpha = .12f), modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Column {
                Text(value, color = Ink, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text(title, color = Muted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
internal fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(RoyalBlue, Color(0xFF355CFF))))
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Foody Villa", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            Text("Backoffice", color = Color.White.copy(alpha = .82f), fontSize = 13.sp)
        }
    }
}
