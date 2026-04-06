package com.example.gowallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

// 1. THIS IS THE CRITICAL IMPORT
import com.example.gowallet.ui.home.HomeView
import com.example.gowallet.ui.theme.GoWalletTheme
import com.example.gowallet.ui.transactions.TransactionsView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoWalletTheme {
                GoWalletApp()
            }
        }
    }
}

@Composable
fun GoWalletApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination }
                )
            }
        }
    ) {
        // The Navigation Logic
        when (currentDestination) {
            AppDestinations.HOME -> HomeView()
            AppDestinations.TRANSACTIONS -> TransactionsView()
            AppDestinations.PROFILE -> PlaceholderScreen("User Profile")
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    TRANSACTIONS("Transactions", Icons.Default.BarChart),
    PROFILE("Profile", Icons.Default.Person),
}

@Composable
fun PlaceholderScreen(title: String) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text(text = "This is the $title screen")
    }
}

// 2. THE PREVIEW FOR THE WHOLE APP
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GoWalletAppPreview() {
    GoWalletTheme {
        GoWalletApp()
    }
}