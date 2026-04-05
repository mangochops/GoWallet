package com.example.gowallet.ui.components // Moved to UI folder

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Replace this with your actual model package if different
import com.example.gowallet.model.Transaction
// Replace this with your actual theme package
import com.example.gowallet.ui.theme.GoWalletTheme

@Composable
fun TransactionCard(transaction: Transaction) {
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
                    text = transaction.sender,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transaction.reference,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column {
                Text(
                    text = "KES ${transaction.amount}",
                    color = Color(0xFF4CAF50), // M-Pesa style green
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${transaction.date} | ${transaction.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
    }
}

@Preview(showBackground = true, name = "Transaction Card Light")
@Composable
fun TransactionCardPreview() {
    GoWalletTheme {
        val mockTransaction = Transaction(
            id = "1",
            sender = "Mary Wangeci",
            amount = 2550.0,
            date = "05 Apr",
            time = "14:30",
            reference = "RCV123456"
        )

        Box(modifier = Modifier.padding(16.dp)) {
            TransactionCard(transaction = mockTransaction)
        }
    }
}

