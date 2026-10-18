package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CallEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.repository.ChatRepository
import com.example.model.AuraScreen
import com.example.model.MessengerTab
import com.example.model.PushNotification
import com.example.model.StatusStory
import com.example.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthProfile(
    val id: String,
    val displayName: String,
    val identifier: String, // email or phone
    val isPhoneAuth: Boolean,
    val avatarMonogram: String = "ME"
)

data class AuraUiState(
    val currentScreen: AuraScreen = AuraScreen.Login,
    val selectedTab: MessengerTab = MessengerTab.CHATS,
    // Auth Mode & Fields
    val authMethod: String = "google", // "google" or "phone"
    val emailInput: String = "alleninnocent23@gmail.com",
    val passwordInput: String = "••••••••••••",
    val phoneCountryCode: String = "+1",
    val phoneNumberInput: String = "415 555 0199",
    val otpCodeInput: String = "",
    val isAwaitingOtp: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val isLoggedIn: Boolean = false,
    val currentProfile: AuthProfile? = null,
    // Real Room Data
    val searchQuery: String = "",
    val roomConversations: List<ConversationEntity> = emptyList(),
    val currentChatMessages: List<MessageEntity> = emptyList(),
    val activeConversation: ConversationEntity? = null,
    val roomCalls: List<CallEntity> = emptyList(),
    val statusStories: List<StatusStory> = emptyList(),
    // Active Push Notification
    val activeNotification: PushNotification? = null,
    // Active Call Sheet
    val activeCallUser: User? = null,
    val isCallActive: Boolean = false,
    val isCallMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val callDurationSec: Int = 0,
    // Customizer Settings
    val glowIntensity: Float = 1.0f,
    val ambientShimmerEnabled: Boolean = true,
    // UI Dialogs
    val showNewContactDialog: Boolean = false,
    val showDirectDialDialog: Boolean = false
)

class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ChatRepository(db, application)
    private val prefs = application.getSharedPreferences("aura_glass_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(AuraUiState())
    val uiState: StateFlow<AuraUiState> = _uiState.asStateFlow()

    private var currentChatObservationJob: Job? = null

    init {
        checkSavedSession()
        observeRoomData()
        loadStaticStories()
    }

    private fun checkSavedSession() {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val name = prefs.getString("user_name", "Allen Innocent") ?: "Allen Innocent"
        val identifier = prefs.getString("user_identifier", "alleninnocent23@gmail.com") ?: "alleninnocent23@gmail.com"
        val isPhone = prefs.getBoolean("is_phone_auth", false)

        if (isLoggedIn) {
            val profile = AuthProfile(
                id = "user_me",
                displayName = name,
                identifier = identifier,
                isPhoneAuth = isPhone,
                avatarMonogram = name.take(2).uppercase()
            )
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    currentProfile = profile,
                    currentScreen = AuraScreen.Messenger(MessengerTab.CHATS)
                )
            }
        }
    }

    private fun observeRoomData() {
        viewModelScope.launch {
            repository.allConversations.collect { convs ->
                _uiState.update { it.copy(roomConversations = convs) }
            }
        }
        viewModelScope.launch {
            repository.allCalls.collect { calls ->
                _uiState.update { it.copy(roomCalls = calls) }
            }
        }
    }

    private fun loadStaticStories() {
        val userAlex = User(
            id = "user_alex",
            name = "Alex Vance",
            username = "@alexvance",
            avatarDrawableRes = R.drawable.img_glass_avatar,
            avatarMonogram = "AV",
            isOnline = true
        )
        val userElena = User(
            id = "user_elena",
            name = "Elena Rostova",
            username = "@elena_r",
            avatarDrawableRes = null,
            avatarMonogram = "ER",
            isOnline = true
        )
        _uiState.update {
            it.copy(
                statusStories = listOf(
                    StatusStory("s1", userAlex, "Refractive index testing in pure black & white.", "32m ago"),
                    StatusStory("s2", userElena, "Monochrome fluid architecture on AMOLED display.", "2h ago")
                )
            )
        }
    }

    // Auth actions
    fun setAuthMethod(method: String) {
        _uiState.update { it.copy(authMethod = method, authError = null, isAwaitingOtp = false) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(emailInput = email, authError = null) }
    }

    fun updatePassword(pass: String) {
        _uiState.update { it.copy(passwordInput = pass, authError = null) }
    }

    fun updatePhoneCountryCode(code: String) {
        _uiState.update { it.copy(phoneCountryCode = code) }
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumberInput = phone, authError = null) }
    }

    fun updateOtpCode(code: String) {
        _uiState.update { it.copy(otpCodeInput = code, authError = null) }
        if (code.length == 6) {
            verifyOtpCode()
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun requestPhoneOtp() {
        if (_uiState.value.phoneNumberInput.isBlank()) {
            _uiState.update { it.copy(authError = "Please enter a valid phone number") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, authError = null) }
            delay(500)
            _uiState.update {
                it.copy(
                    isAuthenticating = false,
                    isAwaitingOtp = true,
                    otpCodeInput = ""
                )
            }
        }
    }

    fun verifyOtpCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, authError = null) }
            delay(600)
            val fullPhone = "${_uiState.value.phoneCountryCode} ${_uiState.value.phoneNumberInput}"
            val profile = AuthProfile(
                id = "user_me",
                displayName = fullPhone,
                identifier = fullPhone,
                isPhoneAuth = true,
                avatarMonogram = "PH"
            )
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_name", fullPhone)
                .putString("user_identifier", fullPhone)
                .putBoolean("is_phone_auth", true)
                .apply()

            _uiState.update {
                it.copy(
                    isAuthenticating = false,
                    isLoggedIn = true,
                    currentProfile = profile,
                    currentScreen = AuraScreen.Messenger(MessengerTab.CHATS)
                )
            }
        }
    }

    fun signInWithGoogleAccount(email: String = "alleninnocent23@gmail.com", name: String = "Allen Innocent") {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, authError = null) }
            delay(500)
            val profile = AuthProfile(
                id = "user_me",
                displayName = name,
                identifier = email,
                isPhoneAuth = false,
                avatarMonogram = name.take(2).uppercase()
            )
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_name", name)
                .putString("user_identifier", email)
                .putBoolean("is_phone_auth", false)
                .apply()

            _uiState.update {
                it.copy(
                    isAuthenticating = false,
                    isLoggedIn = true,
                    currentProfile = profile,
                    currentScreen = AuraScreen.Messenger(MessengerTab.CHATS)
                )
            }
        }
    }

    fun onFirebaseAuthSuccess(user: com.google.firebase.auth.FirebaseUser) {
        val name = user.displayName ?: user.email?.substringBefore("@") ?: "Google User"
        val email = user.email ?: "google.user@auraglass.io"
        val profile = AuthProfile(
            id = user.uid,
            displayName = name,
            identifier = email,
            isPhoneAuth = false,
            avatarMonogram = name.take(2).uppercase()
        )
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", name)
            .putString("user_identifier", email)
            .putBoolean("is_phone_auth", false)
            .apply()

        _uiState.update {
            it.copy(
                isAuthenticating = false,
                isLoggedIn = true,
                currentProfile = profile,
                currentScreen = AuraScreen.Messenger(MessengerTab.CHATS)
            )
        }
    }

    fun signIn() {
        if (_uiState.value.authMethod == "phone") {
            if (_uiState.value.isAwaitingOtp) {
                verifyOtpCode()
            } else {
                requestPhoneOtp()
            }
        } else {
            signInWithGoogleAccount(
                email = _uiState.value.emailInput.ifBlank { "alleninnocent23@gmail.com" },
                name = _uiState.value.emailInput.substringBefore("@").replace(".", " ").capitalizeWords()
            )
        }
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                currentProfile = null,
                isAwaitingOtp = false,
                currentScreen = AuraScreen.Login
            )
        }
    }

    // Navigation & Tabs
    fun navigateToTab(tab: MessengerTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                currentScreen = AuraScreen.Messenger(tab)
            )
        }
    }

    fun openChat(conversationId: String) {
        currentChatObservationJob?.cancel()
        val conv = _uiState.value.roomConversations.firstOrNull { it.id == conversationId }

        _uiState.update {
            it.copy(
                activeConversation = conv,
                currentScreen = AuraScreen.ChatDetail(conversationId)
            )
        }

        currentChatObservationJob = viewModelScope.launch {
            repository.getMessages(conversationId).collect { msgs ->
                _uiState.update { it.copy(currentChatMessages = msgs) }
            }
        }
    }

    fun navigateBackToChats() {
        currentChatObservationJob?.cancel()
        _uiState.update {
            it.copy(
                currentScreen = AuraScreen.Messenger(MessengerTab.CHATS),
                activeConversation = null
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Real Messaging Functions
    fun sendTextMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendTextMessage(conversationId, text)
            // Intelligent liquid simulated reply after 1.5s
            delay(1500)
            repository.receiveReplyMessage(
                conversationId,
                "Received your message! The Room database saved it locally with instant persistence."
            )
        }
    }

    fun sendImageMessage(conversationId: String, uri: Uri) {
        viewModelScope.launch {
            repository.sendImageMessage(conversationId, uri)
            delay(1500)
            repository.receiveReplyMessage(
                conversationId,
                "Stunning photo! The liquid glass bubble rendered your device image seamlessly."
            )
        }
    }

    fun sendFileMessage(conversationId: String, uri: Uri) {
        viewModelScope.launch {
            repository.sendFileMessage(conversationId, uri)
            delay(1500)
            repository.receiveReplyMessage(
                conversationId,
                "File received and indexed. You can tap on it anytime to open it in Android's system viewer."
            )
        }
    }

    fun openFile(uriString: String) {
        repository.openFile(uriString)
    }

    fun sendExternalSms(phoneNumber: String, text: String) {
        repository.sendRealSms(phoneNumber, text)
    }

    // Real Calling Functions
    fun dialContact(conversation: ConversationEntity) {
        viewModelScope.launch {
            repository.logCall(conversation.name, conversation.phoneNumber, isVideo = false)
            repository.dialRealPhone(conversation.phoneNumber)
        }
    }

    fun dialCustomNumber(phoneNumber: String, name: String = "Direct Dial") {
        viewModelScope.launch {
            repository.logCall(name, phoneNumber, isVideo = false)
            repository.dialRealPhone(phoneNumber)
            _uiState.update { it.copy(showDirectDialDialog = false) }
        }
    }

    fun startInAppCall(user: User, isVideo: Boolean) {
        _uiState.update {
            it.copy(
                activeCallUser = user,
                isCallActive = true,
                isCallMuted = false,
                isSpeakerOn = false,
                callDurationSec = 0
            )
        }
        viewModelScope.launch {
            while (_uiState.value.isCallActive) {
                delay(1000)
                _uiState.update { it.copy(callDurationSec = it.callDurationSec + 1) }
            }
        }
    }

    fun endCall() {
        _uiState.update { it.copy(isCallActive = false, activeCallUser = null, callDurationSec = 0) }
    }

    fun toggleCallMute() {
        _uiState.update { it.copy(isCallMuted = !it.isCallMuted) }
    }

    fun toggleSpeaker() {
        _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    // Contact Management
    fun showNewContactDialog(show: Boolean) {
        _uiState.update { it.copy(showNewContactDialog = show) }
    }

    fun showDirectDialDialog(show: Boolean) {
        _uiState.update { it.copy(showDirectDialDialog = show) }
    }

    fun createNewContact(name: String, phoneNumber: String, bio: String) {
        viewModelScope.launch {
            repository.addContact(name, phoneNumber, bio)
            _uiState.update { it.copy(showNewContactDialog = false) }
        }
    }

    // Push Notification
    fun showSimulatedPushNotification(title: String, message: String, conversationId: String) {
        _uiState.update {
            it.copy(
                activeNotification = PushNotification(
                    id = "push_${System.currentTimeMillis()}",
                    title = title,
                    message = message,
                    conversationId = conversationId,
                    senderAvatarRes = R.drawable.img_glass_avatar,
                    senderMonogram = "AV"
                )
            )
        }
    }

    fun dismissPushNotification() {
        _uiState.update { it.copy(activeNotification = null) }
    }

    fun updateGlowIntensity(intensity: Float) {
        _uiState.update { it.copy(glowIntensity = intensity) }
    }

    fun toggleShimmer() {
        _uiState.update { it.copy(ambientShimmerEnabled = !it.ambientShimmerEnabled) }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
