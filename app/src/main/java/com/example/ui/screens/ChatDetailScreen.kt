package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.model.User
import com.example.ui.components.LiquidGlassAvatar
import com.example.ui.components.LiquidGlassCard
import com.example.ui.glass.liquidGlassOrb
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextWhiteFaint
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.AuraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
    conversationId: String,
    uiState: AuraUiState,
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBackToChats()
    }

    val conversation = uiState.roomConversations.firstOrNull { it.id == conversationId }
        ?: uiState.activeConversation

    val messages = uiState.currentChatMessages
    var inputText by remember { mutableStateOf("") }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Real Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.sendImageMessage(conversationId, uri)
        }
    }

    // Real Document Picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.sendFileMessage(conversationId, uri)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Liquid Glass Top Navigation Bar with Real Phone Calling
            LiquidChatTopBar(
                conversation = conversation,
                onBack = { viewModel.navigateBackToChats() },
                onRealCall = {
                    if (conversation != null) {
                        viewModel.dialContact(conversation)
                    }
                },
                onInAppCall = {
                    if (conversation != null) {
                        viewModel.startInAppCall(
                            User(
                                id = conversation.id,
                                name = conversation.name,
                                username = conversation.phoneNumber,
                                avatarMonogram = conversation.avatarMonogram
                            ),
                            isVideo = true
                        )
                    }
                },
                onSendExternalSms = {
                    if (conversation != null) {
                        viewModel.sendExternalSms(conversation.phoneNumber, inputText.ifBlank { "Hello from Aura Glass" })
                    }
                }
            )

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.localId }) { message ->
                    LiquidRealMessageBubble(
                        message = message,
                        onOpenFile = { uri -> viewModel.openFile(uri) }
                    )
                }
            }

            // Liquid Glass Message Input Dock
            LiquidMessageInputDock(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendTextMessage(conversationId, inputText)
                        inputText = ""
                    }
                },
                onOpenAttachmentMenu = { showAttachmentMenu = true },
                onQuickPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }

        // Attachment selection dialog (Photos / Files)
        if (showAttachmentMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { showAttachmentMenu = false }
                    .padding(24.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 60.dp),
                    cornerRadius = 28.dp
                ) {
                    Text(
                        text = "Send Real Media or Document",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Pick Photo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showAttachmentMenu = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(12.dp)
                                .testTag("attach_photo_option")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .liquidGlassOrb(fillAlpha = 0.25f, borderAlpha = 0.6f),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Image, contentDescription = "Photos", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Gallery Photos", color = Color.White, fontSize = 12.sp)
                        }

                        // Pick Document / File
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showAttachmentMenu = false
                                    documentPickerLauncher.launch(arrayOf("*/*"))
                                }
                                .padding(12.dp)
                                .testTag("attach_document_option")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .liquidGlassOrb(fillAlpha = 0.25f, borderAlpha = 0.6f),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = "Files", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Any File / PDF", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidChatTopBar(
    conversation: ConversationEntity?,
    onBack: () -> Unit,
    onRealCall: () -> Unit,
    onInAppCall: () -> Unit,
    onSendExternalSms: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(26.dp), ambientColor = Color(0x40FFFFFF))
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x30FFFFFF), Color(0x12FFFFFF), Color(0x22FFFFFF))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GlassBorderBright, Color(0x25FFFFFF), Color(0x10FFFFFF), Color(0x50FFFFFF))
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("chat_detail_back_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            LiquidGlassAvatar(
                avatarRes = if (conversation?.id == "conv_alex") R.drawable.img_glass_avatar else null,
                monogram = conversation?.avatarMonogram ?: "A",
                size = 40.dp,
                isOnline = conversation?.isOnline ?: true
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation?.name ?: "Chat",
                    color = TextWhitePrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = conversation?.phoneNumber ?: "Real Contact",
                    color = Color(0xB3FFFFFF),
                    fontSize = 11.sp
                )
            }

            // Real Cellular Phone Call button (launches Android dialer)
            IconButton(
                onClick = onRealCall,
                modifier = Modifier
                    .size(36.dp)
                    .liquidGlassOrb(fillAlpha = 0.2f, borderAlpha = 0.5f)
                    .testTag("chat_real_phone_call_button")
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = "Real Phone Call", tint = Color.White, modifier = Modifier.size(17.dp))
            }

            Spacer(modifier = Modifier.width(6.dp))

            // External SMS / Share action
            IconButton(
                onClick = onSendExternalSms,
                modifier = Modifier
                    .size(36.dp)
                    .liquidGlassOrb(fillAlpha = 0.2f, borderAlpha = 0.5f)
                    .testTag("chat_external_sms_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Send via SMS", tint = Color.White, modifier = Modifier.size(17.dp))
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Video / In-app Call
            IconButton(
                onClick = onInAppCall,
                modifier = Modifier
                    .size(36.dp)
                    .liquidGlassOrb(fillAlpha = 0.2f, borderAlpha = 0.5f)
                    .testTag("chat_inapp_video_call_button")
            ) {
                Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LiquidRealMessageBubble(
    message: MessageEntity,
    onOpenFile: (String) -> Unit
) {
    val isMe = message.isFromMe
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .shadow(
                    elevation = if (isMe) 14.dp else 8.dp,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isMe) 20.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 20.dp
                    ),
                    ambientColor = if (isMe) Color(0x60FFFFFF) else Color(0x20FFFFFF)
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isMe) 20.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 20.dp
                    )
                )
                .then(
                    if (isMe) {
                        Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFECECF0))
                                )
                            )
                            .border(
                                1.dp,
                                Color.White,
                                RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                            )
                    } else {
                        Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0x2EFFFFFF), Color(0x14FFFFFF), Color(0x20FFFFFF))
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    colors = listOf(GlassBorderBright, Color(0x20FFFFFF), Color(0x45FFFFFF))
                                ),
                                RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                            )
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                // If message contains a real image from device
                if (message.mediaType == "image" && message.mediaUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = message.mediaUri,
                            contentDescription = "User photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // If message contains a real document/file
                if (message.mediaType == "file" && message.mediaUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isMe) Color(0x18000000) else Color(0x28FFFFFF))
                            .clickable { onOpenFile(message.mediaUri) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "File",
                            tint = if (isMe) TextDarkPrimary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = message.fileName ?: "Document",
                                color = if (isMe) TextDarkPrimary else TextWhitePrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = "Tap to open in viewer",
                                color = if (isMe) Color(0x99000000) else TextWhiteMuted,
                                fontSize = 10.5.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open",
                            tint = if (isMe) TextDarkPrimary else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Text body
                if (message.text.isNotBlank() && (message.mediaType == null || message.text != "📷 Photo")) {
                    Text(
                        text = message.text,
                        color = if (isMe) TextDarkPrimary else TextWhitePrimary,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        fontWeight = if (isMe) FontWeight.Medium else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        color = if (isMe) Color(0x99000000) else TextWhiteMuted,
                        fontSize = 10.sp
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = if (message.isRead) Color.Black else Color(0x80000000),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidMessageInputDock(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onOpenAttachmentMenu: () -> Unit,
    onQuickPhoto: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 18.dp, shape = RoundedCornerShape(28.dp), ambientColor = Color(0x40FFFFFF))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x28FFFFFF), Color(0x12FFFFFF), Color(0x1EFFFFFF))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x80FFFFFF), Color(0x25FFFFFF), Color(0x10FFFFFF), Color(0x50FFFFFF))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment menu button
            IconButton(
                onClick = onOpenAttachmentMenu,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("chat_attach_button")
            ) {
                Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Attach file", tint = TextWhiteSecondary, modifier = Modifier.size(19.dp))
            }

            // Quick Camera / Photo icon
            IconButton(
                onClick = onQuickPhoto,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("chat_quick_photo_button")
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = "Pick Photo", tint = TextWhiteSecondary, modifier = Modifier.size(19.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
            ) {
                if (value.isEmpty()) {
                    Text(text = "Real message or file...", color = TextWhiteMuted, fontSize = 14.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = TextWhitePrimary, fontSize = 14.5.sp),
                    cursorBrush = SolidColor(TextWhitePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_detail_input")
                )
            }

            // Send Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(8.dp, CircleShape, ambientColor = Color(0x80FFFFFF))
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onSend() }
                    .testTag("chat_detail_send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = TextDarkPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
