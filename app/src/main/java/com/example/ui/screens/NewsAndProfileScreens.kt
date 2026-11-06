package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.NewsCategory
import com.example.data.model.StockAlert
import com.example.data.model.StockNewsArticle
import com.example.data.model.StockQuote
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.theme.CharcoalBase
import com.example.ui.theme.CoolGray
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmeraldBullish
import com.example.ui.theme.EmeraldBullishSoft
import com.example.ui.theme.GlassSpecularSubtle
import com.example.ui.theme.IcyBlueAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SilverNeutral
import com.example.ui.theme.SoftRedBearish
import com.example.ui.theme.SoftRedBearishLight
import com.example.ui.theme.SoftWhite
import com.example.util.StockNotificationHelper
import com.example.viewmodel.UiState

private val ALERT_TYPES = listOf(
    "PRICE_SURGE" to "Significant Price Surge (+%)",
    "PRICE_DROP" to "Significant Price Drop (-%)",
    "PREDICTION_SHIFT" to "AI Probability Shift",
    "EARNINGS_NEWS" to "Breaking News & Earnings",
    "SENTIMENT_SHIFT" to "Market Sentiment Reversal",
    "SUPPORT_RESISTANCE" to "Support / Resistance Touch"
)

@Composable
fun NewsIntelligenceScreen(
    articles: List<StockNewsArticle>,
    selectedCategory: NewsCategory,
    onSelectCategory: (NewsCategory) -> Unit,
    onSelectTicker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("news_screen_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Newspaper,
                        contentDescription = null,
                        tint = IcyBlueAccent
                    )
                    Text(
                        text = "Global News & Factor Intelligence",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Verified global financial reporting strictly separated from AI probabilistic impact attribution.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolGray
                )
            }
        }

        // Category Filter Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NewsCategory.entries, key = { it.name }) { cat ->
                    LiquidGlassPill(
                        text = cat.label,
                        selected = cat == selectedCategory,
                        accentColor = IcyBlueAccent,
                        onClick = { onSelectCategory(cat) }
                    )
                }
            }
        }

        items(articles, key = { "global_news_${it.id}" }) { article ->
            Column(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NewsArticleGlassCard(
                    article = article,
                    onOpenUrl = { url ->
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {}
                    }
                )
                // Clickable Related Tickers
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(article.relatedTickers, key = { "${article.id}_$it" }) { ticker ->
                        LiquidGlassPill(
                            text = "Inspect $ticker →",
                            selected = false,
                            accentColor = EmeraldBullishSoft,
                            onClick = { onSelectTicker(ticker) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileAndAlertsScreen(
    userEmail: String?,
    userId: String,
    alertsState: UiState<List<StockAlert>>,
    allStocks: List<StockQuote>,
    onCreateAlert: (StockQuote, String, Double) -> Unit,
    onToggleAlert: (StockAlert, Boolean) -> Unit,
    onDeleteAlert: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAlertStock by remember(allStocks) { mutableStateOf(allStocks.firstOrNull()) }
    var selectedAlertType by remember { mutableStateOf(ALERT_TYPES.first().first) }
    var notificationStatusText by remember {
        mutableStateOf(
            if (StockNotificationHelper.hasNotificationPermission(context)) {
                "System Notifications Enabled"
            } else {
                "Permission Required for System Push Alerts"
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationStatusText = if (granted) {
            StockNotificationHelper.sendStockAlertNotification(
                context = context,
                title = "Global Stock Intelligence Alerts Enabled",
                body = "Real-time price movement, AI probability shift, and support/resistance alerts are now active."
            )
            "System Notifications Active & Verified"
        } else {
            "Notification Permission Declined — In-App Alerts Only"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card
        item {
            LiquidGlassCard(
                cornerRadius = 24.dp,
                elevated = true,
                accentGlowColor = IcyBlueAccent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(IcyBlueAccent.copy(alpha = 0.2f))
                                    .border(1.dp, IcyBlueAccent, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = SoftWhite
                                )
                            }
                            Column {
                                Text(
                                    text = userEmail ?: "Authenticated Institutional User",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "UID: ${userId.take(12)}... • Encrypted Session",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = CoolGray
                                )
                            }
                        }

                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SoftRedBearish.copy(alpha = 0.18f))
                                .testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = SoftRedBearishLight
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = EmeraldBullishSoft,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Bank-Grade TLS + Firebase App Check + Zero-Trust Firestore Isolation Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldBullishSoft
                        )
                    }
                }
            }
        }

        // Notification System & Live Trigger Card
        item {
            LiquidGlassCard(
                cornerRadius = 24.dp,
                elevated = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = IcyBlueAccent
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Intelligent Alert & Notification Engine",
                                style = MaterialTheme.typography.titleLarge,
                                color = SoftWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = notificationStatusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldBullishSoft
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !StockNotificationHelper.hasNotificationPermission(context)
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val target = selectedAlertStock ?: allStocks.firstOrNull()
                                    val sent = StockNotificationHelper.sendStockAlertNotification(
                                        context = context,
                                        title = "AI Probability Alert: ${target?.ticker ?: "NVDA"}",
                                        body = "${target?.companyName ?: "NVIDIA"} 1M Chance Up calibrated at ${target?.primaryPrediction?.chanceUp ?: 72}% (${target?.primaryPrediction?.expectedDirection?.label ?: "Bullish"}). Support S1: ${target?.currency ?: "USD"} ${target?.technicals?.supportLevel1 ?: 132.0}."
                                    )
                                    notificationStatusText = if (sent) {
                                        "Live System Alert Dispatched for ${target?.ticker ?: "NVDA"}"
                                    } else {
                                        "Grant notification permission to receive system push alerts"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IcyBlueAccent,
                                contentColor = DeepObsidian
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_live_notification_button")
                        ) {
                            Text(
                                text = "Dispatch Live Alert",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Configure Custom Cloud Alert Rule:",
                        style = MaterialTheme.typography.labelLarge,
                        color = SoftWhite
                    )

                    // Select Stock for Alert
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allStocks.take(10), key = { "alert_stock_${it.ticker}" }) { st ->
                            LiquidGlassPill(
                                text = st.ticker,
                                selected = selectedAlertStock?.ticker == st.ticker,
                                accentColor = IcyBlueAccent,
                                onClick = { selectedAlertStock = st }
                            )
                        }
                    }

                    // Select Alert Trigger Type
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ALERT_TYPES.forEach { (code, label) ->
                            LiquidGlassPill(
                                text = label,
                                selected = selectedAlertType == code,
                                accentColor = EmeraldBullishSoft,
                                onClick = { selectedAlertType = code }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val st = selectedAlertStock ?: allStocks.firstOrNull()
                            if (st != null) {
                                onCreateAlert(st, selectedAlertType, 3.5)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldBullishSoft,
                            contentColor = DeepObsidian
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_cloud_alert_button")
                    ) {
                        Text(
                            text = "Save Alert Rule for ${selectedAlertStock?.ticker ?: "Stock"}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Cloud Alerts List
        if (alertsState is UiState.Success && alertsState.data.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Active Cloud-Synced Alert Rules",
                    subtitle = "Stored in Firestore under your encrypted user profile",
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }

            items(alertsState.data, key = { it.id }) { alert ->
                LiquidGlassCard(
                    cornerRadius = 18.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${alert.ticker} • ${alert.alertType.replace("_", " ")}",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = SoftWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${alert.companyName} • Threshold ±${alert.thresholdValue}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = CoolGray
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = alert.enabled,
                                onCheckedChange = { onToggleAlert(alert, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DeepObsidian,
                                    checkedTrackColor = EmeraldBullishSoft
                                )
                            )
                            IconButton(onClick = { onDeleteAlert(alert.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Alert",
                                    tint = SoftRedBearishLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // Institutional Methodology & Disclaimer Card
        item {
            LiquidGlassCard(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SilverNeutral,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Statistical Transparency & Risk Disclosure",
                            style = MaterialTheme.typography.titleSmall,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Probabilities (Chance Up, Chance Down, Chance Sideways = 100%) are statistical estimates derived from walk-forward backtested technical, fundamental, sentiment, and macroeconomic indicators. Predictions are probabilistic models, not guarantees of financial return.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray
                    )
                }
            }
        }
    }
}
