package com.example.gowallet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gowallet.ui.theme.GoWalletTheme

@Composable
fun OverviewTabs() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isBalanceVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OverviewCard(
            title = "Today's Income",
            amount = "KES 4,500",
            isSelected = selectedTab == 0,
            modifier = Modifier.weight(1f),
            onClick = { selectedTab = 0 }
        )

        OverviewCard(
            title = "Total Balance",
            amount = if (isBalanceVisible) "KES 120,400" else "KES ••••••",
            isSelected = selectedTab == 1,
            showPrivacyToggle = true,
            isBalanceVisible = isBalanceVisible,
            onTogglePrivacy = { isBalanceVisible = !isBalanceVisible },
            modifier = Modifier.weight(1f),
            onClick = { selectedTab = 1 }
        )
    }
}

@Composable
fun OverviewCard(
    title: String,
    amount: String,
    isSelected: Boolean,
    showPrivacyToggle: Boolean = false,
    isBalanceVisible: Boolean = true,
    onTogglePrivacy: () -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showPrivacyToggle) {
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Privacy",
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OverviewTabsPreview() {
    GoWalletTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OverviewTabs()
        }
    }
}