package com.example.gowallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.gowallet.model.UserViewModel
import androidx.activity.viewModels
import com.example.gowallet.ui.profile.EditProfileView
import com.example.gowallet.ui.profile.ProfileScreen

class MainActivity : ComponentActivity() {
    private val userViewModel by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoWalletTheme {
                GoWalletApp(userViewModel)
            }
        }
    }
}

@Composable
fun GoWalletApp(viewModel: UserViewModel) {
    var showOnboarding by rememberSaveable { mutableStateOf(true) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    // Sub-navigation state for the Profile tab
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }

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
        OnboardingView(
            viewModel = viewModel,
            onFinish = {
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
                        onClick = {
                            currentDestination = destination
                            if (destination != AppDestinations.PROFILE) isEditingProfile = false
                        }
                    )
                }
            }
        ) {
            // The Navigation Logic
            when (currentDestination) {
                AppDestinations.HOME -> HomeView()
                AppDestinations.TRANSACTIONS -> TransactionsView()
                AppDestinations.PROFILE ->  {
                // Toggle between the Profile list and the Edit form
                if (isEditingProfile) {
                    EditProfileView(
                        viewModel = viewModel,
                        onBack = { isEditingProfile = false }
                    )
                } else {
                    ProfileScreen(
                        viewModel = viewModel,
                        onEditClick = { isEditingProfile = true },
                        onLogout = {
                            viewModel.clearData()
                            showOnboarding = true
                        }
                    )
                }
            }
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



// 2. THE PREVIEW FOR THE WHOLE APP
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GoWalletAppPreview() {
    GoWalletTheme {
        GoWalletApp(UserViewModel())
    }
}