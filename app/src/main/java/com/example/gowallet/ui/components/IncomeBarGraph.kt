package com.example.gowallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gowallet.ui.theme.GoWalletTheme

@Composable
fun IncomeBarGraph(data: List<Double>, days: List<String>) {
    val maxIncome = data.maxOrNull()?.takeIf { it > 0 } ?: 1.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Weekly Money-In",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEachIndexed { index, income ->
                    val barHeightFraction = (income / maxIncome).toFloat().coerceIn(0.1f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().weight(1f)
                    ) {
                        // Graph Area
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .width(12.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant), // The "Rail"
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(barHeightFraction)
                                    .background(MaterialTheme.colorScheme.primary) // The actual Data
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = days[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IncomeBarGraphPreview() {
    GoWalletTheme {
        val sampleData = listOf(2000.0, 4500.0, 1500.0, 7000.0, 3000.0, 5500.0, 4000.0)
        val sampleDays = listOf("M", "T", "W", "T", "F", "S", "S")

        Box(modifier = Modifier.padding(16.dp)) {
            IncomeBarGraph(data = sampleData, days = sampleDays)
        }
    }
}