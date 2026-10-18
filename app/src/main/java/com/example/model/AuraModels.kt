package com.example.model

data class User(
    val id: String,
    val name: String,
    val username: String,
    val avatarDrawableRes: Int? = null,
    val avatarMonogram: String,
    val isOnline: Boolean = true,
    val bio: String = "Designing in monochrome liquid glass."
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val isRead: Boolean = true,
    val isVoiceNote: Boolean = false,
    val voiceDurationSec: Int = 0,
    val hasMediaPreview: Boolean = false
)

data class ChatConversation(
    val id: String,
    val user: User,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isTyping: Boolean = false,
    val lastMessageIsVoice: Boolean = false
)

data class StatusStory(
    val id: String,
    val user: User,
    val caption: String,
    val timestamp: String,
    val isViewed: Boolean = false
)

data class CallLog(
    val id: String,
    val user: User,
    val timestamp: String,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val isMissed: Boolean,
    val durationText: String
)

enum class MessengerTab(val title: String) {
    CHATS("Chats"),
    STATUS("Status"),
    CALLS("Calls"),
    SETTINGS("Settings")
}

sealed class AuraScreen {
    object Login : AuraScreen()
    data class Messenger(val tab: MessengerTab = MessengerTab.CHATS) : AuraScreen()
    data class ChatDetail(val conversationId: String) : AuraScreen()
}

data class PushNotification(
    val id: String,
    val title: String,
    val message: String,
    val conversationId: String,
    val senderAvatarRes: Int? = null,
    val senderMonogram: String = "A"
)
