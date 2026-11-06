package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiStockDeepDiveResponse
import com.example.data.model.ChartTimeframe
import com.example.data.model.GlobalIndexQuote
import com.example.data.model.MarketFilter
import com.example.data.model.NewsCategory
import com.example.data.model.PredictionHorizon
import com.example.data.model.StockAlert
import com.example.data.model.StockNewsArticle
import com.example.data.model.StockQuote
import com.example.data.model.WatchlistItem
import com.example.data.remote.AiStockIntelligenceService
import com.example.data.remote.GlobalMarketDataService
import com.example.data.repository.FirestoreWatchlistRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiInsightState {
    data object Idle : AiInsightState
    data object Loading : AiInsightState
    data class Streaming(val partialAnswer: String) : AiInsightState
    data class DeepDiveReady(val response: AiStockDeepDiveResponse) : AiInsightState
    data class QuestionAnswered(val question: String, val answer: String) : AiInsightState
    data class Error(val message: String) : AiInsightState
}

@OptIn(FlowPreview::class)
class StockIntelligenceViewModel(
    private val watchlistRepository: FirestoreWatchlistRepository,
    private val currentUserId: String,
    private val marketDataService: GlobalMarketDataService = GlobalMarketDataService(),
    private val aiService: AiStockIntelligenceService = AiStockIntelligenceService()
) : ViewModel() {

    private val _indices = MutableStateFlow<List<GlobalIndexQuote>>(emptyList())
    val indices: StateFlow<List<GlobalIndexQuote>> = _indices.asStateFlow()

    private val _allStocks = MutableStateFlow<List<StockQuote>>(emptyList())
    val allStocks: StateFlow<List<StockQuote>> = _allStocks.asStateFlow()

    private val _isLoadingMarket = MutableStateFlow(true)
    val isLoadingMarket: StateFlow<Boolean> = _isLoadingMarket.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCountry = MutableStateFlow("All Countries")
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    private val _selectedExchange = MutableStateFlow("All Exchanges")
    val selectedExchange: StateFlow<String> = _selectedExchange.asStateFlow()

    private val _selectedFilter = MutableStateFlow(MarketFilter.ALL)
    val selectedFilter: StateFlow<MarketFilter> = _selectedFilter.asStateFlow()

    private val _selectedStock = MutableStateFlow<StockQuote?>(null)
    val selectedStock: StateFlow<StockQuote?> = _selectedStock.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(ChartTimeframe.SIX_MONTHS)
    val selectedTimeframe: StateFlow<ChartTimeframe> = _selectedTimeframe.asStateFlow()

    private val _selectedHorizon = MutableStateFlow(PredictionHorizon.ONE_MONTH)
    val selectedHorizon: StateFlow<PredictionHorizon> = _selectedHorizon.asStateFlow()

    private val _selectedNewsCategory = MutableStateFlow(NewsCategory.ALL)
    val selectedNewsCategory: StateFlow<NewsCategory> = _selectedNewsCategory.asStateFlow()

    private val _comparisonTickers = MutableStateFlow<List<String>>(listOf("NVDA", "BBCA.JK", "7203.T"))
    val comparisonTickers: StateFlow<List<String>> = _comparisonTickers.asStateFlow()

    private val _showComparisonSheet = MutableStateFlow(false)
    val showComparisonSheet: StateFlow<Boolean> = _showComparisonSheet.asStateFlow()

    private val _aiState = MutableStateFlow<AiInsightState>(AiInsightState.Idle)
    val aiState: StateFlow<AiInsightState> = _aiState.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private var aiJob: Job? = null

    // Two-tier Flow error handling for Firestore Watchlist
    val watchlistState: StateFlow<UiState<List<WatchlistItem>>> = watchlistRepository
        .observeWatchlist(currentUserId)
        .map<List<WatchlistItem>, UiState<List<WatchlistItem>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing watchlist", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to observe watchlist"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Two-tier Flow error handling for Firestore Stock Alerts
    val alertsState: StateFlow<UiState<List<StockAlert>>> = watchlistRepository
        .observeAlerts(currentUserId)
        .map<List<StockAlert>, UiState<List<StockAlert>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing alerts", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to observe alerts"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    init {
        refreshGlobalMarketData()
        observeDebouncedSearch()
    }

    private fun observeDebouncedSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(350L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.trim().length >= 2) {
                        val remoteMatches = marketDataService.searchRemoteGlobalStocks(query)
                        if (remoteMatches.isNotEmpty()) {
                            val existingTickers = _allStocks.value.map { it.ticker.uppercase() }.toSet()
                            val newQuotes = remoteMatches
                                .filter { it.ticker.uppercase() !in existingTickers }
                                .take(3)
                                .map { meta ->
                                    async { marketDataService.fetchStockQuoteWithAnalysis(meta) }
                                }.awaitAll()
                            if (newQuotes.isNotEmpty()) {
                                _allStocks.value = _allStocks.value + newQuotes
                            }
                        }
                    }
                }
        }
    }

    fun refreshGlobalMarketData() {
        viewModelScope.launch {
            _isLoadingMarket.value = true
            try {
                val loadedIndices = marketDataService.fetchGlobalIndices()
                _indices.value = loadedIndices

                val quotes = marketDataService.globalCatalog.map { meta ->
                    async {
                        marketDataService.fetchStockQuoteWithAnalysis(meta, _selectedTimeframe.value)
                    }
                }.awaitAll()

                _allStocks.value = quotes
                // Update selectedStock if already open
                _selectedStock.value?.let { current ->
                    quotes.find { it.ticker == current.ticker }?.let { updated ->
                        _selectedStock.value = updated
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed refreshing market data", e)
            } finally {
                _isLoadingMarket.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCountry(country: String) {
        _selectedCountry.value = country
    }

    fun selectExchange(exchange: String) {
        _selectedExchange.value = exchange
    }

    fun selectMarketFilter(filter: MarketFilter) {
        _selectedFilter.value = filter
    }

    fun selectNewsCategory(category: NewsCategory) {
        _selectedNewsCategory.value = category
    }

    fun openStockDetail(stock: StockQuote) {
        _selectedStock.value = stock
        _aiState.value = AiInsightState.Idle
    }

    fun closeStockDetail() {
        _selectedStock.value = null
        _aiState.value = AiInsightState.Idle
    }

    fun selectChartTimeframe(timeframe: ChartTimeframe) {
        _selectedTimeframe.value = timeframe
        val current = _selectedStock.value ?: return
        val meta = marketDataService.globalCatalog.find { it.ticker == current.ticker } ?: return
        viewModelScope.launch {
            val updated = marketDataService.fetchStockQuoteWithAnalysis(meta, timeframe)
            _selectedStock.value = updated
        }
    }

    fun selectPredictionHorizon(horizon: PredictionHorizon) {
        _selectedHorizon.value = horizon
    }

    fun toggleWatchlist(stock: StockQuote) {
        viewModelScope.launch {
            val currentList = (watchlistState.value as? UiState.Success)?.data.orEmpty()
            val existing = currentList.find { it.ticker.equals(stock.ticker, ignoreCase = true) }
            if (existing != null) {
                val res = watchlistRepository.removeWatchlistStock(existing.id.ifBlank { stock.ticker })
                if (res.isSuccess) {
                    _statusBannerMessage.value = "${stock.ticker} removed from Cloud Watchlist"
                } else {
                    _statusBannerMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to remove from Watchlist"
                }
            } else {
                val nextOrder = (currentList.maxOfOrNull { it.sortOrder } ?: -1) + 1
                val res = watchlistRepository.addOrUpdateWatchlistStock(stock, nextOrder)
                if (res.isSuccess) {
                    _statusBannerMessage.value = "${stock.ticker} saved to Cloud Watchlist"
                } else {
                    _statusBannerMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to save to Watchlist"
                }
            }
        }
    }

    fun moveWatchlistItem(item: WatchlistItem, moveUp: Boolean) {
        viewModelScope.launch {
            val currentList = (watchlistState.value as? UiState.Success)?.data.orEmpty().sortedBy { it.sortOrder }
            val idx = currentList.indexOfFirst { it.id == item.id }
            if (idx == -1) return@launch
            val swapIdx = if (moveUp) idx - 1 else idx + 1
            if (swapIdx !in currentList.indices) return@launch
            val neighbor = currentList[swapIdx]
            watchlistRepository.updateWatchlistItemOrder(item, neighbor.sortOrder)
            watchlistRepository.updateWatchlistItemOrder(neighbor, item.sortOrder)
        }
    }

    fun toggleComparisonTicker(ticker: String) {
        val current = _comparisonTickers.value.toMutableList()
        if (current.contains(ticker)) {
            if (current.size > 2) current.remove(ticker)
        } else {
            if (current.size >= 4) current.removeAt(0)
            current.add(ticker)
        }
        _comparisonTickers.value = current
    }

    fun setShowComparisonSheet(show: Boolean) {
        _showComparisonSheet.value = show
    }

    fun createStockAlert(
        stock: StockQuote,
        alertType: String,
        thresholdValue: Double
    ) {
        viewModelScope.launch {
            val res = watchlistRepository.createAlert(
                ticker = stock.ticker,
                companyName = stock.companyName,
                alertType = alertType,
                thresholdValue = thresholdValue
            )
            if (res.isSuccess) {
                _statusBannerMessage.value = "Alert active for ${stock.ticker} ($alertType)"
            } else {
                _statusBannerMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to create alert"
            }
        }
    }

    fun toggleAlertStatus(alert: StockAlert, enabled: Boolean) {
        viewModelScope.launch {
            watchlistRepository.toggleAlert(alert, enabled)
        }
    }

    fun deleteStockAlert(alertId: String) {
        viewModelScope.launch {
            watchlistRepository.deleteAlert(alertId)
        }
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun triggerAiDeepDive(stock: StockQuote) {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _aiState.value = AiInsightState.Loading
            val relatedNews = marketDataService.getCuratedNewsForStock(stock.ticker)
            val result = aiService.generateStockDeepDive(stock, relatedNews)
            result.fold(
                onSuccess = { response ->
                    _aiState.value = AiInsightState.DeepDiveReady(response)
                },
                onFailure = { error ->
                    _aiState.value = AiInsightState.Error(
                        error.localizedMessage ?: "AI Deep Dive generation failed."
                    )
                }
            )
        }
    }

    fun askAiQuestion(stock: StockQuote, question: String) {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _aiState.value = AiInsightState.Loading
            val relatedNews = marketDataService.getCuratedNewsForStock(stock.ticker)
            var latestText = ""
            try {
                aiService.streamStockQuestionAnswer(stock, question, relatedNews)
                    .collect { partial ->
                        latestText = partial
                        _aiState.value = AiInsightState.Streaming(partial)
                    }
                if (latestText.isNotBlank()) {
                    _aiState.value = AiInsightState.QuestionAnswered(question, latestText)
                } else {
                    _aiState.value = AiInsightState.Error("Model returned an empty response.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI question streaming failed", e)
                _aiState.value = AiInsightState.Error(
                    e.localizedMessage ?: "Failed to analyze question."
                )
            }
        }
    }

    fun getNewsForTicker(ticker: String? = null): List<StockNewsArticle> {
        val raw = marketDataService.getCuratedNewsForStock(ticker)
        val cat = _selectedNewsCategory.value
        return if (cat == NewsCategory.ALL) {
            raw
        } else {
            raw.filter { it.category == cat }
        }
    }

    fun getFilteredStocks(): List<StockQuote> {
        val query = _searchQuery.value.trim().lowercase()
        val country = _selectedCountry.value
        val exchange = _selectedExchange.value
        val filter = _selectedFilter.value

        var list = _allStocks.value.filter { stock ->
            val matchesQuery = query.isEmpty() ||
                stock.ticker.lowercase().contains(query) ||
                stock.companyName.lowercase().contains(query) ||
                stock.country.lowercase().contains(query) ||
                stock.exchange.lowercase().contains(query) ||
                stock.sector.lowercase().contains(query) ||
                stock.fundamentals.marketCapCategory.lowercase().contains(query)

            val matchesCountry = country == "All Countries" || stock.country.equals(country, ignoreCase = true)
            val matchesExchange = exchange == "All Exchanges" || stock.exchange.equals(exchange, ignoreCase = true)
            matchesQuery && matchesCountry && matchesExchange
        }

        list = when (filter) {
            MarketFilter.ALL -> list
            MarketFilter.TOP_GAINERS -> list.filter { it.changePercent > 0 }.sortedByDescending { it.changePercent }
            MarketFilter.TOP_LOSERS -> list.filter { it.changePercent < 0 }.sortedBy { it.changePercent }
            MarketFilter.MOST_ACTIVE -> list.sortedByDescending { it.volume }
            MarketFilter.HIGHEST_VOLUME -> list.sortedByDescending { it.technicals.volumeRatioVsAvg }
            MarketFilter.MOST_VOLATILE -> list.sortedByDescending {
                (it.technicals.bollingerUpper - it.technicals.bollingerLower) / it.currentPrice.coerceAtLeast(1.0)
            }
            MarketFilter.TRENDING -> list.sortedByDescending { it.sentimentMacro.investorSentimentScore }
            MarketFilter.UNDERVALUED -> list.filter { it.fundamentals.peRatio in 1.0..20.0 }.sortedBy { it.fundamentals.peRatio }
            MarketFilter.STRONG_MOMENTUM -> list.sortedByDescending { it.technicals.momentumScore }
            MarketFilter.DIVIDEND -> list.filter { it.fundamentals.dividendYieldPercent >= 1.5 }.sortedByDescending { it.fundamentals.dividendYieldPercent }
        }
        return list
    }

    private companion object {
        const val TAG = "StockIntelVM"
    }
}
