package com.example.helatrack.ui.insights

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun ExpandableMonthlyInsightCard(
    monthName: String,
    totalAmount: Double,
    isIncrease: Boolean,
    digitalAmount: Double,
    cashAmount: Double,
    topCustomers: List<CustomerPaymentSummary> // Top 3 list derived from TransactionEntity
) {
    // State to manage whether the card is expanded
    var isExpanded by remember { mutableStateOf(false) }

    val total = digitalAmount + cashAmount
    val digitalProgress = if (total > 0) (digitalAmount / total).toFloat() else 0f

    // Smooth shape transition
    val cardShape = if (isExpanded) RoundedCornerShape(24.dp) else RoundedCornerShape(16.dp)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { isExpanded = !isExpanded } // Toggle expansion on click
            .animateContentSize( // Smooth animation for height changes
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = cardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Month, Trend Arrow, and Expand indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isIncrease) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isIncrease) " Increase" else " Decrease",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Row 2: Total Amount
            Text(
                text = "KES ${String.format("%,.0f", totalAmount)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Row 3: The Progress Bar (Digital vs Cash)
            Text(
                text = "Digital vs Cash Collection",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { digitalProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary, // Digital
                trackColor = MaterialTheme.colorScheme.secondaryContainer // Cash
            )

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Digital: ${String.format("%.0f%%", digitalProgress * 100)}", style = MaterialTheme.typography.bodySmall)
                Text("Cash: ${String.format("%.0f%%", (1 - digitalProgress) * 100)}", style = MaterialTheme.typography.bodySmall)
            }

            // --- EXPANDED SECTION ---
            if (isExpanded) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Top 3 Customers (This Month)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Use Column (not LazyColumn) inside a scrollable parent/card
                topCustomers.take(3).forEachIndexed { index, customer ->
                    CustomerItem(customer, index + 1)
                }

                // Simple nudge to tell user to click again to close
                Text(
                    text = "Tap again to collapse",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
                )
            }
        }
    }
}


@Composable
fun AddCashDialog(onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Cash Sale") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (KES)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (amt > 0 && customer.isNotEmpty()) onConfirm(amt, customer)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Data class for the preview/ViewModel
data class CustomerPaymentSummary(
    val name: String,
    val totalPaid: Double
)

@Composable
fun CustomerItem(customer: CustomerPaymentSummary, rank: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(30.dp)
            )
            Text(
                text = customer.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "KES ${String.format("%,.0f", customer.totalPaid)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandableCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            // Sample Data
            val sampleCustomers = listOf(
                CustomerPaymentSummary("Willy's General Store", 85000.0),
                CustomerPaymentSummary("Nairobi Groceries Ltd", 45000.0),
                CustomerPaymentSummary("Aunty Jane's Hardware", 20000.0)
            )

            ExpandableMonthlyInsightCard(
                monthName = "April Performance",
                totalAmount = 150000.0,
                isIncrease = true, // Trend comparing April to March
                digitalAmount = 120000.0, // (MPESA + BANK)
                cashAmount = 30000.0, // (CASH)
                topCustomers = sampleCustomers
            )
        }
    }
}