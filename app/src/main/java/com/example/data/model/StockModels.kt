package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class WatchlistItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val ticker: String = "",
    val companyName: String = "",
    val exchange: String = "",
    val country: String = "",
    val sector: String = "",
    val currency: String = "USD",
    val lastPrice: Double = 0.0,
    val changePercent: Double = 0.0,
    val chanceUp: Int = 50,
    val chanceDown: Int = 30,
    val chanceSideways: Int = 20,
    val sortOrder: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StockAlert(
    @DocumentId val id: String = "",
    val userId: String = "",
    val ticker: String = "",
    val companyName: String = "",
    val alertType: String = "PRICE_SURGE",
    val thresholdValue: Double = 5.0,
    val enabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class PredictionHorizon(val label: String, val shortCode: String, val tradingDays: Int) {
    ONE_DAY("1 Day", "1D", 1),
    FIVE_DAYS("5 Days", "5D", 5),
    ONE_MONTH("1 Month", "1M", 21),
    THREE_MONTHS("3 Months", "3M", 63),
    SIX_MONTHS("6 Months", "6M", 126),
    ONE_YEAR("1 Year", "1Y", 252)
}

enum class ChartTimeframe(val label: String, val rangeParam: String, val intervalParam: String) {
    ONE_DAY("1D", "1d", "5m"),
    ONE_WEEK("1W", "5d", "30m"),
    ONE_MONTH("1M", "1mo", "1d"),
    THREE_MONTHS("3M", "3mo", "1d"),
    SIX_MONTHS("6M", "6mo", "1d"),
    ONE_YEAR("1Y", "1y", "1d"),
    FIVE_YEARS("5Y", "5y", "1wk"),
    MAX("MAX", "10y", "1mo")
}

enum class MarketFilter(val label: String) {
    ALL("All Global"),
    TOP_GAINERS("Top Gainers"),
    TOP_LOSERS("Top Losers"),
    MOST_ACTIVE("Most Active"),
    HIGHEST_VOLUME("Highest Volume"),
    MOST_VOLATILE("Most Volatile"),
    TRENDING("Trending"),
    UNDERVALUED("Undervalued"),
    STRONG_MOMENTUM("Strong Momentum"),
    DIVIDEND("Dividend")
}

enum class ExpectedDirection(val label: String) {
    BULLISH("Bullish"),
    BEARISH("Bearish"),
    NEUTRAL("Neutral / Sideways"),
    INSUFFICIENT_DATA("Insufficient Data")
}

data class HorizonProbability(
    val horizon: PredictionHorizon,
    val chanceUp: Int,
    val chanceDown: Int,
    val chanceSideways: Int,
    val expectedDirection: ExpectedDirection,
    val modelReliabilityScore: Int, // 0..100 distinct from directional probability
    val backtestWinRate: Double, // Walk-forward historical accuracy %
    val brierCalibrationScore: Double, // Lower is better calibrated (e.g., 0.14)
    val sampleSizeBars: Int,
    val hasSufficientData: Boolean = true
)

data class TechnicalIndicators(
    val rsi14: Double,
    val rsiSignal: String,
    val macdLine: Double,
    val macdSignalLine: Double,
    val macdHistogram: Double,
    val sma20: Double,
    val ema20: Double,
    val ema50: Double,
    val ema100: Double,
    val ema200: Double,
    val bollingerUpper: Double,
    val bollingerMiddle: Double,
    val bollingerLower: Double,
    val stochasticK: Double,
    val stochasticD: Double,
    val supportLevel1: Double,
    val supportLevel2: Double,
    val resistanceLevel1: Double,
    val resistanceLevel2: Double,
    val volumeRatioVsAvg: Double,
    val priceActionStructure: String,
    val momentumScore: Int, // -100..+100
    val trendStrengthAdx: Double
)

data class FundamentalIndicators(
    val peRatio: Double,
    val eps: Double,
    val revenueGrowthYoY: Double,
    val netIncomeBillions: Double,
    val debtToEquity: Double,
    val roePercent: Double,
    val roaPercent: Double,
    val freeCashFlowBillions: Double,
    val marketCapBillions: Double,
    val marketCapCategory: String, // Mega Cap, Large Cap, Mid Cap
    val dividendYieldPercent: Double,
    val latestEarningsSummary: String
)

data class SentimentAndMacroProfile(
    val investorSentimentScore: Int, // 0..100
    val institutionalActivityStatus: String,
    val analystConsensus: String,
    val analystTargetPrice: Double,
    val newsSentimentPositivePct: Int,
    val newsSentimentNeutralPct: Int,
    val newsSentimentNegativePct: Int,
    val interestRateSensitivity: String,
    val inflationImpact: String,
    val centralBankPolicyNote: String,
    val currencyExposureNote: String,
    val geopoliticalRiskLevel: String
)

data class PredictionExplanation(
    val whyMayRise: List<String>,
    val whyMayFall: List<String>,
    val neutralFactors: List<String>,
    val mostInfluentialIndicators: List<String>,
    val primaryRisks: List<String>,
    val optimisticScenario: String,
    val pessimisticScenario: String,
    val invalidationConditions: List<String>
)

data class PricePoint(
    val timestampEpochSec: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

data class StockQuote(
    val ticker: String,
    val companyName: String,
    val exchange: String,
    val country: String,
    val sector: String,
    val currency: String,
    val currentPrice: Double,
    val previousClose: Double,
    val changeAmount: Double,
    val changePercent: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val volume: Long,
    val avgVolume: Long,
    val priceHistory: List<PricePoint>,
    val horizons: Map<PredictionHorizon, HorizonProbability>,
    val technicals: TechnicalIndicators,
    val fundamentals: FundamentalIndicators,
    val sentimentMacro: SentimentAndMacroProfile,
    val explanation: PredictionExplanation,
    val dataSource: String,
    val lastUpdatedFormatted: String,
    val isRealTimeOrDelayed: String,
    val isLiveApiData: Boolean
) {
    val primaryPrediction: HorizonProbability
        get() = horizons[PredictionHorizon.ONE_MONTH]
            ?: horizons.values.first()
}

data class GlobalIndexQuote(
    val symbol: String,
    val name: String,
    val region: String,
    val value: Double,
    val changePercent: Double,
    val changePoints: Double,
    val isOpen: Boolean,
    val sparkline: List<Double>
)

enum class NewsCategory(val label: String) {
    ALL("All News"),
    COMPANY("Company News"),
    EARNINGS("Earnings Reports"),
    FINANCIAL("Financial Results"),
    ANALYST("Analyst Updates"),
    INSTITUTIONAL("Institutional & Insider"),
    INDUSTRY("Industry News"),
    ECONOMIC("Economic & Central Bank"),
    GEOPOLITICAL("Regulations & Geopolitics")
}

data class StockNewsArticle(
    val id: String,
    val title: String,
    val source: String,
    val publishedAtFormatted: String,
    val publishedEpochSec: Long,
    val url: String,
    val relatedTickers: List<String>,
    val category: NewsCategory,
    val factualExcerpt: String,
    val aiSummary: String,
    val sentiment: String, // Positive, Negative, Neutral
    val estimatedPriceImpact: String, // High Bullish (+), Moderate Bullish, Neutral, Moderate Bearish, High Bearish (-)
    val impactReasoning: String
)

data class AiStockDeepDiveResponse(
    val ticker: String,
    val whyGoingUpOrDown: String,
    val whatHappenedToCompany: String,
    val whatCouldHappenNext: String,
    val biggestRisks: List<String>,
    val keyCatalysts: List<String>,
    val generatedTimestamp: String
)
