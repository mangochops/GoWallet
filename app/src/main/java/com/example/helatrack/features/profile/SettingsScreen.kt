package com.example.helatrack.features.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.helatrack.ui.components.LegalScreenWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    // 1. Define state variables for the toggles
    var isNotificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var isDarkModeEnabled by rememberSaveable { mutableStateOf(false) }

    LegalScreenWrapper(title = "App Settings", onBack = onBack) {
        Text(
            text = "Preferences",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // 2. Link the Push Notifications switch to its state
        ListItem(
            headlineContent = { Text("Push Notifications") },
            supportingContent = { Text("Get alerts for new transactions") },
            trailingContent = {
                Switch(
                    checked = isNotificationsEnabled,
                    onCheckedChange = { isNotificationsEnabled = it }
                )
            }
        )

        HorizontalDivider()

        // 3. Link the Dark Mode switch to its state
        ListItem(
            headlineContent = { Text("Dark Mode") },
            supportingContent = { Text("Follow system theme") },
            trailingContent = {
                Switch(
                    checked = isDarkModeEnabled,
                    onCheckedChange = { isDarkModeEnabled = it }
                )
            }
        )
    }
}