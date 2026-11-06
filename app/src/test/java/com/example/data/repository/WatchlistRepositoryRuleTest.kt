package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.WatchlistItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class WatchlistRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun sampleItem(uid: String, ticker: String): WatchlistItem {
        return WatchlistItem(
            id = ticker,
            userId = uid,
            ticker = ticker,
            companyName = "NVIDIA Corporation",
            exchange = "NASDAQ",
            country = "United States",
            sector = "Semiconductors",
            currency = "USD",
            lastPrice = 138.60,
            changePercent = 3.25,
            chanceUp = 70,
            chanceDown = 18,
            chanceSideways = 12,
            sortOrder = 1
        )
    }

    @Test
    fun createWatchlistItem_validPayload_succeedsAndReadsBack() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = FirestoreWatchlistRepository(firestore)
        val uniqueTicker = "NVDA_${UUID.randomUUID().toString().take(6)}"

        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createWatchlistItemDirect(sampleItem(aliceUid, uniqueTicker))
        }
        assertTrue("Expected create success, got ${createRes.exceptionOrNull()}", createRes.isSuccess)

        val listRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getWatchlistOnce(aliceUid)
        }
        assertTrue(listRes.isSuccess)
        assertTrue(listRes.getOrThrow().any { it.ticker == uniqueTicker })
    }

    @Test
    fun observeWatchlist_authenticatedOwner_emitsRealtimeItems() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = FirestoreWatchlistRepository(firestore)
        val uniqueTicker = "AAPL_${UUID.randomUUID().toString().take(6)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createWatchlistItemDirect(sampleItem(aliceUid, uniqueTicker)).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeWatchlist(aliceUid).first { list -> list.any { it.ticker == uniqueTicker } }
        }
        assertTrue(emitted.any { it.ticker == uniqueTicker })
    }

    @Test
    fun getWatchlistItem_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreWatchlistRepository(firestore)
        val uniqueTicker = "MSFT_${UUID.randomUUID().toString().take(6)}"
        val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createWatchlistItemDirect(sampleItem(aliceUid, uniqueTicker)).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreWatchlistRepository(firestore)
        val bobReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getWatchlistItemById(aliceUid, docId)
        }
        assertTrue("Expected cross-user read to fail", bobReadResult.isFailure)
        val fsEx = bobReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun observeWatchlist_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repo = FirestoreWatchlistRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeWatchlist(aliceUid).first()
            }
            fail("Expected exception for unauthenticated flow observation")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected wrapped FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@stockintel.com"
        const val BOB_EMAIL = "bob@stockintel.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
