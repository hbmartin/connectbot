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

package org.connectbot.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PreferenceConstantsReconnectTest {

    @Test
    fun maxAttempts_nullOrBlank_usesDefault() {
        assertEquals(PreferenceConstants.DEFAULT_RECONNECT_MAX_ATTEMPTS, PreferenceConstants.parseReconnectMaxAttempts(null))
        assertEquals(PreferenceConstants.DEFAULT_RECONNECT_MAX_ATTEMPTS, PreferenceConstants.parseReconnectMaxAttempts(""))
        assertEquals(PreferenceConstants.DEFAULT_RECONNECT_MAX_ATTEMPTS, PreferenceConstants.parseReconnectMaxAttempts("abc"))
    }

    @Test
    fun maxAttempts_parsesAndFloorsAtZero() {
        assertEquals(3, PreferenceConstants.parseReconnectMaxAttempts("3"))
        assertEquals(0, PreferenceConstants.parseReconnectMaxAttempts("0"))
        assertEquals(0, PreferenceConstants.parseReconnectMaxAttempts("-4"))
    }

    @Test
    fun intervalSeconds_nullOrBlank_usesDefault() {
        assertEquals(PreferenceConstants.DEFAULT_RECONNECT_INTERVAL_SECONDS, PreferenceConstants.parseReconnectIntervalSeconds(null))
        assertEquals(PreferenceConstants.DEFAULT_RECONNECT_INTERVAL_SECONDS, PreferenceConstants.parseReconnectIntervalSeconds("x"))
    }

    @Test
    fun intervalSeconds_clampedToBounds() {
        assertEquals(0, PreferenceConstants.parseReconnectIntervalSeconds("0"))
        assertEquals(0, PreferenceConstants.parseReconnectIntervalSeconds("-10"))
        assertEquals(30, PreferenceConstants.parseReconnectIntervalSeconds("30"))
        assertEquals(
            PreferenceConstants.MAX_RECONNECT_INTERVAL_SECONDS,
            PreferenceConstants.parseReconnectIntervalSeconds("99999"),
        )
    }
}
