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
import com.example.helatrack.ui.components.DailyPulseCard
import java.time.LocalDate
import androidx.compose.runtime.mutableStateOf
import com.example.helatrack.model.UserViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import android.app.Application
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeView(viewModel: UserViewModel) {
    val now = LocalDate.now()

    // --- State Management ---
    val dailyCash by viewModel.dailyCashTotal.collectAsState(initial = 0.0)
    val dailyDigital by viewModel.dailyDigitalTotal.collectAsState(initial = 0.0)
    var showCashDialog by remember { mutableStateOf(false) }

    val todayIncome = (dailyCash  + dailyDigital)
    val totalBalance by viewModel.totalBalance.collectAsState(initial = 0.0)

    val dayLabels = remember {
        (0..6).reversed().map { now.minusDays(it.toLong()).dayOfWeek.name.take(1) }
    }

    val weeklyData = (0..6).map { (it * 100).toDouble() }
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

            item {
                DailyPulseCard(
                    cashTotal = dailyCash,
                    digitalTotal = dailyDigital,
                    onAddCash = { showCashDialog = true }
                )
            }
            // Section 3: Bar Graph
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
    if (showCashDialog) {
        // Replace with your actual AddCashDialog component
        AlertDialog(
            onDismissRequest = { showCashDialog = false },
            title = { Text("Record Cash Sale") },
            text = { Text("Record your physical cash sales for today to keep your reports accurate.") },
            confirmButton = {
                Button(onClick = {
                    // This should call a real function in your ViewModel
                    viewModel.addManualCash(500.0, "Daily Cash Sale")
                    showCashDialog = false
                }) { Text("Add KES 500 (Test)") }
            },
            dismissButton = {
                TextButton(onClick = { showCashDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    HelaTrackTheme {
        // We use a "Fake" ViewModel or a simple check to see if we are in Preview mode
        val context = LocalContext.current

        // This is a "Dummy" ViewModel that won't try to access the DB if handled correctly,
        // but the most stable "Quick Fix" for Previews is to pass mocked data.

        Surface {
            // Since we want to avoid complexity, let's just show a Placeholder
            // if the ViewModel fails to load in the Preview.
            Column {
                Text(
                    text = "HelaTrack Preview Mode",
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelSmall
                )
                // If you want the actual UI to show, use the Content method
                // described previously. If not, this avoids the crash.
                HomeView(viewModel = UserViewModel(context.applicationContext as Application))
            }
        }
    }
}