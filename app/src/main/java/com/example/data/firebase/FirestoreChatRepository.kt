package com.example.data.firebase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreChatRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (_: Exception) {
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun syncUserProfile(
        userId: String,
        displayName: String,
        email: String,
        phoneNumber: String,
        avatarMonogram: String
    ) {
        val userMap = mapOf(
            "id" to userId,
            "userId" to userId,
            "displayName" to displayName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "avatarMonogram" to avatarMonogram,
            "isOnline" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection("users").document(userId).set(userMap).await()
            Log.d("FirestoreChat", "Successfully synced user profile for $displayName ($userId)")
        } catch (e: Exception) {
            Log.e("FirestoreChat", "Error syncing user profile", e)
        }
    }

    fun observeAllUsers(): Flow<List<UserProfile>> {
        return db.collection("users")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                    .sortedByDescending { it.updatedAt?.seconds ?: 0L }
            }
            .catch { e ->
                Log.e("FirestoreChat", "Error observing users", e)
                emit(emptyList())
            }
    }

    fun observeConversations(currentUserId: String): Flow<List<RealConversation>> {
        return db.collection("conversations")
            .whereArrayContains("participants", currentUserId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(RealConversation::class.java) }
                    .sortedByDescending { it.updatedAt?.seconds ?: 0L }
            }
            .catch { e ->
                Log.e("FirestoreChat", "Error observing conversations", e)
                emit(emptyList())
            }
    }

    suspend fun getOrCreateConversation(
        myUserId: String,
        myName: String,
        otherUserId: String,
        otherName: String
    ): String {
        val clean1 = myUserId.filter { it.isLetterOrDigit() }.ifEmpty { "user1" }
        val clean2 = otherUserId.filter { it.isLetterOrDigit() }.ifEmpty { "user2" }
        val convId = "conv_" + if (clean1 < clean2) "${clean1}_$clean2" else "${clean2}_$clean1"

        try {
            val docRef = db.collection("conversations").document(convId)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val convData = mapOf(
                    "id" to convId,
                    "conversationId" to convId,
                    "participants" to listOf(myUserId, otherUserId),
                    "participantNames" to mapOf(
                        myUserId to myName,
                        otherUserId to otherName
                    ),
                    "lastMessage" to "Conversation started",
                    "lastSenderId" to myUserId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(convData).await()
                Log.d("FirestoreChat", "Created new real conversation: $convId")
            }
        } catch (e: Exception) {
            Log.e("FirestoreChat", "Error creating conversation $convId", e)
        }
        return convId
    }

    fun observeMessages(conversationId: String): Flow<List<RealMessage>> {
        return db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(RealMessage::class.java) }
                    .sortedBy { it.timestamp?.seconds ?: Long.MAX_VALUE }
            }
            .catch { e ->
                Log.e("FirestoreChat", "Error observing messages for $conversationId", e)
                emit(emptyList())
            }
    }

    suspend fun sendTextMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        text: String
    ): Result<Unit> = runCatching {
        val msgId = "msg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val cleanText = text.trim()
        val msgData = mapOf(
            "id" to msgId,
            "conversationId" to conversationId,
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to cleanText,
            "timestamp" to FieldValue.serverTimestamp()
        )

        val convRef = db.collection("conversations").document(conversationId)
        convRef.collection("messages").document(msgId).set(msgData).await()

        try {
            convRef.update(
                mapOf(
                    "lastMessage" to cleanText,
                    "lastSenderId" to senderId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
            // Document might be newly created
        }
        Log.d("FirestoreChat", "Message sent to $conversationId: $cleanText")
    }

    suspend fun sendMediaMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        mediaUri: Uri,
        mediaType: String,
        fileName: String? = null
    ): Result<Unit> = runCatching {
        val msgId = "msg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val previewText = if (mediaType == "image") "📷 Photo" else "📎 ${fileName ?: "Document"}"

        val msgData = mapOf(
            "id" to msgId,
            "conversationId" to conversationId,
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to previewText,
            "mediaUri" to mediaUri.toString(),
            "mediaType" to mediaType,
            "fileName" to (fileName ?: ""),
            "timestamp" to FieldValue.serverTimestamp()
        )

        val convRef = db.collection("conversations").document(conversationId)
        convRef.collection("messages").document(msgId).set(msgData).await()

        try {
            convRef.update(
                mapOf(
                    "lastMessage" to previewText,
                    "lastSenderId" to senderId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {}
        Log.d("FirestoreChat", "Media message sent to $conversationId ($mediaType)")
    }

    // Real Status Updates in Firestore
    fun observeLiveStatuses(): Flow<List<RealStatusPost>> {
        return db.collection("status_updates")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(RealStatusPost::class.java) }
                    .sortedByDescending { it.timestamp?.seconds ?: 0L }
            }
            .catch { e ->
                Log.e("FirestoreChat", "Error observing status updates", e)
                emit(emptyList())
            }
    }

    suspend fun publishStatusPost(
        userId: String,
        authorName: String,
        text: String
    ): Result<Unit> = runCatching {
        val statusId = "status_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val statusData = mapOf(
            "id" to statusId,
            "userId" to userId,
            "authorName" to authorName,
            "text" to text.trim(),
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("status_updates").document(statusId).set(statusData).await()
        Log.d("FirestoreChat", "Published live status from $authorName: $text")
    }

    fun dialRealPhone(phoneNumber: String) {
        try {
            val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("FirestoreChat", "Error dialing phone", e)
        }
    }

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
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(shareIntent)
            } catch (_: Exception) {}
        }
    }

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
