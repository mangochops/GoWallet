package com.example.helatrack.features.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.helatrack.model.UserViewModel
import com.example.helatrack.ui.theme.HelaTrackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsView(viewModel: UserViewModel) {
    // Collecting real-time data flows from your ViewModel
    val digitalTotal by viewModel.monthlyDigitalTotal.collectAsState(initial = 0.0)
    val cashTotal by viewModel.monthlyCashTotal.collectAsState(initial = 0.0)
    val topCustomers by viewModel.topCustomers.collectAsState(initial = emptyList())

    var showCashDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Insights",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCashDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Cash Sale") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {


                item {
                    // This card reflects REAL database values
                    ExpandableMonthlyInsightCard(
                        monthName = "April Performance",
                        totalAmount = (digitalTotal ?: 0.0) + (cashTotal ?: 0.0),
                        isIncrease = true,
                        digitalAmount = digitalTotal ?: 0.0,
                        cashAmount = cashTotal ?: 0.0,
                        topCustomers = topCustomers
                    )
                }

                // Buffer to prevent the FAB from covering card details
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }

            if (showCashDialog) {
                AddCashDialog(
                    onDismiss = { showCashDialog = false },
                    onConfirm = { amount, name ->
                        viewModel.addManualCash(amount, name)
                        showCashDialog = false
                    }
                )
            }
        }
    }
}

// --- PREVIEW BLOCK ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InsightsViewPreview() {
    HelaTrackTheme {
        // We can't easily pass a real ViewModel to a Preview,
        // but we can see the UI structure here.
        Box(modifier = Modifier.fillMaxSize()) {
            // Mocking the UI look for the preview
            InsightsViewContentMock()
        }
    }
}

@Composable
fun InsightsViewContentMock() {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {},
                text = { Text("Add Cash Sale") },
                icon = { Icon(Icons.Default.Add, null) }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
//            Text("Business Insights", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            ExpandableMonthlyInsightCard(
                monthName = "April Performance",
                totalAmount = 152500.0,
                isIncrease = true,
                digitalAmount = 115000.0,
                cashAmount = 37500.0,
                topCustomers = listOf(
                    CustomerPaymentSummary("Willy's Store", 85000.0),
                    CustomerPaymentSummary("Nairobi Groceries", 45000.0)
                )
            )
        }
    }
}