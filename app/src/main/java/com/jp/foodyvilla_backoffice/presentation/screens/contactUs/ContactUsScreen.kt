package com.jp.foodyvilla_backoffice.presentation.screens.contactUs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ─── CONSTANTS ─────────────────────────────────────────────────────────────
const val PHONE_NUMBER = "7067371183"
private const val EMAIL_ADDRESS = "foodyvilla_backoffice.in@gmail.com"
private const val WHATSAPP_URL = "https://wa.me/917067371183"
private const val WEBSITE_URL = "https://www.foodyvilla.in"
const val MAP_LAT = 20.348417
const val MAP_LNG = 81.959333
const val MAP_LABEL = "Foodyvilla+Nagri+Chhattisgarh"

// ─── DATA MODELS ────────────────────────────────────────────────────────────
data class ContactTile(
    val icon: ImageVector,
    val label: String,
    val subtitle: String,
    val containerColor: (ColorScheme) -> Color,
    val contentColor: (ColorScheme) -> Color,
    val action: (Context) -> Unit
)

data class HourEntry(val day: String, val time: String, val isToday: Boolean = false)

// ─── MAIN SCREEN ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(onNavigateBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val tiles = remember {
        listOf(
            ContactTile(Icons.Rounded.Call, "Call Us", "+91 $PHONE_NUMBER", { it.primaryContainer }, { it.onPrimaryContainer }) { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91$PHONE_NUMBER")))
            },
            ContactTile(Icons.Rounded.Email, "Email Us", "Drop us a mail", { it.secondaryContainer }, { it.onSecondaryContainer }) { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$EMAIL_ADDRESS")
                    putExtra(Intent.EXTRA_SUBJECT, "Inquiry – Foodyvilla")
                })
            },
            ContactTile(Icons.Rounded.Chat, "WhatsApp", "Chat with us", { it.tertiaryContainer }, { it.onTertiaryContainer }) { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_URL)))
            },
            ContactTile(Icons.Rounded.Language, "Website", "foodyvilla_backoffice.in", { it.surfaceVariant }, { it.onSurfaceVariant }) { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL)))
            }
        )
    }

    val hours = listOf(
        HourEntry("Mon - Sat", "11:30 AM – 10:30 PM", true),
        HourEntry("Sunday", "11:30 AM – 11:00 PM")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Contact Us", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
        ) {
            HeroHeader()

            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader("Get In Touch")
                ContactGrid(tiles, context)

                Spacer(Modifier.height(24.dp))
                ShareCard(context)

                Spacer(Modifier.height(32.dp))
                SectionHeader("Our Location")
                LocationSection(context)

                Spacer(Modifier.height(32.dp))
                SectionHeader("Opening Hours")
                HoursSection(hours)

                Spacer(Modifier.height(48.dp))
                Footer()
            }
        }
    }
}

// ─── COMPONENTS ─────────────────────────────────────────────────────────────

@Composable
private fun HeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            ) {
                Text(
                    "Nagri, Chhattisgarh",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Foodyvilla",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                "Indian & Chinese Cuisine",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
        Icon(
            Icons.Rounded.RestaurantMenu,
            null,
            modifier = Modifier.size(80.dp).align(Alignment.CenterEnd).graphicsLayer(alpha = 0.1f),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun ContactGrid(tiles: List<ContactTile>, context: Context) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { tile ->
                    ElevatedCard(
                        onClick = { tile.action(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = tile.containerColor(MaterialTheme.colorScheme)
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Icon(tile.icon, null, tint = tile.contentColor(MaterialTheme.colorScheme))
                            Spacer(Modifier.height(8.dp))
                            Text(tile.label, fontWeight = FontWeight.Bold, color = tile.contentColor(MaterialTheme.colorScheme))
                            Text(tile.subtitle, style = MaterialTheme.typography.labelSmall, color = tile.contentColor(MaterialTheme.colorScheme).copy(0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareCard(context: Context) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp),
        onClick = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out Foodyvilla Nagri for amazing food! $WEBSITE_URL")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
        }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Share, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Invite Friends", fontWeight = FontWeight.Bold)
                Text("Share the taste of Foodyvilla", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Rounded.ChevronRight, null)
        }
    }
}

@Composable
private fun LocationSection(context: Context) {
    OutlinedCard(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp).background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) {
                Icon(Icons.Rounded.LocationOn, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.padding(16.dp)) {
                Text("Main Branch, Nagri", fontWeight = FontWeight.Bold)
                Text("Dhamtari District, Chhattisgarh", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val uri = Uri.parse("geo:$MAP_LAT,$MAP_LNG?q=$MAP_LAT,$MAP_LNG($MAP_LABEL)")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Directions, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Get Directions")
                }
            }
        }
    }
}

@Composable
private fun HoursSection(hours: List<HourEntry>) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            hours.forEach { entry ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(entry.day, fontWeight = if (entry.isToday) FontWeight.Bold else FontWeight.Normal)
                    Text(entry.time, color = if (entry.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun Footer() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Foodyvilla v1.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("Nagri, CG - India", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}