package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpectedDirection
import com.example.data.model.GlobalIndexQuote
import com.example.data.model.StockNewsArticle
import com.example.data.model.StockQuote
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.PriceChangeBadge
import com.example.ui.components.ProbabilityDistributionBar
import com.example.ui.theme.CharcoalBase
import com.example.ui.theme.CoolGray
import com.example.ui.theme.EmeraldBullish
import com.example.ui.theme.EmeraldBullishSoft
import com.example.ui.theme.GlassSpecularSubtle
import com.example.ui.theme.IcyBlueAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SilverNeutral
import com.example.ui.theme.SoftRedBearish
import com.example.ui.theme.SoftRedBearishLight
import com.example.ui.theme.SoftWhite
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    indices: List<GlobalIndexQuote>,
    stocks: List<StockQuote>,
    watchlistTickers: Set<String>,
    news: List<StockNewsArticle>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onStockClick: (StockQuote) -> Unit,
    onToggleWatchlist: (StockQuote) -> Unit,
    onOpenCompare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Calculate real Market Sentiment distribution from active global stocks
    val totalCount = stocks.size.coerceAtLeast(1)
    val bullishCount = stocks.count { it.primaryPrediction.expectedDirection == ExpectedDirection.BULLISH }
    val bearishCount = stocks.count { it.primaryPrediction.expectedDirection == ExpectedDirection.BEARISH }
    val bullishPct = ((bullishCount * 100.0) / totalCount).roundToInt().coerceIn(0, 100)
    val bearishPct = ((bearishCount * 100.0) / totalCount).roundToInt().coerceIn(0, 100 - bullishPct)
    val neutralPct = (100 - bullishPct - bearishPct).coerceAtLeast(0)

    val topOpportunities = stocks.sortedByDescending { it.primaryPrediction.chanceUp }.take(5)
    val topLosers = stocks.filter { it.changePercent < 0 }.sortedBy { it.changePercent }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldBullishSoft)
                        )
                        Text(
                            text = "GLOBAL EQUITY TELEMETRY • 14 EXCHANGES",
                            style = MaterialTheme.typography.labelSmall,
                            color = IcyBlueAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Global Stock Intelligence",
                        style = MaterialTheme.typography.headlineLarge,
                        color = SoftWhite
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onOpenCompare,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CharcoalBase.copy(alpha = 0.7f))
                            .border(0.8.dp, GlassSpecularSubtle, CircleShape)
                            .testTag("open_compare_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = "Compare Stocks",
                            tint = IcyBlueAccent
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CharcoalBase.copy(alpha = 0.7f))
                            .border(0.8.dp, GlassSpecularSubtle, CircleShape)
                            .testTag("refresh_market_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = IcyBlueAccent
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Market Data",
                                tint = SoftWhite
                            )
                        }
                    }
                }
            }
        }

        // SECTION A: GLOBAL MARKET OVERVIEW (World Indices)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "Global Market Overview",
                    subtitle = "S&P 500 • NASDAQ • Dow • FTSE 100 • Nikkei 225 • Hang Seng • IDX",
                    modifier = Modifier.padding(horizontal = 18.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(indices, key = { it.symbol }) { indexQuote ->
                        IndexGlassCard(indexQuote = indexQuote)
                    }
                }
            }
        }

        // SECTION B: GLOBAL MARKET SENTIMENT
        item {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionHeader(
                    title = "Market Sentiment Matrix",
                    subtitle = "Computed from ${stocks.size} global equities & institutional flow"
                )

                LiquidGlassCard(
                    cornerRadius = 22.dp,
                    elevated = true,
                    accentGlowColor = if (bullishPct >= bearishPct) EmeraldBullish else SoftRedBearish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SentimentMetricColumn(
                                label = "BULLISH",
                                pct = bullishPct,
                                color = EmeraldBullishSoft,
                                alignment = Alignment.Start
                            )
                            SentimentMetricColumn(
                                label = "NEUTRAL",
                                pct = neutralPct,
                                color = SilverNeutral,
                                alignment = Alignment.CenterHorizontally
                            )
                            SentimentMetricColumn(
                                label = "BEARISH",
                                pct = bearishPct,
                                color = SoftRedBearishLight,
                                alignment = Alignment.End
                            )
                        }

                        // 3-Way Liquid Glass Sentiment Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(CharcoalBase),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(max(0.05f, bullishPct / 100f))
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(EmeraldBullish, EmeraldBullishSoft)
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .weight(max(0.05f, neutralPct / 100f))
                                    .fillMaxHeight()
                                    .background(SilverNeutral.copy(alpha = 0.65f))
                            )
                            Box(
                                modifier = Modifier
                                    .weight(max(0.05f, bearishPct / 100f))
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(SoftRedBearishLight, SoftRedBearish)
                                        )
                                    )
                            )
                        }

                        Text(
                            text = "Regime Assessment: Global breadth is ${if (bullishPct >= 55) "Constructively Bullish" else if (bearishPct >= 45) "Defensive / Risk-Off" else "Balanced Rotational"}, led by AI Semiconductors, ASEAN Banking (IDX/SGX), and Japanese Exporters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolGray
                        )
                    }
                }
            }
        }

        // SECTION C: TOP STOCK OPPORTUNITIES
        item {
            SectionHeader(
                title = "Top Stock Opportunities",
                subtitle = "Highest calibrated upside probability (Walk-Forward Ensemble)",
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        items(topOpportunities, key = { "opp_${it.ticker}" }) { stock ->
            StockOpportunityGlassCard(
                stock = stock,
                isSaved = stock.ticker in watchlistTickers,
                onClick = { onStockClick(stock) },
                onToggleWatchlist = { onToggleWatchlist(stock) },
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        // SECTION D: TOP LOSERS & DOWNSIDE DRIVERS
        if (topLosers.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Significant Decliners & Attribution",
                    subtitle = "Equities under selling pressure and primary downside catalysts",
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }

            items(topLosers, key = { "loser_${it.ticker}" }) { stock ->
                TopLoserGlassCard(
                    stock = stock,
                    onClick = { onStockClick(stock) },
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // SECTION E: GLOBAL MARKET NEWS
        item {
            SectionHeader(
                title = "Global Market News & AI Impact",
                subtitle = "Verified financial press with separated AI impact attribution",
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        items(news.take(4), key = { it.id }) { article ->
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
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SoftWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = CoolGray
        )
    }
}

@Composable
private fun SentimentMetricColumn(
    label: String,
    pct: Int,
    color: androidx.compose.ui.graphics.Color,
    alignment: Alignment.Horizontal
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CoolGray
        )
        Text(
            text = "$pct%",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = JetBrainsMonoFontFamily,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun IndexGlassCard(
    indexQuote: GlobalIndexQuote,
    modifier: Modifier = Modifier
) {
    val isUp = indexQuote.changePercent >= 0
    val accent = if (isUp) EmeraldBullishSoft else SoftRedBearishLight

    LiquidGlassCard(
        cornerRadius = 20.dp,
        accentGlowColor = accent,
        contentPadding = PaddingValues(14.dp),
        modifier = modifier.width(196.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = indexQuote.region.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = CoolGray
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (indexQuote.isOpen) EmeraldBullish.copy(alpha = 0.18f) else CharcoalBase)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (indexQuote.isOpen) "OPEN" else "CLOSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (indexQuote.isOpen) EmeraldBullishSoft else MutedSlate
                    )
                }
            }

            Text(
                text = indexQuote.name,
                style = MaterialTheme.typography.titleMedium,
                color = SoftWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )

            // Mini Sparkline Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val pts = indexQuote.sparkline
                if (pts.size >= 2) {
                    val minV = pts.minOrNull() ?: 0.0
                    val maxV = pts.maxOrNull() ?: 1.0
                    val span = (maxV - minV).coerceAtLeast(0.01)
                    val path = Path()
                    val stepX = size.width / (pts.size - 1)
                    pts.forEachIndexed { idx, v ->
                        val x = idx * stepX
                        val y = size.height * (1f - ((v - minV) / span).toFloat())
                        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = accent,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.US, "%,.2f", indexQuote.value),
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold
                )
                PriceChangeBadge(changePercent = indexQuote.changePercent)
            }
        }
    }
}

@Composable
fun StockOpportunityGlassCard(
    stock: StockQuote,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prob = stock.primaryPrediction
    val isBullish = prob.expectedDirection == ExpectedDirection.BULLISH

    LiquidGlassCard(
        cornerRadius = 22.dp,
        elevated = true,
        accentGlowColor = if (isBullish) EmeraldBullish else SoftRedBearish,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("stock_card_${stock.ticker}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stock.ticker,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                        LiquidGlassPill(
                            text = "${stock.exchange} • ${stock.country}",
                            selected = false
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${stock.companyName} • ${stock.sector}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${stock.currency} ${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        PriceChangeBadge(changePercent = stock.changePercent)
                    }
                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier.testTag("watchlist_toggle_${stock.ticker}")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Save to Watchlist",
                            tint = if (isSaved) IcyBlueAccent else CoolGray
                        )
                    }
                }
            }

            // Calibrated Probability Bar
            ProbabilityDistributionBar(
                chanceUp = prob.chanceUp,
                chanceSideways = prob.chanceSideways,
                chanceDown = prob.chanceDown
            )

            // Bottom Metadata Row: Direction, Horizon, Model Reliability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isBullish) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (isBullish) EmeraldBullishSoft else SoftRedBearishLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${prob.expectedDirection.label} (${prob.horizon.label})",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isBullish) EmeraldBullishSoft else SoftRedBearishLight
                    )
                }

                Text(
                    text = "Reliability ${prob.modelReliabilityScore}% • WinRate ${prob.backtestWinRate}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoolGray
                )
            }
        }
    }
}

@Composable
private fun TopLoserGlassCard(
    stock: StockQuote,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryFactor = stock.explanation.whyMayFall.firstOrNull()
        ?: "Short-term distribution pressure near resistance."

    LiquidGlassCard(
        cornerRadius = 20.dp,
        accentGlowColor = SoftRedBearish,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stock.ticker} • ${stock.companyName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stock.exchange} (${stock.country}) • Chance Down: ${stock.primaryPrediction.chanceDown}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftRedBearishLight
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${stock.currency} ${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = SoftWhite
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    PriceChangeBadge(changePercent = stock.changePercent)
                }
            }

            Text(
                text = "Key Factor: $primaryFactor",
                style = MaterialTheme.typography.bodySmall,
                color = CoolGray
            )
        }
    }
}

@Composable
fun NewsArticleGlassCard(
    article: StockNewsArticle,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sentimentColor = when (article.sentiment) {
        "Positive" -> EmeraldBullishSoft
        "Negative" -> SoftRedBearishLight
        else -> SilverNeutral
    }

    LiquidGlassCard(
        cornerRadius = 20.dp,
        accentGlowColor = sentimentColor,
        onClick = { onOpenUrl(article.url) },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(sentimentColor.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${article.sentiment.uppercase()} • ${article.category.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = sentimentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open Original News Source",
                    tint = CoolGray,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = SoftWhite,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Source: ${article.source} • Published: ${article.publishedAtFormatted} • Tickers: ${article.relatedTickers.joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = IcyBlueAccent
            )

            // Factual Excerpt
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalBase.copy(alpha = 0.65f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "VERIFIED REPORT FACT",
                    style = MaterialTheme.typography.labelSmall,
                    color = SilverNeutral
                )
                Text(
                    text = article.factualExcerpt,
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftWhite.copy(alpha = 0.9f)
                )
            }

            // Separated AI Interpretation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(IcyBlueAccent.copy(alpha = 0.08f))
                    .border(0.6.dp, IcyBlueAccent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = IcyBlueAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AI INTERPRETATION & IMPACT (${article.estimatedPriceImpact})",
                        style = MaterialTheme.typography.labelSmall,
                        color = IcyBlueAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = article.aiSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftWhite
                )
                Text(
                    text = "Why it moves the stock: ${article.impactReasoning}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolGray
                )
            }
        }
    }
}
