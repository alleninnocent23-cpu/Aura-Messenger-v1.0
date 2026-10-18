package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AuraScreen
import com.example.model.MessengerTab
import com.example.ui.components.LiquidBottomDock
import com.example.ui.components.LiquidPushNotificationBanner
import com.example.ui.glass.LiquidAmbientBackground
import com.example.ui.screens.CallsScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatusScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuraViewModel

import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.example.auth.FirebaseAuthService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val credentialManager = CredentialManager.create(this)

        setContent {
            MyApplicationTheme {
                val viewModel: AuraViewModel = viewModel()

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    FirebaseAuthService.attemptAutoSignIn(
                        context = this@MainActivity,
                        credentialManager = credentialManager,
                        onAuthSuccess = { user ->
                            viewModel.onFirebaseAuthSuccess(user)
                        },
                        onUnauthenticated = {},
                        scope = lifecycleScope
                    )
                }

                AuraApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AuraApp(viewModel: AuraViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LiquidAmbientBackground(glowIntensity = uiState.glowIntensity) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Screen Routing
            when (val screen = uiState.currentScreen) {
                is AuraScreen.Login -> {
                    LoginScreen(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }

                is AuraScreen.Messenger -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (uiState.selectedTab) {
                            MessengerTab.CHATS -> ChatListScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            MessengerTab.STATUS -> StatusScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            MessengerTab.CALLS -> CallsScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            MessengerTab.SETTINGS -> SettingsScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }

                        // Floating Liquid Glass Bottom Dock
                        LiquidBottomDock(
                            currentTab = uiState.selectedTab,
                            onTabSelected = { viewModel.navigateToTab(it) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                        )
                    }
                }

                is AuraScreen.ChatDetail -> {
                    ChatDetailScreen(
                        conversationId = screen.conversationId,
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }

            // Realistic Sliding In Top Push Notification Banner
            LiquidPushNotificationBanner(
                notification = uiState.activeNotification,
                onDismiss = { viewModel.dismissPushNotification() },
                onTap = {
                    val notif = uiState.activeNotification
                    if (notif != null) {
                        viewModel.openChat(notif.conversationId)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            )
        }
    }
}
