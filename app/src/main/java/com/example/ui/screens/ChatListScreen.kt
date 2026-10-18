package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.RealConversation
import com.example.data.firebase.UserProfile
import com.example.ui.components.LiquidGlassAvatar
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassInputField
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.glass.liquidGlassOrb
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.AuraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
    uiState: AuraUiState,
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val myUid = uiState.currentProfile?.id ?: ""

    val filteredConversations = uiState.realConversations.filter { conv ->
        val otherName = conv.participantNames.entries.firstOrNull { it.key != myUid }?.value ?: "User"
        otherName.contains(uiState.searchQuery, ignoreCase = true) ||
                conv.lastMessage.contains(uiState.searchQuery, ignoreCase = true)
    }

    var startChatInput by remember { mutableStateOf("") }
    var startChatNameInput by remember { mutableStateOf("") }
    var dialNumberInput by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar & Search Bar
            LiquidMessengerTopBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.updateSearchQuery(it) },
                onStartChat = { viewModel.showStartChatDialog(true) },
                onDirectDial = { viewModel.showDirectDialDialog(true) },
                onSignOut = { viewModel.signOut() },
                profileName = uiState.currentProfile?.displayName ?: "User"
            )

            // Conversations List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredConversations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(14.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x35FFFFFF))
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0x18FFFFFF))
                                .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(24.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Real Cloud Messaging",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Start a real conversation with any email, phone, or registered user below!",
                                    color = TextWhiteSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LiquidGlassPillButton(
                                    text = "+ Start Real Chat",
                                    onClick = { viewModel.showStartChatDialog(true) },
                                    testTag = "empty_start_chat_button"
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "ACTIVE CONVERSATIONS",
                            color = TextWhiteMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }

                    items(
                        items = filteredConversations,
                        key = { it.id }
                    ) { conversation ->
                        val otherUid = conversation.participants.firstOrNull { it != myUid } ?: myUid
                        val otherName = conversation.participantNames[otherUid] ?: "User"

                        LiquidRealConversationCapsule(
                            otherName = otherName,
                            lastMessage = conversation.lastMessage,
                            updatedAt = conversation.updatedAt?.seconds?.times(1000) ?: System.currentTimeMillis(),
                            onClick = { viewModel.openConversation(conversation) },
                            onCallClick = { viewModel.dialRealPhone(otherUid) }
                        )
                    }
                }

                // Show other real registered users in Firestore
                val otherUsers = uiState.allRegisteredUsers.filter { it.userId != myUid }
                if (otherUsers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "REGISTERED USERS IN FIRESTORE",
                            color = TextWhiteMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    items(otherUsers, key = { it.userId }) { user ->
                        LiquidUserDirectoryRow(
                            user = user,
                            onStartChat = {
                                viewModel.startChatWithRealUser(
                                    otherUserId = user.userId,
                                    otherName = user.displayName
                                )
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.showStartChatDialog(true) },
            containerColor = Color.Transparent,
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 96.dp)
                .shadow(18.dp, CircleShape, ambientColor = Color(0x80FFFFFF))
                .clip(CircleShape)
                .background(Color.White)
                .size(56.dp)
                .testTag("fab_start_chat")
        ) {
            Icon(imageVector = Icons.Default.Chat, contentDescription = "New Chat", tint = TextDarkPrimary, modifier = Modifier.size(24.dp))
        }

        // Start Real Chat Dialog
        if (uiState.showStartChatDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD9000000))
                    .clickable { viewModel.showStartChatDialog(false) }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clickable(enabled = false) {},
                    cornerRadius = 28.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start Real Chat",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.showStartChatDialog(false) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Enter any email address, username, or telephone number to create an active Firestore chat room.",
                        color = TextWhiteSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LiquidGlassInputField(
                        value = startChatInput,
                        onValueChange = { startChatInput = it },
                        placeholder = "Email or Phone (+1 555-0199)",
                        testTag = "input_start_chat_id"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LiquidGlassInputField(
                        value = startChatNameInput,
                        onValueChange = { startChatNameInput = it },
                        placeholder = "Contact Name (optional)",
                        testTag = "input_start_chat_name"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LiquidGlassPillButton(
                        text = "Open Chat Room",
                        onClick = {
                            if (startChatInput.isNotBlank()) {
                                viewModel.startChatWithEmailOrPhone(
                                    identifier = startChatInput,
                                    name = startChatNameInput
                                )
                                startChatInput = ""
                                startChatNameInput = ""
                            }
                        },
                        testTag = "button_confirm_start_chat"
                    )
                }
            }
        }

        // Direct Dial Dialog
        if (uiState.showDirectDialDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD9000000))
                    .clickable { viewModel.showDirectDialDialog(false) }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable(enabled = false) {},
                    cornerRadius = 28.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Call Real Phone",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.showDirectDialDialog(false) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Enter any phone number to dial using the Android cellular system.",
                        color = TextWhiteSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LiquidGlassInputField(
                        value = dialNumberInput,
                        onValueChange = { dialNumberInput = it },
                        placeholder = "+1 555-0199",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = Icons.Default.Phone,
                        testTag = "input_dial_phone"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LiquidGlassPillButton(
                        text = "Dial on Phone",
                        onClick = {
                            if (dialNumberInput.isNotBlank()) {
                                viewModel.dialRealPhone(dialNumberInput)
                                viewModel.showDirectDialDialog(false)
                                dialNumberInput = ""
                            }
                        },
                        icon = Icons.Default.Call,
                        testTag = "button_dial_confirm"
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidMessengerTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onStartChat: () -> Unit,
    onDirectDial: () -> Unit,
    onSignOut: () -> Unit,
    profileName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .liquidGlassOrb(fillAlpha = 0.22f, borderAlpha = 0.6f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profileName.take(1).uppercase().ifBlank { "A" },
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Aura Real Chat",
                        color = TextWhitePrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profileName,
                        color = TextWhiteMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Call Phone button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x18FFFFFF))
                        .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(14.dp))
                        .clickable(onClick = onDirectDial)
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("direct_dial_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Dial", tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Call Phone", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // + Chat button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x18FFFFFF))
                        .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(14.dp))
                        .clickable(onClick = onStartChat)
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("start_chat_top_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat", tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "New Chat", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .size(34.dp)
                        .liquidGlassOrb(fillAlpha = 0.12f, borderAlpha = 0.35f)
                        .testTag("header_lock_button")
                ) {
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = "Lock", tint = TextWhiteSecondary, modifier = Modifier.size(15.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x35FFFFFF))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x24FFFFFF), Color(0x0EFFFFFF), Color(0x1AFFFFFF))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x70FFFFFF), Color(0x20FFFFFF), Color(0x10FFFFFF), Color(0x40FFFFFF)),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextWhiteMuted, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(text = "Search real conversations or users...", color = TextWhiteMuted, fontSize = 13.5.sp)
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        singleLine = true,
                        textStyle = TextStyle(color = TextWhitePrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(TextWhitePrimary),
                        modifier = Modifier.fillMaxWidth().testTag("chat_search_input")
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidRealConversationCapsule(
    otherName: String,
    lastMessage: String,
    updatedAt: Long,
    onClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val timeFormatted = remember(updatedAt) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(updatedAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x30FFFFFF))
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x22FFFFFF), Color(0x0CFFFFFF), Color(0x14FFFFFF))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GlassBorderBright, Color(0x25FFFFFF), Color(0x08FFFFFF), Color(0x40FFFFFF)),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 400f)
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LiquidGlassAvatar(
                avatarRes = null,
                monogram = otherName.take(2).uppercase().ifBlank { "US" },
                size = 50.dp,
                isOnline = true
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = otherName,
                        color = TextWhitePrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = timeFormatted,
                        color = TextWhiteMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lastMessage,
                        color = TextWhiteSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onCallClick, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidUserDirectoryRow(
    user: UserProfile,
    onStartChat: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x20FFFFFF))
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x14FFFFFF))
            .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(18.dp))
            .clickable(onClick = onStartChat)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiquidGlassAvatar(
                    avatarRes = null,
                    monogram = user.avatarMonogram.ifBlank { user.displayName.take(2).uppercase() },
                    size = 42.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = user.displayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = user.email.ifBlank { user.phoneNumber }, color = TextWhiteMuted, fontSize = 12.sp)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x24FFFFFF))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(text = "Chat →", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
