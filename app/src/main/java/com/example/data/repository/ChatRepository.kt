package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CallEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(private val db: AppDatabase, private val context: Context) {

    val allConversations: Flow<List<ConversationEntity>> = db.chatDao().getAllConversations()
    val allCalls: Flow<List<CallEntity>> = db.callDao().getAllCalls()

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return db.chatDao().getMessagesForConversation(conversationId)
    }

    suspend fun sendTextMessage(conversationId: String, text: String) {
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "me",
            text = text.trim(),
            timestamp = now,
            isFromMe = true,
            isRead = false
        )
        db.chatDao().insertMessage(message)
        updateConversationLastMessage(conversationId, text.trim(), now)
    }

    suspend fun sendImageMessage(conversationId: String, imageUri: Uri) {
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "me",
            text = "📷 Photo",
            timestamp = now,
            isFromMe = true,
            isRead = false,
            mediaUri = imageUri.toString(),
            mediaType = "image"
        )
        db.chatDao().insertMessage(message)
        updateConversationLastMessage(conversationId, "📷 Photo", now)
    }

    suspend fun sendFileMessage(conversationId: String, fileUri: Uri) {
        val now = System.currentTimeMillis()
        var fileName = "Document"
        var fileSize = 0L

        try {
            context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: "Document"
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) {}

        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "me",
            text = "📎 $fileName",
            timestamp = now,
            isFromMe = true,
            isRead = false,
            mediaUri = fileUri.toString(),
            mediaType = "file",
            fileName = fileName,
            fileSize = fileSize
        )
        db.chatDao().insertMessage(message)
        updateConversationLastMessage(conversationId, "📎 $fileName", now)
    }

    suspend fun receiveReplyMessage(conversationId: String, text: String) {
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "contact",
            text = text,
            timestamp = now,
            isFromMe = false,
            isRead = true
        )
        db.chatDao().insertMessage(message)
        updateConversationLastMessage(conversationId, text, now)
    }

    private suspend fun updateConversationLastMessage(conversationId: String, lastMsg: String, timestamp: Long) {
        db.chatDao().updateLastMessage(conversationId, lastMsg, timestamp)
    }

    suspend fun addContact(name: String, phoneNumber: String, bio: String = "") {
        val id = "conv_${UUID.randomUUID().toString().take(8)}"
        val monogram = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifBlank { "A" }

        val conversation = ConversationEntity(
            id = id,
            name = name.trim(),
            phoneNumber = phoneNumber.trim(),
            avatarMonogram = monogram,
            lastMessage = "Started a new conversation",
            timestamp = System.currentTimeMillis(),
            unreadCount = 0,
            isOnline = true,
            bio = bio.ifBlank { "Aura Glass member" }
        )
        db.chatDao().insertConversation(conversation)
    }

    suspend fun logCall(contactName: String, phoneNumber: String, isVideo: Boolean) {
        val call = CallEntity(
            contactName = contactName,
            phoneNumber = phoneNumber,
            timestamp = System.currentTimeMillis(),
            isVideo = isVideo,
            isIncoming = false,
            isMissed = false,
            durationText = "Outgoing Call"
        )
        db.callDao().insertCall(call)
    }

    /**
     * Real Call: Opens system dialer with the phone number pre-filled
     */
    fun dialRealPhone(phoneNumber: String) {
        try {
            val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    /**
     * Real SMS / External Messaging Intent
     */
    fun sendRealSms(phoneNumber: String, text: String) {
        try {
            val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanNumber")
                putExtra("sms_body", text)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to generic share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        }
    }

    /**
     * Open real file attachment using system viewer
     */
    fun openFile(uriString: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(uriString)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
