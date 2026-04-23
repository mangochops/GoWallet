package com.example.helatrack.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.helatrack.model.UserViewModel
import com.example.helatrack.ui.theme.HelaTrackTheme
import com.example.helatrack.model.PaymentMethods
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch

@Composable
fun EditProfileView(viewModel: UserViewModel, onBack: () -> Unit) {
    // Collecting state from ViewModel
    val profile by viewModel.userProfile.collectAsState()
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    // 2. Initialize local state with current profile values
    // We use PaymentMethods helper to find the provider by name for the label
    val provider = PaymentMethods.providers.find { it.name == profile?.providerType }

    var nameText by remember { mutableStateOf(profile?.businessName ?: "") }
    var idText by remember { mutableStateOf(profile?.identifierHash ?: "") }

    if (isSaving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        EditProfileContent(
            name = nameText,
            id = idText,
            label = provider?.identifierLabel ?: "Identifier",
            onNameChange = { newName -> nameText = newName },
            onIdChange = { newId -> idText = newId },
            onSave = {
                scope.launch {
                    isSaving = true
                    provider?.let { currentProvider ->
                        val success = viewModel.updateBusinessProfile(nameText, idText, currentProvider.id)
                        if (success) onBack()
                    }
                    isSaving = false
                }
            },
            onBack = onBack
    )
}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    name: String,
    id: String,
    label: String,
    onNameChange: (String) -> Unit, // Fixed parameter type
    onIdChange: (String) -> Unit,   // Fixed parameter type
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Using AutoMirrored version to fix deprecation warning
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = id,
                onValueChange = onIdChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save Changes")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    HelaTrackTheme {
        EditProfileContent(
            name = "Willys Shop", // Added @Suppress("SpellCheckingInspection") if typo warning persists
            id = "123456",
            label = "Store Number",
            onNameChange = {},
            onIdChange = {},
            onSave = {},
            onBack = {}
        )
    }
}