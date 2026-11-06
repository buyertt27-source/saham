package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.FirestoreWatchlistRepository
import com.example.ui.components.LiquidGlassBottomNavigationDock
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassScreenBackground
import com.example.ui.components.MainNavTab
import com.example.ui.components.NavTabItem
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.NewsIntelligenceScreen
import com.example.ui.screens.ProfileAndAlertsScreen
import com.example.ui.screens.StockComparisonGlassMatrix
import com.example.ui.screens.StockDetailScreen
import com.example.ui.screens.WatchlistAndCompareScreen
import com.example.ui.screens.signOut
import com.example.ui.theme.CharcoalBase
import com.example.ui.theme.EmeraldBullishSoft
import com.example.ui.theme.GlassSpecularBright
import com.example.ui.theme.GlobalStockIntelligenceTheme
import com.example.ui.theme.IcyBlueAccent
import com.example.ui.theme.SoftWhite
import com.example.util.StockNotificationHelper
import com.example.util.configureAppCheck
import com.example.viewmodel.StockIntelligenceViewModel
import com.example.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)
        StockNotificationHelper.ensureChannel(this)

        enableEdgeToEdge()

        setContent {
            GlobalStockIntelligenceTheme {
                AppNavigation()
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {}
        )
    } else {
        MainFeatureScreen(
            currentUserId = user.uid,
            userEmail = user.email
        )
    }
}

@Composable
fun MainFeatureScreen(
    currentUserId: String,
    userEmail: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: StockIntelligenceViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                StockIntelligenceViewModel(
                    watchlistRepository = FirestoreWatchlistRepository(db),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val indices by viewModel.indices.collectAsStateWithLifecycle()
    val allStocks by viewModel.allStocks.collectAsStateWithLifecycle()
    val isLoadingMarket by viewModel.isLoadingMarket.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val selectedExchange by viewModel.selectedExchange.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedStock by viewModel.selectedStock.collectAsStateWithLifecycle()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()
    val selectedHorizon by viewModel.selectedHorizon.collectAsStateWithLifecycle()
    val selectedNewsCategory by viewModel.selectedNewsCategory.collectAsStateWithLifecycle()
    val comparisonTickers by viewModel.comparisonTickers.collectAsStateWithLifecycle()
    val showComparisonSheet by viewModel.showComparisonSheet.collectAsStateWithLifecycle()
    val watchlistState by viewModel.watchlistState.collectAsStateWithLifecycle()
    val alertsState by viewModel.alertsState.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(MainNavTab.HOME) }

    val navItems = remember {
        listOf(
            NavTabItem(MainNavTab.HOME, Icons.Filled.Home, Icons.Outlined.Home),
            NavTabItem(MainNavTab.MARKETS, Icons.Filled.Public, Icons.Outlined.Public),
            NavTabItem(MainNavTab.WATCHLIST, Icons.Filled.Star, Icons.Outlined.StarBorder),
            NavTabItem(MainNavTab.NEWS, Icons.Filled.Newspaper, Icons.Outlined.Newspaper),
            NavTabItem(MainNavTab.PROFILE, Icons.Filled.Person, Icons.Outlined.Person)
        )
    }

    val watchlistTickers = remember(watchlistState) {
        (watchlistState as? UiState.Success)?.data?.map { it.ticker }?.toSet().orEmpty()
    }

    val filteredStocks = remember(allStocks, searchQuery, selectedCountry, selectedExchange, selectedFilter) {
        viewModel.getFilteredStocks()
    }

    // Handle Android Back press on non-Home tabs when not inside StockDetailScreen
    BackHandler(enabled = selectedStock == null && (showComparisonSheet || currentTab != MainNavTab.HOME)) {
        if (showComparisonSheet) {
            viewModel.setShowComparisonSheet(false)
        } else {
            currentTab = MainNavTab.HOME
        }
    }

    LiquidGlassScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (selectedStock == null) {
                    LiquidGlassBottomNavigationDock(
                        items = navItems,
                        selectedTab = currentTab,
                        onTabSelected = { tab ->
                            currentTab = tab
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                val detailStock = selectedStock
                if (detailStock != null) {
                    StockDetailScreen(
                        stock = detailStock,
                        isSavedInWatchlist = detailStock.ticker in watchlistTickers,
                        selectedTimeframe = selectedTimeframe,
                        selectedHorizon = selectedHorizon,
                        relatedNews = viewModel.getNewsForTicker(detailStock.ticker),
                        aiState = aiState,
                        onBack = { viewModel.closeStockDetail() },
                        onSelectTimeframe = { viewModel.selectChartTimeframe(it) },
                        onSelectHorizon = { viewModel.selectPredictionHorizon(it) },
                        onToggleWatchlist = { viewModel.toggleWatchlist(detailStock) },
                        onCreateQuickAlert = {
                            viewModel.createStockAlert(detailStock, "PREDICTION_SHIFT", 3.0)
                        },
                        onTriggerAiDeepDive = { viewModel.triggerAiDeepDive(detailStock) },
                        onAskAiQuestion = { q -> viewModel.askAiQuestion(detailStock, q) }
                    )
                } else {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "main_tab_transition"
                    ) { tab ->
                        when (tab) {
                            MainNavTab.HOME -> {
                                HomeScreen(
                                    indices = indices,
                                    stocks = allStocks,
                                    watchlistTickers = watchlistTickers,
                                    news = viewModel.getNewsForTicker(null),
                                    isLoading = isLoadingMarket,
                                    onRefresh = { viewModel.refreshGlobalMarketData() },
                                    onStockClick = { viewModel.openStockDetail(it) },
                                    onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                                    onOpenCompare = { viewModel.setShowComparisonSheet(true) }
                                )
                            }
                            MainNavTab.MARKETS -> {
                                MarketsScreen(
                                    stocks = filteredStocks,
                                    watchlistTickers = watchlistTickers,
                                    searchQuery = searchQuery,
                                    selectedCountry = selectedCountry,
                                    selectedExchange = selectedExchange,
                                    selectedFilter = selectedFilter,
                                    onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                                    onCountrySelected = { viewModel.selectCountry(it) },
                                    onExchangeSelected = { viewModel.selectExchange(it) },
                                    onFilterSelected = { viewModel.selectMarketFilter(it) },
                                    onStockClick = { viewModel.openStockDetail(it) },
                                    onToggleWatchlist = { viewModel.toggleWatchlist(it) }
                                )
                            }
                            MainNavTab.WATCHLIST -> {
                                WatchlistAndCompareScreen(
                                    watchlistState = watchlistState,
                                    allStocks = allStocks,
                                    comparisonTickers = comparisonTickers,
                                    onToggleComparisonTicker = { viewModel.toggleComparisonTicker(it) },
                                    onStockClick = { viewModel.openStockDetail(it) },
                                    onMoveWatchlistItem = { item, up -> viewModel.moveWatchlistItem(item, up) },
                                    onRemoveWatchlistItem = { tickerId ->
                                        allStocks.find { it.ticker.equals(tickerId, ignoreCase = true) }?.let {
                                            viewModel.toggleWatchlist(it)
                                        }
                                    },
                                    onAddSuggestedStock = { viewModel.toggleWatchlist(it) },
                                    onCreateAlertForStock = {
                                        viewModel.createStockAlert(it, "PRICE_SURGE", 3.5)
                                    }
                                )
                            }
                            MainNavTab.NEWS -> {
                                NewsIntelligenceScreen(
                                    articles = viewModel.getNewsForTicker(null),
                                    selectedCategory = selectedNewsCategory,
                                    onSelectCategory = { viewModel.selectNewsCategory(it) },
                                    onSelectTicker = { ticker ->
                                        allStocks.find { it.ticker.equals(ticker, ignoreCase = true) }?.let {
                                            viewModel.openStockDetail(it)
                                        }
                                    }
                                )
                            }
                            MainNavTab.PROFILE -> {
                                ProfileAndAlertsScreen(
                                    userEmail = userEmail,
                                    userId = currentUserId,
                                    alertsState = alertsState,
                                    allStocks = allStocks,
                                    onCreateAlert = { st, type, thresh ->
                                        viewModel.createStockAlert(st, type, thresh)
                                    },
                                    onToggleAlert = { alert, enabled ->
                                        viewModel.toggleAlertStatus(alert, enabled)
                                    },
                                    onDeleteAlert = { alertId ->
                                        viewModel.deleteStockAlert(alertId)
                                    },
                                    onSignOut = {
                                        signOut(
                                            context = context,
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {},
                                            scope = scope
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Floating Comparison Overlay Modal when triggered from Home
                if (showComparisonSheet) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xD905070B))
                            .clickable(enabled = true, onClick = {})
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Global Stock Comparison Matrix",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = SoftWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { viewModel.setShowComparisonSheet(false) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SoftWhite)
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(allStocks, key = { "modal_cmp_${it.ticker}" }) { st ->
                                    LiquidGlassPill(
                                        text = st.ticker,
                                        selected = st.ticker in comparisonTickers,
                                        accentColor = EmeraldBullishSoft,
                                        onClick = { viewModel.toggleComparisonTicker(st.ticker) }
                                    )
                                }
                            }

                            val compared = allStocks.filter { it.ticker in comparisonTickers }
                            if (compared.size >= 2) {
                                StockComparisonGlassMatrix(comparedStocks = compared)
                            }
                        }
                    }
                }

                // Floating Liquid Glass Status Toast Banner
                statusBannerMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp, start = 24.dp, end = 24.dp)
                    ) {
                        LiquidGlassCard(
                            cornerRadius = 50.dp,
                            elevated = true,
                            accentGlowColor = IcyBlueAccent,
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                            onClick = { viewModel.clearStatusBanner() }
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelLarge,
                                color = SoftWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
