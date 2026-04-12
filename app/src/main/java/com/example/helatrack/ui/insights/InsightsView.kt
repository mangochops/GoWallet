package com.example.helatrack.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.helatrack.model.UserViewModel

/**
 * InsightsView provides a scrollable list of monthly performance cards.
 * This is the main entry point for the Insights tab in HelaTrack.
 */
@Composable
fun InsightsView(viewModel: UserViewModel) {
    // Dummy data for the UI structure.
    // In the next step, we can collect this from the Room database via the ViewModel.
    val monthlyData = remember {
        listOf(
            InsightData(
                name = "April Performance",
                total = 150000.0,
                isIncrease = true,
                digital = 120000.0,
                cash = 30000.0
            ),
            InsightData(
                name = "March Performance",
                total = 125000.0,
                isIncrease = true,
                digital = 90000.0,
                cash = 35000.0
            ),
            InsightData(
                name = "February Performance",
                total = 140000.0,
                isIncrease = false,
                digital = 100000.0,
                cash = 40000.0
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            // Padding added to top to avoid overlapping with the status bar/header
            Text(
                text = "Business Insights",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }

        items(monthlyData) { data ->
            ExpandableMonthlyInsightCard(
                monthName = data.name,
                totalAmount = data.total,
                isIncrease = data.isIncrease,
                digitalAmount = data.digital,
                cashAmount = data.cash,
                topCustomers = emptyList() // We will plug in the Top 3 query here later
            )
        }

        // Extra spacer at the bottom so the last card isn't hidden by the Nav Bar
        item {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 32.dp))
        }
    }
}

data class InsightData(
    val name: String,
    val total: Double,
    val isIncrease: Boolean,
    val digital: Double,
    val cash: Double
)