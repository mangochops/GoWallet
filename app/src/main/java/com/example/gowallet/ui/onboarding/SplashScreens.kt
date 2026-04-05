package com.example.gowallet.ui.onboarding

import com.example.gowallet.R

data class OnboardingPage(
    val title: String,
    val description: String,
//    val icon: Int // Resource ID for your drawable
)

val onboardingPages = listOf(
    OnboardingPage(
        "Welcome to NuKlear",
        "Monitor nuclear energy stations across the globe in real-time.",

    ),
    OnboardingPage(
        "Save Favorites",
        "Heart your most-watched stations to access them instantly from your dashboard.",

    ),
    OnboardingPage(
        "Stay Informed",
        "Get the latest energy news and safety standards directly in the app.",

    )
)