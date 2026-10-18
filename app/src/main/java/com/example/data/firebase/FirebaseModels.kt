package com.example.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val avatarMonogram: String = "",
    val bio: String = "Online on Aura Glass",
    val isOnline: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RealConversation(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RealMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val mediaUri: String? = null,
    val mediaType: String? = null,
    val fileName: String? = null,
    @ServerTimestamp val timestamp: Timestamp? = null
)

@IgnoreExtraProperties
data class RealStatusPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val text: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)
