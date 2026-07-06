/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2026 Kenny Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DisconnectPolicyTest {

    private fun decide(
        reason: DisconnectReason,
        quickDisconnect: Boolean = false,
        stayConnected: Boolean = false,
        reconnectAttempts: Int = 0,
        maxReconnectAttempts: Int = 0,
    ) = DisconnectPolicy.decide(reason, quickDisconnect, stayConnected, reconnectAttempts, maxReconnectAttempts)

    // USER_REQUESTED always closes immediately regardless of flags

    @Test
    fun userRequested_defaultFlags_closesImmediately() {
        assertTrue(decide(DisconnectReason.USER_REQUESTED) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun userRequested_quickDisconnect_closesImmediately() {
        assertTrue(decide(DisconnectReason.USER_REQUESTED, quickDisconnect = true) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun userRequested_stayConnected_closesImmediately() {
        assertTrue(decide(DisconnectReason.USER_REQUESTED, stayConnected = true) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun userRequested_bothFlags_closesImmediately() {
        assertTrue(decide(DisconnectReason.USER_REQUESTED, quickDisconnect = true, stayConnected = true) is DisconnectAction.CloseImmediately)
    }

    // quickDisconnect=true closes immediately for all non-user reasons

    @Test
    fun remoteEof_quickDisconnect_closesImmediately() {
        assertTrue(decide(DisconnectReason.REMOTE_EOF, quickDisconnect = true) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun ioError_quickDisconnect_closesImmediately() {
        assertTrue(decide(DisconnectReason.IO_ERROR, quickDisconnect = true) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun networkLost_quickDisconnect_closesImmediately() {
        assertTrue(decide(DisconnectReason.NETWORK_LOST, quickDisconnect = true) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun authFail_quickDisconnect_closesImmediately() {
        assertTrue(decide(DisconnectReason.AUTH_FAIL, quickDisconnect = true) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun unknown_quickDisconnect_closesImmediately() {
        assertTrue(decide(DisconnectReason.UNKNOWN, quickDisconnect = true) is DisconnectAction.CloseImmediately)
    }

    // quickDisconnect wins even when stayConnected is also true

    @Test
    fun remoteEof_bothFlags_closesImmediately() {
        assertTrue(decide(DisconnectReason.REMOTE_EOF, quickDisconnect = true, stayConnected = true) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun ioError_bothFlags_closesImmediately() {
        assertTrue(decide(DisconnectReason.IO_ERROR, quickDisconnect = true, stayConnected = true) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun networkLost_bothFlags_closesImmediately() {
        assertTrue(decide(DisconnectReason.NETWORK_LOST, quickDisconnect = true, stayConnected = true) is DisconnectAction.CloseImmediately)
    }

    @Test
    fun unknown_bothFlags_closesImmediately() {
        assertTrue(decide(DisconnectReason.UNKNOWN, quickDisconnect = true, stayConnected = true) is DisconnectAction.CloseImmediately)
    }

    // Default flags (both false) — show overlay so user can read terminal output

    @Test
    fun remoteEof_defaultFlags_showsReconnectOverlay() {
        // Key regression test: REMOTE_EOF must NOT close immediately by default
        assertTrue(decide(DisconnectReason.REMOTE_EOF) is DisconnectAction.ShowReconnectOverlay)
    }

    @Test
    fun ioError_defaultFlags_showsReconnectOverlay() {
        assertTrue(decide(DisconnectReason.IO_ERROR) is DisconnectAction.ShowReconnectOverlay)
    }

    @Test
    fun networkLost_defaultFlags_showsReconnectOverlay() {
        assertTrue(decide(DisconnectReason.NETWORK_LOST) is DisconnectAction.ShowReconnectOverlay)
    }

    @Test
    fun authFail_defaultFlags_showsReconnectOverlay() {
        assertTrue(decide(DisconnectReason.AUTH_FAIL) is DisconnectAction.ShowReconnectOverlay)
    }

    @Test
    fun unknown_defaultFlags_showsReconnectOverlay() {
        assertTrue(decide(DisconnectReason.UNKNOWN) is DisconnectAction.ShowReconnectOverlay)
    }

    // stayConnected=true auto-reconnects (except AUTH_FAIL)

    @Test
    fun remoteEof_stayConnected_autoReconnects() {
        assertTrue(decide(DisconnectReason.REMOTE_EOF, stayConnected = true) is DisconnectAction.AutoReconnect)
    }

    @Test
    fun ioError_stayConnected_autoReconnects() {
        assertTrue(decide(DisconnectReason.IO_ERROR, stayConnected = true) is DisconnectAction.AutoReconnect)
    }

    @Test
    fun networkLost_stayConnected_autoReconnects() {
        assertTrue(decide(DisconnectReason.NETWORK_LOST, stayConnected = true) is DisconnectAction.AutoReconnect)
    }

    @Test
    fun unknown_stayConnected_autoReconnects() {
        assertTrue(decide(DisconnectReason.UNKNOWN, stayConnected = true) is DisconnectAction.AutoReconnect)
    }

    // AUTH_FAIL never auto-reconnects even with stayConnected — would lock accounts

    @Test
    fun authFail_stayConnected_showsReconnectOverlay() {
        assertTrue(decide(DisconnectReason.AUTH_FAIL, stayConnected = true) is DisconnectAction.ShowReconnectOverlay)
    }

    @Test
    fun authFail_bothFlags_closesImmediately() {
        // quickDisconnect wins over AUTH_FAIL special case
        assertTrue(decide(DisconnectReason.AUTH_FAIL, quickDisconnect = true, stayConnected = true) is DisconnectAction.CloseImmediately)
    }

    // Reconnect attempt limit — maxReconnectAttempts of 0 means unlimited

    @Test
    fun stayConnected_unlimitedAttempts_alwaysAutoReconnects() {
        assertTrue(
            decide(
                DisconnectReason.IO_ERROR,
                stayConnected = true,
                reconnectAttempts = 1000,
                maxReconnectAttempts = 0,
            ) is DisconnectAction.AutoReconnect,
        )
    }

    @Test
    fun stayConnected_belowAttemptLimit_autoReconnects() {
        assertTrue(
            decide(
                DisconnectReason.IO_ERROR,
                stayConnected = true,
                reconnectAttempts = 2,
                maxReconnectAttempts = 3,
            ) is DisconnectAction.AutoReconnect,
        )
    }

    @Test
    fun stayConnected_atAttemptLimit_givesUp() {
        assertTrue(
            decide(
                DisconnectReason.IO_ERROR,
                stayConnected = true,
                reconnectAttempts = 3,
                maxReconnectAttempts = 3,
            ) is DisconnectAction.GiveUpReconnect,
        )
    }

    @Test
    fun stayConnected_aboveAttemptLimit_givesUp() {
        assertTrue(
            decide(
                DisconnectReason.NETWORK_LOST,
                stayConnected = true,
                reconnectAttempts = 5,
                maxReconnectAttempts = 3,
            ) is DisconnectAction.GiveUpReconnect,
        )
    }

    @Test
    fun userRequested_atAttemptLimit_stillClosesImmediately() {
        assertTrue(
            decide(
                DisconnectReason.USER_REQUESTED,
                stayConnected = true,
                reconnectAttempts = 3,
                maxReconnectAttempts = 3,
            ) is DisconnectAction.CloseImmediately,
        )
    }

    @Test
    fun notStayConnected_attemptLimitIrrelevant_showsReconnectOverlay() {
        assertTrue(
            decide(
                DisconnectReason.IO_ERROR,
                reconnectAttempts = 3,
                maxReconnectAttempts = 3,
            ) is DisconnectAction.ShowReconnectOverlay,
        )
    }

    // Reconnect delay computation

    @Test
    fun firstAttempt_isImmediate() {
        assertEquals(0L, DisconnectPolicy.reconnectDelayMs(attempt = 1, intervalSeconds = 5, exponentialBackoff = false))
        assertEquals(0L, DisconnectPolicy.reconnectDelayMs(attempt = 1, intervalSeconds = 5, exponentialBackoff = true))
    }

    @Test
    fun laterAttempts_withoutBackoff_useFixedInterval() {
        assertEquals(5000L, DisconnectPolicy.reconnectDelayMs(attempt = 2, intervalSeconds = 5, exponentialBackoff = false))
        assertEquals(5000L, DisconnectPolicy.reconnectDelayMs(attempt = 7, intervalSeconds = 5, exponentialBackoff = false))
    }

    @Test
    fun laterAttempts_withBackoff_doubleEachAttempt() {
        assertEquals(5000L, DisconnectPolicy.reconnectDelayMs(attempt = 2, intervalSeconds = 5, exponentialBackoff = true))
        assertEquals(10000L, DisconnectPolicy.reconnectDelayMs(attempt = 3, intervalSeconds = 5, exponentialBackoff = true))
        assertEquals(20000L, DisconnectPolicy.reconnectDelayMs(attempt = 4, intervalSeconds = 5, exponentialBackoff = true))
    }

    @Test
    fun backoff_isCappedAtMaxDelay() {
        assertEquals(
            DisconnectPolicy.MAX_RECONNECT_DELAY_MS,
            DisconnectPolicy.reconnectDelayMs(attempt = 20, intervalSeconds = 5, exponentialBackoff = true),
        )
        // Very large attempt numbers must not overflow
        assertEquals(
            DisconnectPolicy.MAX_RECONNECT_DELAY_MS,
            DisconnectPolicy.reconnectDelayMs(attempt = 1000, intervalSeconds = 5, exponentialBackoff = true),
        )
    }

    @Test
    fun backoffCap_neverCutsBelowConfiguredInterval() {
        // Interval longer than the cap: the configured interval wins
        assertEquals(600_000L, DisconnectPolicy.reconnectDelayMs(attempt = 2, intervalSeconds = 600, exponentialBackoff = true))
        assertEquals(600_000L, DisconnectPolicy.reconnectDelayMs(attempt = 5, intervalSeconds = 600, exponentialBackoff = true))
        assertEquals(3_600_000L, DisconnectPolicy.reconnectDelayMs(attempt = 1000, intervalSeconds = 3600, exponentialBackoff = true))
    }

    @Test
    fun zeroInterval_meansImmediateRetries() {
        assertEquals(0L, DisconnectPolicy.reconnectDelayMs(attempt = 3, intervalSeconds = 0, exponentialBackoff = false))
        assertEquals(0L, DisconnectPolicy.reconnectDelayMs(attempt = 3, intervalSeconds = 0, exponentialBackoff = true))
    }

    @Test
    fun negativeInterval_treatedAsZero() {
        assertEquals(0L, DisconnectPolicy.reconnectDelayMs(attempt = 2, intervalSeconds = -5, exponentialBackoff = false))
    }
}
