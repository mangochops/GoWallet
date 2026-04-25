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
import com.example.helatrack.ui.features.profile.EditProfileView

// 1. THIS IS THE CRITICAL IMPORT
import com.example.helatrack.features.insights.InsightsView
import com.example.helatrack.ui.home.HomeView
import com.example.helatrack.features.transactions.TransactionsView
import com.example.helatrack.ui.onboarding.OnboardingView
import com.example.helatrack.model.UserViewModel
import androidx.activity.viewModels
import com.example.helatrack.ui.profile.ProfileScreen
import com.example.helatrack.ui.theme.HelaTrackTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import androidx.work.Constraints
import java.util.concurrent.TimeUnit
import java.util.Calendar
import android.content.Context
import com.example.helatrack.worker.EodSummaryWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.example.helatrack.features.profile.SettingsScreen
import com.example.helatrack.features.profile.TermsScreen
import com.example.helatrack.features.profile.PrivacyPolicyScreen



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
//            MaterialTheme {
//                Text("If you can see this, the problem is in the ViewModel")
//            }
        }
    }
}

fun scheduleEodWork(context: Context) {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 20) // 8:00 PM
    calendar.set(Calendar.MINUTE, 0)

    var delay = calendar.timeInMillis - System.currentTimeMillis()
    if (delay < 0) {
        // If it's already past 8PM, schedule for tomorrow
        delay += TimeUnit.DAYS.toMillis(1)
    }

    val eodRequest = PeriodicWorkRequestBuilder<EodSummaryWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "EOD_SUMMARY",
        ExistingPeriodicWorkPolicy.REPLACE,
        eodRequest
    )
}


@Composable
fun GoWalletApp(viewModel: UserViewModel) {
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }

    var showingSettings by rememberSaveable { mutableStateOf(false) }
    var showingTerms by rememberSaveable { mutableStateOf(false) }
    var showingPrivacy by rememberSaveable { mutableStateOf(false) }

    // --- SMS PERMISSION LOGIC ---
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val receiveGranted = permissions[Manifest.permission.RECEIVE_SMS] ?: false
        val readGranted = permissions[Manifest.permission.READ_SMS] ?: false
        val notifyGranted = permissions.getOrElse(Manifest.permission.POST_NOTIFICATIONS) { false }
        if (receiveGranted && readGranted) {
            // Full access granted!
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
        // 2. Show Onboarding and hide the rest of the app
    else if (userProfile == null) {
        OnboardingView(
            viewModel = viewModel,
            onFinish = {
                val permissions = mutableListOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
                )

                // Only add notification permission if the phone is Android 13 or newer
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                smsPermissionLauncher.launch(permissions.toTypedArray())
                scheduleEodWork(context)

            }
        )
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
                            if (destination != AppDestinations.PROFILE) {
                                isEditingProfile = false
                            }
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
                        onSettingsClick = { showingSettings = true },
                        onTermsClick = { showingTerms = true },
                        onPrivacyClick = { showingPrivacy = true },
                        onLogout = {
                            viewModel.clearData()

                        }
                    )
                }
            }
            }
        }
    }
    if (showingSettings) {
        SettingsScreen(onBack = { showingSettings = false })
    }
    if (showingTerms) {
        TermsScreen(onBack = { showingTerms = false })
    }
    if (showingPrivacy) {
        PrivacyPolicyScreen(onBack = { showingPrivacy = false })
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