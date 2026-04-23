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

// 1. FIXED: Observe the consolidated profile instead of a non-existent businessName flow
    val profile by viewModel.userProfile.collectAsState()
    val businessName = profile?.businessName ?: "My SME"
    val realTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(TimeFilter.ALL) }

    // Logic to filter transactions based on both Search and Time Tags
    val filteredTransactions = remember(realTransactions,searchQuery, selectedFilter) {
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (filteredTransactions.isNotEmpty()) {
                    exportTransactionsToPdf(
                        context = context,
                        transactions = filteredTransactions,
                        businessName = businessName.ifEmpty { "My SME" }
                    )
                } else {
                    Toast.makeText(context, "No transactions to export", Toast.LENGTH_SHORT).show()
                } },
                icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                text = { Text("Export PDF") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
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
            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                ) {
                    items(filteredTransactions) { transaction ->
                        TransactionCard(transaction = transaction)
                    }
                }
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