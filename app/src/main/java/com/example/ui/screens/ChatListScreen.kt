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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.ConversationEntity
import com.example.ui.components.LiquidGlassAvatar
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassInputField
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.glass.liquidGlassOrb
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
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
    val filteredConversations = uiState.roomConversations.filter {
        it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(uiState.searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(uiState.searchQuery)
    }

    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }
    var newContactBio by remember { mutableStateOf("") }

    var dialNumberInput by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Poured Liquid Glass Header & Search Bar
            LiquidMessengerTopBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.updateSearchQuery(it) },
                onAddContact = { viewModel.showNewContactDialog(true) },
                onDirectDial = { viewModel.showDirectDialDialog(true) },
                onSignOut = { viewModel.signOut() },
                profileName = uiState.currentProfile?.displayName ?: "Aura Member"
            )

            // Status Stories Tray
            LiquidStatusTray(stories = uiState.statusStories)

            Spacer(modifier = Modifier.height(6.dp))

            // Conversation List: Floating Liquid Glass Capsules from Room
            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No conversations yet",
                            color = TextWhitePrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap '+ Contact' to add a real person with their phone number.",
                            color = TextWhiteMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredConversations,
                        key = { it.id }
                    ) { conversation ->
                        LiquidConversationCapsule(
                            conversation = conversation,
                            onClick = { viewModel.openChat(conversation.id) },
                            onCallClick = { viewModel.dialContact(conversation) }
                        )
                    }
                }
            }
        }

        // Floating Action Button: Add New Contact
        FloatingActionButton(
            onClick = { viewModel.showNewContactDialog(true) },
            containerColor = Color.Transparent,
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 96.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x80FFFFFF),
                    spotColor = Color(0x90000000)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFE4E4EB))
                    )
                )
                .border(1.dp, Color.White, CircleShape)
                .size(56.dp)
                .testTag("fab_add_contact")
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "New Contact",
                tint = TextDarkPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Add Real Contact Modal
        if (uiState.showNewContactDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD9000000))
                    .clickable { viewModel.showNewContactDialog(false) }
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
                            text = "Add Real Contact",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.showNewContactDialog(false) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LiquidGlassInputField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        placeholder = "Contact Name (e.g. Alex Vance)",
                        testTag = "input_contact_name"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LiquidGlassInputField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        placeholder = "Real Phone Number (+1 555-0199)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        testTag = "input_contact_phone"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LiquidGlassInputField(
                        value = newContactBio,
                        onValueChange = { newContactBio = it },
                        placeholder = "Bio / Note (optional)",
                        testTag = "input_contact_bio"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LiquidGlassPillButton(
                        text = "Save Contact & Start Chat",
                        onClick = {
                            if (newContactName.isNotBlank() && newContactPhone.isNotBlank()) {
                                viewModel.createNewContact(newContactName, newContactPhone, newContactBio)
                                newContactName = ""
                                newContactPhone = ""
                                newContactBio = ""
                            }
                        },
                        testTag = "save_contact_button"
                    )
                }
            }
        }

        // Direct Dial Modal
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
                            text = "Direct Phone Call",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.showDirectDialDialog(false) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Enter any real telephone number to place an immediate carrier/cellular phone call through Android dialer.",
                        color = TextWhiteSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LiquidGlassInputField(
                        value = dialNumberInput,
                        onValueChange = { dialNumberInput = it },
                        placeholder = "+1 (555) 000-0000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = Icons.Default.Phone,
                        testTag = "input_direct_dial"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LiquidGlassPillButton(
                        text = "Dial on Real Phone",
                        onClick = {
                            if (dialNumberInput.isNotBlank()) {
                                viewModel.dialCustomNumber(dialNumberInput)
                                dialNumberInput = ""
                            }
                        },
                        icon = Icons.Default.Call,
                        testTag = "button_execute_dial"
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
    onAddContact: () -> Unit,
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
                        text = "A",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Aura Chats",
                        color = TextWhitePrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profileName,
                        color = TextWhiteMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Direct Phone Dial button
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
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Dial",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dial",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // + Contact button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x18FFFFFF))
                        .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(14.dp))
                        .clickable(onClick = onAddContact)
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("add_contact_top_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Contact",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Contact",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
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
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Lock",
                        tint = TextWhiteSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0x35FFFFFF))
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextWhiteMuted,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search messages, real names or phones...",
                            color = TextWhiteMuted,
                            fontSize = 13.5.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        singleLine = true,
                        textStyle = TextStyle(color = TextWhitePrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(TextWhitePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_search_input")
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidStatusTray(stories: List<com.example.model.StatusStory>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    LiquidGlassAvatar(
                        avatarRes = R.drawable.img_glass_avatar,
                        monogram = "ME",
                        size = 52.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFF0C0C10), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(12.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "My Status", color = TextWhiteSecondary, fontSize = 11.sp)
            }
        }

        items(stories) { story ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LiquidGlassAvatar(
                    avatarRes = story.user.avatarDrawableRes,
                    monogram = story.user.avatarMonogram,
                    size = 52.dp,
                    hasStoryRing = true,
                    storyViewed = story.isViewed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.user.name.substringBefore(" "),
                    color = TextWhiteSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LiquidConversationCapsule(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val timeFormatted = remember(conversation.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(conversation.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp), ambientColor = Color(0x30FFFFFF))
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
            .testTag("conversation_row_${conversation.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassAvatar(
                avatarRes = if (conversation.id == "conv_alex") R.drawable.img_glass_avatar else null,
                monogram = conversation.avatarMonogram,
                size = 50.dp,
                isOnline = conversation.isOnline
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.name,
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

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = conversation.phoneNumber,
                    color = Color(0x80FFFFFF),
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        color = TextWhiteSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Quick call button
                    IconButton(
                        onClick = onCallClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call ${conversation.name}",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
