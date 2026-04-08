package com.example.helatrack.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.helatrack.ui.components.IncomeBarGraph
import com.example.helatrack.ui.components.OverviewTabs
import com.example.helatrack.ui.components.TransactionCard
import com.example.helatrack.ui.theme.HelaTrackTheme
import com.example.helatrack.model.MockData
import androidx.compose.runtime.remember

@Composable
fun HomeView() {
    val now = java.time.LocalDate.now()

    // 1. Calculate Today's Income
    val todayIncome = remember {
        MockData.transactions.filter {
            val datePart = it.date.split("|")[0].trim() + " ${now.year}"
            val txnDate = java.time.LocalDate.parse(datePart, java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
            txnDate.isEqual(now)
        }.sumOf { it.amount }
    }

    // 2. Calculate Total Balance
    val totalBalance = remember {
        MockData.transactions.sumOf { it.amount }
    }

    // 3. Prepare Graph Data (Last 7 Days)
    val weeklyData = remember {
        // This generates a list of the last 7 sums for the bar graph
        (0..6).reversed().map { daysAgo ->
            val targetDate = now.minusDays(daysAgo.toLong())
            MockData.transactions.filter {
                val datePart = it.date.split("|")[0].trim() + " ${now.year}"
                val txnDate = java.time.LocalDate.parse(datePart, java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
                txnDate.isEqual(targetDate)
            }.sumOf { it.amount }
        }
    }

    val dayLabels = remember {
        (0..6).reversed().map { now.minusDays(it.toLong()).dayOfWeek.name.take(1) }
    }


    val recentTransactions = MockData.transactions.take(5)

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
                        text = "HelaTrack",
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
                OverviewTabs(todayIncome = todayIncome, totalBalance = totalBalance)
            }

            // Section 2: Bar Graph
            item {
                IncomeBarGraph(data = weeklyData, days = dayLabels)
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
    HelaTrackTheme {
        HomeView()
    }
}