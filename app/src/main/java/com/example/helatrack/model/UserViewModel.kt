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
import android.content.Context
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale
import android.os.Build
import android.provider.MediaStore

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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ... (Keep existing StateFlows) ...

    init {
        // Safe initialization
        viewModelScope.launch {
            checkOnboardingStatus()
        }
    }

    // In UserViewModel.kt

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            _isLoading.value = true

            val prefs = getApplication<Application>().getSharedPreferences("hela_prefs", Context.MODE_PRIVATE)
            val isComplete = prefs.getBoolean("onboarding_complete", false)

            // 1. Check if Supabase already has a saved session on the device
            val sessionUser = supabase.auth.currentUserOrNull()

            if (isComplete && sessionUser != null) {
                // User finished onboarding AND has a valid session
                fetchUserProfile()
            } else if (isComplete && sessionUser == null) {
                // They finished onboarding but session expired/cleared
                // You might want to show a Login screen here instead of Onboarding
                _userProfile.value = null
                _isLoading.value = false
            } else {
                // New user, show onboarding
                _isLoading.value = false
                _userProfile.value = null
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

    private fun markOnboardingComplete() {
        val prefs = getApplication<Application>().getSharedPreferences("hela_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_complete", true).apply()
    }
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

            markOnboardingComplete()
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

    // Inside UserViewModel.kt
    fun generateMonthlyReport(context: Context, monthYear: String) {
        viewModelScope.launch {
            // 1. Collect the current snapshots of your data flows
            val transactions = allTransactions.first()
            val digitalTotal = monthlyDigitalTotal.first()
            val cashTotal = monthlyCashTotal.first()
            val grandTotal = digitalTotal + cashTotal

            val pdfDocument = android.graphics.pdf.PdfDocument()
            val paint = android.graphics.Paint()
            val titlePaint = android.graphics.Paint()
            val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Title Section [cite: 1]
            titlePaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            titlePaint.textSize = 20f
            canvas.drawText("HelaTrack: $monthYear Report", 40f, 50f, titlePaint)

            paint.textSize = 14f
            var yPos = 100f

            // Totals Section matching your existing report format [cite: 2, 3, 5]
            canvas.drawText("Digital Subtotal: KES ${String.format("%,.0f", digitalTotal)}", 40f, yPos, paint)
            yPos += 25f
            canvas.drawText("Cash Subtotal: KES ${String.format("%,.0f", cashTotal)}", 40f, yPos, paint)
            yPos += 25f
            canvas.drawText("Total Collection: KES ${String.format("%,.0f", grandTotal)}", 40f, yPos, titlePaint)

            yPos += 50f
            // Table Header
            canvas.drawText("Date | Customer | Method | Amount", 40f, yPos, titlePaint)
            canvas.drawLine(40f, yPos + 5f, 550f, yPos + 5f, paint)
            yPos += 30f

            // List Transactions using correct TransactionEntity properties
            transactions.forEach { trans ->
                if (yPos < 800f) {
                    // Formatting timestamp to a readable date
                    val dateStr = dateFormatter.format(java.util.Date(trans.timestamp))
                    // Using 'person' and 'category' from your TransactionEntity
                    canvas.drawText("$dateStr | ${trans.person} | ${trans.category} | KES ${String.format("%,.0f", trans.amount)}", 40f, yPos, paint)
                    yPos += 20f
                }
            }

            pdfDocument.finishPage(page)

            val fileName = "HelaTrack_Report_${monthYear.replace(" ", "_")}.pdf"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Logic for Android 10 (API 29) and above using MediaStore
                val contentValues = android.content.ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }

                // Access the URI safely
                val downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

                val uri = context.contentResolver.insert(downloadsUri, contentValues)
                try {
                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            pdfDocument.writeTo(outputStream)
                        }
                        android.util.Log.d("HelaTrack", "Report saved to Downloads folder!")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HelaTrack", "Failed to save via MediaStore: ${e.message}")
                } finally {
                    pdfDocument.close()
                }
            } else {
                // Logic for Android 9 (API 28) and below using traditional File API
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(downloadsDir, fileName)
                try {
                    pdfDocument.writeTo(java.io.FileOutputStream(file))
                    android.util.Log.d("HelaTrack", "Report saved to legacy Downloads folder: ${file.absolutePath}")
                } catch (e: Exception) {
                    android.util.Log.e("HelaTrack", "Failed to save via File API: ${e.message}")
                } finally {
                    pdfDocument.close()
                }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // 1. Re-fetch the profile
            fetchUserProfile()
            // 2. Add any other refresh logic (like re-syncing Room with Supabase)
            kotlinx.coroutines.delay(1000) // Small delay for UX
            _isRefreshing.value = false
        }
    }
    fun clearData() {
        viewModelScope.launch {
            try {
                // 1. Clear Cloud Session
                supabase.auth.signOut()

                // 2. Clear Local Room Data (CRITICAL)
                dao.deleteAllTransactions()

                val prefs = getApplication<Application>().getSharedPreferences("hela_prefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
                // 3. Reset UI State
                _userProfile.value = null

                android.util.Log.d("HelaDebug", "Local and Cloud data wiped successfully")
            } catch (e: Exception) {
                android.util.Log.e("HelaTrack", "Logout Error: ${e.message}")
                _userProfile.value = null
            }
        }
    }}