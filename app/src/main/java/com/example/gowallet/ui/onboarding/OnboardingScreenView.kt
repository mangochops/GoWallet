package com.example.gowallet.ui.onboarding

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.gowallet.R
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun OnboardingView(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> AnimationPage(
                    title = "Track Every Coin",
                    desc = "Automatically sync your M-Pesa and Bank transactions safely.",
                    resId = R.raw.finance
                )
                1 -> AnimationPage(
                    title = "Smart Insights",
                    desc = "See your spending habits with clean, automated graphs.",
                    resId = R.raw.analytics
                )
                2 -> PaymentSelectionPage(onFinish = onFinish)
            }
        }

        // Pager Indicator
        Row(
            Modifier.height(50.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun AnimationPage(title: String, desc: String, resId: Int) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(resId)
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(300.dp))
        Spacer(Modifier.height(32.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(desc, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PaymentSelectionPage(onFinish: () -> Unit) {
    val paymentMethods = listOf("M-Pesa", "Airtel Money", "M-Pesa Paybill", "M-Pesa Till", "NCBA", "Family Bank", "ABSA", "Equity", "National Bank")
    val selectedMethods = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("To Get Started", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Choose your primary payment methods", color = MaterialTheme.colorScheme.onSurfaceVariant)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f).padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(paymentMethods) { method ->
                val isSelected = selectedMethods.contains(method)
                FilterChip(
                    selected = isSelected,
                    onClick = { if (isSelected) selectedMethods.remove(method) else selectedMethods.add(method) },
                    label = { Text(method, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.height(50.dp)
                )
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Get Started")
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingPreview() {
    com.example.gowallet.ui.theme.GoWalletTheme {
        // We pass an empty lambda {} because we don't need logic for the preview
        OnboardingView(onFinish = {})
    }
}
