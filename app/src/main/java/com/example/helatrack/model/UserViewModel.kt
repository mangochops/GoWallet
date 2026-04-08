package com.example.helatrack.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {
    // Reactive states for business data
    private val _businessName = MutableStateFlow("")
    val businessName: StateFlow<String> = _businessName

    private val _identifier = MutableStateFlow("")
    val identifier: StateFlow<String> = _identifier

    private val _selectedProvider = MutableStateFlow<PaymentProvider?>(null)
    val selectedProvider: StateFlow<PaymentProvider?> = _selectedProvider

    // Updates data from Onboarding or Edit screens
    fun updateUserData(name: String, id: String, provider: PaymentProvider) {
        _businessName.value = name
        _identifier.value = id
        _selectedProvider.value = provider
    }

    // Clears state for logout
    fun clearData() {
        _businessName.value = ""
        _identifier.value = ""
        _selectedProvider.value = null
    }
}
