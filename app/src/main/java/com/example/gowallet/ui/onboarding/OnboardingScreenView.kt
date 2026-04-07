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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.gowallet.model.PaymentProvider
import com.example.gowallet.model.PaymentMethods
import com.example.gowallet.model.UserViewModel

@Composable
fun OnboardingView(onFinish: () -> Unit, viewModel: UserViewModel) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    // State to keep track of the full object selected in page 2
    var selectedMethod by remember { mutableStateOf<PaymentProvider?>(null) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> AnimationPage(
                    title = "Track Every Coin",
                    desc = "Automatically sync your M-Pesa or Bank transactions safely.",
                    resId = R.raw.finance,
                    onNext = { scope.launch { pagerState.animateScrollToPage(1) } }
                )
                1 -> AnimationPage(
                    title = "Smart Insights",
                    desc = "See your earning with clean, automated graphs.",
                    resId = R.raw.analytics,
                    onNext = { scope.launch { pagerState.animateScrollToPage(2) } }
                )
                2 -> PaymentSelectionPage(
                    onMethodSelected = { method ->
                        selectedMethod = method
                        scope.launch { pagerState.animateScrollToPage(3) }
                    }
                )
                3 -> {
                    // Pass the whole provider object to the credentials page
                    selectedMethod?.let { provider ->
                        CredentialsPage(
                            provider = provider,
                            onFinish = { bName, id ->
                                onFinish()
                            }
                        )
                    }
                }
            }
        }

        // Pager Indicator
        Row(
            Modifier.height(50.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { iteration ->
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

// ... (AnimationPage and PaymentSelectionPage remain the same)

@Composable
fun AnimationPage(title: String, desc: String, resId: Int, onNext: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(300.dp))
        Spacer(Modifier.height(32.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(desc, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next")
        }
    }
}

@Composable
fun PaymentSelectionPage(onMethodSelected: (PaymentProvider) -> Unit) {
    val providers = PaymentMethods.providers

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Select Payment Source", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Which service do you use for your SME payments?", color = MaterialTheme.colorScheme.onSurfaceVariant)

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.weight(1f).padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(providers) { provider ->
                OutlinedCard(
                    onClick = { onMethodSelected(provider) },
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, provider.brandColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(provider.brandColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = provider.logoRes),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CredentialsPage(provider: PaymentProvider, onFinish: (String, String) -> Unit) {
    var identifier by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }

    // Use the label directly from the provider object
    val labelText = provider.identifierLabel

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Link ${provider.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name (e.g., Willys Shop)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text(labelText) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onFinish(businessName, identifier) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = identifier.isNotEmpty() && businessName.isNotEmpty()
        ) {
            Text("Start Tracking Payments")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingPreview() {
    com.example.gowallet.ui.theme.GoWalletTheme {
        OnboardingView(
            onFinish = {},
            viewModel = UserViewModel()
            )
    }
}