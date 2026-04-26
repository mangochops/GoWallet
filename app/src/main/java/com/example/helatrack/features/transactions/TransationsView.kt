package com.example.helatrack.features.transactions

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.helatrack.ui.components.TransactionCard
import com.example.helatrack.ui.theme.HelaTrackTheme
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.helatrack.model.UserViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.Instant
import java.time.ZoneId
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

// Enum to manage our filter states
enum class TimeFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsView(viewModel: UserViewModel) {
    val context = LocalContext.current // Fix: Resolves 'context' reference error
    val isRefreshing by viewModel.isRefreshing.collectAsState()

// 1. FIXED: Observe the consolidated profile instead of a non-existent businessName flow
    val profile by viewModel.userProfile.collectAsState()
    val businessName = profile?.businessName ?: "My SME"
    val realTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(TimeFilter.ALL) }

    // Logic to filter transactions based on both Search and Time Tags
    val groupedTransactions = remember(realTransactions,searchQuery, selectedFilter) {
        val now = LocalDate.now()

        realTransactions.filter { entity ->
            val matchesSearch = entity.person.contains(searchQuery, ignoreCase = true) ||
                    entity.ref.contains(searchQuery, ignoreCase = true)

            // Time filter logic using the Long timestamp
            val txnDate = Instant.ofEpochMilli(entity.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val matchesFilter = when (selectedFilter) {
                TimeFilter.ALL -> true
                TimeFilter.TODAY -> txnDate.isEqual(now)
                TimeFilter.THIS_WEEK -> ChronoUnit.DAYS.between(txnDate, now) <= 7
                TimeFilter.THIS_MONTH -> txnDate.month == now.month && txnDate.year == now.year
            }

            matchesSearch && matchesFilter
        }
            .groupBy { entity ->
                // Group by LocalDate to get the "Start of Day" equivalent
                Instant.ofEpochMilli(entity.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .toSortedMap(compareByDescending { it })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Transactions",
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
//        floatingActionButton = {
//            ExtendedFloatingActionButton(
//                onClick = { if (groupedTransactions.isNotEmpty()) {
//                    exportTransactionsToPdf(
//                        context = context,
//                        transactions = groupedTransactions,
//                        businessName = businessName.ifEmpty { "My SME" }
//                    )
//                } else {
//                    Toast.makeText(context, "No transactions to export", Toast.LENGTH_SHORT).show()
//                } },
//                icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
//                text = { Text("Export PDF") },
//                containerColor = MaterialTheme.colorScheme.primary,
//                contentColor = MaterialTheme.colorScheme.onPrimary,
//                shape = RoundedCornerShape(16.dp)
//            )
//        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- SEARCH & FILTERS HEADER ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name or reference...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- FILTER TAGS (Horizontal Scroll) ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(TimeFilter.entries) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label) },
                            leadingIcon = if (selectedFilter == filter) {
                                { /* Optional checkmark icon */ }
                            } else null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // --- TRANSACTION LIST ---

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshData() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (groupedTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions found", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp) // Space for Export FAB
                    ) {
                        groupedTransactions.forEach { (date, transactions) ->
                            // Equivalent to SwiftUI Section(header: DailyTotalHeader)
                            stickyHeader {
                                DailyTotalHeader(date = date, transactions = transactions)
                            }

                            items(transactions) { transaction ->
                                TransactionCard(transaction = transaction)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DailyTotalHeader(date: LocalDate, transactions: List<com.example.helatrack.data.local.TransactionEntity>) {
    val totalAmount = transactions.sumOf { it.amount.toDouble() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Pinned header background
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = date.toString(), // You can use a Formatter for "Apr 16, 2026"
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Daily Total: KES ${totalAmount.toInt()}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TransactionsViewPreview() {
    HelaTrackTheme {
        // This is a "fake" application instance just for the compiler
        val dummyApp = Application()
        TransactionsView(viewModel = UserViewModel(dummyApp))
    }
}