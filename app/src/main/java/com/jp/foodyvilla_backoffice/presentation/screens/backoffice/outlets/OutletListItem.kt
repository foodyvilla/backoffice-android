package com.jp.foodyvilla_backoffice.presentation.screens.backoffice.outlets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.foodyvilla_backoffice.data.model.backoffice.Outlet
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.*

@Composable
fun OutletListItem(
    outlet: Outlet,
    onClick: () -> Unit
) {
    PremiumCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecordImage(url = outlet.logoUrl, label = outlet.name, size = 62)
            
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = outlet.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${outlet.city} | ${outlet.phone ?: "No phone"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                if (outlet.isActive == true) {
                    StatusPill("Active", MaterialTheme.colorScheme.primary)
                } else {
                    StatusPill("Inactive", MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
