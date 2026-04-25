package com.example.helatrack.ui.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.helatrack.model.PaymentProvider
import com.example.helatrack.model.UserViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.helatrack.model.PaymentMethods
import com.example.helatrack.ui.theme.HelaTrackTheme

@Composable
fun ProfileScreen(
    viewModel: UserViewModel,
    onEditClick: () -> Unit,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    // 1. Observe the consolidated userProfile state
    val profile by viewModel.userProfile.collectAsState()

    // 2. Map the saved provider string back to a PaymentProvider object for UI branding
    val selectedProvider = remember(profile) {
        PaymentMethods.providers.find { it.name == profile?.providerType }
    }

    ProfileView(
        businessName = profile?.businessName ?: "Business Account",
        identifier = profile?.identifierHash ?: "N/A",
        selectedProvider = selectedProvider,
        onEditClick = onEditClick,
        onLogout = onLogout,
        onSettingsClick = onSettingsClick,
        onTermsClick = onTermsClick,
        onPrivacyClick = onPrivacyClick
    )
}

@Composable
fun ProfileView(
    businessName: String,
    identifier: String,
    selectedProvider: PaymentProvider?,
    onEditClick: () -> Unit,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(selectedProvider?.brandColor?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.primaryContainer)
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(selectedProvider?.brandColor ?: MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (businessName.isNotEmpty()) businessName.take(1).uppercase() else "G",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(businessName.ifEmpty { "Business Account" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }

        // --- DETAILS ---
        SectionHeader("Identity")
        ProfileItem(Icons.Default.Store, "Business Name", businessName, isClickable = true, onClick = onEditClick)
        ProfileItem(Icons.Default.Payments, selectedProvider?.identifierLabel ?: "ID", identifier, isClickable = true, onClick = onEditClick)

        Spacer(Modifier.height(32.dp))
        SectionHeader("Support & Legal")
        ProfilePages(
            icon = Icons.Default.Settings,
            label = "App Settings",
            onClick = onSettingsClick // Pass this in as a parameter
        )
        ProfilePages(
            icon = Icons.Default.Description,
            label = "Terms & Conditions",
            onClick = onTermsClick
        )
        ProfilePages(
            icon = Icons.Default.PrivacyTip,
            label = "Privacy Policy",
            onClick = onPrivacyClick,

        )
        // Add this if you want a dedicated button instead of just tap-to-edit rows
        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Edit Business Profile")
        }

        Spacer(Modifier.height(16.dp))

        // --- LOGOUT ---
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Logout & Reset")
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun ProfileItem(icon: ImageVector, label: String, value: String, isClickable: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfilePages(
    icon: ImageVector,
     label: String,
     isClickable: Boolean = true, // Added default value
     onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f) // Pushes the chevron to the end
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )

    }
}

@Preview(showBackground = true, name = "M-Pesa Profile")
@Composable
fun ProfilePreview() {
    val mpesaProvider = PaymentMethods.providers.first() // Assuming M-Pesa is first
    HelaTrackTheme {
        ProfileView(
            businessName = "Willys Electronics",
            identifier = "0712345678",
            selectedProvider = mpesaProvider,
            onEditClick = {},
            onLogout = {},
            onSettingsClick = {},
            onTermsClick = {},
            onPrivacyClick = {}
        )
    }
}