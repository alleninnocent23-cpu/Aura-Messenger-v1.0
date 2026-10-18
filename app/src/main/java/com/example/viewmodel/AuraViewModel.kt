package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.FirebaseAuthService
import com.example.data.firebase.FirestoreChatRepository
import com.example.data.firebase.RealConversation
import com.example.data.firebase.RealMessage
import com.example.data.firebase.RealStatusPost
import com.example.data.firebase.UserProfile
import com.example.model.AuraScreen
import com.example.model.MessengerTab
import com.example.model.PushNotification
import com.google.firebase.auth.FirebaseUser
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
    val displayNameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val phoneCountryCode: String = "+1",
    val phoneNumberInput: String = "",
    val otpCodeInput: String = "",
    val isAwaitingOtp: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val isLoggedIn: Boolean = false,
    val currentProfile: AuthProfile? = null,
    // Real Firestore Data
    val searchQuery: String = "",
    val realConversations: List<RealConversation> = emptyList(),
    val allRegisteredUsers: List<UserProfile> = emptyList(),
    val liveStatuses: List<RealStatusPost> = emptyList(),
    val activeConversation: RealConversation? = null,
    val activeChatMessages: List<RealMessage> = emptyList(),
    val activeChatRecipientName: String = "",
    val activeChatRecipientPhone: String = "",
    // Active Push Notification
    val activeNotification: PushNotification? = null,
    // UI Dialogs
    val showStartChatDialog: Boolean = false,
    val showDirectDialDialog: Boolean = false,
    // Customizer Settings
    val glowIntensity: Float = 1.0f,
    val ambientShimmerEnabled: Boolean = true
)

class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepo = FirestoreChatRepository(application)
    private val prefs = application.getSharedPreferences("aura_glass_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(AuraUiState())
    val uiState: StateFlow<AuraUiState> = _uiState.asStateFlow()

    private var conversationsJob: Job? = null
    private var messagesJob: Job? = null
    private var usersDirectoryJob: Job? = null
    private var statusesJob: Job? = null

    init {
        checkSavedSession()
    }

    private fun checkSavedSession() {
        val firebaseUser = FirebaseAuthService.currentUser
        if (firebaseUser != null) {
            onFirebaseAuthSuccess(firebaseUser)
            return
        }

        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val uid = prefs.getString("user_id", "") ?: ""
        val name = prefs.getString("user_name", "") ?: ""
        val identifier = prefs.getString("user_identifier", "") ?: ""
        val isPhone = prefs.getBoolean("is_phone_auth", false)

        if (isLoggedIn && uid.isNotBlank() && name.isNotBlank()) {
            val profile = AuthProfile(
                id = uid,
                displayName = name,
                identifier = identifier,
                isPhoneAuth = isPhone,
                avatarMonogram = name.take(2).uppercase()
            )
            activateUserSession(profile)
        }
    }

    private fun activateUserSession(profile: AuthProfile) {
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                currentProfile = profile,
                currentScreen = AuraScreen.Messenger(MessengerTab.CHATS)
            )
        }

        // Sync with Firestore database
        viewModelScope.launch {
            firestoreRepo.syncUserProfile(
                userId = profile.id,
                displayName = profile.displayName,
                email = if (!profile.isPhoneAuth) profile.identifier else "",
                phoneNumber = if (profile.isPhoneAuth) profile.identifier else "",
                avatarMonogram = profile.avatarMonogram
            )
        }

        // Listen to real-time conversations for this user
        conversationsJob?.cancel()
        conversationsJob = viewModelScope.launch {
            firestoreRepo.observeConversations(profile.id).collect { convs ->
                _uiState.update { it.copy(realConversations = convs) }
            }
        }

        // Listen to registered user directory
        usersDirectoryJob?.cancel()
        usersDirectoryJob = viewModelScope.launch {
            firestoreRepo.observeAllUsers().collect { users ->
                _uiState.update { it.copy(allRegisteredUsers = users) }
            }
        }

        // Listen to real live statuses
        statusesJob?.cancel()
        statusesJob = viewModelScope.launch {
            firestoreRepo.observeLiveStatuses().collect { statuses ->
                _uiState.update { it.copy(liveStatuses = statuses) }
            }
        }
    }

    // Auth actions
    fun setAuthMethod(method: String) {
        _uiState.update { it.copy(authMethod = method, authError = null, isAwaitingOtp = false) }
    }

    fun updateDisplayName(name: String) {
        _uiState.update { it.copy(displayNameInput = name, authError = null) }
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
        val phone = _uiState.value.phoneNumberInput.trim()
        if (phone.isBlank()) {
            _uiState.update { it.copy(authError = "Please enter your phone number") }
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
            delay(400)
            val fullPhone = "${_uiState.value.phoneCountryCode} ${_uiState.value.phoneNumberInput.trim()}"
            val customName = _uiState.value.displayNameInput.trim().ifEmpty { fullPhone }
            val uid = "user_${fullPhone.filter { it.isDigit() }}"
            val profile = AuthProfile(
                id = uid,
                displayName = customName,
                identifier = fullPhone,
                isPhoneAuth = true,
                avatarMonogram = customName.take(2).uppercase()
            )
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_id", uid)
                .putString("user_name", customName)
                .putString("user_identifier", fullPhone)
                .putBoolean("is_phone_auth", true)
                .apply()

            activateUserSession(profile)
        }
    }

    fun onFirebaseAuthSuccess(user: FirebaseUser) {
        val name = user.displayName ?: user.email?.substringBefore("@") ?: "User"
        val email = user.email ?: "user@auraglass.io"
        val profile = AuthProfile(
            id = user.uid,
            displayName = name,
            identifier = email,
            isPhoneAuth = false,
            avatarMonogram = name.take(2).uppercase()
        )
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", user.uid)
            .putString("user_name", name)
            .putString("user_identifier", email)
            .putBoolean("is_phone_auth", false)
            .apply()

        activateUserSession(profile)
    }

    fun signInWithAccount(email: String, name: String) {
        val cleanEmail = email.trim().ifEmpty { "user@auraglass.io" }
        val cleanName = name.trim().ifEmpty { cleanEmail.substringBefore("@").replace(".", " ").capitalizeWords() }
        val uid = "user_" + cleanEmail.filter { it.isLetterOrDigit() }
        val profile = AuthProfile(
            id = uid,
            displayName = cleanName,
            identifier = cleanEmail,
            isPhoneAuth = false,
            avatarMonogram = cleanName.take(2).uppercase()
        )
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", uid)
            .putString("user_name", cleanName)
            .putString("user_identifier", cleanEmail)
            .putBoolean("is_phone_auth", false)
            .apply()

        activateUserSession(profile)
    }

    fun signIn() {
        if (_uiState.value.authMethod == "phone") {
            if (_uiState.value.isAwaitingOtp) {
                verifyOtpCode()
            } else {
                requestPhoneOtp()
            }
        } else {
            val email = _uiState.value.emailInput.trim()
            val name = _uiState.value.displayNameInput.trim()
            if (email.isBlank()) {
                _uiState.update { it.copy(authError = "Please enter your email or username") }
                return
            }
            signInWithAccount(email, name)
        }
    }

    fun signOut() {
        prefs.edit().clear().apply()
        conversationsJob?.cancel()
        messagesJob?.cancel()
        usersDirectoryJob?.cancel()
        statusesJob?.cancel()
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                currentProfile = null,
                realConversations = emptyList(),
                activeChatMessages = emptyList(),
                activeConversation = null,
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
        val conv = _uiState.value.realConversations.firstOrNull { it.id == conversationId }
        if (conv != null) {
            openConversation(conv)
        } else {
            startChatWithEmailOrPhone(conversationId)
        }
    }

    fun openConversation(conversation: RealConversation) {
        messagesJob?.cancel()
        val myUid = _uiState.value.currentProfile?.id ?: ""
        val otherUid = conversation.participants.firstOrNull { it != myUid } ?: myUid
        val otherName = conversation.participantNames[otherUid] ?: "User"

        _uiState.update {
            it.copy(
                activeConversation = conversation,
                activeChatRecipientName = otherName,
                activeChatRecipientPhone = otherUid,
                currentScreen = AuraScreen.ChatDetail(conversation.id)
            )
        }

        messagesJob = viewModelScope.launch {
            firestoreRepo.observeMessages(conversation.id).collect { msgs ->
                _uiState.update { it.copy(activeChatMessages = msgs) }
            }
        }
    }

    fun startChatWithRealUser(otherUserId: String, otherName: String) {
        val myProfile = _uiState.value.currentProfile ?: return
        viewModelScope.launch {
            val convId = firestoreRepo.getOrCreateConversation(
                myUserId = myProfile.id,
                myName = myProfile.displayName,
                otherUserId = otherUserId,
                otherName = otherName
            )
            _uiState.update { it.copy(showStartChatDialog = false) }

            val conv = RealConversation(
                id = convId,
                conversationId = convId,
                participants = listOf(myProfile.id, otherUserId),
                participantNames = mapOf(myProfile.id to myProfile.displayName, otherUserId to otherName),
                lastMessage = "Start of chat"
            )
            openConversation(conv)
        }
    }

    fun startChatWithEmailOrPhone(identifier: String, name: String = "") {
        val cleanId = identifier.trim()
        if (cleanId.isBlank()) return
        val displayName = if (name.isNotBlank()) name.trim() else cleanId
        startChatWithRealUser(otherUserId = cleanId, otherName = displayName)
    }

    fun navigateBackToChats() {
        messagesJob?.cancel()
        _uiState.update {
            it.copy(
                currentScreen = AuraScreen.Messenger(MessengerTab.CHATS),
                activeConversation = null,
                activeChatMessages = emptyList()
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Real Messaging Functions (writes directly to Firestore with optimistic instant UI update)
    fun sendTextMessage(conversationId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val myProfile = _uiState.value.currentProfile ?: return

        // Optimistic instant append for fluid user feedback
        val tempId = "temp_${System.currentTimeMillis()}"
        val optimisticMsg = RealMessage(
            id = tempId,
            conversationId = conversationId,
            senderId = myProfile.id,
            senderName = myProfile.displayName,
            text = trimmed,
            timestamp = null
        )
        _uiState.update { state ->
            state.copy(activeChatMessages = state.activeChatMessages + optimisticMsg)
        }

        viewModelScope.launch {
            val res = firestoreRepo.sendTextMessage(
                conversationId = conversationId,
                senderId = myProfile.id,
                senderName = myProfile.displayName,
                text = trimmed
            )
            if (res.isFailure) {
                Log.e("AuraViewModel", "Failed to send text message", res.exceptionOrNull())
            }
        }
    }

    fun sendImageMessage(conversationId: String, uri: Uri) {
        val myProfile = _uiState.value.currentProfile ?: return

        val tempId = "temp_${System.currentTimeMillis()}"
        val optimisticMsg = RealMessage(
            id = tempId,
            conversationId = conversationId,
            senderId = myProfile.id,
            senderName = myProfile.displayName,
            text = "📷 Photo",
            mediaUri = uri.toString(),
            mediaType = "image",
            timestamp = null
        )
        _uiState.update { state ->
            state.copy(activeChatMessages = state.activeChatMessages + optimisticMsg)
        }

        viewModelScope.launch {
            firestoreRepo.sendMediaMessage(
                conversationId = conversationId,
                senderId = myProfile.id,
                senderName = myProfile.displayName,
                mediaUri = uri,
                mediaType = "image"
            )
        }
    }

    fun sendFileMessage(conversationId: String, uri: Uri) {
        val myProfile = _uiState.value.currentProfile ?: return
        var fileName = "Document"
        try {
            getApplication<Application>().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIdx != -1) {
                    fileName = cursor.getString(nameIdx) ?: "Document"
                }
            }
        } catch (_: Exception) {}

        val tempId = "temp_${System.currentTimeMillis()}"
        val optimisticMsg = RealMessage(
            id = tempId,
            conversationId = conversationId,
            senderId = myProfile.id,
            senderName = myProfile.displayName,
            text = "📎 $fileName",
            mediaUri = uri.toString(),
            mediaType = "file",
            fileName = fileName,
            timestamp = null
        )
        _uiState.update { state ->
            state.copy(activeChatMessages = state.activeChatMessages + optimisticMsg)
        }

        viewModelScope.launch {
            firestoreRepo.sendMediaMessage(
                conversationId = conversationId,
                senderId = myProfile.id,
                senderName = myProfile.displayName,
                mediaUri = uri,
                mediaType = "file",
                fileName = fileName
            )
        }
    }

    // Real Live Status Post
    fun publishLiveStatus(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        val profile = _uiState.value.currentProfile ?: return
        viewModelScope.launch {
            firestoreRepo.publishStatusPost(
                userId = profile.id,
                authorName = profile.displayName,
                text = clean
            )
        }
    }

    fun openFile(uriString: String) {
        firestoreRepo.openFile(uriString)
    }

    fun dialRealPhone(phoneNumber: String) {
        firestoreRepo.dialRealPhone(phoneNumber)
    }

    fun sendExternalSms(phoneNumber: String, text: String) {
        firestoreRepo.sendRealSms(phoneNumber, text)
    }

    fun showStartChatDialog(show: Boolean) {
        _uiState.update { it.copy(showStartChatDialog = show) }
    }

    fun showDirectDialDialog(show: Boolean) {
        _uiState.update { it.copy(showDirectDialDialog = show) }
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
