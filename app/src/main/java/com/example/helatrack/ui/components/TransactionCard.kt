package com.example.helatrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.helatrack.model.Transaction
import com.example.helatrack.model.TransactionType
import com.example.helatrack.ui.theme.HelaTrackTheme
import com.example.helatrack.data.local.TransactionEntity
import androidx.compose.runtime.remember
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat

@Composable
fun TransactionCard(transaction: TransactionEntity) {
    // Format the timestamp into human-readable strings
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val dateString = dateFormatter.format(Date(transaction.timestamp))
    val timeString = timeFormatter.format(Date(transaction.timestamp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.person, // Changed from .sender to .person
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transaction.ref, // Changed from .reference to .id
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "KES ${transaction.amount}",
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "$dateString | $timeString",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionCardPreview() {
    HelaTrackTheme {
        val mockEntity = TransactionEntity(
            ref = "RCV123456",
            person = "Mary Wangeci",
            amount = 2550.0,
            category = "MPESA",
            timestamp = System.currentTimeMillis()
        )

        Box(modifier = Modifier.padding(16.dp)) {
            TransactionCard(transaction = mockEntity)
        }
    }
}