package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MarketFilter
import com.example.data.model.StockQuote
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.theme.CharcoalBase
import com.example.ui.theme.CoolGray
import com.example.ui.theme.EmeraldBullishSoft
import com.example.ui.theme.GlassSpecularBright
import com.example.ui.theme.GlassSpecularSubtle
import com.example.ui.theme.IcyBlueAccent
import com.example.ui.theme.SilverNeutral
import com.example.ui.theme.SoftWhite

private val GLOBAL_COUNTRIES = listOf(
    "All Countries",
    "United States",
    "Indonesia",
    "United Kingdom",
    "Japan",
    "Hong Kong",
    "China",
    "South Korea",
    "India",
    "Germany",
    "France / Europe",
    "Australia",
    "Canada",
    "Singapore"
)

private val GLOBAL_EXCHANGES = listOf(
    "All Exchanges",
    "NASDAQ",
    "NYSE",
    "IDX",
    "LSE",
    "TSE",
    "HKEX",
    "SZSE",
    "KRX",
    "NSE",
    "XETRA",
    "Euronext",
    "ASX",
    "TSX",
    "SGX"
)

@Composable
fun MarketsScreen(
    stocks: List<StockQuote>,
    watchlistTickers: Set<String>,
    searchQuery: String,
    selectedCountry: String,
    selectedExchange: String,
    selectedFilter: MarketFilter,
    onSearchQueryChanged: (String) -> Unit,
    onCountrySelected: (String) -> Unit,
    onExchangeSelected: (String) -> Unit,
    onFilterSelected: (MarketFilter) -> Unit,
    onStockClick: (StockQuote) -> Unit,
    onToggleWatchlist: (StockQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("markets_screen_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = IcyBlueAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Global Stock Market Directory",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Liquid Glass Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.search_stocks_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CoolGray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = IcyBlueAccent
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = CoolGray
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CharcoalBase.copy(alpha = 0.85f),
                        unfocusedContainerColor = CharcoalBase.copy(alpha = 0.65f),
                        focusedBorderColor = GlassSpecularBright,
                        unfocusedBorderColor = GlassSpecularSubtle,
                        focusedTextColor = SoftWhite,
                        unfocusedTextColor = SoftWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_stock_search_input")
                )
            }
        }

        // Country Filter Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GLOBAL_COUNTRIES, key = { it }) { country ->
                    LiquidGlassPill(
                        text = country,
                        selected = selectedCountry == country,
                        accentColor = IcyBlueAccent,
                        onClick = { onCountrySelected(country) }
                    )
                }
            }
        }

        // Exchange Filter Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GLOBAL_EXCHANGES, key = { it }) { exchange ->
                    LiquidGlassPill(
                        text = exchange,
                        selected = selectedExchange == exchange,
                        accentColor = EmeraldBullishSoft,
                        onClick = { onExchangeSelected(exchange) }
                    )
                }
            }
        }

        // 9 Quantitative Smart Filters Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MarketFilter.entries, key = { it.name }) { filter ->
                    LiquidGlassPill(
                        text = filter.label,
                        selected = selectedFilter == filter,
                        accentColor = IcyBlueAccent,
                        onClick = { onFilterSelected(filter) }
                    )
                }
            }
        }

        // Data Transparency & Coverage Banner
        item {
            Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                LiquidGlassCard(
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = IcyBlueAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Showing ${stocks.size} matching global equities. Live Yahoo Finance global search is active—type any global ticker (e.g. AMD, BBRI.JK, 9988.HK, SIE.DE) to fetch & analyze.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverNeutral
                        )
                    }
                }
            }
        }

        if (stocks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No matching stocks for current filters",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoftWhite
                        )
                        Text(
                            text = "Try resetting country/exchange filters or typing a ticker symbol to search global exchanges.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolGray
                        )
                    }
                }
            }
        } else {
            items(stocks, key = { "mkt_${it.ticker}" }) { stock ->
                StockOpportunityGlassCard(
                    stock = stock,
                    isSaved = stock.ticker in watchlistTickers,
                    onClick = { onStockClick(stock) },
                    onToggleWatchlist = { onToggleWatchlist(stock) },
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }
    }
}
