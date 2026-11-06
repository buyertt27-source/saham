package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.StockAlert
import com.example.data.model.StockQuote
import com.example.data.model.WatchlistItem
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreWatchlistRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeWatchlist(userId: String = requireUserId()): Flow<List<WatchlistItem>> = flow {
        val path = "users/$userId/watchlist"
        emitAll(
            db.collection("users").document(userId).collection("watchlist")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(
                            WatchlistItem::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    }.sortedBy { it.sortOrder }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getWatchlistOnce(userId: String = requireUserId()): Result<List<WatchlistItem>> {
        val path = "users/$userId/watchlist"
        return try {
            val snapshot = db.collection("users").document(userId).collection("watchlist")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(
                    WatchlistItem::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }.sortedBy { it.sortOrder }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun getWatchlistItemById(targetUserId: String, tickerId: String): Result<WatchlistItem?> {
        val docRef = db.collection("users").document(targetUserId).collection("watchlist").document(tickerId)
        return try {
            val snap = docRef.get().await()
            Result.success(
                snap.toObject(
                    WatchlistItem::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun addOrUpdateWatchlistStock(
        stock: StockQuote,
        sortOrder: Int = 0
    ): Result<String> {
        val uid = requireUserId()
        val sanitizedId = stock.ticker.replace("[^a-zA-Z0-9_.\\-]".toRegex(), "_")
        val docRef = db.collection("users").document(uid).collection("watchlist").document(sanitizedId)
        val primary = stock.primaryPrediction
        val item = WatchlistItem(
            id = sanitizedId,
            userId = uid,
            ticker = stock.ticker,
            companyName = stock.companyName.take(150),
            exchange = stock.exchange.take(40),
            country = stock.country.take(60),
            sector = stock.sector.take(80),
            currency = stock.currency.take(10),
            lastPrice = stock.currentPrice,
            changePercent = stock.changePercent,
            chanceUp = primary.chanceUp,
            chanceDown = primary.chanceDown,
            chanceSideways = primary.chanceSideways,
            sortOrder = sortOrder.coerceIn(0, 10000),
            createdAt = null,
            updatedAt = null
        )
        return try {
            docRef.set(item).await()
            Result.success(sanitizedId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createWatchlistItemDirect(item: WatchlistItem): Result<String> {
        val uid = requireUserId()
        val sanitizedId = item.ticker.replace("[^a-zA-Z0-9_.\\-]".toRegex(), "_").ifBlank { "ITEM" }
        val docRef = db.collection("users").document(uid).collection("watchlist").document(sanitizedId)
        val toSave = item.copy(
            id = sanitizedId,
            userId = uid,
            createdAt = null,
            updatedAt = null
        )
        return try {
            docRef.set(toSave).await()
            Result.success(sanitizedId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateWatchlistItemOrder(item: WatchlistItem, newSortOrder: Int): Result<Unit> {
        val uid = requireUserId()
        require(item.id.isNotBlank()) { "WatchlistItem id must not be blank" }
        requireNotNull(item.createdAt) { "WatchlistItem createdAt must not be null for update" }
        val docRef = db.collection("users").document(uid).collection("watchlist").document(item.id)
        return try {
            docRef.set(
                item.copy(
                    sortOrder = newSortOrder.coerceIn(0, 10000),
                    updatedAt = null
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun removeWatchlistStock(tickerOrId: String): Result<Unit> {
        val uid = requireUserId()
        val sanitizedId = tickerOrId.replace("[^a-zA-Z0-9_.\\-]".toRegex(), "_")
        val docRef = db.collection("users").document(uid).collection("watchlist").document(sanitizedId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeAlerts(userId: String = requireUserId()): Flow<List<StockAlert>> = flow {
        val path = "users/$userId/alerts"
        emitAll(
            db.collection("users").document(userId).collection("alerts")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(
                            StockAlert::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createAlert(
        ticker: String,
        companyName: String,
        alertType: String,
        thresholdValue: Double
    ): Result<String> {
        val uid = requireUserId()
        val alertId = "${ticker.replace("[^a-zA-Z0-9_.\\-]".toRegex(), "_")}_$alertType"
        val docRef = db.collection("users").document(uid).collection("alerts").document(alertId)
        val alert = StockAlert(
            id = alertId,
            userId = uid,
            ticker = ticker.take(30),
            companyName = companyName.take(150),
            alertType = alertType,
            thresholdValue = thresholdValue,
            enabled = true,
            createdAt = null,
            updatedAt = null
        )
        return try {
            docRef.set(alert).await()
            Result.success(alertId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleAlert(alert: StockAlert, enabled: Boolean): Result<Unit> {
        val uid = requireUserId()
        require(alert.id.isNotBlank()) { "Alert ID must not be blank" }
        requireNotNull(alert.createdAt) { "Alert createdAt must not be null" }
        val docRef = db.collection("users").document(uid).collection("alerts").document(alert.id)
        return try {
            docRef.set(alert.copy(enabled = enabled, updatedAt = null)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteAlert(alertId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("alerts").document(alertId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
