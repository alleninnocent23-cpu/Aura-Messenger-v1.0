package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val avatarMonogram: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = true,
    val bio: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val messageId: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean,
    val isRead: Boolean = true,
    val mediaUri: String? = null,
    val mediaType: String? = null, // "image", "file", "audio"
    val fileName: String? = null,
    val fileSize: Long? = null
)

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String,
    val phoneNumber: String,
    val timestamp: Long,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val isMissed: Boolean,
    val durationText: String
)
