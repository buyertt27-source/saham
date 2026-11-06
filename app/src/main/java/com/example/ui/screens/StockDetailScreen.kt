package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChartTimeframe
import com.example.data.model.ExpectedDirection
import com.example.data.model.PredictionHorizon
import com.example.data.model.StockNewsArticle
import com.example.data.model.StockQuote
import com.example.ui.components.InteractiveLiquidGlassChart
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.PriceChangeBadge
import com.example.ui.components.ProbabilityDistributionBar
import com.example.ui.theme.AmberWarning
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
import com.example.viewmodel.AiInsightState
import java.util.Locale

private val PRESET_AI_QUESTIONS = listOf(
    "Why is this stock going up?",
    "Why is this stock going down?",
    "What happened to this company?",
    "What could happen next?",
    "What are the biggest risks?"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StockDetailScreen(
    stock: StockQuote,
    isSavedInWatchlist: Boolean,
    selectedTimeframe: ChartTimeframe,
    selectedHorizon: PredictionHorizon,
    relatedNews: List<StockNewsArticle>,
    aiState: AiInsightState,
    onBack: () -> Unit,
    onSelectTimeframe: (ChartTimeframe) -> Unit,
    onSelectHorizon: (PredictionHorizon) -> Unit,
    onToggleWatchlist: () -> Unit,
    onCreateQuickAlert: () -> Unit,
    onTriggerAiDeepDive: () -> Unit,
    onAskAiQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val activeProb = stock.horizons[selectedHorizon] ?: stock.primaryPrediction
    val isBullish = activeProb.expectedDirection == ExpectedDirection.BULLISH

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stock_detail_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. COMPANY NAME, TICKER & DATA TRANSPARENCY HEADER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CharcoalBase.copy(alpha = 0.75f))
                            .border(0.8.dp, GlassSpecularSubtle, CircleShape)
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SoftWhite
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onCreateQuickAlert,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CharcoalBase.copy(alpha = 0.75f))
                                .border(0.8.dp, GlassSpecularSubtle, CircleShape)
                                .testTag("detail_alert_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Create Alert for ${stock.ticker}",
                                tint = IcyBlueAccent
                            )
                        }

                        IconButton(
                            onClick = onToggleWatchlist,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CharcoalBase.copy(alpha = 0.75f))
                                .border(0.8.dp, GlassSpecularSubtle, CircleShape)
                                .testTag("detail_watchlist_button")
                        ) {
                            Icon(
                                imageVector = if (isSavedInWatchlist) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Toggle Watchlist",
                                tint = if (isSavedInWatchlist) IcyBlueAccent else SoftWhite
                            )
                        }
                    }
                }

                // Company Name & Ticker + Exchange Metadata
                LiquidGlassCard(
                    cornerRadius = 24.dp,
                    elevated = true,
                    accentGlowColor = if (stock.changePercent >= 0) EmeraldBullish else SoftRedBearish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LiquidGlassPill(
                                text = "${stock.exchange} • ${stock.country}",
                                selected = true,
                                accentColor = IcyBlueAccent
                            )
                            Text(
                                text = stock.isRealTimeOrDelayed,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (stock.isLiveApiData) EmeraldBullishSoft else AmberWarning
                            )
                        }

                        Text(
                            text = "${stock.companyName} (${stock.ticker})",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )

                        // 2 & 3. CURRENT PRICE & DAILY CHANGE
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${stock.currency} ${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                                style = MaterialTheme.typography.displayMedium,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = SoftWhite,
                                fontWeight = FontWeight.Bold
                            )
                            PriceChangeBadge(changePercent = stock.changePercent)
                            Text(
                                text = "${if (stock.changeAmount >= 0) "+" else ""}${String.format(Locale.US, "%.2f", stock.changeAmount)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = if (stock.changeAmount >= 0) EmeraldBullishSoft else SoftRedBearishLight
                            )
                        }

                        Text(
                            text = "Source: ${stock.dataSource} • Updated: ${stock.lastUpdatedFormatted} • Sector: ${stock.sector}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoolGray
                        )
                    }
                }
            }
        }

        // 4 & 5. INTERACTIVE PRICE MOVEMENT CHART & TIMEFRAME SELECTOR (1D, 1W, 1M, 3M, 6M, 1Y, 5Y, MAX)
        item {
            LiquidGlassCard(
                cornerRadius = 24.dp,
                elevated = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InteractiveLiquidGlassChart(
                        bars = stock.priceHistory,
                        currency = stock.currency,
                        isPositive = stock.changePercent >= 0,
                        supportLevel = stock.technicals.supportLevel1,
                        resistanceLevel = stock.technicals.resistanceLevel1,
                        ema20 = stock.technicals.ema20,
                        ema50 = stock.technicals.ema50
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ChartTimeframe.entries, key = { it.name }) { tf ->
                            LiquidGlassPill(
                                text = tf.label,
                                selected = tf == selectedTimeframe,
                                accentColor = IcyBlueAccent,
                                onClick = { onSelectTimeframe(tf) }
                            )
                        }
                    }
                }
            }
        }

        // 6. STATISTICAL & WALK-FORWARD PROBABILITY ENGINE (All 6 Horizons)
        item {
            LiquidGlassCard(
                cornerRadius = 24.dp,
                elevated = true,
                accentGlowColor = if (isBullish) EmeraldBullish else SoftRedBearish,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AI & Statistical Probability Engine",
                                style = MaterialTheme.typography.titleLarge,
                                color = SoftWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Walk-Forward Calibrated • Up + Down + Sideways = 100%",
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolGray
                            )
                        }
                        Icon(
                            imageVector = if (isBullish) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isBullish) EmeraldBullishSoft else SoftRedBearishLight,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Horizon Selector (1 Day, 5 Days, 1 Month, 3 Months, 6 Months, 1 Year)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PredictionHorizon.entries, key = { it.name }) { horizon ->
                            LiquidGlassPill(
                                text = horizon.label,
                                selected = horizon == selectedHorizon,
                                accentColor = if (isBullish) EmeraldBullishSoft else IcyBlueAccent,
                                onClick = { onSelectHorizon(horizon) }
                            )
                        }
                    }

                    if (!activeProb.hasSufficientData) {
                        Text(
                            text = "Insufficient Data (<15 historical bars) — Model refuses to fabricate probabilities.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberWarning
                        )
                    } else {
                        ProbabilityDistributionBar(
                            chanceUp = activeProb.chanceUp,
                            chanceSideways = activeProb.chanceSideways,
                            chanceDown = activeProb.chanceDown
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricMiniBox(
                                label = "Expected Direction",
                                value = activeProb.expectedDirection.label,
                                valueColor = if (isBullish) EmeraldBullishSoft else SoftRedBearishLight,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            MetricMiniBox(
                                label = "Model Reliability",
                                value = "${activeProb.modelReliabilityScore}% Confidence",
                                valueColor = IcyBlueAccent,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            MetricMiniBox(
                                label = "Walk-Forward Win",
                                value = "${activeProb.backtestWinRate}% (Brier ${activeProb.brierCalibrationScore})",
                                valueColor = SilverNeutral,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // All 6 Horizons Comparison Table
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharcoalBase.copy(alpha = 0.65f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "MULTI-HORIZON PROBABILITY TERM STRUCTURE",
                                style = MaterialTheme.typography.labelSmall,
                                color = IcyBlueAccent
                            )
                            PredictionHorizon.entries.forEach { h ->
                                val hp = stock.horizons[h]
                                if (hp != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = h.label,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (h == selectedHorizon) SoftWhite else CoolGray,
                                            fontWeight = if (h == selectedHorizon) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.width(80.dp)
                                        )
                                        Text(
                                            text = "Up ${hp.chanceUp}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EmeraldBullishSoft
                                        )
                                        Text(
                                            text = "Side ${hp.chanceSideways}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SilverNeutral
                                        )
                                        Text(
                                            text = "Down ${hp.chanceDown}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = SoftRedBearishLight
                                        )
                                        Text(
                                            text = hp.expectedDirection.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (hp.expectedDirection == ExpectedDirection.BULLISH) EmeraldBullishSoft else SoftRedBearishLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. AI PREDICTION & INTERACTIVE Q&A (Firebase AI Logic / Gemini 3.8 Flash)
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = IcyBlueAccent
                            )
                            Column {
                                Text(
                                    text = "AI Institutional Analyst (Gemini 3.8)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap any question or run full AI Deep Dive on ${stock.ticker}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CoolGray
                                )
                            }
                        }

                        Button(
                            onClick = onTriggerAiDeepDive,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IcyBlueAccent,
                                contentColor = DeepObsidian
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("run_ai_deep_dive_button")
                        ) {
                            Text(
                                text = "Deep Dive",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 5 Required AI Questions
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PRESET_AI_QUESTIONS.forEach { q ->
                            LiquidGlassPill(
                                text = q,
                                selected = (aiState as? AiInsightState.QuestionAnswered)?.question == q,
                                accentColor = IcyBlueAccent,
                                onClick = { onAskAiQuestion(q) }
                            )
                        }
                    }

                    when (aiState) {
                        is AiInsightState.Idle -> {
                            Text(
                                text = "Select a question above or tap 'Deep Dive' to generate live AI attribution backed by ${stock.ticker}'s technical, fundamental, and news telemetry.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CoolGray
                            )
                        }
                        is AiInsightState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = IcyBlueAccent
                                )
                                Text(
                                    text = "Synthesizing live market telemetry & news with Gemini 3.8...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftWhite
                                )
                            }
                        }
                        is AiInsightState.Streaming -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CharcoalBase.copy(alpha = 0.75f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = aiState.partialAnswer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftWhite
                                )
                            }
                        }
                        is AiInsightState.QuestionAnswered -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CharcoalBase.copy(alpha = 0.75f))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Q: ${aiState.question}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = IcyBlueAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = aiState.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftWhite
                                )
                            }
                        }
                        is AiInsightState.DeepDiveReady -> {
                            val dd = aiState.response
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CharcoalBase.copy(alpha = 0.75f))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "WHY ${dd.ticker} IS MOVING (${dd.generatedTimestamp})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IcyBlueAccent
                                )
                                Text(dd.whyGoingUpOrDown, style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                                Text("WHAT HAPPENED TO THE COMPANY", style = MaterialTheme.typography.labelSmall, color = EmeraldBullishSoft)
                                Text(dd.whatHappenedToCompany, style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                                Text("WHAT COULD HAPPEN NEXT", style = MaterialTheme.typography.labelSmall, color = SilverNeutral)
                                Text(dd.whatCouldHappenNext, style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                                if (dd.biggestRisks.isNotEmpty()) {
                                    Text("BIGGEST RISKS", style = MaterialTheme.typography.labelSmall, color = SoftRedBearishLight)
                                    dd.biggestRisks.forEach { r ->
                                        Text("• $r", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                                    }
                                }
                            }
                        }
                        is AiInsightState.Error -> {
                            Text(
                                text = "AI Notice: ${aiState.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftRedBearishLight
                            )
                        }
                    }
                }
            }
        }

        // 10. BULLISH & BEARISH FACTORS (WHY THIS STOCK MAY RISE / FALL)
        item {
            LiquidGlassCard(
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "WHY THIS STOCK MAY RISE (Bullish Factors)",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldBullishSoft,
                        fontWeight = FontWeight.Bold
                    )
                    stock.explanation.whyMayRise.forEach { reason ->
                        FactorBulletRow(text = reason, iconColor = EmeraldBullishSoft, isBullish = true)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "WHY THIS STOCK MAY FALL (Bearish Factors)",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftRedBearishLight,
                        fontWeight = FontWeight.Bold
                    )
                    stock.explanation.whyMayFall.forEach { reason ->
                        FactorBulletRow(text = reason, iconColor = SoftRedBearishLight, isBullish = false)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "MOST INFLUENTIAL INDICATORS",
                        style = MaterialTheme.typography.labelMedium,
                        color = IcyBlueAccent
                    )
                    stock.explanation.mostInfluentialIndicators.forEach { ind ->
                        Text("• $ind", style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                    }
                }
            }
        }

        // 8 & 11. TECHNICAL INDICATORS & SUPPORT / RESISTANCE
        item {
            val t = stock.technicals
            LiquidGlassCard(
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Technical Indicators & Support/Resistance",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Structure: ${t.priceActionStructure} • Momentum Score: ${t.momentumScore}/100 • ADX Trend: ${t.trendStrengthAdx}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IcyBlueAccent
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricMiniBox("RSI (14)", "${t.rsi14} (${t.rsiSignal.substringBefore(" ")})", SoftWhite, Modifier.weight(1f))
                        MetricMiniBox("MACD (12,26,9)", "${t.macdLine} / Hist ${t.macdHistogram}", if (t.macdHistogram >= 0) EmeraldBullishSoft else SoftRedBearishLight, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricMiniBox("EMA 20 / 50", "${t.ema20} / ${t.ema50}", SoftWhite, Modifier.weight(1f))
                        MetricMiniBox("EMA 100 / 200", "${t.ema100} / ${t.ema200}", SoftWhite, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricMiniBox("Bollinger Bands", "${t.bollingerLower} .. ${t.bollingerUpper}", SilverNeutral, Modifier.weight(1f))
                        MetricMiniBox("Stochastic %K/%D", "${t.stochasticK} / ${t.stochasticD}", SoftWhite, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricMiniBox("Support S1 / S2", "${stock.currency} ${t.supportLevel1} / ${t.supportLevel2}", EmeraldBullishSoft, Modifier.weight(1f))
                        MetricMiniBox("Resistance R1 / R2", "${stock.currency} ${t.resistanceLevel1} / ${t.resistanceLevel2}", SoftRedBearishLight, Modifier.weight(1f))
                    }
                }
            }
        }

        // 9. FUNDAMENTAL INDICATORS & MACRO SENTIMENT
        item {
            val f = stock.fundamentals
            val sm = stock.sentimentMacro
            LiquidGlassCard(
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Fundamental & Macroeconomic Analysis",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricMiniBox("P/E Ratio & EPS", "${f.peRatio}x • EPS ${f.eps}", SoftWhite, Modifier.weight(1f))
                        MetricMiniBox("YoY Rev Growth", "+${f.revenueGrowthYoY}%", EmeraldBullishSoft, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricMiniBox("ROE / ROA", "${f.roePercent}% / ${f.roaPercent}%", SoftWhite, Modifier.weight(1f))
                        MetricMiniBox("Debt-to-Equity", "${f.debtToEquity}x", SilverNeutral, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricMiniBox("Free Cash Flow", "$${f.freeCashFlowBillions}B (Net Inc $${f.netIncomeBillions}B)", EmeraldBullishSoft, Modifier.weight(1f))
                        MetricMiniBox("Market Cap & Div", "$${f.marketCapBillions}B • Yield ${f.dividendYieldPercent}%", IcyBlueAccent, Modifier.weight(1f))
                    }

                    Text(
                        text = "Earnings Report: ${f.latestEarningsSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftWhite
                    )
                    Text(
                        text = "Analyst Consensus: ${sm.analystConsensus} (Target ${stock.currency} ${sm.analystTargetPrice}) • Institutional: ${sm.institutionalActivityStatus}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IcyBlueAccent
                    )
                    Text(
                        text = "Macro & Central Bank: ${sm.centralBankPolicyNote} • Geopolitical Risk: ${sm.geopoliticalRiskLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray
                    )
                }
            }
        }

        // 13 & 14. RISK ASSESSMENT, SCENARIOS & INVALIDATION CONDITIONS
        item {
            val exp = stock.explanation
            LiquidGlassCard(
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Risk Assessment & Scenario Modeling",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text("OPTIMISTIC SCENARIO", style = MaterialTheme.typography.labelSmall, color = EmeraldBullishSoft)
                    Text(exp.optimisticScenario, style = MaterialTheme.typography.bodySmall, color = SoftWhite)

                    Text("PESSIMISTIC SCENARIO", style = MaterialTheme.typography.labelSmall, color = SoftRedBearishLight)
                    Text(exp.pessimisticScenario, style = MaterialTheme.typography.bodySmall, color = SoftWhite)

                    Text("PREDICTION INVALIDATION CONDITIONS", style = MaterialTheme.typography.labelSmall, color = AmberWarning)
                    exp.invalidationConditions.forEach { cond ->
                        Text("• $cond", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                    }
                }
            }
        }

        // 12. NEWS ANALYSIS PER STOCK
        item {
            SectionHeader(
                title = "${stock.ticker} News & Factor Attribution",
                subtitle = "Verified headlines directly impacting ${stock.companyName}",
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        items(relatedNews, key = { "detail_news_${it.id}" }) { article ->
            NewsArticleGlassCard(
                article = article,
                onOpenUrl = { url ->
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) {}
                },
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }
    }
}

@Composable
private fun MetricMiniBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CharcoalBase.copy(alpha = 0.7f))
            .border(0.6.dp, GlassSpecularSubtle, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CoolGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = JetBrainsMonoFontFamily,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FactorBulletRow(
    text: String,
    iconColor: Color,
    isBullish: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isBullish) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = SoftWhite
        )
    }
}
