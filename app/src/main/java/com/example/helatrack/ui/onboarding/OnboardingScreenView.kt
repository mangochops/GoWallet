package com.example.helatrack.ui.onboarding

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
import com.example.helatrack.R
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.helatrack.model.PaymentProvider
import com.example.helatrack.model.PaymentMethods
import com.example.helatrack.model.UserViewModel
import com.example.helatrack.ui.theme.HelaTrackTheme
import android.app.Application
import androidx.compose.ui.platform.LocalContext

@Composable
fun OnboardingView(onFinish: () -> Unit, viewModel: UserViewModel) {
    val pagerState = rememberPagerState(pageCount = { 5 })
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
                    desc = "Automatically sync your M-Pesa, Airtel Money or Bank transactions safely.",
                    resId = R.raw.finance,
                    onNext = { scope.launch { pagerState.animateScrollToPage(1) } }
                )
                1 -> AnimationPage(
                    title = "Your Personal Assistant",
                    desc = "Close your books every evening. Track cash and mobile payments in one place for a complete financial picture.",
                    resId = R.raw.assistant,// Or a specific reporting Lottie if available
                    onNext = { scope.launch { pagerState.animateScrollToPage(2) } }
                )
                2 -> AnimationPage(
                    title = "Smart Insights",
                    desc = "See your earning with clean, automated graphs.",
                    resId = R.raw.analytics,
                    onNext = { scope.launch { pagerState.animateScrollToPage(3) } }
                )
                3 -> PaymentSelectionPage(
                    onMethodSelected = { method ->
                        selectedMethod = method
                        scope.launch { pagerState.animateScrollToPage(4) }
                    }
                )
                4 -> {
                    // Pass the whole provider object to the credentials page
                    selectedMethod?.let { provider ->
                        CredentialsPage(
                            provider = provider,
                            onFinish = { bName, id ->
                                viewModel.updateUserData(bName, id, provider)
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
            repeat(5) { iteration ->
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
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, // This uses your HelaNavy
                contentColor = Color.White                          // This ensures the "Next" text is white
            )
        ) {
            Text("Next",fontWeight = FontWeight.Bold)
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
    val labelText = provider.identifierLabel

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) // Ensure a solid background
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Link ${provider.name}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold, // Increased weight for contrast
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            // Explicitly setting colors for higher border/text contrast
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = provider.brandColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = provider.brandColor,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text(labelText) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = provider.brandColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = provider.brandColor,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onFinish(businessName, identifier) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            // Use the provider's brand color for the button background
            colors = ButtonDefaults.buttonColors(
                containerColor = provider.brandColor,
                contentColor = Color.White,
                disabledContainerColor = provider.brandColor.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            enabled = identifier.isNotEmpty() && businessName.isNotEmpty(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Start Tracking Payments",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingPreview() {
    HelaTrackTheme {
        // Quick Fix: Use LocalContext or a dummy Application instance for the ViewModel
        val context = LocalContext.current
        val application = context.applicationContext as? Application ?: Application()
        OnboardingView(
            onFinish = {},
            viewModel = UserViewModel(application)
            )
    }
}