package com.example.helatrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.helatrack.ui.theme.HelaTrackTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun DailyPulseCard(
    cashTotal: Double,
    digitalTotal: Double,
    onAddCash: () -> Unit
) {
    val totalSales = cashTotal + digitalTotal
    val hasCash = cashTotal > 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasCash) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Performance",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (hasCash) Color.White.copy(alpha = 0.9f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (totalSales > 0) "KES ${String.format("%,.0f", totalSales)}" else "Ready for today?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = if (hasCash) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!hasCash) {
                    Button(
                        onClick = onAddCash,
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp), // Pill/Rounded shape matching iOS
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Cash",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add Cash",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Recorded",
                            tint = MaterialTheme.colorScheme.primary, // Purple icon on white circle
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (!hasCash) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "💡 Habit: Record your cash sales every evening to see your true profit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, name = "1. Action Required (No Cash)")
@Composable
fun DailyPulseEmptyPreview() {
    HelaTrackTheme {
        Box(Modifier.padding(16.dp)) {
            // Simulating only digital sales (from SMS) but no cash recorded yet
            DailyPulseCard(
                cashTotal = 0.0,
                digitalTotal = 1250.0,
                onAddCash = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. Habit Completed (Cash Added)")
@Composable
fun DailyPulseCompletedPreview() {
    HelaTrackTheme {
        Box(Modifier.padding(16.dp)) {
            // Simulating a full record
            DailyPulseCard(
                cashTotal = 800.0,
                digitalTotal = 1250.0,
                onAddCash = {}
            )
        }
    }
}

