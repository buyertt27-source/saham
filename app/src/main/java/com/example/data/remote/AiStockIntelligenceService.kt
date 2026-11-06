package com.example.data.remote

import android.util.Log
import com.example.data.model.AiStockDeepDiveResponse
import com.example.data.model.StockNewsArticle
import com.example.data.model.StockQuote
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "AiStockService"

class AiStockIntelligenceService {

    private val deepDiveSchema = Schema.obj(
        properties = mapOf(
            "whyGoingUpOrDown" to Schema.string(description = "Data-backed explanation of why the stock is rising or falling based on technicals, fundamentals, and news"),
            "whatHappenedToCompany" to Schema.string(description = "Recent corporate events, earnings, and operational developments"),
            "whatCouldHappenNext" to Schema.string(description = "Forward-looking probabilistic outlook and key price levels"),
            "biggestRisks" to Schema.array(
                items = Schema.string(),
                description = "Top 3 specific risks facing the stock"
            ),
            "keyCatalysts" to Schema.array(
                items = Schema.string(),
                description = "Top 3 upcoming positive catalysts"
            )
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = deepDiveSchema
            }
        )
    }

    private val streamingModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    suspend fun generateStockDeepDive(
        stock: StockQuote,
        relatedNews: List<StockNewsArticle>
    ): Result<AiStockDeepDiveResponse> {
        val newsContext = relatedNews.joinToString("\n") {
            "- [${it.publishedAtFormatted}] (${it.source}) ${it.title}: ${it.factualExcerpt}"
        }
        val prompt = """
            Analyze global stock ${stock.companyName} (${stock.ticker}) listed on ${stock.exchange} (${stock.country}).
            Current Price: ${stock.currency} ${stock.currentPrice} (${stock.changePercent}%).
            Technical Indicators: RSI-14=${stock.technicals.rsi14}, MACD Hist=${stock.technicals.macdHistogram}, EMA50=${stock.technicals.ema50}, EMA200=${stock.technicals.ema200}, Support=${stock.technicals.supportLevel1}, Resistance=${stock.technicals.resistanceLevel1}.
            Fundamental Indicators: P/E=${stock.fundamentals.peRatio}, EPS=${stock.fundamentals.eps}, YoY Revenue Growth=${stock.fundamentals.revenueGrowthYoY}%, ROE=${stock.fundamentals.roePercent}%, Debt/Equity=${stock.fundamentals.debtToEquity}.
            1-Month Calibrated Probability: Up ${stock.primaryPrediction.chanceUp}%, Down ${stock.primaryPrediction.chanceDown}%, Sideways ${stock.primaryPrediction.chanceSideways}%.
            Recent Verified News:
            $newsContext
            
            Provide a concise, institutional-grade analysis separating factual drivers from probabilistic interpretation.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                structuredModel.generateContent(prompt)
            }
            val jsonString = response.text ?: throw IllegalStateException("Model returned empty response.")
            val json = JSONObject(jsonString)

            val risksArr = json.optJSONArray("biggestRisks")
            val risks = if (risksArr != null) {
                (0 until risksArr.length()).map { risksArr.getString(it) }
            } else emptyList()

            val catalystsArr = json.optJSONArray("keyCatalysts")
            val catalysts = if (catalystsArr != null) {
                (0 until catalystsArr.length()).map { catalystsArr.getString(it) }
            } else emptyList()

            val nowStr = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).format(Date())
            Result.success(
                AiStockDeepDiveResponse(
                    ticker = stock.ticker,
                    whyGoingUpOrDown = json.optString("whyGoingUpOrDown", ""),
                    whatHappenedToCompany = json.optString("whatHappenedToCompany", ""),
                    whatCouldHappenNext = json.optString("whatCouldHappenNext", ""),
                    biggestRisks = risks,
                    keyCatalysts = catalysts,
                    generatedTimestamp = nowStr
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI deep dive timed out", e)
            Result.failure(Exception("AI analysis timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI deep dive generation failed", e)
            Result.failure(e)
        }
    }

    fun streamStockQuestionAnswer(
        stock: StockQuote,
        question: String,
        relatedNews: List<StockNewsArticle>
    ): Flow<String> = flow {
        val newsSummary = relatedNews.joinToString("; ") { "${it.title} (${it.source}, ${it.publishedAtFormatted})" }
        val prompt = """
            You are an institutional quantitative equity analyst inside Global Stock Intelligence.
            Stock: ${stock.companyName} (${stock.ticker}, ${stock.exchange}, ${stock.country})
            Price: ${stock.currency} ${stock.currentPrice} (${stock.changePercent}%)
            RSI-14: ${stock.technicals.rsi14}, MACD Hist: ${stock.technicals.macdHistogram}, Support S1: ${stock.technicals.supportLevel1}, Resistance R1: ${stock.technicals.resistanceLevel1}
            P/E: ${stock.fundamentals.peRatio}x, Rev Growth: ${stock.fundamentals.revenueGrowthYoY}%, ROE: ${stock.fundamentals.roePercent}%, Latest Earnings: ${stock.fundamentals.latestEarningsSummary}
            1M Probability: Up ${stock.primaryPrediction.chanceUp}% | Down ${stock.primaryPrediction.chanceDown}% | Sideways ${stock.primaryPrediction.chanceSideways}% (Model Reliability: ${stock.primaryPrediction.modelReliabilityScore}%)
            Recent News: $newsSummary
            
            User Question: "$question"
            
            Answer clearly in 3-5 concise bullet points using the actual metrics and news above. Clearly distinguish factual data from AI probabilistic interpretation.
        """.trimIndent()

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            streamingModel.generateContentStream(prompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        }
    }
}
