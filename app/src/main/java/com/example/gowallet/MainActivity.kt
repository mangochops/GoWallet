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
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// 1. THIS IS THE CRITICAL IMPORT
import com.example.gowallet.ui.home.HomeView
import com.example.gowallet.ui.theme.GoWalletTheme
import com.example.gowallet.ui.transactions.TransactionsView
import com.example.gowallet.ui.onboarding.OnboardingView

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
    var showOnboarding by rememberSaveable { mutableStateOf(true) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    // --- SMS PERMISSION LOGIC ---
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted! The SmsReceiver will now start catching messages.
        }
    }

    if (showOnboarding) {
        // 2. Show Onboarding and hide the rest of the app
        OnboardingView(onFinish = {
            // Request permission right as they finish onboarding
            smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            showOnboarding = false
        })
    } else {
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