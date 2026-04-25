package com.example.helatrack.features.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.helatrack.ui.components.LegalScreenWrapper
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    LegalScreenWrapper(title = "Privacy Policy", onBack = onBack) {
        Text("Last Updated: April 2026", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Text("Data Collection", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text("HelaTrack processes SMS messages from financial providers (M-PESA, Equity, Absa, etc.) locally on your device. We do not upload your transaction history to any external servers.")

        Spacer(Modifier.height(16.dp))
        Text("Security", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text("Your data is stored in a local encrypted database using Android Room. Authentication is handled via Supabase, ensuring your account profile remains secure.")
    }
}