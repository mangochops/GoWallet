package com.example.helatrack.model

import androidx.compose.ui.graphics.Color
import com.example.helatrack.R

data class PaymentProvider(
    val id: String,
    val name: String,
    val logoRes: Int,
    val identifierLabel: String,
    val brandColor: Color // New field for brand identity
)

object PaymentMethods {
    val providers = listOf(
        PaymentProvider(
            id = "MPESA_PERS",
            name = "M-Pesa",
            logoRes = R.drawable.mpesa,
            identifierLabel = "Phone Number",
            brandColor = Color(0xFF00AA13) // Safaricom Green
        ),
        PaymentProvider(
            id = "MPESA_TILL",
            name = "M-Pesa Till",
            logoRes = R.drawable.lipa_na_mpesa,
            identifierLabel = "Till Number",
            brandColor = Color(0xFF00AA13)
        ),
        PaymentProvider(
            id = "AIRTEL",
            name = "Airtel Money",
            logoRes = R.drawable.airtel_money,
            identifierLabel = "Phone Number",
            brandColor = Color(0xFFFF0000) // Airtel Red
        ),
        PaymentProvider(
            id = "EQUITY",
            name = "Equity Bank",
            logoRes = R.drawable.equity,
            identifierLabel = "Last 4 Digits of Acc",
            brandColor = Color(0xFFA32A29) // Equity Maroon
        ),
        PaymentProvider(
            id = "FAMILY",
            name = "Family Bank",
            logoRes = R.drawable.family_bank,
            identifierLabel = "Last 4 Digits of Acc",
            brandColor = Color(0xFF0054A6) // Family Bank Blue
        ),
        PaymentProvider(
            id = "NCBA",
            name = "NCBA",
            logoRes = R.drawable.ncba,
            identifierLabel = "Last 4 Digits of Acc",
            brandColor = Color(0xFF002E5D) // NCBA Dark Blue
        )
    )
}