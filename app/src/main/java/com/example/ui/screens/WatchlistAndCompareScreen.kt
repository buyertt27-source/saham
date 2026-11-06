package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StockQuote
import com.example.data.model.WatchlistItem
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
import com.example.ui.theme.LiquidCyanRefraction
import com.example.ui.theme.SilverNeutral
import com.example.ui.theme.SoftRedBearish
import com.example.ui.theme.SoftRedBearishLight
import com.example.ui.theme.SoftWhite
import com.example.viewmodel.UiState
import java.util.Locale

private val COMPARE_LINE_COLORS = listOf(
    EmeraldBullishSoft,
    IcyBlueAccent,
    AmberWarning,
    SoftRedBearishLight
)

@Composable
fun WatchlistAndCompareScreen(
    watchlistState: UiState<List<WatchlistItem>>,
    allStocks: List<StockQuote>,
    comparisonTickers: List<String>,
    onToggleComparisonTicker: (String) -> Unit,
    onStockClick: (StockQuote) -> Unit,
    onMoveWatchlistItem: (WatchlistItem, Boolean) -> Unit,
    onRemoveWatchlistItem: (String) -> Unit,
    onAddSuggestedStock: (StockQuote) -> Unit,
    onCreateAlertForStock: (StockQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    val comparedStocks = allStocks.filter { it.ticker in comparisonTickers }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("watchlist_screen_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
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
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = IcyBlueAccent
                    )
                    Text(
                        text = "Cloud-Synced Watchlist & Comparison",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Synced securely to your Google account via Firestore Enterprise • Reorder, compare, and set probability alerts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolGray
                )
            }
        }

        // Watchlist Content
        when (watchlistState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = IcyBlueAccent)
                    }
                }
            }
            is UiState.Error -> {
                item {
                    LiquidGlassCard(
                        cornerRadius = 18.dp,
                        accentGlowColor = SoftRedBearish,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    ) {
                        Text(
                            text = watchlistState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftRedBearishLight
                        )
                    }
                }
            }
            is UiState.Success -> {
                val items = watchlistState.data
                if (items.isEmpty()) {
                    item {
                        LiquidGlassCard(
                            cornerRadius = 22.dp,
                            elevated = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Your Cloud Watchlist is Empty",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Quick-add institutional leaders below to track real-time probabilities and sync across devices:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CoolGray
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(allStocks.take(6), key = { "suggest_${it.ticker}" }) { stock ->
                                        Button(
                                            onClick = { onAddSuggestedStock(stock) },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = IcyBlueAccent,
                                                contentColor = DeepObsidian
                                            )
                                        ) {
                                            Text(
                                                text = "+ ${stock.ticker}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(items, key = { it.id.ifBlank { it.ticker } }) { item ->
                        val matchingStock = allStocks.find { it.ticker.equals(item.ticker, ignoreCase = true) }
                        WatchlistItemGlassCard(
                            item = item,
                            onClick = {
                                if (matchingStock != null) onStockClick(matchingStock)
                            },
                            onMoveUp = { onMoveWatchlistItem(item, true) },
                            onMoveDown = { onMoveWatchlistItem(item, false) },
                            onRemove = { onRemoveWatchlistItem(item.id.ifBlank { item.ticker }) },
                            onCreateAlert = {
                                if (matchingStock != null) onCreateAlertForStock(matchingStock)
                            },
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                }
            }
        }

        // STOCK COMPARISON MATRIX SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = null,
                        tint = EmeraldBullishSoft
                    )
                    Column {
                        Text(
                            text = "Multi-Stock Comparison Matrix",
                            style = MaterialTheme.typography.titleLarge,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select 2 to 4 global stocks to compare normalized performance, valuation, risk & AI probabilities",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolGray
                        )
                    }
                }

                // Ticker Selection Row for Comparison
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allStocks, key = { "cmp_pick_${it.ticker}" }) { stock ->
                        val isSelected = stock.ticker in comparisonTickers
                        LiquidGlassPill(
                            text = stock.ticker,
                            selected = isSelected,
                            accentColor = EmeraldBullishSoft,
                            onClick = { onToggleComparisonTicker(stock.ticker) }
                        )
                    }
                }

                if (comparedStocks.size >= 2) {
                    StockComparisonGlassMatrix(comparedStocks = comparedStocks)
                }
            }
        }
    }
}

@Composable
private fun WatchlistItemGlassCard(
    item: WatchlistItem,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onCreateAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        cornerRadius = 20.dp,
        elevated = true,
        accentGlowColor = if (item.chanceUp >= item.chanceDown) EmeraldBullish else SoftRedBearish,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.ticker} • ${item.exchange}",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.companyName} (${item.country})",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.currency} ${String.format(Locale.US, "%,.2f", item.lastPrice)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    PriceChangeBadge(changePercent = item.changePercent)
                }
            }

            ProbabilityDistributionBar(
                chanceUp = item.chanceUp,
                chanceSideways = item.chanceSideways,
                chanceDown = item.chanceDown
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = CoolGray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = CoolGray, modifier = Modifier.size(16.dp))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onCreateAlert, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Alert", tint = IcyBlueAccent, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = SoftRedBearishLight, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StockComparisonGlassMatrix(
    comparedStocks: List<StockQuote>,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        elevated = true,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Normalized Relative Performance Chart (% Return)",
                style = MaterialTheme.typography.labelLarge,
                color = SoftWhite,
                fontWeight = FontWeight.Bold
            )

            // Legend Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                comparedStocks.forEachIndexed { idx, s ->
                    val color = COMPARE_LINE_COLORS[idx % COMPARE_LINE_COLORS.size]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(
                            text = s.ticker,
                            style = MaterialTheme.typography.labelMedium,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Multi-stock Normalized Return Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalBase.copy(alpha = 0.7f))
                    .padding(10.dp)
            ) {
                val normalizedSeries = comparedStocks.map { stock ->
                    val closes = stock.priceHistory.takeLast(60).map { it.close }
                    val base = closes.firstOrNull()?.coerceAtLeast(0.01) ?: 1.0
                    closes.map { ((it - base) / base) * 100.0 }
                }
                val allVals = normalizedSeries.flatten()
                val minPct = (allVals.minOrNull() ?: -10.0) - 1.0
                val maxPct = (allVals.maxOrNull() ?: 10.0) + 1.0
                val span = (maxPct - minPct).coerceAtLeast(1.0)

                normalizedSeries.forEachIndexed { sIdx, series ->
                    if (series.size >= 2) {
                        val color = COMPARE_LINE_COLORS[sIdx % COMPARE_LINE_COLORS.size]
                        val path = Path()
                        val stepX = size.width / (series.size - 1)
                        series.forEachIndexed { i, pct ->
                            val x = i * stepX
                            val y = size.height * (1f - ((pct - minPct) / span).toFloat())
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Side-by-Side Comparison Table
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Metric Labels Column
                Column(
                    modifier = Modifier.width(125.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("METRIC", style = MaterialTheme.typography.labelSmall, color = IcyBlueAccent)
                    Text("Price & Daily %", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                    Text("AI Direction (1M)", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                    Text("Chance Up / Down", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                    Text("Model Reliability", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                    Text("YoY Rev Growth", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                    Text("Valuation (P/E)", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                    Text("Profitability (ROE)", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                    Text("Risk (Debt/Eq)", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                    Text("RSI-14 & Momentum", style = MaterialTheme.typography.bodySmall, color = CoolGray)
                }

                comparedStocks.forEach { stock ->
                    val p = stock.primaryPrediction
                    Column(
                        modifier = Modifier
                            .width(142.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CharcoalBase.copy(alpha = 0.65f))
                            .border(0.6.dp, GlassSpecularSubtle, RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${stock.ticker} (${stock.exchange})",
                            style = MaterialTheme.typography.labelMedium,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stock.currency} ${stock.currentPrice} (${if (stock.changePercent >= 0) "+" else ""}${stock.changePercent}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (stock.changePercent >= 0) EmeraldBullishSoft else SoftRedBearishLight
                        )
                        Text(
                            text = p.expectedDirection.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (p.chanceUp >= p.chanceDown) EmeraldBullishSoft else SoftRedBearishLight,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "↑${p.chanceUp}% / ↓${p.chanceDown}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftWhite
                        )
                        Text(
                            text = "${p.modelReliabilityScore}% (Win ${p.backtestWinRate}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = IcyBlueAccent
                        )
                        Text(
                            text = "+${stock.fundamentals.revenueGrowthYoY}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldBullishSoft
                        )
                        Text(
                            text = "${stock.fundamentals.peRatio}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftWhite
                        )
                        Text(
                            text = "${stock.fundamentals.roePercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftWhite
                        )
                        Text(
                            text = "D/E ${stock.fundamentals.debtToEquity}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverNeutral
                        )
                        Text(
                            text = "RSI ${stock.technicals.rsi14} (${stock.technicals.momentumScore})",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftWhite
                        )
                    }
                }
            }
        }
    }
}
