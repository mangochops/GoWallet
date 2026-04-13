package com.example.helatrack.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.helatrack.data.local.AppDatabase
import com.example.helatrack.data.local.TransactionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.helatrack.ui.insights.CustomerPaymentSummary

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.transactionDao()

    // --- Profile State ---
    private val _businessName = MutableStateFlow("")
    val businessName: StateFlow<String> = _businessName

    private val _identifier = MutableStateFlow("")
    val identifier: StateFlow<String> = _identifier

    private val _selectedProvider = MutableStateFlow<PaymentProvider?>(null)
    val selectedProvider: StateFlow<PaymentProvider?> = _selectedProvider

    // --- Insights Data (Money-In) ---
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

    // Collect totals from Room
    // Updated calculation in UserViewModel to catch all digital sources
    // --- NEW: Missing Daily Flows for HomeView ---
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

    // --- NEW: Missing Total Balance for HomeView ---
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

    // --- Actions ---
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

    fun updateUserData(name: String, id: String, provider: PaymentProvider) {
        _businessName.value = name
        _identifier.value = id
        _selectedProvider.value = provider
    }

    fun clearData() {
        _businessName.value = ""
        _identifier.value = ""
        _selectedProvider.value = null
    }
}