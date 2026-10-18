package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CallDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.CallEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ConversationEntity::class, MessageEntity::class, CallEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun callDao(): CallDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_glass_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default conversations on first run
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                seedInitialData(database)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val initialConversations = listOf(
                ConversationEntity(
                    id = "conv_alex",
                    name = "Alex Vance",
                    phoneNumber = "+1 415 555 0192",
                    avatarMonogram = "AV",
                    lastMessage = "The liquid glass reflections are completely fluid. Send me an image or file!",
                    timestamp = now - 60000,
                    unreadCount = 1,
                    isOnline = true,
                    bio = "Optics & fluid interface designer."
                ),
                ConversationEntity(
                    id = "conv_elena",
                    name = "Elena Rostova",
                    phoneNumber = "+1 206 555 0148",
                    avatarMonogram = "ER",
                    lastMessage = "You can tap Call to dial my real phone number anytime.",
                    timestamp = now - 3600000,
                    unreadCount = 0,
                    isOnline = true,
                    bio = "Architect of liquid glass spaces."
                ),
                ConversationEntity(
                    id = "conv_marcus",
                    name = "Marcus Chen",
                    phoneNumber = "+1 650 555 0184",
                    avatarMonogram = "MC",
                    lastMessage = "Real room database and file pickers are active.",
                    timestamp = now - 7200000,
                    unreadCount = 0,
                    isOnline = false,
                    bio = "Android systems engineer."
                )
            )
            db.chatDao().insertConversations(initialConversations)

            val initialMessages = listOf(
                MessageEntity(
                    messageId = "m1",
                    conversationId = "conv_alex",
                    senderId = "user_alex",
                    text = "Welcome to Aura Glass. Every surface is built with frosted fluid depth.",
                    timestamp = now - 300000,
                    isFromMe = false
                ),
                MessageEntity(
                    messageId = "m2",
                    conversationId = "conv_alex",
                    senderId = "me",
                    text = "The organic rounded edges and specular borders look unreal.",
                    timestamp = now - 180000,
                    isFromMe = true,
                    isRead = true
                ),
                MessageEntity(
                    messageId = "m3",
                    conversationId = "conv_alex",
                    senderId = "user_alex",
                    text = "The liquid glass reflections are completely fluid. Send me an image or file!",
                    timestamp = now - 60000,
                    isFromMe = false
                )
            )
            db.chatDao().insertMessages(initialMessages)

            val initialCalls = listOf(
                CallEntity(
                    contactName = "Alex Vance",
                    phoneNumber = "+1 415 555 0192",
                    timestamp = now - 1800000,
                    isVideo = true,
                    isIncoming = true,
                    isMissed = false,
                    durationText = "14m 22s"
                ),
                CallEntity(
                    contactName = "Elena Rostova",
                    phoneNumber = "+1 206 555 0148",
                    timestamp = now - 86400000,
                    isVideo = false,
                    isIncoming = false,
                    isMissed = false,
                    durationText = "4m 10s"
                )
            )
            db.callDao().insertCalls(initialCalls)
        }
    }
}
