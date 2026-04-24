package com.example.helatrack.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.helatrack.data.local.AppDatabase
import com.example.helatrack.data.local.TransactionEntity
import com.example.helatrack.features.insights.CustomerPaymentSummary
import com.example.helatrack.auth.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.transactionDao()

    // This ensures Supabase is NOT touched until fetchUserProfile() is called
    private val supabase by lazy { SupabaseConfig.client }
    // --- Core App State ---
    private val _userProfile = MutableStateFlow<BusinessProfile?>(null)
    val userProfile: StateFlow<BusinessProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- Onboarding Selection State ---
    private val _selectedProvider = MutableStateFlow<PaymentProvider?>(null)
    val selectedProvider: StateFlow<PaymentProvider?> = _selectedProvider



    // ... (Keep existing StateFlows) ...

    init {
        // Safe initialization
        viewModelScope.launch {
            try {
                fetchUserProfile()
            } catch (e: Exception) {
                android.util.Log.e("HelaTrack", "Init Error: ${e.message}")
                _isLoading.value = false
            }
        }
    }



    // --- Time Helpers ---
    private val startOfDay: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private val startOfMonth: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    // --- Data Flows (Room) ---
    val dailyDigitalTotal: Flow<Double> = dao.getAllTransactions().map { list ->
        list.filter {
            it.timestamp >= startOfDay &&
                    (it.category == "MPESA" || it.category == "AIRTEL" || it.category == "BANK")
        }.sumOf { it.amount }
    }.distinctUntilChanged()

    val dailyCashTotal: Flow<Double> = dao.getAllTransactions().map { list ->
        list.filter {
            it.category == "CASH" && it.timestamp >= startOfDay
        }.sumOf { it.amount }
    }.distinctUntilChanged()

    val totalBalance: Flow<Double> = dao.getAllTransactions().map { list ->
        list.sumOf { it.amount }
    }.distinctUntilChanged()

    val monthlyDigitalTotal: Flow<Double> = dao.getAllTransactions().map { list ->
        list.filter {
            it.timestamp >= startOfMonth &&
                    (it.category == "MPESA" || it.category == "AIRTEL" || it.category == "BANK")
        }.sumOf { it.amount }
    }.distinctUntilChanged()

    val monthlyCashTotal: Flow<Double> = dao.getAllTransactions().map { list ->
        list.filter {
            it.category == "CASH" && it.timestamp >= startOfMonth
        }.sumOf { it.amount }
    }.distinctUntilChanged()

    val topCustomers: Flow<List<CustomerPaymentSummary>> = dao.getAllTransactions().map { list ->
        list.filter { it.timestamp >= startOfMonth }
            .groupBy { it.person }
            .map { (person, txs) -> CustomerPaymentSummary(person, txs.sumOf { it.amount }) }
            .sortedByDescending { it.totalPaid }
            .take(3)
    }

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
        .map { list -> list.sortedByDescending { it.timestamp } }
        .distinctUntilChanged()

    // --- Actions & Database Operations ---

    fun addManualCash(amount: Double, person: String) {
        viewModelScope.launch {
            dao.insertTransaction(
                TransactionEntity(
                    ref = "CASH-${System.currentTimeMillis()}",
                    amount = amount,
                    person = person,
                    category = "CASH",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Check if Supabase has a persisted session on disk
                val user = supabase.auth.currentUserOrNull()

                if (user != null) {
                    android.util.Log.d("HelaDebug", "Found existing session for: ${user.id}")

                    // 2. Try to fetch the business profile for THIS specific user
                    val profile = supabase.from("businesses")
                        .select()
                        .decodeSingleOrNull<BusinessProfile>()

                    if (profile != null) {
                        _userProfile.value = profile
                        android.util.Log.d("HelaDebug", "Profile loaded: ${profile.businessName}")
                    } else {
                        // Session exists but no profile record?
                        // This happens if onboarding was interrupted.
                        _userProfile.value = null
                    }
                } else {
                    android.util.Log.d("HelaDebug", "No session found.")
                    _userProfile.value = null
                }
            } catch (e: Exception) {
                android.util.Log.e("HelaTrack", "Fetch Profile Error: ${e.message}")
                _userProfile.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    // In UserViewModel.kt

    /**
     * Specifically for existing users to change their details.
     */
    suspend fun updateBusinessProfile(newName: String, newId: String, providerId: String): Boolean {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return false

            val updatedProfile = BusinessProfile(
                userId = userId,
                businessName = newName,
                providerType = providerId,
                identifierHash = newId
            )

            // Using our private helper (which performs the insert/upsert)
            createBusinessProfile(updatedProfile)

            // Update local state so the UI reflects changes immediately
            _userProfile.value = updatedProfile
            true
        } catch (e: Exception) {
            android.util.Log.e("HelaTrack", "Update Profile Failed: ${e.message}")
            false
        }
    }

    // In UserViewModel.kt

    /**
     * High-level action called by the UI.
     * Coordinates Auth, DB insertion, and State update.
     */
    suspend fun finalizeOnboarding(bName: String, id: String, providerId: String): Boolean {
        return try {
            android.util.Log.d("HelaDebug", "1. Start finalizeOnboarding")
            val userId = ensureAuthenticatedUser()
            android.util.Log.d("HelaDebug", "2. Auth success, UserID: $userId")

            // 2. Create the data object
            val profile = BusinessProfile(
                userId = userId ?: return false,
                businessName = bName,
                providerType = providerId,
                identifierHash = id
            )

            // 3. Perform the Database Operation
            createBusinessProfile(profile)
            android.util.Log.d("HelaDebug", "3. Supabase Insert Success")

            // 4. Update the UI State
            _userProfile.value = profile
            android.util.Log.d("HelaDebug", "4. StateFlow updated with: ${profile.businessName}")
            true
        } catch (e: Exception) {
            android.util.Log.e("HelaTrack", "Onboarding Failed: ${e.message}")
            false
        }
    }

    // Private helper: Does only Auth
    private suspend fun ensureAuthenticatedUser(): String? {
        val auth = supabase.auth
        val user = auth.currentUserOrNull() ?: run {
            auth.signInAnonymously()
            auth.currentUserOrNull()
        }
        return user?.id
    }


    private suspend fun createBusinessProfile(profile: BusinessProfile) {
        // upsert ensures that if the record exists, it updates; if not, it creates.
        supabase.from("businesses").upsert(profile)
    }

    fun clearData() {
        viewModelScope.launch {
            try {
                // 1. Clear Cloud Session
                supabase.auth.signOut()

                // 2. Clear Local Room Data (CRITICAL)
                dao.deleteAllTransactions()

                // 3. Reset UI State
                _userProfile.value = null

                android.util.Log.d("HelaDebug", "Local and Cloud data wiped successfully")
            } catch (e: Exception) {
                android.util.Log.e("HelaTrack", "Logout Error: ${e.message}")
                _userProfile.value = null
            }
        }
    }}