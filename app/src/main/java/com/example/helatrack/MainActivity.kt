package com.example.helatrack

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
import androidx.compose.foundation.layout.Box
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column

// 1. THIS IS THE CRITICAL IMPORT
import com.example.helatrack.ui.insights.InsightsView
import com.example.helatrack.ui.home.HomeView
import com.example.helatrack.ui.transactions.TransactionsView
import com.example.helatrack.ui.onboarding.OnboardingView
import com.example.helatrack.model.UserViewModel
import androidx.activity.viewModels
import com.example.helatrack.ui.profile.EditProfileView
import com.example.helatrack.ui.profile.ProfileScreen
import com.example.helatrack.ui.theme.HelaTrackTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelaTrackTheme {
                // Add this Surface to force the theme background
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GoWalletApp(userViewModel)
                }
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
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val receiveGranted = permissions[Manifest.permission.RECEIVE_SMS] ?: false
        val readGranted = permissions[Manifest.permission.READ_SMS] ?: false

        if (receiveGranted && readGranted) {
            // Full access granted!
        }
    }

    if (showOnboarding) {
        // 2. Show Onboarding and hide the rest of the app
        OnboardingView(
            viewModel = viewModel,
            onFinish = {
            // Request permission right as they finish onboarding
                smsPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS
                    )
                )
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
                AppDestinations.HOME -> HomeView(viewModel = viewModel)
                AppDestinations.TRANSACTIONS -> TransactionsView(viewModel = viewModel)
                AppDestinations.INSIGHTS -> InsightsView(viewModel = viewModel)
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
    INSIGHTS("Insights", Icons.AutoMirrored.Filled.TrendingUp),
    PROFILE("Profile", Icons.Default.Person),
}



// 2. THE PREVIEW FOR THE WHOLE APP
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GoWalletAppPreview() {
    HelaTrackTheme {
        // Use a "Surface" to ensure the background isn't transparent/white
        Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // We use a dummy UI state instead of the real app logic
            // to stop the Preview from trying to open a database.
            MockOnboardingPreview()
        }
    }
}

@Composable
fun MockOnboardingPreview() {
    // This allows you to see the UI layout without
    // triggering the ViewModel's database logic.
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text("HelaTrack Preview Mode", style = MaterialTheme.typography.labelSmall)
        // Add a placeholder for where the actual view would be
        Box(modifier = androidx.compose.ui.Modifier.weight(1f)) {
            Text("Onboarding/Home UI will appear here in the real app.")
        }
    }
}