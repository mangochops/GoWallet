package com.example.gowallet.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gowallet.model.UserViewModel
import com.example.gowallet.ui.theme.GoWalletTheme

@Composable
fun EditProfileView(viewModel: UserViewModel, onBack: () -> Unit) {
    // Collecting state from ViewModel
    val currentName by viewModel.businessName.collectAsState()
    val currentId by viewModel.identifier.collectAsState()
    val provider by viewModel.selectedProvider.collectAsState()

    // Local state for editing
    var nameText by remember { mutableStateOf(currentName) }
    var idText by remember { mutableStateOf(currentId) }

    EditProfileContent(
        name = nameText,
        id = idText,
        label = provider?.identifierLabel ?: "ID",
        onNameChange = { nameText = it },
        onIdChange = { idText = it },
        onSave = {
            provider?.let { viewModel.updateUserData(nameText, idText, it) }
            onBack()
        },
        onBack = onBack
    )
}

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
    GoWalletTheme {
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