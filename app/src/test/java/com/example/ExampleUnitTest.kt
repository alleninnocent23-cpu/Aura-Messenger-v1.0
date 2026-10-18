package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.model.AuraScreen
import com.example.viewmodel.AuraViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testInitialState() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AuraViewModel(app)
        val state = viewModel.uiState.value
        assertEquals(AuraScreen.Login, state.currentScreen)
    }

    @Test
    fun testPhoneAuthRequest() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AuraViewModel(app)
        viewModel.setAuthMethod("phone")
        viewModel.updatePhoneNumber("4155550199")
        assertEquals("phone", viewModel.uiState.value.authMethod)
        assertEquals("4155550199", viewModel.uiState.value.phoneNumberInput)
    }

    @Test
    fun testSearchQueryUpdate() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AuraViewModel(app)
        viewModel.updateSearchQuery("Elena")
        assertEquals("Elena", viewModel.uiState.value.searchQuery)
    }
}
