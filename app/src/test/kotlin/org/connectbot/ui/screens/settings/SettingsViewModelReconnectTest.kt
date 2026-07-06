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

package org.connectbot.ui.screens.settings

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.connectbot.data.ProfileRepository
import org.connectbot.data.entity.Profile
import org.connectbot.di.CoroutineDispatchers
import org.connectbot.di.FakeLanguagePackManager
import org.connectbot.util.PreferenceConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelReconnectTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = CoroutineDispatchers(
        default = testDispatcher,
        io = testDispatcher,
        main = testDispatcher,
    )
    private lateinit var prefs: SharedPreferences
    private lateinit var prefsEditor: SharedPreferences.Editor
    private lateinit var profileRepository: ProfileRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() = runTest {
        prefs = mock()
        prefsEditor = mock()
        profileRepository = mock()

        whenever(prefs.edit()).thenReturn(prefsEditor)
        whenever(prefsEditor.putString(any(), any())).thenReturn(prefsEditor)
        whenever(prefsEditor.putBoolean(any(), any())).thenReturn(prefsEditor)
        whenever(prefsEditor.putFloat(any(), any())).thenReturn(prefsEditor)
        whenever(prefs.getBoolean(any(), any())).thenReturn(false)
        whenever(prefs.getString(any(), any())).thenReturn("")
        whenever(prefs.getFloat(any(), any())).thenReturn(0.25f)
        whenever(profileRepository.getAll()).thenReturn(emptyList<Profile>())

        viewModel = createViewModel()
        advanceUntilIdle()
    }

    private fun createViewModel(): SettingsViewModel = SettingsViewModel(
        prefs,
        profileRepository,
        RuntimeEnvironment.getApplication(),
        dispatchers,
        FakeLanguagePackManager(),
    )

    @Test
    fun reconnectSettings_haveExpectedDefaults() = runTest {
        val state = viewModel.uiState.value
        assertEquals("0", state.reconnectMaxAttempts)
        assertEquals("5", state.reconnectInterval)
        assertFalse(state.reconnectBackoff)
    }

    @Test
    fun reconnectSettings_loadFromPreferences() = runTest {
        whenever(prefs.getString(eq(PreferenceConstants.RECONNECT_MAX_ATTEMPTS), isNull())).thenReturn("7")
        whenever(prefs.getString(eq(PreferenceConstants.RECONNECT_INTERVAL), isNull())).thenReturn("30")
        whenever(prefs.getBoolean(eq(PreferenceConstants.RECONNECT_BACKOFF), any())).thenReturn(true)

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("7", state.reconnectMaxAttempts)
        assertEquals("30", state.reconnectInterval)
        assertTrue(state.reconnectBackoff)
    }

    @Test
    fun updateReconnectMaxAttempts_writesToPreferences() = runTest {
        viewModel.updateReconnectMaxAttempts("3")
        advanceUntilIdle()

        assertEquals("3", viewModel.uiState.value.reconnectMaxAttempts)
        verify(prefsEditor).putString(PreferenceConstants.RECONNECT_MAX_ATTEMPTS, "3")
    }

    @Test
    fun updateReconnectMaxAttempts_rejectsNonNumericInput() = runTest {
        viewModel.updateReconnectMaxAttempts("abc")
        advanceUntilIdle()

        assertEquals("0", viewModel.uiState.value.reconnectMaxAttempts)
        verify(prefsEditor, never()).putString(eq(PreferenceConstants.RECONNECT_MAX_ATTEMPTS), any())
    }

    @Test
    fun updateReconnectInterval_writesToPreferences() = runTest {
        viewModel.updateReconnectInterval("15")
        advanceUntilIdle()

        assertEquals("15", viewModel.uiState.value.reconnectInterval)
        verify(prefsEditor).putString(PreferenceConstants.RECONNECT_INTERVAL, "15")
    }

    @Test
    fun updateReconnectInterval_rejectsNonNumericInput() = runTest {
        viewModel.updateReconnectInterval("1.5")
        advanceUntilIdle()

        assertEquals("5", viewModel.uiState.value.reconnectInterval)
        verify(prefsEditor, never()).putString(eq(PreferenceConstants.RECONNECT_INTERVAL), any())
    }

    @Test
    fun updateReconnectBackoff_writesToPreferences() = runTest {
        viewModel.updateReconnectBackoff(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.reconnectBackoff)
        verify(prefsEditor).putBoolean(PreferenceConstants.RECONNECT_BACKOFF, true)
    }
}
