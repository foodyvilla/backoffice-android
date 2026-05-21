package com.jp.foodyvilla_backoffice.presentation.screens.backoffice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonObject

@Composable
internal fun CustomerRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RecordImage(row.firstImageUrl(), row["name"].toDisplayText("Customer"), 64)
            Column(Modifier.weight(1f)) {
                Text(row["name"].toDisplayText("No name"), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(row["phone"].toDisplayText("No phone"), color = Muted)
                Text(row["email"].toDisplayText("No email"), color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun CartRecordCard(
    row: JsonObject,
    onSendNotification: (String) -> Unit,
    onClick: () -> Unit
) {
    PremiumCard(onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                RecordImage(row["product_image"].firstUrlOrNull(), row["product_name"].toDisplayText("Product"), 64)
                Column(Modifier.weight(1f)) {
                    Text(row["customer_name"].toDisplayText("Customer"), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(row["product_name"].toDisplayText("Product"), color = Muted, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusPill("Qty ${row["qty"].toDisplayText("1")}", RoyalBlue)
                        StatusPill("Rs ${row["product_price"].toDisplayText("0")}", Success)
                    }
                }
            }
            
            HorizontalDivider(color = SoftLine)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Added on", color = Muted, fontSize = 11.sp)
                    Text(row["created_at"].toDisplayText().formatTimestamp(), color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                
                val fcmToken = row["customer_fcm"].toDisplayText()
                if (fcmToken != "-") {
                    Button(
                        onClick = { onSendNotification(fcmToken) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Notifications, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Remind", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ReviewRecordCard(row: JsonObject, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RecordImage(row.firstImageUrl(), "Review", 68)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(row["title"].toDisplayText("Review"), fontWeight = FontWeight.Bold)
                Text(row["description"].toDisplayText("No description"), color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    StatusPill("${row["rating"].toDisplayText("0")} stars", Warning)
                    Text(row["created_at"].toDisplayText().formatTimestamp(), color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}
