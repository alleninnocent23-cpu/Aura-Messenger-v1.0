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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun CallsScreen(
    uiState: AuraUiState,
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var dialNumberInput by remember { mutableStateOf("") }
    var showDialerModal by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Phone Calls",
                        color = TextWhitePrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Direct Carrier & Cellular Calling",
                        color = TextWhiteMuted,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1AFFFFFF))
                        .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(16.dp))
                        .clickable { showDialerModal = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("calls_open_dialer_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Dial", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Keypad Dial", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dial Card Prompt
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x35FFFFFF))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0x18FFFFFF))
                            .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .liquidGlassOrb(fillAlpha = 0.25f, borderAlpha = 0.6f),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Real Cellular Telephone Calling", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aura Glass integrates directly with Android's system telecom stack. Dial any real landline, mobile, or international number without third-party gateways.",
                                color = TextWhiteSecondary,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LiquidGlassPillButton(
                                text = "Open Phone Dialer",
                                onClick = { showDialerModal = true },
                                icon = Icons.Default.Call,
                                testTag = "open_phone_dialer_action"
                            )
                        }
                    }
                }

                // Quick dial from conversations
                if (uiState.realConversations.isNotEmpty()) {
                    item {
                        Text(
                            text = "QUICK DIAL ACTIVE CONTACTS",
                            color = TextWhiteMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    items(uiState.realConversations, key = { it.id }) { conv ->
                        val myUid = uiState.currentProfile?.id ?: ""
                        val otherUid = conv.participants.firstOrNull { it != myUid } ?: myUid
                        val otherName = conv.participantNames[otherUid] ?: "User"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x25FFFFFF))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x14FFFFFF))
                                .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LiquidGlassAvatar(
                                        avatarRes = null,
                                        monogram = otherName.take(2).uppercase().ifBlank { "U" },
                                        size = 46.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = otherName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = otherUid, color = TextWhiteMuted, fontSize = 12.sp)
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.dialRealPhone(otherUid) },
                                    modifier = Modifier.size(38.dp).liquidGlassOrb(fillAlpha = 0.2f, borderAlpha = 0.5f)
                                ) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = "Dial", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // Community Contacts
                val communityUsers = uiState.allRegisteredUsers.filter { it.userId != (uiState.currentProfile?.id ?: "") }
                if (communityUsers.isNotEmpty()) {
                    item {
                        Text(
                            text = "COMMUNITY CONTACTS",
                            color = TextWhiteMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    items(communityUsers, key = { it.userId }) { user ->
                        val targetDial = user.phoneNumber.ifEmpty { user.userId }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x25FFFFFF))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x14FFFFFF))
                                .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LiquidGlassAvatar(
                                        avatarRes = null,
                                        monogram = user.displayName.take(2).uppercase().ifBlank { "U" },
                                        size = 46.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = user.displayName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = if (user.phoneNumber.isNotBlank()) user.phoneNumber else user.email.ifBlank { user.userId },
                                            color = TextWhiteMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { viewModel.dialRealPhone(targetDial) },
                                        modifier = Modifier.size(38.dp).liquidGlassOrb(fillAlpha = 0.2f, borderAlpha = 0.5f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Phone Dialer Keypad Modal
        if (showDialerModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD9000000))
                    .clickable { showDialerModal = false }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(0.95f).clickable(enabled = false) {},
                    cornerRadius = 28.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Cellular Phone Dialer", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showDialerModal = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LiquidGlassInputField(
                        value = dialNumberInput,
                        onValueChange = { dialNumberInput = it },
                        placeholder = "+1 555-0199",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = Icons.Default.Phone,
                        testTag = "keypad_dial_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Keypad Matrix
                    val keypadRows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("*", "0", "#")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        keypadRows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { digit ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .liquidGlassOrb(fillAlpha = 0.15f, borderAlpha = 0.4f)
                                            .clickable { dialNumberInput += digit },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = digit, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LiquidGlassPillButton(
                        text = "Place Real Call",
                        onClick = {
                            if (dialNumberInput.isNotBlank()) {
                                viewModel.dialRealPhone(dialNumberInput)
                                showDialerModal = false
                                dialNumberInput = ""
                            }
                        },
                        icon = Icons.Default.Call,
                        testTag = "button_dial_keypad_submit"
                    )
                }
            }
        }
    }
}
