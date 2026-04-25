package com.example.helatrack.features.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.helatrack.ui.components.LegalScreenWrapper
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height

@Composable
fun TermsScreen(onBack: () -> Unit) {
    LegalScreenWrapper(title = "Terms & Conditions", onBack = onBack) {
        Text("1. Acceptance of Terms", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text("By using HelaTrack, you agree to allow the app to read financial SMS notifications for the purpose of transaction tracking.")

        Spacer(Modifier.height(16.dp))
        Text("2. Disclaimer", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text("HelaTrack is a financial tracking tool and does not provide financial advice. We are not responsible for discrepancies between the app's data and your official bank statements.")
    }
}