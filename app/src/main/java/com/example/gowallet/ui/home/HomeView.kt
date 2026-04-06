package com.example.gowallet.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gowallet.model.Transaction
import androidx.compose.ui.Alignment
import com.example.gowallet.ui.components.IncomeBarGraph
import com.example.gowallet.ui.components.OverviewTabs
import com.example.gowallet.ui.components.TransactionCard
import com.example.gowallet.ui.theme.GoWalletTheme

@Composable
fun HomeView() {
    // 1. Prepare Sample Data
    val weeklyData = listOf(2000.0, 4500.0, 1500.0, 7000.0, 3000.0, 5500.0, 4000.0)
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")

    val recentTransactions = listOf(
        Transaction("1", "Mary Wangechi", 1500.0, "05 Apr", "09:00", "SCD12345"),
        Transaction("2", "Isaac Kipruto", 450.0, "05 Apr", "12:45", "SCD67890"),
        Transaction("3", "Oscar Mutuku", 2100.0, "04 Apr", "13:20", "SCD11223"),
        Transaction("4", "James Njoroge", 2100.0, "04 Apr", "14:00", "SCD11224"),
        Transaction("5", "Wendy Kadzo", 2100.0, "04 Apr", "15:20", "SCD11225"),
        Transaction("6", "William Samoei", 2100.0, "04 Apr", "16:20", "SCD11226")


    )

    Scaffold(
        topBar = {
            // This is a stable way to create a header without experimental APIs
            Surface(
                shadowElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // Ensures it doesn't overlap the clock/battery
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "GoWallet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { innerPadding ->
        // Use LazyColumn so the whole page can scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Overview Tabs
            item {
                OverviewTabs()
            }

            // Section 2: Bar Graph
            item {
                IncomeBarGraph(data = weeklyData, days = weekDays)
            }

            // Section 3: Latest Transactions Header
            item {
                Row(
                    modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Latest Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )


                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(top = 8.dp)

                    )
                }

            }

            // Section 4: The Paginated List
            items(recentTransactions) { transaction ->
                TransactionCard(transaction = transaction)
            }

            // Add some bottom padding so the last card isn't cut off
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    GoWalletTheme {
        HomeView()
    }
}