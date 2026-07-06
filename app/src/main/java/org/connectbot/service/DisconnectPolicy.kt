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

object DisconnectPolicy {
    /** Longest delay between automatic reconnect attempts when backoff is enabled. */
    const val MAX_RECONNECT_DELAY_MS: Long = 300_000L

    fun decide(
        reason: DisconnectReason,
        quickDisconnect: Boolean,
        stayConnected: Boolean,
        reconnectAttempts: Int = 0,
        maxReconnectAttempts: Int = 0,
    ): DisconnectAction {
        if (reason == DisconnectReason.USER_REQUESTED) return DisconnectAction.CloseImmediately
        if (quickDisconnect) return DisconnectAction.CloseImmediately
        // Never auto-reconnect on auth failures — looping would lock accounts
        if (reason == DisconnectReason.AUTH_FAIL) return DisconnectAction.ShowReconnectOverlay
        if (stayConnected) {
            // maxReconnectAttempts == 0 means unlimited attempts
            return if (maxReconnectAttempts > 0 && reconnectAttempts >= maxReconnectAttempts) {
                DisconnectAction.GiveUpReconnect
            } else {
                DisconnectAction.AutoReconnect
            }
        }
        return DisconnectAction.ShowReconnectOverlay
    }

    /**
     * Delay before automatic reconnect attempt number [attempt] (1-based).
     *
     * The first attempt fires immediately so a dropped session recovers as fast as
     * before this setting existed; later attempts wait [intervalSeconds], doubling
     * each attempt (capped at [MAX_RECONNECT_DELAY_MS]) when [exponentialBackoff]
     * is enabled.
     */
    fun reconnectDelayMs(
        attempt: Int,
        intervalSeconds: Int,
        exponentialBackoff: Boolean,
    ): Long {
        if (attempt <= 1) return 0L
        val baseMs = intervalSeconds.coerceAtLeast(0).toLong() * 1000L
        if (!exponentialBackoff) return baseMs
        val exponent = (attempt - 2).coerceAtMost(MAX_BACKOFF_EXPONENT)
        // The cap limits backoff growth but never cuts below the configured interval
        return (baseMs shl exponent).coerceAtMost(maxOf(baseMs, MAX_RECONNECT_DELAY_MS))
    }

    // 2^9 * 1s already exceeds MAX_RECONNECT_DELAY_MS; capping the exponent avoids shift overflow
    private const val MAX_BACKOFF_EXPONENT: Int = 9
}
